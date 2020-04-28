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
package org.exoplatform.commons.api.notification.lifecycle;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.model.NotificationInfo;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Dec 12, 2014  
 */
public abstract class AbstractNotificationLifecycle {
  private AbstractChannel channel;
  
  /**
   * Gets the channel
   * @return
   */
  public AbstractChannel getChannel() {
    return channel;
  }

  /**
   * Sets the channel to the lifecycle
   * @param channel
   */
  public void setChannel(AbstractChannel channel) {
    this.channel = channel;
  }

  /**
   * Process the notification for multi-users
   * @param ctx
   * @param userIds
   */
  public abstract void process(NotificationContext ctx, String...userIds);
  
  /**
   * Process the notification for the user
   * @param ctx
   * @param userId
   */
  public abstract void process(NotificationContext ctx, String userId);
  
  /**
   * Storage the notification into the db
   * @param notifInfo
   */
  public void store(NotificationInfo notifInfo) {}
  
  /**
   * Update an existing notification
   * @param notifInfo
   */
  public void update(NotificationInfo notifInfo) {}
  
  /**
   * Sends the message by Mail, UI or any the configured channel
   * @param ctx
   */
  public void send(NotificationContext ctx) {}
}
