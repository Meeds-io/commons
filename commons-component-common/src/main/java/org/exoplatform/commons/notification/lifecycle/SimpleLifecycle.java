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
package org.exoplatform.commons.notification.lifecycle;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.lifecycle.AbstractNotificationLifecycle;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.commons.utils.CommonsUtils;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Dec 12, 2014  
 */
public final class SimpleLifecycle extends AbstractNotificationLifecycle {
  @Override
  public void process(NotificationContext ctx, String userId) {
    getChannel().dispatch(ctx, userId);
  }
  
  @Override
  public void process(NotificationContext ctx, String... userIds) {
    NotificationInfo notification = ctx.getNotificationInfo();
    String pluginId = notification.getKey().getId();
    UserSettingService userService = CommonsUtils.getService(UserSettingService.class);
    
    for (String userId : userIds) {
      UserSetting userSetting = userService.get(userId);
      //check channel active for user
      if (!userSetting.isEnabled() || !userSetting.isChannelActive(getChannel().getId())) {
        continue;
      }
      
      if (userSetting.isActive(getChannel().getId(), pluginId)) {
        process(ctx.setNotificationInfo(notification.clone(true).setTo(userId)), userId);
      }
    }
  }
  
  @Override
  public void send(NotificationContext ctx) {
  }
}
