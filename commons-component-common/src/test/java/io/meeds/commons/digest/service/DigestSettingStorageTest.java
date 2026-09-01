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

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.jpa.BaseTest;

import io.meeds.commons.digest.model.DigestUserSettings;

/**
 * Checks that the digest settings are really written in the digest own scope
 * through the {@link SettingService}, and read back identically.
 */
public class DigestSettingStorageTest extends BaseTest {

  private static final Scope           DIGEST_SCOPE       = Scope.APPLICATION.id("NotificationDigestSetting");

  private static final String          DIGEST_ALLOWED_KEY = "exo:digestAllowed";

  private static final String          USERNAME           = "digestUser";

  private DigestSettingStorage         settingStorage;

  private SettingService               settingService;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    settingService = getService(SettingService.class);
    settingStorage = new DigestSettingStorage(settingService);
    clean();
  }

  @Override
  protected void tearDown() throws Exception {
    clean();
    super.tearDown();
  }

  public void testDigestIsNotAllowedByDefault() {
    assertFalse(settingStorage.isDigestAllowed());
  }

  public void testSaveDigestAllowed() {
    settingStorage.saveDigestAllowed(true);
    assertTrue(settingStorage.isDigestAllowed());

    // The value must be really persisted, readable outside the storage
    SettingValue<?> settingValue = settingService.get(Context.GLOBAL, DIGEST_SCOPE, DIGEST_ALLOWED_KEY);
    assertNotNull(settingValue);
    assertEquals("true", String.valueOf(settingValue.getValue()));

    settingStorage.saveDigestAllowed(false);
    assertFalse(settingStorage.isDigestAllowed());
  }

  public void testUserSettingsAreEmptyByDefault() {
    DigestUserSettings settings = settingStorage.getUserSettings(USERNAME);
    assertNotNull(settings);
    assertFalse(settings.isDaily());
    assertFalse(settings.isWeekly());
    assertTrue(settings.getDailyCategories().isEmpty());
    assertTrue(settings.getWeeklyCategories().isEmpty());
  }

  public void testSaveUserSettings() {
    settingStorage.saveUserSettings(USERNAME,
                                    new DigestUserSettings(true,
                                                           Arrays.asList("spaces", "feed"),
                                                           false,
                                                           Collections.emptyList()));

    DigestUserSettings settings = settingStorage.getUserSettings(USERNAME);
    assertTrue(settings.isDaily());
    assertEquals(Arrays.asList("spaces", "feed"), settings.getDailyCategories());
    assertFalse(settings.isWeekly());
    assertTrue(settings.getWeeklyCategories().isEmpty());
  }

  public void testSaveUserSettingsOverwritesThePreviousChoices() {
    settingStorage.saveUserSettings(USERNAME,
                                    new DigestUserSettings(true,
                                                           Arrays.asList("spaces", "feed"),
                                                           false,
                                                           Collections.emptyList()));
    settingStorage.saveUserSettings(USERNAME,
                                    new DigestUserSettings(false,
                                                           Collections.emptyList(),
                                                           true,
                                                           Collections.singletonList("spaces")));

    DigestUserSettings settings = settingStorage.getUserSettings(USERNAME);
    assertFalse(settings.isDaily());
    assertTrue(settings.getDailyCategories().isEmpty());
    assertTrue(settings.isWeekly());
    assertEquals(Collections.singletonList("spaces"), settings.getWeeklyCategories());
  }

  public void testUserSettingsAreNotSharedBetweenUsers() {
    settingStorage.saveUserSettings(USERNAME,
                                    new DigestUserSettings(true,
                                                           Collections.singletonList("spaces"),
                                                           false,
                                                           Collections.emptyList()));

    assertFalse(settingStorage.getUserSettings("anotherUser").isDaily());
  }

  private void clean() {
    settingService.remove(Context.GLOBAL, DIGEST_SCOPE, DIGEST_ALLOWED_KEY);
    settingService.remove(Context.USER.id(USERNAME), DIGEST_SCOPE);
    settingService.remove(Context.USER.id("anotherUser"), DIGEST_SCOPE);
  }

}
