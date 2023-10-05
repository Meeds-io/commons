/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.notification.lifecycle;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.lifecycle.AbstractNotificationLifecycle;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.service.setting.PluginSettingService;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.commons.api.notification.service.storage.WebNotificationStorage;
import org.exoplatform.commons.notification.channel.WebChannel;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.util.Calendar;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Dec 12, 2014  
 */
public class WebLifecycle extends AbstractNotificationLifecycle {
  private static final Log LOG = ExoLogger.getLogger(WebLifecycle.class);

  @Override
  public void process(NotificationContext ctx, String... userIds) {
    NotificationInfo notification = ctx.getNotificationInfo();
    String pluginId = notification.getKey().getId();
    if (!CommonsUtils.getService(PluginSettingService.class).isActive(WebChannel.ID, pluginId)
        || notification.isRead()) {
      return;
    }

    UserSettingService userService = CommonsUtils.getService(UserSettingService.class);
    for (String userId : userIds) {
      UserSetting userSetting = userService.get(userId);
      if (userSetting == null
          || !userSetting.isEnabled()
          || !userSetting.isChannelGloballyActive(WebChannel.ID)
          || !userSetting.isActive(WebChannel.ID, pluginId)
          || userSetting.isSpaceMuted(notification.getSpaceId())) {
        continue;
      }
      NotificationInfo notif = notification.clone(true).setTo(userId).setLastModifiedDate(Calendar.getInstance());
      store(notif);

      //build message
      ctx.setNotificationInfo(notif);
      MessageInfo msg = buildMessageInfo(ctx);
      if (msg == null) {
        CommonsUtils.getService(WebNotificationStorage.class).remove(notif.getId());
        continue;
      }

      ctx.append(WebChannel.MESSAGE_INFO, msg);
      ctx.value(WebChannel.MESSAGE_INFO).setId(notif.getId());
      //send
      getChannel().dispatch(ctx, userId);
    }
  }
  
  @Override
  public void process(NotificationContext ctx, String userId) {
    LOG.info("Web Notification process user: " + userId);
  }
  
  @Override
  public void store(NotificationInfo notifInfo) {
    // Get notification to update
    AbstractTemplateBuilder builder = getChannel().getTemplateBuilder(notifInfo.getKey());
    notifInfo = builder.getNotificationToStore(notifInfo);
    notifInfo.setOnPopOver(true);
    notifInfo.setRead(false);
    CommonsUtils.getService(WebNotificationStorage.class).save(notifInfo);
  }
  
  @Override
  public void send(NotificationContext ctx) {
    LOG.info("WEB:: Send the message by Web channel.");
  }

  /**
   * Builds the message inform from the notification context.
   * 
   * @param ctx
   * @return
   */
  private MessageInfo buildMessageInfo(NotificationContext ctx) {
    AbstractTemplateBuilder builder = getChannel().getTemplateBuilder(ctx.getNotificationInfo().getKey());
    return builder.buildMessage(ctx);
  }

}
