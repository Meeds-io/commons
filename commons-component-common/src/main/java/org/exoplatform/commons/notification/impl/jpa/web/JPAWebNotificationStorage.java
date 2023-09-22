package org.exoplatform.commons.notification.impl.jpa.web;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

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
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class JPAWebNotificationStorage implements WebNotificationStorage {

  private static final Log         LOG            = ExoLogger.getLogger(JPAWebNotificationStorage.class);

  private final UserSettingService userSettingService;

  private WebNotifDAO              webNotifDAO;

  private WebParamsDAO             webParamsDAO;

  private WebUsersDAO              webUsersDAO;

  private static final String      NTF_NAME_SPACE = "ntf:";

  public JPAWebNotificationStorage(WebNotifDAO webNotifDAO,
                                   WebParamsDAO webParamsDAO,
                                   WebUsersDAO webUsersDAO,
                                   UserSettingService userSettingService) {
    this.userSettingService = userSettingService;
    this.webNotifDAO = webNotifDAO;
    this.webParamsDAO = webParamsDAO;
    this.webUsersDAO = webUsersDAO;
  }

  @Override
  @ExoTransactional
  public void save(NotificationInfo notification) {
    save(notification, false);
  }

  @Override
  @ExoTransactional
  public List<NotificationInfo> get(WebNotificationFilter filter, int offset, int limit) {
    return webUsersDAO.findWebNotificationsByFilter(filter, offset, limit)
                      .stream()
                      .map(n -> {
                        try {
                          return this.convertWebNotifEntityToNotificationInfo(n);
                        } catch (Exception e) {
                          LOG.warn("Error while converting Web Notification Entity {} to DTO", n, e);
                          return null;
                        }
                      })
                      .filter(Objects::nonNull)
                      .toList();
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
      return true;
    }
    return false;
  }

  @Override
  @ExoTransactional
  public boolean remove(long seconds) {
    boolean removed = false;
    Calendar cal = Calendar.getInstance();
    long delayTime = System.currentTimeMillis() - (seconds * 1000);
    cal.setTimeInMillis(delayTime);
    List<WebNotifEntity> notifEntities = new ArrayList<>();
    List<WebUsersEntity> webUserNotifs = webUsersDAO.findWebNotifsByLastUpdatedDate(cal);
    for (WebUsersEntity webUsersEntity : webUserNotifs) {
      WebNotifEntity notification = webUsersEntity.getNotification();
      if (!notifEntities.contains(notification)) {
        notifEntities.add(notification);
      }
      webUsersDAO.delete(webUsersEntity);
    }
    removed = !notifEntities.isEmpty();
    if (removed) {
      for (WebNotifEntity webNotifEntity : notifEntities) {
        webParamsDAO.deleteAll(new ArrayList<>(webNotifEntity.getParameters()));
      }
      webNotifDAO.deleteAll(notifEntities);
    }
    return removed;
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

      // FIXME: Start:: Delete when all Web notifs migrated to use Vue based templates
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

      // FIXME: Start:: Delete when all Web notifs migrated to use Vue based templates
      NotificationInfo notification = get(notificationId);
      Map<String, String> ownerParameters = notification.getOwnerParameter();
      ownerParameters.put(NotificationMessageUtils.READ_PORPERTY.getKey(), String.valueOf(webUsersEntity.isRead()));
      ownerParameters.put(NotificationMessageUtils.SHOW_POPOVER_PROPERTY.getKey(), String.valueOf(webUsersEntity.isShowPopover()));
      updateNotificationParameters(webUsersEntity.getNotification(), ownerParameters, false);
      // FIXME: End
    }
  }

  @Override
  @ExoTransactional
  public void markAllRead(String userId) {
    webUsersDAO.markAllRead(userId);
    userSettingService.saveLastReadDate(userId, System.currentTimeMillis());
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
    }
  }

  @Override
  public void resetNumberOnBadge(List<String> plugins, String username) {
    List<WebUsersEntity> notifsWithBadge = webUsersDAO.findNotifsWithBadgeByPlugins(plugins, username);
    if (CollectionUtils.isNotEmpty(notifsWithBadge)) {
      notifsWithBadge.forEach(n -> n.setResetNumberOnBadge(true));
      webUsersDAO.updateAll(notifsWithBadge);
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
    // FIXME: Start:: Delete when all Web notifs migrated to use Vue based templates
    if (ownerParameters != null && ownerParameters.containsKey(NotificationMessageUtils.READ_PORPERTY.getKey())) {
      webUsersEntity.setRead(Boolean.parseBoolean(ownerParameters.get(NotificationMessageUtils.READ_PORPERTY.getKey()).toLowerCase()));
    } else {
      webUsersEntity.setRead(notification.isRead());
    }
    if (ownerParameters != null && ownerParameters.containsKey(NotificationMessageUtils.SHOW_POPOVER_PROPERTY.getKey())) {
      webUsersEntity.setShowPopover(Boolean.parseBoolean(ownerParameters.get(NotificationMessageUtils.SHOW_POPOVER_PROPERTY.getKey()).toLowerCase()));
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
  }

  private void updateNotificationParameters(WebNotifEntity webNotifEntity, Map<String, String> ownerParameters, boolean isNew) {
    Set<WebParamsEntity> parameters = webNotifEntity.getParameters();
    if (ownerParameters != null && !ownerParameters.isEmpty()) {
      for (String key : ownerParameters.keySet()) {
        String propertyName = key.replace(NTF_NAME_SPACE, "");
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
        webParamsEntity.setValue(ownerParameters.get(key));
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
    // FIXME: Start:: Delete when all Web notifs migrated to use Vue based templates
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
