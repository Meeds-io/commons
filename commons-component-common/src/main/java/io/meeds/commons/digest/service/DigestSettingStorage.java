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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;

import io.meeds.commons.digest.model.DigestUserSettings;

/**
 * Reads and writes the digest settings through {@link SettingService}, whose
 * cache is in-memory and cluster-coherent: a change saved on one server applies
 * immediately on every server. All the digest keys live in the digest own
 * scope, the legacy notification settings are never used.
 */
@Component
public class DigestSettingStorage {

  private static final Scope   DIGEST_SCOPE            = Scope.APPLICATION.id("NotificationDigestSetting");

  private static final String  DIGEST_ALLOWED_KEY      = "exo:digestAllowed";

  private static final String  DIGEST_DAILY_KEY        = "exo:digestDaily";

  private static final String  DIGEST_DAILY_CATEGORIES = "exo:digestDailyCategories";

  private static final String  DIGEST_WEEKLY_KEY       = "exo:digestWeekly";

  private static final String  DIGEST_WEEKLY_CATEGORIES = "exo:digestWeeklyCategories";

  private static final String  CATEGORIES_SEPARATOR    = ",";

  private final SettingService settingService;

  public DigestSettingStorage(SettingService settingService) {
    this.settingService = settingService;
  }

  public boolean isDigestAllowed() {
    return getBoolean(Context.GLOBAL, DIGEST_ALLOWED_KEY);
  }

  public void saveDigestAllowed(boolean allowed) {
    set(Context.GLOBAL, DIGEST_ALLOWED_KEY, String.valueOf(allowed));
  }

  public DigestUserSettings getUserSettings(String username) {
    Context userContext = Context.USER.id(username);
    return new DigestUserSettings(getBoolean(userContext, DIGEST_DAILY_KEY),
                                  getCategories(userContext, DIGEST_DAILY_CATEGORIES),
                                  getBoolean(userContext, DIGEST_WEEKLY_KEY),
                                  getCategories(userContext, DIGEST_WEEKLY_CATEGORIES));
  }

  public void saveUserSettings(String username, DigestUserSettings settings) {
    Context userContext = Context.USER.id(username);
    set(userContext, DIGEST_DAILY_KEY, String.valueOf(settings.isDaily()));
    set(userContext, DIGEST_DAILY_CATEGORIES, join(settings.getDailyCategories()));
    set(userContext, DIGEST_WEEKLY_KEY, String.valueOf(settings.isWeekly()));
    set(userContext, DIGEST_WEEKLY_CATEGORIES, join(settings.getWeeklyCategories()));
  }

  private boolean getBoolean(Context context, String key) {
    SettingValue<?> settingValue = settingService.get(context, DIGEST_SCOPE, key);
    return settingValue != null && Boolean.parseBoolean(String.valueOf(settingValue.getValue()));
  }

  private List<String> getCategories(Context context, String key) {
    SettingValue<?> settingValue = settingService.get(context, DIGEST_SCOPE, key);
    if (settingValue == null) {
      return Collections.emptyList();
    }
    String value = String.valueOf(settingValue.getValue());
    if (StringUtils.isBlank(value)) {
      return Collections.emptyList();
    }
    return Arrays.asList(StringUtils.split(value, CATEGORIES_SEPARATOR));
  }

  private String join(List<String> categories) {
    return categories == null ? "" : StringUtils.join(categories, CATEGORIES_SEPARATOR);
  }

  private void set(Context context, String key, String value) {
    settingService.set(context, DIGEST_SCOPE, key, SettingValue.create(value));
  }

}
