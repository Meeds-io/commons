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
package org.exoplatform.commons.notification.impl.service;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.channel.ChannelManager;
import org.exoplatform.commons.api.notification.lifecycle.AbstractNotificationLifecycle;
import org.exoplatform.commons.api.notification.model.ChannelKey;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.config.PluginConfig;
import org.exoplatform.commons.api.notification.service.setting.PluginSettingService;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.commons.api.notification.service.storage.NotificationService;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.notification.NotificationContextFactory;
import org.exoplatform.commons.notification.impl.AbstractService;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserProfile;

public class NotificationServiceImpl extends AbstractService implements NotificationService {

  private static final Log                 LOG = ExoLogger.getLogger(NotificationServiceImpl.class);

  /** */
  private final UserSettingService         userService;

  /** */
  private final NotificationContextFactory notificationContextFactory;

  /** */
  private final ChannelManager             channelManager;

  /** */
  private final OrganizationService        organizationService;

  private final ListenerService            listenerService;

  public NotificationServiceImpl(ChannelManager channelManager,
                                 UserSettingService userService,
                                 OrganizationService organizationService,
                                 NotificationContextFactory notificationContextFactory,
                                 ListenerService listenerService) {
    this.listenerService = listenerService;
    this.userService = userService;
    this.organizationService = organizationService;
    this.notificationContextFactory = notificationContextFactory;
    this.channelManager = channelManager;
  }

  @Override
  public void process(NotificationInfo notification) throws Exception {
    if (notification == null) {
      throw new IllegalArgumentException("Notification argument shouldn't be null");
    }
    String pluginId = notification.getKey().getId();
    // statistic metrics
    if (this.notificationContextFactory.getStatisticsService().isStatisticsEnabled()) {
      this.notificationContextFactory.getStatisticsCollector().createNotificationInfoCount(pluginId);
    }
    //
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(notification);

    broadcastProcessedEvent(notification);
    //
    PluginSettingService pluginSettingService = CommonsUtils.getService(PluginSettingService.class);
    SettingService settingService = CommonsUtils.getService(SettingService.class);
    List<AbstractChannel> channels = channelManager.getChannels();
    Exception error = null;
    for (AbstractChannel channel : channels) {
      try {
        if (!pluginSettingService.isActive(channel.getId(), pluginId)) {
          continue;
        }
        process(settingService, ctx, notification, channel);
      } catch (Exception e) {
        LOG.warn("Error processing notification with id '{}' on channel '{}' for plugin '{}'",
                 notification.getId(),
                 channel.getId(),
                 notification.getKey().getId(),
                 e);
        error = e;
      }
    }
    if (error != null) {
      // Must indicate error status for the notification
      throw error;
    }
  }

  @Override
  public void process(Collection<NotificationInfo> messages) throws Exception {
    for (NotificationInfo message : messages) {
      if (message == null) {
        continue;
      }
      process(message);
    }
  }

  private AbstractNotificationLifecycle process(SettingService settingService,
                                                NotificationContext notificationContext,
                                                NotificationInfo notification,
                                                AbstractChannel channel) {
    AbstractNotificationLifecycle lifecycle = channelManager.getLifecycle(ChannelKey.key(channel.getId()));
    if (notification.isSendAll() || notification.isSendAllInternals()) {
      processSendAll(settingService, notificationContext, notification, lifecycle);
    } else {
      if (notification.getSendToUserIds() == null || notification.getSendToUserIds().isEmpty()) {
        LOG.debug("Notification with id '{}' and parameters = '{}' not sent because receivers are empty",
                  notification.getId(),
                  notification.getOwnerParameter());
      } else {
        processSendToUsers(notificationContext, notification, lifecycle);
      }
    }
    return lifecycle;
  }

  private void processSendAll(SettingService settingService,
                              NotificationContext notificationContext,
                              NotificationInfo notification,
                              AbstractNotificationLifecycle lifecycle) {
    long usersCount = settingService.countContextsByType(Context.USER.getName());
    int maxResults = 100;
    for (int i = 0; i < usersCount; i += maxResults) {
      List<String> users = settingService.getContextNamesByType(Context.USER.getName(), i, maxResults);
      if (notification.isSendAllInternals()) {
        users = users.stream().filter(userId -> {
          // Filter on external users
          try {
            UserProfile userProfile = organizationService.getUserProfileHandler().findUserProfileByName(userId);
            return userProfile == null || !StringUtils.equals(userProfile.getAttribute(UserProfile.OTHER_KEYS[2]), "true");
          } catch (Exception e) {
            return false;
          }
        }).toList();
      }
      if (!notification.getExcludedUsersIds().isEmpty()) {
        users = users.stream().filter(userId -> !notification.isExcluded(userId)).toList();
      }
      if (!users.isEmpty()) {
        processLifecycle(notificationContext, lifecycle, users);
      }
    }
  }

  private void processSendToUsers(NotificationContext notificationContext,
                                  NotificationInfo notification,
                                  AbstractNotificationLifecycle lifecycle) {
    List<String> userIds = notification.getSendToUserIds();
    processLifecycle(notificationContext, lifecycle, userIds);
  }

  private void processLifecycle(NotificationContext notificationContext,
                                AbstractNotificationLifecycle lifecycle,
                                List<String> userIds) {
    NotificationInfo notificationInfo = notificationContext.getNotificationInfo();
    long spaceId = notificationInfo.getSpaceId();
    if (spaceId > 0) {
      PluginSettingService pluginSettingService = CommonsUtils.getService(PluginSettingService.class);
      PluginConfig pluginConfig = pluginSettingService.getPluginConfig(notificationInfo.getKey().getId());
      if (pluginConfig.isMutable()) {
        userIds = userIds.stream()
                         .filter(username -> !pluginConfig.isMutable() || !userService.get(username).isSpaceMuted(spaceId))
                         .toList();
      }
    }
    lifecycle.process(notificationContext, userIds.toArray(new String[userIds.size()]));
  }

  /**
   * Tells whoever wants to know, starting with the digest capture, that a
   * notification goes out — whatever the channels do with it. A listener
   * failure is logged and dropped: nothing here may ever make the notification
   * itself fail.
   */
  private void broadcastProcessedEvent(NotificationInfo notification) {
    try {
      listenerService.broadcast(NOTIFICATION_PROCESSED_EVENT, this, notification);
    } catch (Exception e) {
      LOG.warn("Error broadcasting the processed event of notification '{}' of plugin '{}'",
               notification.getId(),
               notification.getKey().getId(),
               e);
    }
  }
}
