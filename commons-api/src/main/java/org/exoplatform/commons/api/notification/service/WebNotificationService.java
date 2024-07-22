/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Affero General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.api.notification.service;

import java.util.List;
import java.util.Map;

import org.exoplatform.commons.api.notification.model.ArgumentLiteral;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.model.WebNotificationFilter;

public interface WebNotificationService {

  /** Define the argument parameter for popup over context */  
  public static final ArgumentLiteral<Boolean> POPUP_OVER = new ArgumentLiteral<>(Boolean.class, "popupOver");

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
   * Get the notificationInfo for the provided id
   *
   *
   * @param notificationId the notification id
   * @LevelAPI Platform
   * @since PLF 6.2
   */
  NotificationInfo getNotificationInfo(String notificationId);
  
  
  /**
   * Marks the notification to be read by the userId
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
   * Marks all notifications of the user as read for the designated notification types.
   * 
   * @param username the user name
   * @param plugins {@link List} of {@link PluginKey} ids
   */
  void markAllRead(List<String> plugins, String username);

  /**
   * Updates the notification's popover list status to be FALSE value
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
   * @return The notification bodies list matched the given filter
   * @LevelAPI Platform
   * @since PLF 4.2
   */
  List<String> get(WebNotificationFilter filter, int offset, int limit);

  /**
   * Gets the NotificationInfo list by the given filter.
   * <p>
   * The filter consist of these criteria:
   * + UserId
   * + isPopover TRUE/FALSE
   * + Read TRUE/FALSE
   * + parameter key,value
   *
   * @param filter the filter condition
   * @param offset
   * @param limit
   * @return The notification list matched the given filter
   * @LevelAPI Platform
   * @since PLF 5.1
   */
  List<NotificationInfo> getNotificationInfos(WebNotificationFilter filter, int offset, int limit);

  /**
   * Retrieve Built Message using Groovy Template engine.
   * @deprecated replaced by Frontend UI building instead of Server end message building
   */
  @Deprecated(forRemoval = true, since = "1.5.0")
  String getNotificationMessage(NotificationInfo notification, boolean isOnPopover);

  /**
   * Removes the notification by the notificationId
   * 
   * @param notificationId
   * @return Returns TRUE if removing successfully Otherwise FALSE
   * @LevelAPI Platform
   * @since PLF 4.2
   */
  boolean remove(String notificationId);
  
  /**
   * Gets the number on the badge by the specified user
   * @param username user to retrieve its badges
   * @return
   * @LevelAPI Platform
   * @since PLF 4.2
   */
  int getNumberOnBadge(String username);

  /**
   * @param username user to retrieve its badges
   * @return {@link Map} of Badges per plugin
   */
  Map<String, Integer> countUnreadByPlugin(String username);

  /**
   * @param userId
   * @LevelAPI Platform
   * @since PLF 4.2
   */
  void resetNumberOnBadge(String userId);

  /**
   * Reset badge on notifications of the user for the designated notification types.
   * 
   * @param username the user name
   * @param plugins {@link List} of {@link PluginKey} ids
   */
  void resetNumberOnBadge(List<String> plugins, String username);

  /**
   * Gets the notification by the given conditions
   * 
   * @param pluginId
   * @param activityId
   * @param userId
   * @return {@link NotificationInfo}
   */
  NotificationInfo getUnreadNotification(String pluginId, String activityId, String userId);

}
