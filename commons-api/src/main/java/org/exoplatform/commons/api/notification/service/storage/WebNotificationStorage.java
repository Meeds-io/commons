/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.commons.api.notification.service.storage;

import java.util.List;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.WebNotificationFilter;

public interface WebNotificationStorage {
  
  /**
   * Creates the new notification message to the specified user.
   * The userId gets from the notification#getTo().
   * 
   * @param notification the notification
   * @LevelAPI Platform
   * @since PLF 4.2
   */
  void save(NotificationInfo notification);
  
  /**
   * Update an existing notification message.
   * 
   * @param notification the notification
   * @param moveTop After updating, MUST move the notification to top of list
   * @LevelAPI Platform
   * @since PLF 4.2
   */
  void update(NotificationInfo notification, boolean moveTop);

  /**
   * Marks the notification to be read by the userId
   * 
   * @param notificationId the Notification Id
   * @LevelAPI Platform
   * @since PLF 4.2
   */
  void markRead(String notificationId);

  /**
   * Marks all notifications what belong to the user to be read.
   * 
   * 
   * @param userId the userId
   * @LevelAPI Platform
   * @since PLF 4.2
   */
  void markAllRead(String userId);

  /**
   * Updates the notification's popover status to be FALSE value
   * However it's still showing on View All page.
   * 
   * @param notificationId the Notification Id
   * @LevelAPI Platform
   * @since PLF 4.2
   */
  void hidePopover(String notificationId);

  /**
   * Gets the notification list by the given filter.
   * 
   * The filter consist of these criteria:
   * + UserId
   * + isPopover TRUE/FALSE
   * + Read TRUE/FALSE
   * 
   * @param filter the filter condition
   * @param offset
   * @param limit
   * @return The notification list matched the given filter
   * @LevelAPI Platform
   * @since PLF 4.2
   */
  List<NotificationInfo> get(WebNotificationFilter filter, int offset, int limit);

  /**
   * Gets the notification by the Id
   * 
   * @param notificationId
   * @return the Notification matched the given Id
   * @LevelAPI Platform
   * @since PLF 4.2
   */
  NotificationInfo get(String notificationId);
  
  /**
   * Removes the notification by given Id
   * 
   * @param notificationId the Id of the notification
   * @LevelAPI Platform
   * @since PLF 4.2
   */
  boolean remove(String notificationId);

  /**
   * Remove the NotificationInfo live after X days
   * 
   * @param userId 
   * @param seconds 
   * @return Returns TRUE if removing successfully Otherwise FALSE
   * @LevelAPI Platform
   * @since PLF 4.2
   */
  boolean remove(String userId, long seconds);
  
  /**
   * Gets the notification by the given conditions
   * @param pluginId
   * @param activityId
   * @param owner
   * @return
   * @LevelAPI Platform
   * @since PLF 4.2
   */
  NotificationInfo getUnreadNotification(String pluginId, String activityId, String owner);

  /**
   * Gets the number on the badge by the specified user 
   * 
   * @param userId the userId
   * @return
   * @LevelAPI Platform
   * @since PLF 4.2
   */
  int getNumberOnBadge(String userId);
  
  
  /**
   * Reset the number on badge of the specified user
   *  
   * @param userId the userId
   * @LevelAPI Platform
   * @since PLF 4.2
   */
  void resetNumberOnBadge(String userId);

  /**
   * Remove the NotificationInfo live after X days
   * 
   * @param seconds
   * @return Returns TRUE if removing successfully Otherwise FALSE
   * @LevelAPI Platform
   * @since PLF 4.2
   */
  boolean remove(long seconds);
}
