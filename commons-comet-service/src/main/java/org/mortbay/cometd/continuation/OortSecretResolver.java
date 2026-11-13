/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.mortbay.cometd.continuation;

import java.security.SecureRandom;
import java.util.Base64;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.security.codec.AbstractCodec;

/**
 * Resolves the secret the nodes of a cluster share to trust each other's
 * cometd connections: the configured one when it is the deployment's own,
 * otherwise a random one that the first node to start stores in the global
 * settings, encrypted with the platform codec, and every node reads. The nodes
 * of a cluster share the codec key: the codec refuses to start a node whose key
 * differs from the one the database knows.
 */
class OortSecretResolver {

  static final String       DEFAULT_SECRET = "exo";

  static final String       SETTING_KEY    = "oortSecret";

  private static final SecureRandom RANDOM = new SecureRandom();

  static final Scope        SETTING_SCOPE  = Scope.APPLICATION.id("cometd");

  /**
   * The cache of the settings service, which keeps the value this node stored,
   * or that none was found, whatever another node stored since.
   */
  static final String       SETTING_CACHE  = "commons.SettingService";

  private static final Log  LOG            = ExoLogger.getLogger(OortSecretResolver.class);

  private final ExoContainer   container;

  private final SettingService settingService;

  private final CacheService   cacheService;

  private final AbstractCodec  codec;

  private final long           settleMillis;

  OortSecretResolver(ExoContainer container,
                     SettingService settingService,
                     CacheService cacheService,
                     AbstractCodec codec,
                     long settleMillis) {
    this.container = container;
    this.settingService = settingService;
    this.cacheService = cacheService;
    this.codec = codec;
    this.settleMillis = settleMillis;
  }

  /**
   * @param secret a configured secret
   * @return true when the secret is set and is not the public default one
   */
  static boolean isOwnSecret(String secret) {
    return secret != null && !secret.isBlank() && !DEFAULT_SECRET.equals(secret.trim());
  }

  /**
   * @param configuredSecret the secret the deployment configured, if any
   * @return the configured secret when it is the deployment's own, else the
   *         stored shared secret, stored first when no node did. Nodes that
   *         start at the same time may each store one, and the store of all but
   *         one may fail: a node that stored, or tried to, waits for the other
   *         stores to land, drops what its settings cache holds and reads the
   *         database, so that every node keeps the value the database kept.
   *         A stored value this node cannot decode is never replaced: the
   *         node resolves no secret instead.
   */
  String resolve(String configuredSecret) throws InterruptedException {
    if (isOwnSecret(configuredSecret)) {
      return configuredSecret;
    }
    String storedValue = readStoredValue();
    if (storedValue != null) {
      return decode(storedValue);
    }
    try {
      storeSecret(generateSecret());
      LOG.info("Stored a new secret shared by the nodes of the cluster");
    } catch (RuntimeException e) {
      LOG.info("Could not store a new secret shared by the nodes of the cluster: reading the stored one back");
      LOG.debug("Store of the secret shared by the nodes of the cluster failed", e);
    }
    Thread.sleep(settleMillis);
    cacheService.getCacheInstance(SETTING_CACHE).clearCache();
    String readBackValue = readStoredValue();
    return readBackValue == null ? null : decode(readBackValue);
  }

  private String decode(String storedValue) {
    try {
      String secret = codec.decode(storedValue);
      if (isOwnSecret(secret)) {
        return secret;
      }
    } catch (RuntimeException e) {
      LOG.debug("Decoding the secret shared by the nodes of the cluster failed", e);
    }
    LOG.error("The secret shared by the nodes of the cluster, stored in the global settings, cannot be decoded with this node's codec key: it is left as is");
    return null;
  }

  private String readStoredValue() {
    RequestLifeCycle.begin(container);
    try {
      SettingValue<?> value = settingService.get(Context.GLOBAL, SETTING_SCOPE, SETTING_KEY);
      String storedValue = value == null || value.getValue() == null ? null : value.getValue().toString();
      return storedValue == null || storedValue.isBlank() ? null : storedValue;
    } finally {
      RequestLifeCycle.end();
    }
  }

  private void storeSecret(String secret) {
    RequestLifeCycle.begin(container);
    try {
      settingService.set(Context.GLOBAL, SETTING_SCOPE, SETTING_KEY, SettingValue.create(codec.encode(secret)));
    } finally {
      RequestLifeCycle.end();
    }
  }

  private static String generateSecret() {
    byte[] bytes = new byte[32];
    RANDOM.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }
}
