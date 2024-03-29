package org.exoplatform.commons.notification.storage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.WebNotificationFilter;
import org.exoplatform.commons.api.notification.service.storage.WebNotificationStorage;
import org.exoplatform.commons.notification.BaseNotificationTestCase;
import org.exoplatform.commons.notification.impl.service.storage.cache.CachedWebNotificationStorage;
import org.exoplatform.commons.notification.impl.service.storage.cache.model.*;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;

public class CachedWebNotificationStorageTest extends BaseNotificationTestCase {
  protected WebNotificationStorage cachedStorage;
  private final static String WEB_NOTIFICATION_CACHING_NAME = "commons.WebNotificationCache";
  private final static String LIST_WEB_NOTIFICATION_CACHING_NAME = "commons.WebNotificationsCache";
  private final static String WEB_NOTIFICATION_COUNT_CACHING_NAME = "commons.WebNotificationsCache";
  private  CacheService cacheService;
  private ExoCache<ListWebNotificationsKey, ListWebNotificationsData> exoWebNotificationsCache;
  private ExoCache<WebNotifInfoCacheKey, NotificationInfo> exoWebNotificationCache;
  private ExoCache<WebNotifInfoCacheKey, IntegerData> exoWebNotificationCountCache;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    cachedStorage = getService(WebNotificationStorage.class);
    cacheService = getService(CacheService.class);
    cacheService.getCacheInstance(WEB_NOTIFICATION_CACHING_NAME);
    exoWebNotificationCache = cacheService.getCacheInstance(WEB_NOTIFICATION_CACHING_NAME);
    exoWebNotificationsCache = cacheService.getCacheInstance(LIST_WEB_NOTIFICATION_CACHING_NAME);
    exoWebNotificationCountCache = cacheService.getCacheInstance(WEB_NOTIFICATION_COUNT_CACHING_NAME);

