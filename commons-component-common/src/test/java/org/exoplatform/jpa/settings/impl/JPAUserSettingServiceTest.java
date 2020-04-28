/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.jpa.settings.impl;

import java.util.List;

import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.notification.channel.MailChannel;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.jpa.BaseTest;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.idm.UserImpl;
import org.exoplatform.settings.jpa.JPAUserSettingServiceImpl;

public class JPAUserSettingServiceTest extends BaseTest {

  protected static JPAUserSettingServiceImpl userSettingService;
  protected static OrganizationService organizationService;

  @Override
  public void setUp() {
    super.setUp();
    userSettingService = getService(JPAUserSettingServiceImpl.class);
    organizationService = getService(OrganizationService.class);
  }

  @Override
  protected void tearDown() {
    for (int i = 0; i < 10; i++) {
      try {
        organizationService.getUserHandler().removeUser("user_" + i, false);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void test_1_GetDefautSetting() throws Exception {
    for (int i = 0; i < 10; i++) {
      User user = new UserImpl("user_" + i);
      organizationService.getUserHandler().createUser(user, false);
      userSettingService.initDefaultSettings(user.getUserName());
    }
    List<UserSetting> list = userSettingService.getDigestDefaultSettingForAllUser(0, 5);

    assertEquals(5, list.size());

    list = userSettingService.getDigestDefaultSettingForAllUser(0, 0);

    assertEquals(10, list.size());
  }

  public void testDisabledUser() throws Exception {
    User u = CommonsUtils.getService(OrganizationService.class).getUserHandler().createUserInstance("binh");
    u.setEmail("email@test");
    u.setFirstName("first");
    u.setLastName("last");
    u.setPassword("pwdADDSomeSaltToBeCompliantWithSomeIS00");
    CommonsUtils.getService(OrganizationService.class).getUserHandler().createUser(u, true);

    userSettingService.save(createUserSetting("binh", null, null, null));
    UserSetting userSetting = userSettingService.get("binh");
    assertTrue(userSetting.isEnabled());
    assertTrue(userSetting.isChannelActive(MailChannel.ID));

    //disable user "root"
    CommonsUtils.getService(OrganizationService.class).getUserHandler().setEnabled("binh", false, true);
    userSetting = userSettingService.get("binh");
    assertTrue(userSetting.isChannelActive(MailChannel.ID));
    assertFalse(userSetting.isEnabled());

    //enable user "root" but not change the active channel status
    CommonsUtils.getService(OrganizationService.class).getUserHandler().setEnabled("binh", true, true);
    userSetting = userSettingService.get("binh");
    assertTrue(userSetting.isChannelActive(MailChannel.ID));
    assertTrue(userSetting.isEnabled());

    CommonsUtils.getService(OrganizationService.class).getUserHandler().removeUser("binh", false);
    assertNull(CommonsUtils.getService(OrganizationService.class).getUserHandler().findUserByName("binh"));

  }

  private UserSetting createUserSetting(String userId, List<String> instantly, List<String> daily, List<String> weekly) {
    UserSetting model = new UserSetting();
    model.setUserId(userId);
    model.setChannelActive(UserSetting.EMAIL_CHANNEL);
    model.setDailyPlugins(daily);
    model.setChannelPlugins(UserSetting.EMAIL_CHANNEL, instantly);
    model.setWeeklyPlugins(weekly);
    return model;
  }
}
