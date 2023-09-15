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

package org.exoplatform.commons.notification.impl.service.storage.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.WebNotificationFilter;
import org.exoplatform.commons.api.notification.service.storage.WebNotificationStorage;
import org.exoplatform.commons.cache.future.FutureExoCache;
import org.exoplatform.commons.cache.future.Loader;
import org.exoplatform.commons.notification.impl.jpa.web.JPAWebNotificationStorage;
import org.exoplatform.commons.notification.impl.service.storage.cache.model.*;
import org.exoplatform.services.cache.*;

public class CachedWebNotificationStorage implements WebNotificationStorage {
  //
  private final static String WEB_NOTIFICATION_CACHING_NAME = "commons.WebNotificationCache";
  private final static String LIST_WEB_NOTIFICATION_CACHING_NAME = "commons.WebNotificationsCache";
  private final static String WEB_NOTIFICATION_COUNT_CACHING_NAME = "commons.WebNotificationCountCache";
  private final static String WEB_NOTIFICATION_BY_PLUGIN_COUNT_CACHING_NAME = "commons.WebNotificationCountByPluginCache";
  //
  private final ExoCache<WebNotifInfoCacheKey, WebNotifInfoData> webNotificationCache;
  private final ExoCache<WebNotifInfoCacheKey, IntegerData> webNotificationCountCache;
  private final ExoCache<WebNotifInfoCacheKey, Map<String, Integer>> webNotificationCountByPluginCache;
  private final ExoCache<ListWebNotificationsKey, ListWebNotificationsData> webNotificationsCache;
  //
  private FutureExoCache<WebNotifInfoCacheKey, WebNotifInfoData, ServiceContext<WebNotifInfoData>> futureWebNotificationCache;
  private FutureExoCache<ListWebNotificationsKey, ListWebNotificationsData, ServiceContext<ListWebNotificationsData>> futureWebNotificationsCache;
  private FutureExoCache<WebNotifInfoCacheKey, IntegerData, ServiceContext<IntegerData>> futureWebNotificationCountCache;
  private FutureExoCache<WebNotifInfoCacheKey, Map<String, Integer>, Object> futureWebNotificationCountByPluginCache;

  private WebNotificationStorage storage;

  public CachedWebNotificationStorage(JPAWebNotificationStorage storage, CacheService cacheService) {
    this.storage = storage;
    webNotificationCache = cacheService.getCacheInstance(WEB_NOTIFICATION_CACHING_NAME);
    webNotificationsCache = cacheService.getCacheInstance(LIST_WEB_NOTIFICATION_CACHING_NAME);
    webNotificationCountCache = cacheService.getCacheInstance(WEB_NOTIFICATION_COUNT_CACHING_NAME);
    webNotificationCountByPluginCache = cacheService.getCacheInstance(WEB_NOTIFICATION_BY_PLUGIN_COUNT_CACHING_NAME);
    //
    futureWebNotificationCache = createFutureCache(webNotificationCache);
    futureWebNotificationsCache = createFutureCache(webNotificationsCache);
    futureWebNotificationCountCache = createFutureCache(webNotificationCountCache);
    futureWebNotificationCountByPluginCache = new FutureExoCache<>(new Loader<WebNotifInfoCacheKey, Map<String, Integer>, Object>() {
      @Override
      public Map<String, Integer> retrieve(Object context,
                                           WebNotifInfoCacheKey key) throws Exception {
        return storage.getNumberOnBadgeByPlugin(key.getId());
      }
    }, webNotificationCountByPluginCache);
  }

  @Override
  public void save(NotificationInfo notification) {
    //check the notification is existing or not
    //calling update or create new. 
    if (notification.isUpdate()) {
      storage.update(notification, true);
    } else {
      storage.save(notification);
    }
    webNotificationCache.put(WebNotifInfoCacheKey.key(notification.getId()), new WebNotifInfoData(notification));
    clearWebNotificationCountCache(notification.getTo());
    clearUserWebNotificationList(notification.getTo());
  }

  @Override
  public void update(NotificationInfo notification, boolean moveTop) {
    notification.setUpdate(true);
    notification.setResetOnBadge(!moveTop);

    storage.update(notification, moveTop);
    //
    WebNotifInfoCacheKey key = WebNotifInfoCacheKey.key(notification.getId());
    webNotificationCache.remove(key);
    clearWebNotificationCountCache(notification.getTo());
    clearUserWebNotificationList(notification.getTo());
  }

  @Override
  public void markRead(String notificationId) {
    storage.markRead(notificationId);
    //
    updateRead(notificationId, true);
  }

  @Override
  public void markAllRead(String userId) {
    storage.markAllRead(userId);
    updateCacheByUser(userId, true);
  }

