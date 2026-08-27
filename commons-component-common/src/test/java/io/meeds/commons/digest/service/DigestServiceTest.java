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

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.jpa.BaseTest;

import io.meeds.commons.digest.DigestService;

public class DigestServiceTest extends BaseTest {

  private static final Scope  DIGEST_SCOPE       = Scope.APPLICATION.id("NotificationDigestSetting");

  private static final String DIGEST_ALLOWED_KEY = "exo:digestAllowed";

  private DigestService       digestService;

  private SettingService      settingService;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    settingService = getService(SettingService.class);
    settingService.remove(Context.GLOBAL, DIGEST_SCOPE, DIGEST_ALLOWED_KEY);
    // The bean is started by the Spring context (SocialApplication scans
    // io.meeds.commons.digest); here it is instantiated directly against the
    // real SettingService of the test container
    digestService = new DigestServiceImpl(settingService);
  }

  @Override
  protected void tearDown() throws Exception {
    settingService.remove(Context.GLOBAL, DIGEST_SCOPE, DIGEST_ALLOWED_KEY);
    super.tearDown();
  }

  public void testDigestIsNotAllowedByDefault() {
    assertFalse(digestService.isDigestAllowed());
  }

  public void testSaveDigestAllowed() {
    digestService.saveDigestAllowed(true);
    assertTrue(digestService.isDigestAllowed());

    // The value must be really persisted, readable outside the service
    SettingValue<?> settingValue = settingService.get(Context.GLOBAL, DIGEST_SCOPE, DIGEST_ALLOWED_KEY);
    assertNotNull(settingValue);
    assertEquals("true", String.valueOf(settingValue.getValue()));

    digestService.saveDigestAllowed(false);
    assertFalse(digestService.isDigestAllowed());
  }

}
