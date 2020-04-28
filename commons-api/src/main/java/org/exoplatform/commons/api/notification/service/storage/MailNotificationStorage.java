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

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.model.UserSetting;

import java.util.List;
import java.util.Map;


public interface MailNotificationStorage {

  /**
   * Saves information of a notification.
   * 
   * @param notification The notification to be saved.
   * @throws Exception
   */
  void save(NotificationInfo notification) throws Exception;
  
  /**
   * Gets information of all notifications of a user.
   * 
   * @param userSetting The notification settings of the user.
   * @return Information of notifications.
   */
  Map<PluginKey, List<NotificationInfo>> getByUser(NotificationContext context, UserSetting userSetting);

  /**
   * Removes all messages after they have been sent.
   *
   * @param context The notification context to determine the type of digest.
   * @throws Exception
   */
  void removeMessageAfterSent(NotificationContext context) throws Exception;

  /**
   * Removes all digest messages. This is used for migration purpose
   *
   * @throws Exception
   */
  default void deleteAllDigests() throws Exception {
    throw new UnsupportedOperationException();
  }

}