  @Override
  public void hidePopover(String notificationId) {
    storage.hidePopover(notificationId);

    // update data showPopover
    WebNotifInfoCacheKey key = WebNotifInfoCacheKey.key(notificationId);
    WebNotifInfoData infoData = webNotificationCache.get(key);
    if (infoData != null) {
      infoData.updateShowPopover(false);
      webNotificationCache.put(key, infoData);

      clearWebNotificationCountCache(infoData.getTo());
      clearUserWebNotificationList(infoData.getTo());
    }
  }

  @Override
  public List<NotificationInfo> get(WebNotificationFilter filter, int offset, int limit) {
    final ListWebNotificationsKey key = new ListWebNotificationsKey(filter,
                                                                    offset,
                                                                    limit);
      //
    ListWebNotificationsData keys = futureWebNotificationsCache.get(
        new ServiceContext<ListWebNotificationsData>() {
          public ListWebNotificationsData execute() {
            List<NotificationInfo> got = storage.get(filter, offset, limit);
            return buildWebNotifDataIds(key, got);
          }
        }, key);
      //
    return buildNotifications(keys);
  }

  @Override
  public boolean remove(String notificationId) {
    NotificationInfo notification = get(notificationId);
    if (notification == null) {
      return false;
    }
    //
    storage.remove(notificationId);
    //
    clearWebNotificationCache(notificationId);
    //clear badge number in for notification's TO user.
    clearWebNotificationCountCache(notification.getTo());

    // Clear cache notifications Lists for user
    clearUserWebNotificationList(notification.getTo());
    return true;
  }

