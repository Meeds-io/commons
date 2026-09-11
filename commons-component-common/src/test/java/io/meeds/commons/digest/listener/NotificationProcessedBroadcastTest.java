/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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
package io.meeds.commons.digest.listener;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.service.storage.NotificationService;
import org.exoplatform.jpa.BaseTest;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.listener.ListenerService;

/**
 * Checks the insertion point of the digest in the dispatcher: every processed
 * notification is announced by one event, whatever the channels do with it,
 * and a failing listener never makes the notification itself fail.
 */
public class NotificationProcessedBroadcastTest extends BaseTest {

  private NotificationService          notificationService;

  private ListenerService              listenerService;

  private final List<NotificationInfo> receivedNotifications = new ArrayList<>();

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    notificationService = getService(NotificationService.class);
    listenerService = getService(ListenerService.class);
    receivedNotifications.clear();
  }

  public void testEveryProcessedNotificationIsAnnounced() throws Exception {
    listenerService.addListener(NotificationService.NOTIFICATION_PROCESSED_EVENT, collector());

    NotificationInfo notification = notification();
    notificationService.process(notification);

    assertEquals(1, receivedNotifications.size());
    assertSame(notification, receivedNotifications.get(0));
  }

  public void testAFailingListenerNeverFailsTheNotification() throws Exception {
    listenerService.addListener(NotificationService.NOTIFICATION_PROCESSED_EVENT,
                                new Listener<Object, NotificationInfo>() {
                                  @Override
                                  public void onEvent(Event<Object, NotificationInfo> event) {
                                    throw new IllegalStateException("The digest side is broken");
                                  }
                                });
    listenerService.addListener(NotificationService.NOTIFICATION_PROCESSED_EVENT, collector());

    // Must complete without any exception: the digest, or whoever listens,
    // never sits on the failure path of the notification
    notificationService.process(notification());

    assertEquals(1, receivedNotifications.size());
  }

  private Listener<Object, NotificationInfo> collector() {
    return new Listener<Object, NotificationInfo>() {
      @Override
      public void onEvent(Event<Object, NotificationInfo> event) {
        receivedNotifications.add(event.getData());
      }
    };
  }

  private NotificationInfo notification() {
    // A plugin unknown to every channel: the channel loop is a no-op, the test
    // only exercises the announcement
    return NotificationInfo.instance().key(new PluginKey("DigestBroadcastTestPlugin")).to("mary");
  }

}
