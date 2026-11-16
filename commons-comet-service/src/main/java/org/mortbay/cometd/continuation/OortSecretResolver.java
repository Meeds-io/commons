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
import java.util.function.LongSupplier;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.security.codec.AbstractCodec;

/**
 * Resolves the secret the nodes of a cluster share to trust each other's
 * cometd connections: the configured one when it is the deployment's own,
 * otherwise a random one that the first node to start stores in the global
 * settings, encrypted with the platform codec, and every node reads. The nodes
 * of a cluster share the codec key: the codec refuses to start a node whose key
 * differs from the one the database knows. The settings are read and written
 * through the settings store itself, never through its cache: a read that
 * fails is told apart from a value that is not stored.
 */
class OortSecretResolver {

  static final String       DEFAULT_SECRET = "exo";

  static final String       SETTING_KEY    = "oortSecret";

  static final Scope        SETTING_SCOPE  = Scope.APPLICATION.id("cometd");

  /**
   * How long after it was stored a value may still be replaced by the store of
   * a node that started at the same time: a node that finds a value as young
   * reads it again once the stores landed. Far above the clock skew a cluster
   * tolerates.
   */
  static final long         STORE_WINDOW_MILLIS = 60_000;

  /**
   * Separates the instant a value was stored from the encoded secret.
   */
  static final char         STORED_AT_SEPARATOR = ':';

  private static final SecureRandom RANDOM = new SecureRandom();

  private static final Log  LOG            = ExoLogger.getLogger(OortSecretResolver.class);

  private final ExoContainer   container;

  private final SettingService settingStore;

  private final AbstractCodec  codec;

  private final long           settleMillis;

  private final LongSupplier   clock;

  OortSecretResolver(ExoContainer container,
                     SettingService settingStore,
                     AbstractCodec codec,
                     long settleMillis,
                     LongSupplier clock) {
    this.container = container;
    this.settingStore = settingStore;
    this.codec = codec;
    this.settleMillis = settleMillis;
    this.clock = clock;
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
   *         start at the same time may each store one: a store that finds no
   *         value inserts one, and all such inserts but one fail, while a store
   *         that finds one replaces it, so a node may find the value one of
   *         them stored before another's store lands. A node that stored, or
   *         tried to, or that found a value stored less than
   *         {@link #STORE_WINDOW_MILLIS} ago,
   *         waits for the stores to land and reads the database again, so that
   *         every node keeps the value the database kept. A node that finds an
   *         older value keeps it at once, with no wait. A value the store
   *         cannot read, that the codec refuses, or that decodes to no own
   *         secret is never replaced: the node resolves no secret instead. The
   *         codec does not refuse every value encoded under another key, but the
   *         nodes of a cluster share one key.
   * @throws InterruptedException when interrupted while waiting for the stores
   */
  String resolve(String configuredSecret) throws InterruptedException {
    if (isOwnSecret(configuredSecret)) {
      return configuredSecret;
    }
    String storedValue;
    try {
      storedValue = readStoredValue();
      if (storedValue != null && !isYoung(storedValue)) {
        return decode(storedValue);
      } else if (storedValue == null) {
        storeNewSecret();
      }
      Thread.sleep(settleMillis);
      storedValue = readStoredValue();
    } catch (RuntimeException e) {
      LOG.error("The secret shared by the nodes of the cluster cannot be read from the global settings: this node refuses the other nodes",
                e);
      return null;
    }
    return storedValue == null ? null : decode(storedValue);
  }

  private void storeNewSecret() {
    try {
      storeSecret(generateSecret());
      LOG.info("Stored a new secret shared by the nodes of the cluster");
    } catch (RuntimeException e) {
      LOG.info("Could not store a new secret shared by the nodes of the cluster: reading the stored one back");
      LOG.debug("Store of the secret shared by the nodes of the cluster failed", e);
    }
  }

  /**
   * @param storedValue a stored value
   * @return true when it was stored less than {@link #STORE_WINDOW_MILLIS}
   *         ago; a value stored with no instant is not
   */
  private boolean isYoung(String storedValue) {
    long storedAt = storedAt(storedValue);
    return storedAt > 0 && clock.getAsLong() - storedAt < STORE_WINDOW_MILLIS;
  }

  private static long storedAt(String storedValue) {
    int separator = storedValue.indexOf(STORED_AT_SEPARATOR);
    if (separator <= 0) {
      return 0;
    }
    try {
      return Long.parseLong(storedValue.substring(0, separator));
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  private String decode(String storedValue) {
    String encodedSecret = storedAt(storedValue) > 0 ? storedValue.substring(storedValue.indexOf(STORED_AT_SEPARATOR) + 1)
                                                     : storedValue;
    try {
      String secret = codec.decode(encodedSecret);
      if (isOwnSecret(secret)) {
        return secret;
      }
    } catch (RuntimeException e) {
      LOG.debug("Decoding the secret shared by the nodes of the cluster failed", e);
    }
    LOG.error("The secret shared by the nodes of the cluster, stored in the global settings, is not a secret this node can use: it is left as is");
    return null;
  }

  private String readStoredValue() {
    RequestLifeCycle.begin(container);
    try {
      SettingValue<?> value = settingStore.get(Context.GLOBAL, SETTING_SCOPE, SETTING_KEY);
      String storedValue = value == null || value.getValue() == null ? null : value.getValue().toString();
      return storedValue == null || storedValue.isBlank() ? null : storedValue;
    } finally {
      RequestLifeCycle.end();
    }
  }

  private void storeSecret(String secret) {
    RequestLifeCycle.begin(container);
    try {
      settingStore.set(Context.GLOBAL,
                       SETTING_SCOPE,
                       SETTING_KEY,
                       SettingValue.create(clock.getAsLong() + String.valueOf(STORED_AT_SEPARATOR) + codec.encode(secret)));
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