  @Override
  public NotificationInfo get(final String notificationId) {
    WebNotifInfoCacheKey key = WebNotifInfoCacheKey.key(notificationId);
    WebNotifInfoData notificationInfo = futureWebNotificationCache.get(
        new ServiceContext<WebNotifInfoData>() {
          public WebNotifInfoData execute() {
            try {
              NotificationInfo got = storage.get(notificationId);
              if (got != null) {
                return new WebNotifInfoData(got);
              }
              return null;
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          }
        }, key);
    //
    if(notificationInfo == null) {
      return null;
    }
    return notificationInfo.build();
  }

  public NotificationInfo getUnreadNotification(String pluginId, String activityId, String owner) {
    return storage.getUnreadNotification(pluginId, activityId, owner);
  }

  @Override
  public boolean remove(String userId, long seconds) {
    clearWebNotificationCountCache(userId);
    updateCacheByUser(userId, false);
    //
    return storage.remove(userId, seconds);
  }

  @Override
  public boolean remove(long seconds) {
    boolean removed = false;
    try {
      removed = storage.remove(seconds);
      return removed;
    } finally {
      if(removed) {
        webNotificationCache.clearCache();
        webNotificationCountCache.clearCache();
        webNotificationCountByPluginCache.clearCache();
      }
    }
  }

  @Override
  public int getNumberOnBadge(final String userId) {
    if (StringUtils.isNotBlank(userId)) {
      WebNotifInfoCacheKey key = WebNotifInfoCacheKey.key(userId);
      IntegerData numberOfMessageData = futureWebNotificationCountCache.get(
              new ServiceContext<IntegerData>() {
                public IntegerData execute() {
                  try {
                    int number = storage.getNumberOnBadge(userId);
                    return new IntegerData(number);
                  } catch (Exception e) {
                    throw new RuntimeException(e);
                  }
                }
              }, key);
      return numberOfMessageData.build().intValue();
    } else {
      return 0;
    }
  }

  @Override
  public Map<String, Integer> getNumberOnBadgeByPlugin(String userId) {
    return futureWebNotificationCountByPluginCache.get(null, WebNotifInfoCacheKey.key(userId));
  }

  @Override
  public void resetNumberOnBadge(String userId) {
    storage.resetNumberOnBadge(userId);
    //
    clearWebNotificationCountCache(userId);
  }

  public void setStorage(WebNotificationStorage storage) {
    this.storage = storage;
  }

  public void updateAllRead(String userId) {
    updateCacheByUser(userId, true);
  }

  private void updateRead(String notificationId, boolean isRead) {
    WebNotifInfoCacheKey key = WebNotifInfoCacheKey.key(notificationId);
    WebNotifInfoData infoData = webNotificationCache.get(key);
    if (infoData != null) {
      infoData.updateRead(isRead);
      webNotificationCache.put(key, infoData);
    }
  }

  private void updateCacheByUser(final String userId, final boolean isUpdateRead) {
    if (!isUpdateRead) {
      clearUserWebNotificationList(userId);
      clearUserWebNotifications(userId);
    } else {
      // In case isUpdateRead, we will just update the cached WebNotification entries of user
      updateReadForUserWebNotifications(userId);
    }
  }

  private void updateReadForUserWebNotifications(final String userId) {
    try {
      webNotificationCache.select(new CachedObjectSelector<WebNotifInfoCacheKey, WebNotifInfoData>() {
        @Override
        public boolean select(WebNotifInfoCacheKey key, ObjectCacheInfo<? extends WebNotifInfoData> ocinfo) {
          return ocinfo.get() != null && userId.equals(ocinfo.get().getTo());
        }

        @Override
        public void onSelect(ExoCache<? extends WebNotifInfoCacheKey, ? extends WebNotifInfoData> cache,
                             WebNotifInfoCacheKey key,
                             ObjectCacheInfo<? extends WebNotifInfoData> ocinfo) throws Exception {
          ocinfo.get().updateRead(true);
        }
      });
    } catch (Exception e) {
      throw new IllegalStateException("Can't update Notification Cache entries for user " + userId, e);
    }
  }

  private void clearUserWebNotifications(final String userId) {
    try {
      webNotificationCache.select(new CachedObjectSelector<WebNotifInfoCacheKey, WebNotifInfoData>() {
        @Override
        public boolean select(WebNotifInfoCacheKey key, ObjectCacheInfo<? extends WebNotifInfoData> ocinfo) {
          return ocinfo.get() != null && userId.equals(ocinfo.get().getTo());
        }
        
        @Override
        public void onSelect(ExoCache<? extends WebNotifInfoCacheKey, ? extends WebNotifInfoData> cache,
                             WebNotifInfoCacheKey key,
                             ObjectCacheInfo<? extends WebNotifInfoData> ocinfo) throws Exception {
          cache.remove(key);
        }
      });
    } catch (Exception e) {
      throw new IllegalStateException("Can't update Notifications List Cache entries for user " + userId, e);
    }
  }

  private void clearUserWebNotificationList(final String userId) {
    try {
      webNotificationsCache.select(new CachedObjectSelector<ListWebNotificationsKey, ListWebNotificationsData>() {
        @Override
        public boolean select(ListWebNotificationsKey key, ObjectCacheInfo<? extends ListWebNotificationsData> ocinfo) {
          return userId.equals(key.getUserId());
        }

        @Override
        public void onSelect(ExoCache<? extends ListWebNotificationsKey, ? extends ListWebNotificationsData> cache,
                             ListWebNotificationsKey key,
                             ObjectCacheInfo<? extends ListWebNotificationsData> ocinfo) throws Exception {
          cache.remove(key);
        }
      });
    } catch (Exception e) {
      throw new IllegalStateException("Can't update Notification Cache entries for user " + userId, e);
    }
  }

  private ListWebNotificationsData getWebNotificationsData(ListWebNotificationsKey key) {
    ListWebNotificationsData data = this.webNotificationsCache.get(key);
    if (data == null) {
      data = new ListWebNotificationsData(key);
      this.webNotificationsCache.put(key, data);
    }
    return data;
  }

  private ListWebNotificationsData buildWebNotifDataIds(ListWebNotificationsKey key, List<NotificationInfo> notifications) {
    ListWebNotificationsData data = getWebNotificationsData(key);
    //
    for (int i = 0, len = notifications.size(); i < len; i++) {
      NotificationInfo notif = notifications.get(i);
      // handle the activity is NULL
      if (notif == null) {
        continue;
      }
      // Update single Nofification cache when the notification doesn't exit there yet
      WebNotifInfoCacheKey webNotifKey = WebNotifInfoCacheKey.key(notif.getId());
      if (webNotificationCache.get(webNotifKey) == null) {
        webNotificationCache.put(webNotifKey, new WebNotifInfoData(notif));
      }
      // Insert notification at the end of list
      if (!data.contains(notif.getId())) {
        data.insertLast(notif.getId());
      }
    }
    return data;
  }
  
  private List<NotificationInfo> buildNotifications(ListWebNotificationsData data) {
    List<NotificationInfo> notifications = new ArrayList<>();
    for (String id : data.getList()) {
      NotificationInfo a = get(id);
      if (a != null) {
        notifications.add(a);
      }
    }
    return notifications;
  }

  /**
   * Clear the notification badge number of the specified user.
   * @param userId
   */
  private void clearWebNotificationCountCache(String userId) {
    WebNotifInfoCacheKey key = WebNotifInfoCacheKey.key(userId);
    webNotificationCountCache.remove(key);
    webNotificationCountByPluginCache.remove(key);
  }

  /**
   * Clear the notification from the cache.
   * @param notificationId
   */
  public void clearWebNotificationCache(String notificationId) {
    WebNotifInfoCacheKey key = WebNotifInfoCacheKey.key(notificationId);
    webNotificationCache.remove(key);
  }

  private <K extends CacheKey, V extends Serializable> FutureExoCache<K, V, ServiceContext<V>> createFutureCache(ExoCache<K, V> cache) {
    return new FutureExoCache<>(new CacheLoader<>(), cache);
  }
}
