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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import jakarta.persistence.EntityManager;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.channel.ChannelManager;
import org.exoplatform.commons.api.notification.lifecycle.AbstractNotificationLifecycle;
import org.exoplatform.commons.api.notification.model.ChannelKey;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.config.PluginConfig;
import org.exoplatform.commons.api.notification.service.NotificationCompletionService;
import org.exoplatform.commons.api.notification.service.setting.PluginSettingService;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.commons.api.notification.service.storage.NotificationService;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.notification.NotificationContextFactory;
import org.exoplatform.commons.notification.impl.AbstractService;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserProfile;

public class NotificationServiceImpl extends AbstractService implements NotificationService {

  private static final Log                 LOG       = ExoLogger.getLogger(NotificationServiceImpl.class);

  private static final int                 PAGE_SIZE = 100;

  /** */
  private final UserSettingService         userService;

  /** */
  private final NotificationContextFactory notificationContextFactory;

  /** */
  private final ChannelManager             channelManager;

  /** */
  private final OrganizationService        organizationService;

  private final ListenerService            listenerService;

  private final NotificationCompletionService completionService;

  private final EntityManagerService       entityManagerService;

  public NotificationServiceImpl(ChannelManager channelManager,
                                 UserSettingService userService,
                                 OrganizationService organizationService,
                                 NotificationContextFactory notificationContextFactory,
                                 ListenerService listenerService,
                                 NotificationCompletionService completionService,
                                 EntityManagerService entityManagerService) {
    this.listenerService = listenerService;
    this.completionService = completionService;
    this.entityManagerService = entityManagerService;
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
    List<AbstractChannel> channels = new ArrayList<>();
    Exception error = null;
    for (AbstractChannel channel : channelManager.getChannels()) {
      try {
        if (pluginSettingService.isActive(channel.getId(), pluginId)) {
          channels.add(channel);
        }
      } catch (Exception e) {
        logChannelError(notification, channel, e);
        error = e;
      }
    }
    Exception processingError = null;
    if (notification.isSendAll() || notification.isSendAllInternals()) {
      processingError = processSendAll(settingService, ctx, notification, channels);
    } else if (notification.getSendToUserIds() == null || notification.getSendToUserIds().isEmpty()) {
      LOG.debug("Notification with id '{}' and parameters = '{}' not sent because receivers are empty",
                notification.getId(),
                notification.getOwnerParameter());
    } else {
      processingError = processSendToUsers(ctx, notification, channels);
    }
    if (processingError != null) {
      error = processingError;
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

  /**
   * Walks every user page by page, and hands each page to every channel in turn:
   * a page is listed and filtered once, its user settings are loaded by the
   * first channel and looked up in the user settings cache by the next ones, as
   * long as the cache holds the page.
   *
   * @return the last error a channel raised, null if none did
   */
  private Exception processSendAll(SettingService settingService,
                                   NotificationContext notificationContext,
                                   NotificationInfo notification,
                                   List<AbstractChannel> channels) {
    if (channels.isEmpty()) {
      return null;
    }
    List<AbstractChannel> remainingChannels = new ArrayList<>(channels);
    Exception error = null;
    long usersCount = settingService.countContextsByType(Context.USER.getName());
    for (int i = 0; i < usersCount && !remainingChannels.isEmpty(); i += PAGE_SIZE) {
      List<String> users = settingService.getContextNamesByType(Context.USER.getName(), i, PAGE_SIZE);
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
      error = lastError(processPage(notificationContext, notification, remainingChannels, users), error);
    }
    return error;
  }

  /**
   * Hands the recipients page by page to every channel in turn, as a send-all
   * does.
   *
   * @return the last error a channel raised, null if none did
   */
  private Exception processSendToUsers(NotificationContext notificationContext,
                                       NotificationInfo notification,
                                       List<AbstractChannel> channels) {
    List<AbstractChannel> remainingChannels = new ArrayList<>(channels);
    Exception error = null;
    List<String> userIds = notification.getSendToUserIds();
    for (int i = 0; i < userIds.size() && !remainingChannels.isEmpty(); i += PAGE_SIZE) {
      List<String> users = userIds.subList(i, Math.min(i + PAGE_SIZE, userIds.size()));
      error = lastError(processPage(notificationContext, notification, remainingChannels, users), error);
    }
    return error;
  }

  /**
   * Hands one page of recipients to every remaining channel in turn, then
   * empties the persistence context. A channel that fails is removed from the
   * remaining channels: it is not given the next pages.
   *
   * @return the last error a channel raised on this page, null if none did
   */
  private Exception processPage(NotificationContext notificationContext,
                                NotificationInfo notification,
                                List<AbstractChannel> remainingChannels,
                                List<String> users) {
    Exception error = null;
    if (!users.isEmpty()) {
      Iterator<AbstractChannel> channelsIterator = remainingChannels.iterator();
      while (channelsIterator.hasNext()) {
        AbstractChannel channel = channelsIterator.next();
        try {
          processLifecycle(notificationContext, getLifecycle(channel), users);
        } catch (Exception e) {
          logChannelError(notification, channel, e);
          error = e;
          channelsIterator.remove();
        }
      }
    }
    clearPersistenceContext();
    return error;
  }

  private Exception lastError(Exception pageError, Exception previousError) {
    return pageError == null ? previousError : pageError;
  }

  /**
   * Detaches what the kernel EntityManager of the thread has loaded so far. Each
   * transactional read of a user's settings commits, and each commit
   * dirty-checks every entity of the persistence context: without this, the
   * context grows with every recipient, and so does the cost of each next
   * read. Done only on a thread of the notification pool, whose task
   * begins and ends its own request lifecycle and so owns the EntityManager,
   * and only outside a transaction, whose pending changes a clear would discard.
   */
  private void clearPersistenceContext() {
    if (!completionService.isPoolThread()) {
      return;
    }
    EntityManager entityManager = entityManagerService.getEntityManager();
    if (entityManager != null && !entityManager.getTransaction().isActive()) {
      entityManager.clear();
    }
  }

  private AbstractNotificationLifecycle getLifecycle(AbstractChannel channel) {
    return channelManager.getLifecycle(ChannelKey.key(channel.getId()));
  }

  private void logChannelError(NotificationInfo notification, AbstractChannel channel, Exception e) {
    LOG.warn("Error processing notification with id '{}' on channel '{}' for plugin '{}'",
             notification.getId(),
             channel.getId(),
             notification.getKey().getId(),
             e);
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
