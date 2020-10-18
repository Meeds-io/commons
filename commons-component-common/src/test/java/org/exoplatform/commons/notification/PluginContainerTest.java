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
package org.exoplatform.commons.notification;

import java.util.List;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.channel.template.PluginTemplateBuilderAdapter;
import org.exoplatform.commons.api.notification.model.ChannelKey;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.commons.api.notification.service.setting.PluginContainer;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.notification.channel.MailChannel;
import org.exoplatform.commons.notification.impl.DigestDailyPlugin;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.notification.plugin.PluginPLF41Test;
import org.exoplatform.commons.notification.plugin.PluginTest;
import org.exoplatform.commons.notification.template.TemplateUtils;

public class PluginContainerTest extends BaseNotificationTestCase {
  
  private PluginContainer container;
  public PluginContainerTest() {
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    container = getService(PluginContainer.class);
    assertNotNull(container);    
  }
  
  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }
  
  public void testPlugin() {
    // check existing plugin
    PluginKey pluginKey = new PluginKey(PluginTest.ID);
    BaseNotificationPlugin plugin = container.getPlugin(pluginKey);
    assertNotNull(plugin);
    // get child
    List<PluginKey> chikdKeys = container.getChildPluginKeys(pluginKey);
    assertEquals(1, chikdKeys.size());
    assertEquals("Child_Plugin", chikdKeys.get(0).getId());
    
    //
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    NotificationInfo notificationInfo = plugin.buildNotification(ctx);
    assertNotNull(notificationInfo);
    assertEquals("demo", notificationInfo.getSendToUserIds().get(0));
    assertEquals(PluginTest.ID, notificationInfo.getKey().getId());
    //
    ctx.setNotificationInfo(notificationInfo);
    //
    AbstractChannel channel = ctx.getChannelManager().getChannel(ChannelKey.key(MailChannel.ID));
    assertNotNull(channel);
    
    AbstractTemplateBuilder builder = channel.getTemplateBuilder(pluginKey);
    MessageInfo messageInfo = builder.buildMessage(ctx);
    
    // check subject
    assertEquals("The subject Test plugin notification", messageInfo.getSubject());
    // check content
    assertTrue(messageInfo.getBody().indexOf("root") > 0);
    assertTrue(messageInfo.getBody().indexOf("Test value") > 0);

    // check process resource-bundle on plugin
    assertTrue(messageInfo.getBody().indexOf("The test plugin") > 0);
    // check child plugin content
    assertTrue(messageInfo.getBody().indexOf("The content of child plugin") > 0);
    // check process resource-bundle on plugin
    assertTrue(messageInfo.getBody().indexOf("The test child plugin") > 0);
  }
  
  public void testPLF41Plugin() {
    // check existing plugin
    PluginKey pluginKey = new PluginKey(PluginPLF41Test.ID);
    BaseNotificationPlugin plugin = container.getPlugin(pluginKey);
    assertNotNull(plugin);
    
    //
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    NotificationInfo notificationInfo = plugin.buildNotification(ctx);
    assertNotNull(notificationInfo);
    assertEquals("demo", notificationInfo.getTo());
    assertEquals(PluginPLF41Test.ID, notificationInfo.getKey().getId());
    //
    ctx.setNotificationInfo(notificationInfo);
    //
    AbstractChannel channel = ctx.getChannelManager().getChannel(ChannelKey.key(MailChannel.ID));
    assertNotNull(channel);
    AbstractTemplateBuilder templateBuilder = channel.getTemplateBuilder(pluginKey);
    
    assertTrue((templateBuilder instanceof PluginTemplateBuilderAdapter));
    
    MessageInfo messageInfo = templateBuilder.buildMessage(ctx);
    // check subject
    assertEquals(PluginPLF41Test.SUBJECT, messageInfo.getSubject());
    // check process resource-bundle on plugin
    assertTrue(messageInfo.getBody().indexOf("The test plugin") > 0);
  }

  public void testRenderPlugin() throws Exception {
    TemplateContext ctx = TemplateContext.newChannelInstance(ChannelKey.key(MailChannel.ID), DigestDailyPlugin.ID, null);
    ctx.put("FIRSTNAME", "User ROOT");
    ctx.put("USER", "root");
    ctx.put("ACTIVITY", "Content of Activity");
    String s = TemplateUtils.processGroovy(ctx);
    // check process resource-bundle
    assertEquals(true, s.indexOf("Test resource bundle.") > 0);
    // check process Groovy
    assertEquals(true, s.indexOf("Content of Activity") > 0);
  }
}