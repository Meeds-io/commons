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
package org.exoplatform.commons.notification.channel;

import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.channel.ChannelManager;
import org.exoplatform.commons.api.notification.model.ChannelKey;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.notification.BaseNotificationTestCase;
import org.exoplatform.commons.notification.impl.DigestDailyPlugin;
import org.exoplatform.commons.notification.impl.DigestWeeklyPlugin;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Dec 13, 2014  
 */
public class ChannelManagerTest extends BaseNotificationTestCase {
  private ChannelManager manager;
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    manager = getService(ChannelManager.class);
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }
  
  public void testChannelSize() throws Exception {
    assertTrue(manager.sizeChannels() > 0);
  }
  
  public void testGetChannel() throws Exception {
    AbstractChannel channel = manager.getChannel(ChannelKey.key(MailChannel.ID));
    assertTrue(channel != null);
    //check the daily
    String actual = channel.getTemplateFilePath(PluginKey.key(DigestDailyPlugin.ID));
    String expected = "classpath:/groovy/notification/template/provider1.gtmpl";
    assertEquals(expected, actual);
    //check the weekly
    actual = channel.getTemplateFilePath(PluginKey.key(DigestWeeklyPlugin.ID));
    expected = "classpath:/groovy/notification/template/provider1.gtmpl";
    assertEquals(expected, actual);
  }
}
