/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
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
package org.exoplatform.commons.api.notification.plugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.UserSetting;

import junit.framework.TestCase;

public class ObjectsCloneTest extends TestCase {

  public void testCloneUserSettting() {
    String channelId = "channel_test";
    String channelId1 = "channel_test1";
    String pluginId = "NewUserPlugin";

    UserSetting setting = UserSetting.getInstance();
    setting.setUserId("test");
    setting.setChannelActive(channelId);
    Arrays.stream(new String[] { "ActivityMentionPlugin", "PostActivityPlugin", "ActivityCommentPlugin", "SpaceInvitationPlugin",
        "RequestJoinSpacePlugin" }).forEach(id -> setting.addChannelPlugin(channelId, id));

    UserSetting clone = setting.clone();
    assertEquals(setting.getUserId(), clone.getUserId()); // NOSONAR

    clone.setUserId("test1");
    clone.setChannelActive(channelId1);
    clone.addChannelPlugin(channelId, pluginId);
    clone.addChannelPlugin(channelId1, pluginId);
    //
    assertFalse(setting.getUserId().equals(clone.getUserId()));
    //
    assertFalse(setting.getPlugins(channelId).contains(pluginId));
    assertTrue(clone.getPlugins(channelId).contains(pluginId));
    //
    assertTrue(setting.getPlugins(channelId1).isEmpty());
    assertTrue(clone.getPlugins(channelId1).contains(pluginId));
    //
    assertTrue(setting.getChannelActives().contains(channelId));
    assertFalse(setting.getChannelActives().contains(channelId1));
    assertTrue(clone.getChannelActives().contains(channelId));
    assertTrue(clone.getChannelActives().contains(channelId1));
  }

  public void testNotificationInfoClone() {
    Map<String, String> ownerParameter = new HashMap<>();
    ownerParameter.put("test", "value test");
    NotificationInfo info = NotificationInfo.instance();
    info.setFrom("demo")
        .key("notifiId")
        .setOrder(1)
        .setOwnerParameter(ownerParameter)
        .setSendToDaily(new String[] { "plugin1", "plugin2" })
        .setSendToWeekly(new String[] { "plugin2", "plugin3" })
        .setTo("root");
    NotificationInfo clone = info.clone();
    assertEquals(info.getId(), clone.getId());
    assertEquals(info.getId(), clone.getId());
    assertEquals(info.getFrom(), clone.getFrom());
    assertEquals(info.getTo(), clone.getTo());
    //
    assertTrue(Arrays.equals(info.getSendToDaily(), clone.getSendToDaily()));
    assertTrue(Arrays.equals(info.getSendToWeekly(), clone.getSendToWeekly()));
    assertTrue(CollectionUtils.isEqualCollection(info.getOwnerParameter().keySet(), clone.getOwnerParameter().keySet()));
    assertTrue(CollectionUtils.isEqualCollection(info.getOwnerParameter().values(), clone.getOwnerParameter().values()));
    assertEquals(info.getValueOwnerParameter("test"), clone.getValueOwnerParameter("test"));
    //
    clone.getSendToDaily()[0] = "plugin4";
    clone.getOwnerParameter().put("test", "value clone");
    //
    assertFalse(Arrays.equals(info.getSendToDaily(), clone.getSendToDaily()));
    assertFalse(CollectionUtils.isEqualCollection(info.getOwnerParameter().values(), clone.getOwnerParameter().values()));
    //
    assertFalse(info.getValueOwnerParameter("test").equals(clone.getValueOwnerParameter("test")));
  }

}
