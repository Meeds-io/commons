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
package org.exoplatform.commons.notification.lifecycle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.lifecycle.DefaultLifecycle;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.plugin.config.PluginConfig;
import org.exoplatform.commons.api.notification.service.setting.PluginSettingService;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.container.ExoContainerContext;

class DefaultLifecycleTest {

  private static final String PLUGIN_ID  = "pluginId";

  private static final String CHANNEL_ID = "channelId";

  @Test
  void testARecipientWhoseSettingsCannotBeReadIsSkippedAndTheOthersAreProcessed() {
    List<String> dispatchedUsers = new ArrayList<>();
    AbstractChannel channel = mock(AbstractChannel.class);
    when(channel.getId()).thenReturn(CHANNEL_ID);
    doAnswer(invocation -> dispatchedUsers.add(invocation.getArgument(1)))
                .when(channel)
                .dispatch(any(NotificationContext.class),
                         anyString());
    UserSettingService userSettingService = mock(UserSettingService.class);
    UserSetting readable = new UserSetting();
    readable.setChannelActive(CHANNEL_ID);
    readable.setChannelPlugins(CHANNEL_ID, List.of(PLUGIN_ID));
    when(userSettingService.get("unreadable")).thenReturn(null);
    when(userSettingService.get("readable")).thenReturn(readable);
    PluginSettingService pluginSettingService = mock(PluginSettingService.class);
    when(pluginSettingService.getPluginConfig(PLUGIN_ID)).thenReturn(new PluginConfig());
    NotificationContext ctx = NotificationContextImpl.cloneInstance()
                                                     .setNotificationInfo(NotificationInfo.instance().key(PLUGIN_ID));
    DefaultLifecycle lifecycle = new DefaultLifecycle();
    lifecycle.setChannel(channel);

    try (MockedStatic<ExoContainerContext> container = mockStatic(ExoContainerContext.class)) {
      container.when(() -> ExoContainerContext.getService(UserSettingService.class)).thenReturn(userSettingService);
      container.when(() -> ExoContainerContext.getService(PluginSettingService.class)).thenReturn(pluginSettingService);
      lifecycle.process(ctx, "unreadable", "readable");
    }

    assertEquals(List.of("readable"), dispatchedUsers);
  }

}
