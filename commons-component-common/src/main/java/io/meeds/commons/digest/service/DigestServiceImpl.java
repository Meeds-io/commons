/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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
package io.meeds.commons.digest.service;

import org.springframework.stereotype.Service;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;

import io.meeds.commons.digest.DigestService;

/**
 * Stores the digest settings through {@link SettingService}, whose cache is
 * in-memory and cluster-coherent: a change saved on one server applies
 * immediately on every server.
 */
@Service
public class DigestServiceImpl implements DigestService {

  private static final Scope   DIGEST_SCOPE       = Scope.APPLICATION.id("NotificationDigestSetting");

  private static final String  DIGEST_ALLOWED_KEY = "exo:digestAllowed";

  private final SettingService settingService;

  public DigestServiceImpl(SettingService settingService) {
    this.settingService = settingService;
  }

  @Override
  public boolean isDigestAllowed() {
    // Reads go through SettingService on purpose: its cache
    // (commons.SettingService ExoCache) is in-memory AND cluster-coherent,
    // so an admin change on one server applies immediately on all servers
    SettingValue<?> settingValue = settingService.get(Context.GLOBAL, DIGEST_SCOPE, DIGEST_ALLOWED_KEY);
    return settingValue != null && Boolean.parseBoolean(String.valueOf(settingValue.getValue()));
  }

  @Override
  public void saveDigestAllowed(boolean allowed) {
    settingService.set(Context.GLOBAL, DIGEST_SCOPE, DIGEST_ALLOWED_KEY, SettingValue.create(String.valueOf(allowed)));
  }

}
