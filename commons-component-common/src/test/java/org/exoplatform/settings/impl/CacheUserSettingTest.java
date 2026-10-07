/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
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
package org.exoplatform.settings.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.concurrent.ConcurrentFIFOExoCache;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.settings.jpa.CacheUserSettingServiceImpl;
import org.exoplatform.settings.jpa.JPAUserSettingServiceImpl;

public class CacheUserSettingTest {

  private static final String USER_ID = "demo";
  private CacheService cacheService;
  private JPAUserSettingServiceImpl userSettingServiceImpl;
  private CacheUserSettingServiceImpl cacheUserSettingServiceImpl;
  private ListenerService listenerService;
  private UserSetting userSetting = new UserSetting();

  @Before
  public void setUp() throws Exception {
    cacheService = mock(CacheService.class);
    userSettingServiceImpl = mock(JPAUserSettingServiceImpl.class);
    listenerService = mock(ListenerService.class);

    userSetting.setUserId(USER_ID);

    when(cacheService.getCacheInstance(CacheUserSettingServiceImpl.CACHE_NAME)).thenReturn(new ConcurrentFIFOExoCache<>());
    when(userSettingServiceImpl.get(USER_ID)).thenReturn(userSetting);

    cacheUserSettingServiceImpl = new CacheUserSettingServiceImpl(cacheService, listenerService, userSettingServiceImpl);
  }

  @Test
  public void testGetUserSetting() {
    cacheUserSettingServiceImpl.get(USER_ID);
    verify(userSettingServiceImpl, times(1)).get(USER_ID);

    cacheUserSettingServiceImpl.get(USER_ID);
    verify(userSettingServiceImpl, times(1)).get(USER_ID);
  }

  /**
   * The cache hands out a copy of the loaded settings: a disabled user must
   * stay disabled in it, on the loading call and on the cached ones, or every
   * notification lifecycle delivers to that user (EXO-90779).
   */
  @Test
  public void testGetKeepsTheEnabledStatus() {
    UserSetting disabledSetting = new UserSetting();
    disabledSetting.setUserId(USER_ID);
    disabledSetting.setEnabled(false);
    UserSetting enabledSetting = new UserSetting();
    enabledSetting.setUserId(USER_ID);
    when(userSettingServiceImpl.get(USER_ID)).thenReturn(disabledSetting, enabledSetting);

    assertFalse(cacheUserSettingServiceImpl.get(USER_ID).isEnabled());
    assertFalse(cacheUserSettingServiceImpl.get(USER_ID).isEnabled());

    // Re-enabling the user evicts the disabled settings from the cache
    cacheUserSettingServiceImpl.setUserEnabled(USER_ID, true);
    assertTrue(cacheUserSettingServiceImpl.get(USER_ID).isEnabled());
    assertTrue(cacheUserSettingServiceImpl.get(USER_ID).isEnabled());
  }

  @Test
  public void testGetAndRemoveUserSetting() {
    cacheUserSettingServiceImpl.get(USER_ID);
    verify(userSettingServiceImpl, times(1)).get(USER_ID);

    cacheUserSettingServiceImpl.setUserEnabled(USER_ID, true);

    cacheUserSettingServiceImpl.get(USER_ID);
    verify(userSettingServiceImpl, times(2)).get(USER_ID);
  }

  @Test
  public void testGetAndSaveUserSetting() {
    cacheUserSettingServiceImpl.get(USER_ID);
    verify(userSettingServiceImpl, times(1)).get(USER_ID);

    cacheUserSettingServiceImpl.save(userSetting);

    cacheUserSettingServiceImpl.get(USER_ID);
    verify(userSettingServiceImpl, times(2)).get(USER_ID);
  }

}
