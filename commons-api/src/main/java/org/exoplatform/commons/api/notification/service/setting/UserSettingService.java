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
package org.exoplatform.commons.api.notification.service.setting;

import java.util.List;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.services.organization.User;

public interface UserSettingService {

  /**
   * Saves the notification settings of a user.
   * 
   * @param notificationSetting The notification settings.
   */
  void save(UserSetting notificationSetting);

  /**
   * Gets the notification settings of a user by his remote Id.
   * 
   * @param userId The user's remote Id.
   * @return The notification settings.
   */
  UserSetting get(String userId);

  /**
   * Gets the list of user settings which has at least the plug-in to be configured by weekly or daily.
   * the weekly or daily condition input by NotificationContext.
   * 
   * @param context the weekly or daily condition
   * @param offset The start point from which the user settings are got.
   * @param limit The limited number of user settings.
   * @return The list of user settings.
   */
  List<UserSetting> getDigestSettingForAllUser(NotificationContext context, int offset, int limit);
  
  /**
   * Gets all settings of users registering for default daily notifications.
   * @param offset The start point from which the user settings are got.
   * @param limit The limited number of user settings.
   * @return The list of user settings.
   */
  List<UserSetting> getDigestDefaultSettingForAllUser(int offset, int limit);

  /**
   * Adds the default settings to a user's node.
   * 
   * @param userId The user's remote Id.
   */
  void initDefaultSettings(String userId);
  
  /**
   * Adds the default settings to a list of users.
   * 
   * @param users The list of users.
   */
  void initDefaultSettings(User[] users);

  /**
   * Returns the default settings of an user
   * @return {@link UserSetting}
   */
  UserSetting getDefaultSettings();

  /**
   * Stores the read time point when user clicks mark all read his/her messages.
   * This value will be using to decide the read status of message.
   *  + If less than the read time point, Read = TRUE
   *  + Else depends on the the status of the message
   * @param userId The user's id
   * @param time The time milliseconds 
   * @LevelAPI Platform
   * @since PLF 4.2
   */
  void saveLastReadDate(String userId, Long time);

  /**
   * Enables user settings
   * 
   * @param username user id
   * @param enabled true/false
   */
  void setUserEnabled(String username, boolean enabled);

  /**
   * Reset computed default setting for users
   */
  default void clearDefaultSetting() {}

}
