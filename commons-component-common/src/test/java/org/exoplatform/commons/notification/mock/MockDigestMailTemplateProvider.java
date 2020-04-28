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
package org.exoplatform.commons.notification.mock;

import java.io.Writer;
import java.util.List;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.annotation.TemplateConfig;
import org.exoplatform.commons.api.notification.annotation.TemplateConfigs;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationChildPlugin;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.commons.api.notification.service.setting.PluginContainer;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.notification.channel.template.DigestMailTemplateProvider;
import org.exoplatform.commons.notification.impl.DigestDailyPlugin;
import org.exoplatform.commons.notification.impl.DigestWeeklyPlugin;
import org.exoplatform.commons.notification.plugin.PluginTest;
import org.exoplatform.commons.notification.template.TemplateUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.xml.InitParams;

@TemplateConfigs(
   templates = {
       @TemplateConfig(pluginId = PluginTest.ID, template = "classpath:/groovy/notification/template/TestPlugin.gtmpl"),
       @TemplateConfig(pluginId = DigestDailyPlugin.ID, template = "classpath:/groovy/notification/template/provider1.gtmpl"),
       @TemplateConfig(pluginId = DigestWeeklyPlugin.ID, template = "classpath:/groovy/notification/template/provider1.gtmpl")
   }
 )
public class MockDigestMailTemplateProvider extends DigestMailTemplateProvider {

  public MockDigestMailTemplateProvider(InitParams initParams) {
    super(initParams);
    templateBuilders.put(new PluginKey(PluginTest.ID), testBuilder);
  }
  AbstractTemplateBuilder testBuilder = new AbstractTemplateBuilder() {
    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      NotificationInfo notification = ctx.getNotificationInfo();
      String language = getLanguage(notification);
      TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
      
      
      templateContext.put("USER", notification.getValueOwnerParameter("USER"));
      templateContext.put("SUBJECT", "Test plugin notification");
      String subject = TemplateUtils.processSubject(templateContext);

      String value = notification.getValueOwnerParameter("TEST_VALUE");
      templateContext.put("VALUE", value);
      StringBuilder childContent = new StringBuilder();
      
      PluginContainer pluginContainer = CommonsUtils.getService(PluginContainer.class);
      List<PluginKey> childKeys = pluginContainer.getChildPluginKeys(new PluginKey(PluginTest.ID));
      for (PluginKey notificationKey : childKeys) {
        BaseNotificationPlugin child = pluginContainer.getPlugin(notificationKey);
        childContent.append("<br>").append(((AbstractNotificationChildPlugin) child).makeContent(ctx));
      }
      templateContext.put("CHILD_CONTENT", childContent.toString());
      
      return new MessageInfo().subject(subject).body(TemplateUtils.processGroovy(templateContext)).end();
    }
    @Override
    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
      return false;
    }
  };
}
