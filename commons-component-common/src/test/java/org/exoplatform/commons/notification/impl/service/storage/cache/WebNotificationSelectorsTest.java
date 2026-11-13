/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
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
package org.exoplatform.commons.notification.impl.service.storage.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.WebNotificationFilter;
import org.exoplatform.commons.notification.impl.jpa.web.JPAWebNotificationStorage;
import org.exoplatform.commons.notification.impl.service.storage.cache.model.ListWebNotificationsKey;
import org.exoplatform.commons.notification.impl.service.storage.cache.model.WebNotifInfoCacheKey;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.CachedObjectSelector;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.ExoCacheConfigPlugin;
import org.exoplatform.services.cache.ObjectCacheInfo;
import org.exoplatform.services.cache.concurrent.ConcurrentFIFOExoCache;

class WebNotificationSelectorsTest {

  @Test
  void theNotificationsSentToTheUserAreSelected() {
    WebNotificationsOfUserSelector selector = new WebNotificationsOfUserSelector("john");

    assertTrue(selector.select(WebNotifInfoCacheKey.key("1"), info(NotificationInfo.instance().to("john"))));
    assertFalse(selector.select(WebNotifInfoCacheKey.key("2"), info(NotificationInfo.instance().to("mary"))));
    assertFalse(selector.select(WebNotifInfoCacheKey.key("3"), info(null)));
  }

  @Test
  void theNotificationListsOfTheUserAreSelected() {
    WebNotificationListsOfUserSelector selector = new WebNotificationListsOfUserSelector("john");

    assertTrue(selector.select(new ListWebNotificationsKey(new WebNotificationFilter("john", true), 0, 10), null));
    assertFalse(selector.select(new ListWebNotificationsKey(new WebNotificationFilter("mary", true), 0, 10), null));
  }

  @Test
  void theSelectionsTravelAsTheSameSelections() throws Exception {
    WebNotificationsOfUserSelector notifications = new WebNotificationsOfUserSelector("john");
    WebNotificationListsOfUserSelector lists = new WebNotificationListsOfUserSelector("john");

    Object receivedNotifications = overTheWire(notifications);
    Object receivedLists = overTheWire(lists);

    assertEquals(notifications, receivedNotifications);
    assertEquals(notifications.hashCode(), receivedNotifications.hashCode());
    assertEquals(lists, receivedLists);
    assertEquals(lists.hashCode(), receivedLists.hashCode());
    assertFalse(notifications.equals(new WebNotificationsOfUserSelector("mary")));
    assertFalse(notifications.equals(lists));
  }

  @Test
  void theStorageSelectsTheEntriesOfTheUserWithTheSelectionsThatTravel() {
    Map<String, SelectionRecordingCache> caches = new HashMap<>();
    CachedWebNotificationStorage storage = new CachedWebNotificationStorage(mock(JPAWebNotificationStorage.class),
                                                                            cacheService(caches));

    storage.markAllRead("john");

    assertEquals(List.of(new WebNotificationsOfUserSelector("john")),
                 caches.get("commons.WebNotificationCache").selections);
    assertEquals(List.of(new WebNotificationListsOfUserSelector("john")),
                 caches.get("commons.WebNotificationsCache").selections);
  }

  private static CacheService cacheService(Map<String, SelectionRecordingCache> caches) {
    return new CacheService() {
      @Override
      public void addExoCacheConfig(ExoCacheConfigPlugin plugin) {
        // Not used
      }

      @SuppressWarnings("unchecked")
      @Override
      public <K extends Serializable, V> ExoCache<K, V> getCacheInstance(String region) {
        return (ExoCache<K, V>) caches.computeIfAbsent(region, SelectionRecordingCache::new);
      }

      @Override
      public java.util.Collection<ExoCache<? extends Serializable, ?>> getAllCacheInstances() {
        return new ArrayList<>(caches.values());
      }
    };
  }

  /**
   * A cache of this node that records the selections other nodes would receive
   */
  private static class SelectionRecordingCache extends ConcurrentFIFOExoCache<Serializable, Object> {

    private final List<CachedObjectSelector<?, ?>> selections = new ArrayList<>();

    SelectionRecordingCache(String name) {
      setName(name);
    }

    @Override
    public void select(CachedObjectSelector<? super Serializable, ? super Object> selector) throws Exception {
      selections.add(selector);
      super.select(selector);
    }
  }

  private static ObjectCacheInfo<NotificationInfo> info(NotificationInfo notification) {
    return new ObjectCacheInfo<>() {
      @Override
      public long getExpireTime() {
        return -1;
      }

      @Override
      public NotificationInfo get() {
        return notification;
      }
    };
  }

  private static Object overTheWire(Object value) throws IOException, ClassNotFoundException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
      out.writeObject(value);
    }
    try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
      return in.readObject();
    }
  }
}
