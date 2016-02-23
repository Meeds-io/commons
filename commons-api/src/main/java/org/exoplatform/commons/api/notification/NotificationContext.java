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
package org.exoplatform.commons.api.notification;

import java.util.List;

import org.exoplatform.commons.api.notification.channel.ChannelManager;
import org.exoplatform.commons.api.notification.command.NotificationCommand;
import org.exoplatform.commons.api.notification.command.NotificationExecutor;
import org.exoplatform.commons.api.notification.model.ArgumentLiteral;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.service.setting.PluginContainer;
import org.exoplatform.commons.api.notification.service.setting.PluginSettingService;

public interface NotificationContext extends Cloneable {
  
  /**
   * Determines the writing processing or NOT
   * @return
   */
  boolean isWritingProcess();
  
  /**
   * Sets the writing status to the context
   * 
   * @param writingStatus
   */
  void setWritingProcess(boolean writingStatus);
  
  /**
   * Append the argument literal.
   * @param argument
   * @param value
   * @return
   */
  <T> NotificationContext append(ArgumentLiteral<T> argument, Object value);
  
  /**
   * Removes the query parameter.
   * @param filter
   * @return
   */
  <T> NotificationContext remove(ArgumentLiteral<T> filter);
  
  /**
   * Clear all of filter optional
   */
  void clear();
  /**
   * Gets FilterOption which was existing.
   * @param argument
   * @return
   */
  <T> T value(ArgumentLiteral<T> argument);
  
  /**
   * Gets notification information
   * @return
   */
  NotificationInfo getNotificationInfo();
  
  /**
   * Sets notification infomation
   * @param notification
   */
  NotificationContext setNotificationInfo(NotificationInfo notification);
  
  /**
   * Sets notification information list
   * @param notifications
   */
  void setNotificationInfos(List<NotificationInfo> notifications);
  
  /**
   * Gets notification message list
   * @return
   */
  List<NotificationInfo> getNotificationInfos();
  
  /**
   * Gets the exception if have any when the done processing
   * @return
   */
  Exception getException();
  
  /**
   * Gets the exception if have any when the done processing
   * @param type
   * @return
   */
  <T> T getException(Class<T> type);
  
  /**
   * Sets the exception if have any when the done processing
   * @param t
   */
  void setException(Throwable t);
  
  /**
   * The signal lets you know the processing is successfully or not
   * @return
   */
  boolean isFailed();
  
  /**
   * Gets the NotificationExceutor service
   * @return
   */
  NotificationExecutor getNotificationExecutor();
  
  /**
   * Makes the NotificationCommand by the NotificationKey
   * 
   * @param key
   * @return
   */
  NotificationCommand makeCommand(PluginKey key);

  /**
   * Creates and returns a copy of this object
   * 
   * If class extends NotificationContextImpl and implements method clone(),
   * then must use supper.clone();
   * @see java.lang.Object#clone()
   */
  NotificationContext clone();
  
  /**
   * Gets the PluginSettingService service
   * @return
   */
  PluginSettingService getPluginSettingService();
  
  /**
   * Gets the plugin container what contains all plugins on Notification
   * @return
   */
  PluginContainer getPluginContainer();
  
  /**
   * Gets the channel manager what contains all channel on Notification
   * @return
   */
  ChannelManager getChannelManager();
}