    assertTrue(cachedStorage instanceof CachedWebNotificationStorage);
  }
  
  @Override
  public void tearDown() throws Exception {
    exoWebNotificationCache.clearCache();
    exoWebNotificationsCache.clearCache();
    exoWebNotificationCountCache.clearCache();
    
    super.tearDown();
  }
  
  public void testSave() throws Exception {
    String userId = "demo3";
    userIds.add(userId);
    NotificationInfo info = makeWebNotificationInfo(userId);
    cachedStorage.save(info);
    //
    NotificationInfo notifInfo = cachedStorage.get(info.getId());
    assertNotNull(notifInfo);
    assertEquals(1, cachedStorage.get(new WebNotificationFilter(userId, false), 0, 10).size());
  }

  public void testGetNotifications() {
    String userId = "demo6";
    userIds.add(userId);
    //
    List<NotificationInfo> onPopoverInfos = cachedStorage.get(new WebNotificationFilter(userId, true), 0 , 10);
    onPopoverInfos = cachedStorage.get(new WebNotificationFilter(userId, true), 0 , 10);
    assertEquals(0, onPopoverInfos.size());
    for (int i = 0; i < 2; i++) {
      try {
        cachedStorage.save(makeWebNotificationInfo(userId));
      } catch (Exception e) {
        fail(e);
      }
    }
    onPopoverInfos = cachedStorage.get(new WebNotificationFilter(userId, true), 0 , 10);
    onPopoverInfos = cachedStorage.get(new WebNotificationFilter(userId, true), 0 , 10);
    //
    assertEquals(2, onPopoverInfos.size());

    for (int i = 0; i < 20; i++) {
      try {
        cachedStorage.save(makeWebNotificationInfo(userId));
      } catch (Exception e) {
        fail(e);
      }
    }
    end();
    begin();

    onPopoverInfos = cachedStorage.get(new WebNotificationFilter(userId, true), 5 , 10);
    onPopoverInfos = cachedStorage.get(new WebNotificationFilter(userId, true), 5 , 10);
    assertEquals(10, onPopoverInfos.size());

    //
    onPopoverInfos = cachedStorage.get(new WebNotificationFilter(userId, true), 5 , 15);
    onPopoverInfos = cachedStorage.get(new WebNotificationFilter(userId, true), 5 , 15);
    assertEquals(15, onPopoverInfos.size());

    //
    onPopoverInfos = cachedStorage.get(new WebNotificationFilter(userId, true), 0 , 30);
    onPopoverInfos = cachedStorage.get(new WebNotificationFilter(userId, true), 0 , 30);
    assertEquals(22, onPopoverInfos.size());
  }
  
  public void testRemove() throws Exception {
    String userId = "demo7";
    userIds.add(userId);
    NotificationInfo info = makeWebNotificationInfo(userId);
    cachedStorage.save(info);
    //
    NotificationInfo notifInfo = cachedStorage.get(info.getId());
    assertEquals(1, cachedStorage.get(new WebNotificationFilter(userId, false), 0 , 10).size());
    
    NotificationInfo info1 = makeWebNotificationInfo(userId);
    cachedStorage.save(info1);
    //
    notifInfo = cachedStorage.get(info1.getId());
    assertNotNull(notifInfo);
    assertEquals(2, cachedStorage.get(new WebNotificationFilter(userId, false), 0 , 10).size());
    assertEquals(2, cachedStorage.get(new WebNotificationFilter(userId, true), 0 , 10).size());
    
    cachedStorage.remove(info1.getId());
    
    assertNull(cachedStorage.get(info1.getId()));
    assertEquals(1, cachedStorage.get(new WebNotificationFilter(userId, false), 0 , 10).size());
    assertEquals(1, cachedStorage.get(new WebNotificationFilter(userId, true), 0 , 10).size());
  }
  
  public void testRead() throws Exception {
    String userId = "demo9";
    userIds.add(userId);
    NotificationInfo info = makeWebNotificationInfo(userId);
    cachedStorage.save(info);
    //
    NotificationInfo notifInfo = cachedStorage.get(info.getId());
    assertFalse(notifInfo.isRead());
    //
    assertEquals(1, cachedStorage.countUnreadByPlugin(userId).size());
    cachedStorage.markRead(notifInfo.getId());
    assertEquals(0, cachedStorage.countUnreadByPlugin(userId).size());
    //
    notifInfo = cachedStorage.get(info.getId());
    assertTrue(notifInfo.isRead());
  }

  public void testMarkAllRead() {
    String userId = "demo8";
    userIds.add(userId);
    List<NotificationInfo> notifs = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      NotificationInfo notif = makeWebNotificationInfo(userId);
      cachedStorage.save(notif);
      notifs.add(notif);
    }
    long updateTime = System.currentTimeMillis();
    //
    List<NotificationInfo> onPopoverInfos = cachedStorage.get(new WebNotificationFilter(userId, true), 0 , 10);
    assertEquals(10, onPopoverInfos.size());
    for (NotificationInfo notifInfo : onPopoverInfos) {
      assertFalse(notifInfo.isRead());
    }
    List<NotificationInfo> viewAllInfos = cachedStorage.get(new WebNotificationFilter(userId, false), 0 , 10);
    assertEquals(10, viewAllInfos.size());
    for (NotificationInfo notifInfo : viewAllInfos) {
      assertFalse(notifInfo.isRead());
      assertTrue(notifInfo.getLastModifiedDate() <= updateTime);
    }
    //
    cachedStorage.markAllRead(userId);
    restartTransaction();
    //
    onPopoverInfos = cachedStorage.get(new WebNotificationFilter(userId, true), 0 , 10);
    assertEquals(10, onPopoverInfos.size());
    for (NotificationInfo notifInfo : onPopoverInfos) {
      assertTrue(notifInfo.isRead());
      assertTrue(notifInfo.getLastModifiedDate() <= updateTime);
    }
    viewAllInfos = cachedStorage.get(new WebNotificationFilter(userId, false), 0 , 10);
    assertEquals(10, viewAllInfos.size());
    for (NotificationInfo notifInfo : viewAllInfos) {
      assertTrue(notifInfo.isRead());
      assertTrue(notifInfo.getLastModifiedDate() <= updateTime);
    }
    //
    for (NotificationInfo info : viewAllInfos) {
      WebNotifInfoCacheKey notifKey = WebNotifInfoCacheKey.key(info.getId());
      NotificationInfo notifInfo = exoWebNotificationCache.get(notifKey);
      assertTrue(notifInfo.isRead());
      assertTrue(notifInfo.getLastModifiedDate() <= updateTime);
    }
  }

  public void testHidePopover() {
    String userId = "demo10";
    userIds.add(userId);
    NotificationInfo info = makeWebNotificationInfo(userId);
    try {
      cachedStorage.save(info);
    } catch (Exception e) {
      fail(e);
    }
    //
    NotificationInfo notifInfo = cachedStorage.get(info.getId());
    assertTrue(notifInfo.isOnPopOver());
    //
    //checks caching
    WebNotificationFilter filter = new WebNotificationFilter(userId, true);
    cachedStorage.get(filter, 0 , 10);

    ListWebNotificationsKey key = new ListWebNotificationsKey(filter, 0, 10);
    ListWebNotificationsData listData = exoWebNotificationsCache.get(key);
    assertNotNull(listData);
    assertEquals(1,listData.size());

    //
    List<NotificationInfo> infos = cachedStorage.get(new WebNotificationFilter(userId, true), 0 , 10);
    assertEquals(infos.get(0), notifInfo);
    assertTrue(infos.get(0).isOnPopOver());

    //
    cachedStorage.hidePopover(notifInfo.getId());
    restartTransaction();

    //
    notifInfo = cachedStorage.get(notifInfo.getId());
    assertFalse(notifInfo.isOnPopOver());
  }
  
  public void testUpdate() throws Exception {
    String userId = "mary9";
    userIds.add(userId);
    NotificationInfo info = makeWebNotificationInfo(userId);
    cachedStorage.save(info);
    //
    NotificationInfo createdFirstInfo = cachedStorage.get(info.getId());
    assertEquals(info.getTitle(), createdFirstInfo.getTitle());
    for (int i = 0; i < 5; i++) {
      // this sleep makes sure notifications are saved at different timestamps, so they are sorted correctly when fetching
      Thread.sleep(1);
      cachedStorage.save(makeWebNotificationInfo(userId));
    }
    end();
    begin();

    List<NotificationInfo> onPopoverInfos = cachedStorage.get(new WebNotificationFilter(userId, true), 0 , 10);
    assertEquals(6, onPopoverInfos.size());
    List<NotificationInfo> viewAllInfos = cachedStorage.get(new WebNotificationFilter(userId, false), 0 , 10);
    assertEquals(6, viewAllInfos.size());

    //
    NotificationInfo lastOnPopoverInfo = onPopoverInfos.get(onPopoverInfos.size() - 1);
    assertEquals(createdFirstInfo.getId(), lastOnPopoverInfo.getId());
    NotificationInfo lastViewAllInfos = viewAllInfos.get(viewAllInfos.size() - 1);
    assertEquals(createdFirstInfo.getId(), lastViewAllInfos.getId());
    //
    String newTitle = "The new title";
    createdFirstInfo.setTitle(newTitle);

    //
    cachedStorage.update(createdFirstInfo, true);
    end();
    begin();

    //
    createdFirstInfo = cachedStorage.get(info.getId());
    //
    assertEquals(newTitle, createdFirstInfo.getTitle());
    //
    onPopoverInfos = cachedStorage.get(new WebNotificationFilter(userId, true), 0 , 10);
    viewAllInfos = cachedStorage.get(new WebNotificationFilter(userId, false), 0 , 10);
    //
    NotificationInfo firstOnPopoverInfo = onPopoverInfos.get(0);
    assertEquals(newTitle, firstOnPopoverInfo.getTitle());
    NotificationInfo firstViewAllInfos = viewAllInfos.get(0);
    assertEquals(newTitle, firstViewAllInfos.getTitle());

    cachedStorage.resetNumberOnBadge(userId);
    //
    int num = cachedStorage.getNumberOnBadge(userId);
    assertEquals(0, num);
    //moveTop = false, mean no need to increase notification badge number
    cachedStorage.update(createdFirstInfo, false);
    num = cachedStorage.getNumberOnBadge(userId);
    assertEquals(0, num);

    cachedStorage.update(createdFirstInfo, true);
    num = cachedStorage.getNumberOnBadge(userId);
    assertEquals(1, num);
  }

  public void testRemoveByJob() throws Exception {
    // Create data for old notifications 
    /* Example:
     *  PastTime is 1/12/2014
     *  Today is 15/12/2014
     *  Create 50 notifications for:
     *   + 04/12/2014
     *   + 06/12/2014
     *   + 08/12/2014
     *   + 10/12/2014
     *   + 12/12/2014
     *  Case 1: Delay time 9 days, remove all web notification on days:
     *   + 04/12/2014
     *   + 06/12/2014
     *  Expected: remaining is 30 notifications on 3 days
     *  Case 2: Delay time 3 days, remove all web notification on days:
     *   + 08/12/2014
     *   + 10/12/2014
     *   + 12/12/2014
     *  Expected: remaining is 0 notification
    */
    int daySeconds = 86400;
    String userId = "demo4";
    Calendar cal = Calendar.getInstance();
    long t = daySeconds * 1000l;
    long current = cal.getTimeInMillis();
    for (int i = 12; i > 3; i = i - 2) {
      cal.setTimeInMillis(current - i * t);
      for (int j = 0; j < 10; j++) {
        NotificationInfo notifInfi = makeWebNotificationInfo(userId);
        NotificationInfo info = notifInfi.setDateCreated(cal);
        notifInfi.setLastModifiedDate(cal);
        //
        cachedStorage.save(info);
      }
    }
    // check data on cache
    List<NotificationInfo>  info = cachedStorage.get(new WebNotificationFilter(userId, false), 0, 60);
    assertEquals(50, info.size());
    //
    cachedStorage.remove(userId, 9 * daySeconds);
    //
    info = cachedStorage.get(new WebNotificationFilter(userId, false), 0, 60);
    assertEquals(30, info.size());
    //
    cachedStorage.remove(userId, 3 * daySeconds);
    info = cachedStorage.get(new WebNotificationFilter(userId, false), 0, 60);
    assertEquals(0, info.size());
  }
  
  public void testGetNewMessage() throws Exception  {
    String userId = "root82";
    userIds.add(userId);
    assertEquals(0, cachedStorage.getNumberOnBadge(userId));
    //
    NotificationInfo notificationInfo = makeWebNotificationInfo(userId);
    cachedStorage.save(notificationInfo);
    //
    assertEquals(1, cachedStorage.getNumberOnBadge(userId));
    cachedStorage.save(makeWebNotificationInfo(userId));
    assertEquals(2, cachedStorage.getNumberOnBadge(userId));
    for (int i = 0; i < 10; ++i) {
      cachedStorage.save(makeWebNotificationInfo(userId));
    }
    //
    List<NotificationInfo> list = cachedStorage.get(new WebNotificationFilter(userId), 0, 15);
    assertEquals(12, list.size());
    //
    assertEquals(12, cachedStorage.getNumberOnBadge(userId));
    //
    cachedStorage.resetNumberOnBadge(Collections.singletonList("testPluginFake"), userId);
    assertEquals(12, cachedStorage.getNumberOnBadge(userId));

    cachedStorage.resetNumberOnBadge(Collections.singletonList(notificationInfo.getKey().getId()), userId);
    assertEquals(0, cachedStorage.getNumberOnBadge(userId));

    NotificationInfo notif = makeWebNotificationInfo(userId);
    cachedStorage.save(notif);
    assertEquals(1, cachedStorage.getNumberOnBadge(userId));

    cachedStorage.resetNumberOnBadge(userId);
    assertEquals(0, cachedStorage.getNumberOnBadge(userId));
    NotificationInfo cachedNotif = cachedStorage.get(notif.getId());
    assertEquals(notif.getLastModifiedDate(), cachedNotif.getLastModifiedDate());
  }

}
