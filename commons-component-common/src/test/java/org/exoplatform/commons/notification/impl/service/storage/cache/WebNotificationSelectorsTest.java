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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.TestCase;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.WebNotificationFilter;
import org.exoplatform.commons.notification.impl.service.storage.cache.model.ListWebNotificationsKey;
import org.exoplatform.commons.notification.impl.service.storage.cache.model.WebNotifInfoCacheKey;
import org.exoplatform.services.cache.ObjectCacheInfo;

/**
 * The selections of the web notifications of a user, which travel to the other nodes of a cluster.
 */
public class WebNotificationSelectorsTest extends TestCase {

  public void testTheNotificationsSentToTheUserAreSelected() {
    WebNotificationsOfUserSelector selector = new WebNotificationsOfUserSelector("john");

    assertTrue(selector.select(WebNotifInfoCacheKey.key("1"), info(NotificationInfo.instance().to("john"))));
    assertFalse(selector.select(WebNotifInfoCacheKey.key("2"), info(NotificationInfo.instance().to("mary"))));
    assertFalse(selector.select(WebNotifInfoCacheKey.key("3"), info(null)));
  }

  public void testTheNotificationListsOfTheUserAreSelected() {
    WebNotificationListsOfUserSelector selector = new WebNotificationListsOfUserSelector("john");

    assertTrue(selector.select(new ListWebNotificationsKey(new WebNotificationFilter("john", true), 0, 10), null));
    assertFalse(selector.select(new ListWebNotificationsKey(new WebNotificationFilter("mary", true), 0, 10), null));
  }

  public void testTheSelectionsTravelAsTheSameSelections() throws Exception {
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
