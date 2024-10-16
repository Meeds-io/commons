/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Affero General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.notification.impl.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.channel.ChannelManager;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.model.ChannelKey;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.model.WebNotificationFilter;
import org.exoplatform.commons.api.notification.plugin.config.PluginConfig;
import org.exoplatform.commons.api.notification.service.WebNotificationService;
import org.exoplatform.commons.api.notification.service.setting.PluginSettingService;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.commons.api.notification.service.storage.WebNotificationStorage;
import org.exoplatform.commons.notification.channel.WebChannel;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.notification.net.WebNotificationSender;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class WebNotificationServiceImpl implements WebNotificationService {

  private static final Log LOG = ExoLogger.getLogger(WebNotificationServiceImpl.class);

  private final PluginSettingService   pluginSettingService;

  private final UserSettingService     userSettingService;

  private final WebNotificationStorage storage;

  private final AbstractChannel        webChannel;
  
  public WebNotificationServiceImpl(PluginSettingService pluginSettingService,
                                    UserSettingService userSettingService,
                                    WebNotificationStorage webStorage,
                                    ChannelManager channelManager) {
    this.storage = webStorage;
    this.pluginSettingService = pluginSettingService;
    this.userSettingService = userSettingService;
    this.webChannel = channelManager.getChannel(ChannelKey.key(WebChannel.ID));
  }

  @Override
  public void save(NotificationInfo notification) {
    storage.save(notification);
    String username = notification.getTo();
    sendWebNotificationUpdate(username);
  }

  @Override
  public void update(NotificationInfo notification, boolean moveTop) {
    storage.update(notification, moveTop);
    String username = notification.getTo();
    sendWebNotificationUpdate(username);
  }

  @Override
  public NotificationInfo getUnreadNotification(String pluginId, String activityId, String userId) {
    return storage.getUnreadNotification(pluginId, activityId, userId);
  }

  @Override
  public NotificationInfo getNotificationInfo(String notificationId) {
    return storage.get(notificationId);
  }
  
  @Override
  public void markRead(String notificationId) {
    storage.markRead(notificationId);
    NotificationInfo notification = storage.get(notificationId);
    String username = notification.getTo();
    sendWebNotificationUpdate(username);
  }

  @Override
  public void markAllRead(String userId) {
    storage.markAllRead(userId);
    sendWebNotificationUpdate(userId, 0);
  }

  @Override
  public void markAllRead(List<String> plugins, String username) {
    if (CollectionUtils.isEmpty(plugins)) {
      markAllRead(username);
    } else {
      storage.markAllRead(plugins, username);
      int badgeNumber = storage.getNumberOnBadge(username);
      sendWebNotificationUpdate(username, badgeNumber);
    }
  }

  @Override
  public List<NotificationInfo> getNotificationInfos(WebNotificationFilter filter, int offset, int limit) {
    List<NotificationInfo> notifications = new ArrayList<>();
    boolean limitReached = true;
    do {
      List<NotificationInfo> list = storage.get(filter, offset, limit);
      notifications.addAll(list);
      limitReached = list.size() < limit || notifications.size() >= limit;
      offset += limit;
    } while (!limitReached);
    return limit > 0 ? notifications.stream()
                     .limit(limit)
                     .map(this::buildSpaceMuteProperties)
                     .toList() : notifications;
  }

  @Override
  @Deprecated(forRemoval = true, since = "1.5.0")
  public String getNotificationMessage(NotificationInfo notification, boolean isOnPopover) {
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.append(POPUP_OVER, isOnPopover);
    return getNotificationMessage(ctx, notification);
  }

  @Override
  public List<String> get(WebNotificationFilter filter, int offset, int limit) {
    List<NotificationInfo> notifications = getNotificationInfos(filter, offset, limit);

    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.append(POPUP_OVER, filter.isOnPopover());
    return notifications.stream().map(notification -> getNotificationMessage(ctx, notification)).toList();
  }

  @Override
  public boolean remove(String notificationId) {
    NotificationInfo notification = storage.get(notificationId);
    try {
      return storage.remove(notificationId);
    } finally {
      String username = notification.getTo();
      sendWebNotificationUpdate(username);
    }
  }

  @Override
  public void hidePopover(String notificationId) {
    storage.hidePopover(notificationId);
    NotificationInfo notification = storage.get(notificationId);
    String username = notification.getTo();
    sendWebNotificationUpdate(username);
  }

  @Override
  public void resetNumberOnBadge(String userId) {
    storage.resetNumberOnBadge(userId);
    sendWebNotificationUpdate(userId, 0);
  }

  @Override
  public void resetNumberOnBadge(List<String> plugins, String username) {
    if (CollectionUtils.isEmpty(plugins)) {
      resetNumberOnBadge(username);
    } else {
      storage.resetNumberOnBadge(plugins, username);
      sendWebNotificationUpdate(username, 0);
    }
  }

  @Override
  public int getNumberOnBadge(String userId) {
    if (StringUtils.isBlank(userId)) {
      throw new IllegalArgumentException("User is mandatory");
    }
    return storage.getNumberOnBadge(userId);
  }

  @Override
  public Map<String, Integer> countUnreadByPlugin(String userId) {
    if (StringUtils.isBlank(userId)) {
      throw new IllegalArgumentException("User is mandatory");
    }
    return storage.countUnreadByPlugin(userId);
  }

  private NotificationInfo buildSpaceMuteProperties(NotificationInfo notification) {
    if (notification.getSpaceId() > 0) {
      PluginConfig pluginConfig = pluginSettingService.getPluginConfig(notification.getKey().getId());
      notification.setMutable(pluginConfig != null && pluginConfig.isMutable());
      if (notification.getTo() != null) {
        UserSetting userSetting = userSettingService.get(notification.getTo());
        if (userSetting != null) {
          notification.setSpaceMuted(userSetting.isSpaceMuted(notification.getSpaceId()));
        }
      }
    }
    return notification;
  }

  private String getNotificationMessage(NotificationContext ctx, NotificationInfo notification) {
    AbstractTemplateBuilder builder = webChannel.getTemplateBuilder(notification.getKey());
    if (builder != null) {
      try {
        MessageInfo msg = builder.buildMessage(ctx.setNotificationInfo(notification));
        if (ctx.isFailed()) {
          LOG.warn("Error while building message for notification with id '{}'", notification.getId(), ctx.getException());
        } else if (msg != null && msg.getBody() != null && !msg.getBody().isEmpty()) {
          return msg.getBody();
        }
      } catch (Exception e) {
        LOG.error("Error while building message for notification with id '{}'", notification.getId(), e);
      }
    }
    return null;
  }

  private void sendWebNotificationUpdate(String username) {
    if (StringUtils.isNotBlank(username)) {
      int badgeNumber = storage.getNumberOnBadge(username);
      sendWebNotificationUpdate(username, badgeNumber);
    }
  }

  private void sendWebNotificationUpdate(String username, int badgeNumber) {
    WebNotificationSender.sendJsonMessage(username,
                                          new MessageInfo().setNumberOnBadge(badgeNumber));
  }

}
