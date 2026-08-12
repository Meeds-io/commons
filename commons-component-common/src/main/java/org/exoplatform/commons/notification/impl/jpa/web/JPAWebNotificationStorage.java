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
package org.exoplatform.commons.notification.impl.jpa.web;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.api.notification.NotificationMessageUtils;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.model.WebNotificationFilter;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.commons.api.notification.service.storage.WebNotificationStorage;
import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.notification.impl.jpa.web.dao.WebNotifDAO;
import org.exoplatform.commons.notification.impl.jpa.web.dao.WebParamsDAO;
import org.exoplatform.commons.notification.impl.jpa.web.dao.WebUsersDAO;
import org.exoplatform.commons.notification.impl.jpa.web.entity.WebNotifEntity;
import org.exoplatform.commons.notification.impl.jpa.web.entity.WebParamsEntity;
import org.exoplatform.commons.notification.impl.jpa.web.entity.WebUsersEntity;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class JPAWebNotificationStorage implements WebNotificationStorage {

  private static final Log         LOG = ExoLogger.getLogger(JPAWebNotificationStorage.class);

  private final UserSettingService userSettingService;

  private WebNotifDAO              webNotifDAO;

  private WebParamsDAO             webParamsDAO;

  private WebUsersDAO              webUsersDAO;

  private ListenerService          listenerService;

  public JPAWebNotificationStorage(WebNotifDAO webNotifDAO,
                                   WebParamsDAO webParamsDAO,
                                   WebUsersDAO webUsersDAO,
                                   UserSettingService userSettingService,
                                   ListenerService listenerService) {
    this.userSettingService = userSettingService;
    this.webNotifDAO = webNotifDAO;
    this.webParamsDAO = webParamsDAO;
    this.webUsersDAO = webUsersDAO;
    this.listenerService = listenerService;
  }

  @Override
  @ExoTransactional
  public void save(NotificationInfo notification) {
    save(notification, false);
  }

  @Override
  @ExoTransactional
  public List<NotificationInfo> get(WebNotificationFilter filter, int offset, int limit) {
    return webUsersDAO.findWebNotificationsByFilter(filter, offset, limit).stream().map(n -> {
      try {
        return this.convertWebNotifEntityToNotificationInfo(n);
      } catch (Exception e) {
        LOG.warn("Error while converting Web Notification Entity {} to DTO", n, e);
        return null;
      }
    }).filter(Objects::nonNull).toList();
  }

  @Override
  @ExoTransactional
  public NotificationInfo get(String id) {
    if (StringUtils.isBlank(id) || id.startsWith(NotificationInfo.PREFIX_ID)) {
      return null;
    }
    WebUsersEntity webUsersEntity = getWebNotification(parseNotificationId(id));
    if (webUsersEntity != null) {
      return convertWebNotifEntityToNotificationInfo(webUsersEntity);
    }
    return null;
  }

  @Override
  @ExoTransactional
  public boolean remove(String notificationId) {
    WebUsersEntity webUsersEntity = getWebNotification(parseNotificationId(notificationId));
    if (webUsersEntity != null) {
      webUsersDAO.delete(webUsersEntity);
      listenerService.broadcast(NOTIFICATION_WEB_DELETED_EVENT, notificationId, null);
      broadcastBadgeUpdated(webUsersEntity.getReceiver());
      return true;
    }
    return false;
  }

  @Override
  @ExoTransactional
  public boolean remove(long seconds) {
    Calendar cal = Calendar.getInstance();
    long delayTime = System.currentTimeMillis() - (seconds * 1000);
    cal.setTimeInMillis(delayTime);
    int webUserNotisCount = webUsersDAO.countWebNotifsByLastUpdatedDate(cal);
    int processedNotifs = 0;
    int batch = 100;
    List<WebNotifEntity> notifEntities = new ArrayList<>();
    while (webUserNotisCount > processedNotifs) {
      List<WebUsersEntity> webUserNotifs = webUsersDAO.findWebNotifsByLastUpdatedDate(cal, -1, batch);
      for (WebUsersEntity webUsersEntity : webUserNotifs) {
        WebNotifEntity notification = webUsersEntity.getNotification();
        if (!notifEntities.contains(notification)) {
          notifEntities.add(notification);
        }
        webUsersDAO.delete(webUsersEntity);
        processedNotifs++;
        listenerService.broadcast(NOTIFICATION_WEB_DELETED_EVENT, String.valueOf(notification.getId()), null);
      }
      if (!notifEntities.isEmpty() && (notifEntities.size() >= 100 || webUserNotisCount == processedNotifs)) {
        for (WebNotifEntity webNotifEntity : notifEntities) {
          try {
            webNotifDAO.delete(webNotifDAO.find(webNotifEntity.getId()));
          } catch (jakarta.persistence.RollbackException rollbackException) {
            LOG.warn("Could not deleting the notification {}, proceeding to delete the following one", webNotifEntity.getId());
          }
        }
        RequestLifeCycle.restartTransaction();
        notifEntities = new ArrayList<>();
        LOG.info("Deleted {} / {} web notifications of users", processedNotifs, webUserNotisCount);
      }
    }
    return false;
  }

  @Override
  @ExoTransactional
  public boolean remove(String userId, long seconds) {
    Calendar calendar = Calendar.getInstance();
    long timeInMilliseconds = calendar.getTimeInMillis() - seconds * 1000;
    calendar.setTimeInMillis(timeInMilliseconds);

    boolean removed = false;
    for (WebUsersEntity webUsersEntity : webUsersDAO.findWebNotifsOfUserByLastUpdatedDate(userId, calendar)) {
      try {
        webUsersDAO.delete(webUsersEntity);
        removed = true;
      } catch (Exception e) {
        LOG.error("Failed to remove notification with id '" + webUsersEntity.getId() + "' for the user id: " + userId, e);
        return false;
      }
    }
    if (removed) {
      // Once for the whole batch rather than per deleted notification
      broadcastBadgeUpdated(userId);
    }
    return removed;
  }

  @Override
  @ExoTransactional
  public void markRead(String notificationId) {
    long notifIdLong = parseNotificationId(notificationId);
    WebUsersEntity webUsersEntity = webUsersDAO.find(notifIdLong);
    if (webUsersEntity != null) {
      webUsersEntity.setRead(true);
      webUsersEntity.setResetNumberOnBadge(true);
      webUsersEntity = webUsersDAO.update(webUsersEntity);
      listenerService.broadcast(NOTIFICATION_WEB_READ_EVENT, notificationId, null);

      // FIXME: Start:: Delete when all Web notifs migrated to use Vue based
      // templates
      NotificationInfo notification = get(notificationId);
      Map<String, String> ownerParameters = notification.getOwnerParameter();
      ownerParameters.put(NotificationMessageUtils.READ_PORPERTY.getKey(), String.valueOf(webUsersEntity.isRead()));
      updateNotificationParameters(webUsersEntity.getNotification(), ownerParameters, false);
      // FIXME: End
    }
  }

  @Override
  @ExoTransactional
  public void hidePopover(String notificationId) {
    WebUsersEntity webUsersEntity = webUsersDAO.find(parseNotificationId(notificationId));
    if (webUsersEntity != null) {
      webUsersEntity.setShowPopover(false);
      webUsersEntity.setRead(true);
      webUsersEntity.setResetNumberOnBadge(true);
      webUsersEntity = webUsersDAO.update(webUsersEntity);

      // FIXME: Start:: Delete when all Web notifs migrated to use Vue based
      // templates
      NotificationInfo notification = get(notificationId);
      Map<String, String> ownerParameters = notification.getOwnerParameter();
      ownerParameters.put(NotificationMessageUtils.READ_PORPERTY.getKey(), String.valueOf(webUsersEntity.isRead()));
      ownerParameters.put(NotificationMessageUtils.SHOW_POPOVER_PROPERTY.getKey(),
                          String.valueOf(webUsersEntity.isShowPopover()));
      updateNotificationParameters(webUsersEntity.getNotification(), ownerParameters, false);
      // FIXME: End
      broadcastBadgeUpdated(webUsersEntity.getReceiver());
    }
  }

  @Override
  @ExoTransactional
  public void markAllRead(String username) {
    webUsersDAO.markAllRead(username);
    userSettingService.saveLastReadDate(username, System.currentTimeMillis());
    listenerService.broadcast(NOTIFICATION_WEB_READ_ALL_EVENT, username, null);
    broadcastBadgeUpdated(username);
  }

  @Override
  public void markAllRead(List<String> plugins, String username) {
    List<WebUsersEntity> notifsWithBadge = webUsersDAO.findUnreadByUserAndPlugins(plugins, username);
    if (CollectionUtils.isNotEmpty(notifsWithBadge)) {
      notifsWithBadge.forEach(n -> {
        n.setResetNumberOnBadge(true);
        n.setRead(true);
      });
      webUsersDAO.updateAll(notifsWithBadge);
      notifsWithBadge.forEach(n -> listenerService.broadcast(NOTIFICATION_WEB_READ_EVENT, String.valueOf(n.getId()), null));
      broadcastBadgeUpdated(username);
    }
  }

  @Override
  @ExoTransactional
  public NotificationInfo getUnreadNotification(String pluginId, String activityId, String userId) {
    List<WebUsersEntity> list = webUsersDAO.findUnreadNotification(pluginId, userId, "activityId", activityId);

    if (!list.isEmpty()) {
      WebUsersEntity webUsersNotification = list.get(0);
      return convertWebNotifEntityToNotificationInfo(webUsersNotification);
    }
    return null;
  }

  @Override
  @ExoTransactional
  public void update(NotificationInfo notification, boolean moveTop) {
    // if moveTop == true, the number on badge will increase
    // else the number on badge will not increase
    save(notification, moveTop);
  }

  @Override
  public void updateNotificationParameters(String notificationId, Map<String, String> ownerParameters) {
    WebUsersEntity webUsersEntity = webUsersDAO.find(parseNotificationId(notificationId));
    if (webUsersEntity != null) {
      updateNotificationParameters(webUsersEntity.getNotification(), ownerParameters, false);
    }
  }

  @Override
  @ExoTransactional
  public int getNumberOnBadge(String userId) {
    return webUsersDAO.getNumberOnBadge(userId);
  }

  @Override
  public Map<String, Integer> countUnreadByPlugin(String userId) {
    return webUsersDAO.countUnreadByPlugin(userId);
  }

  @Override
  @ExoTransactional
  public void resetNumberOnBadge(String userId) {
    List<WebUsersEntity> notifsWithBadge = webUsersDAO.findNotifsWithBadge(userId);
    if (CollectionUtils.isNotEmpty(notifsWithBadge)) {
      for (WebUsersEntity webUsersEntity : notifsWithBadge) {
        webUsersEntity.setResetNumberOnBadge(true);
      }
      webUsersDAO.updateAll(notifsWithBadge);
      broadcastBadgeUpdated(userId);
    }
  }

  @Override
  public void resetNumberOnBadge(List<String> plugins, String username) {
    List<WebUsersEntity> notifsWithBadge = webUsersDAO.findNotifsWithBadgeByPlugins(plugins, username);
    if (CollectionUtils.isNotEmpty(notifsWithBadge)) {
      notifsWithBadge.forEach(n -> n.setResetNumberOnBadge(true));
      webUsersDAO.updateAll(notifsWithBadge);
      broadcastBadgeUpdated(username);
    }
  }

  /**
   * Creates the notification message to the specified user.
   *
   * @param notification The notification to save
   * @param moveTop The status to update count on Popover or not
   */
  private void save(NotificationInfo notification, boolean moveTop) {
    WebUsersEntity webUsersEntity = null;
    if (notification.getId() != null && !notification.getId().startsWith(NotificationInfo.PREFIX_ID)) {
      webUsersEntity = webUsersDAO.find(Long.parseLong(notification.getId()));
    }
    boolean isNew = webUsersEntity == null;
    WebNotifEntity webNotifEntity = null;
    if (isNew) {
      webNotifEntity = new WebNotifEntity();
      webUsersEntity = new WebUsersEntity();
    } else {
      webNotifEntity = webUsersEntity.getNotification();
    }
    // fill WebNotifEntity with data from notification
    webNotifEntity.setType(notification.getKey().getId());
    webNotifEntity.setText(notification.getTitle());
    webNotifEntity.setSender(notification.getFrom());
    if (webNotifEntity.getCreationDate() == null) {
      webNotifEntity.setCreationDate(Calendar.getInstance());
    }
    if (isNew) {
      webNotifEntity = webNotifDAO.create(webNotifEntity);
    } else {
      webNotifEntity = webNotifDAO.update(webNotifEntity);
    }

    Map<String, String> ownerParameters = notification.getOwnerParameter();
    updateNotificationParameters(webNotifEntity, ownerParameters, isNew);

    // fill WebUsersEntity with data from notification
    webUsersEntity.setReceiver(notification.getTo());
    Calendar calendar = Calendar.getInstance();
    if (moveTop) {
      webUsersEntity.setUpdateDate(calendar);
    } else if (notification.getLastModifiedDate() > 0) {
      calendar.setTimeInMillis(notification.getLastModifiedDate());
      webUsersEntity.setUpdateDate(calendar);
    } else {
      webUsersEntity.setUpdateDate(webNotifEntity.getCreationDate());
    }

    webUsersEntity.setResetNumberOnBadge(notification.isResetOnBadge());
    // FIXME: Start:: Delete when all Web notifs migrated to use Vue based
    // templates
    if (ownerParameters != null && ownerParameters.containsKey(NotificationMessageUtils.READ_PORPERTY.getKey())) {
      webUsersEntity.setRead(Boolean.parseBoolean(ownerParameters.get(NotificationMessageUtils.READ_PORPERTY.getKey())
                                                                 .toLowerCase()));
    } else {
      webUsersEntity.setRead(notification.isRead());
    }
    if (ownerParameters != null && ownerParameters.containsKey(NotificationMessageUtils.SHOW_POPOVER_PROPERTY.getKey())) {
      webUsersEntity.setShowPopover(Boolean.parseBoolean(ownerParameters.get(NotificationMessageUtils.SHOW_POPOVER_PROPERTY.getKey())
                                                                        .toLowerCase()));
    } else {
      webUsersEntity.setShowPopover(notification.isOnPopOver());
    }
    // FIXME: End

    webUsersEntity.setNotification(webNotifEntity);
    if (isNew) {
      webUsersEntity = webUsersDAO.create(webUsersEntity);
      notification.setId(String.valueOf(webUsersEntity.getId()));
    } else {
      webUsersDAO.update(webUsersEntity);
    }
    listenerService.broadcast(NOTIFICATION_WEB_SAVED_EVENT, notification.getId(), isNew);
    // Reached by both save(NotificationInfo) and update(NotificationInfo,
    // boolean), which funnel here — so the badge event is raised once, not twice
    broadcastBadgeUpdated(webUsersEntity.getReceiver());
  }

  /**
   * Announces that a user's notification badge counter may have changed.
   * <p>
   * Centralized on purpose: the counter is altered by a handful of unrelated
   * operations — a notification saved or updated, one removed, a whole batch
   * purged, all of them marked read, the popover hidden, or the counter reset
   * when the user opens the drawer — and every one of them must raise the same
   * event. Consumers displaying that counter elsewhere, such as the Application
   * Center badge on the notification centre tile, then need to know about one
   * event rather than about each operation.
   * <p>
   * Kept out of {@code WebNotificationService}: several services call this
   * storage directly rather than going through it, so the service layer is not
   * an exhaustive interception point.
   *
   * @param username the user whose counter changed, ignored when blank
   */
  private void broadcastBadgeUpdated(String username) {
    if (StringUtils.isBlank(username)) {
      return;
    }
    listenerService.broadcast(NOTIFICATION_WEB_BADGE_UPDATED_EVENT, username, null);
  }

  private void updateNotificationParameters(WebNotifEntity webNotifEntity, Map<String, String> ownerParameters, boolean isNew) {
    Set<WebParamsEntity> parameters = webNotifEntity.getParameters();
    if (ownerParameters != null && !ownerParameters.isEmpty()) {
      for (String propertyName : ownerParameters.keySet()) {
        // fill WebParamsEntity with data from notification
        WebParamsEntity webParamsEntity = null;
        boolean isParamNew = true;
        if (isNew) {
          webParamsEntity = new WebParamsEntity();
        } else {
          for (WebParamsEntity webParamsEntityTmp : parameters) {
            if (webParamsEntityTmp.getName().equals(propertyName)) {
              webParamsEntity = webParamsEntityTmp;
              isParamNew = false;
              break;
            }
          }
        }
        if (webParamsEntity == null) {
          webParamsEntity = new WebParamsEntity();
        }
        webParamsEntity.setName(propertyName);
        webParamsEntity.setValue(ownerParameters.get(propertyName));
        webParamsEntity.setNotification(webNotifEntity);
        if (isParamNew) {
          webParamsDAO.create(webParamsEntity);
        } else {
          webParamsDAO.update(webParamsEntity);
        }
      }
    }
  }

  private long parseNotificationId(String notificationId) {
    return Long.parseLong(notificationId);
  }

  @ExoTransactional
  private WebUsersEntity getWebNotification(Long notificationId) {
    return webUsersDAO.find(notificationId);
  }

  /**
   * Convert user web notification entity to notification DTO
   * 
   * @param webUsersEntity user web notification
   * @return notification DTO
   */
  public NotificationInfo convertWebNotifEntityToNotificationInfo(WebUsersEntity webUsersEntity) {
    NotificationInfo notificationInfo = new NotificationInfo();
    WebNotifEntity notification = webUsersEntity.getNotification();

    notificationInfo.setLastModifiedDate(webUsersEntity.getUpdateDate());

    Set<WebParamsEntity> parameters = notification.getParameters();
    Map<String, String> ownerParameters =
                                        parameters.stream()
                                                  .collect(Collectors.toMap(WebParamsEntity::getName,
                                                                            value -> value.getValue() == null ? ""
                                                                                                              : value.getValue(),
                                                                            (v1, v2) -> v2));
    // FIXME: Start:: Delete when all Web notifs migrated to use Vue based
    // templates
    ownerParameters = new HashMap<>(ownerParameters);
    ownerParameters.put(NotificationMessageUtils.READ_PORPERTY.getKey(), String.valueOf(webUsersEntity.isRead()));
    ownerParameters.put(NotificationMessageUtils.SHOW_POPOVER_PROPERTY.getKey(), String.valueOf(webUsersEntity.isShowPopover()));
    // FIXME: End
    notificationInfo.setOwnerParameter(ownerParameters);
    notificationInfo.key(new PluginKey(notification.getType()));
    notificationInfo.setTitle(notification.getText());
    notificationInfo.setFrom(notification.getSender());
    notificationInfo.to(webUsersEntity.getReceiver());
    notificationInfo.setRead(webUsersEntity.isRead());
    notificationInfo.setOnPopOver(webUsersEntity.isShowPopover());
    notificationInfo.setResetOnBadge(webUsersEntity.isResetNumberOnBadge());
    notificationInfo.setDateCreated(notification.getCreationDate());
    notificationInfo.setId(String.valueOf(webUsersEntity.getId()));
    return notificationInfo;
  }
}
