/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.notification.impl.command;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.command.NotificationCommand;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;

public class NotificationCommandImpl implements NotificationCommand {

  private final BaseNotificationPlugin plugin;
  
  public NotificationCommandImpl(BaseNotificationPlugin plugin) {
    this.plugin = plugin;
  }
  
  @Override
  public BaseNotificationPlugin getPlugin() {
    return this.plugin;
  }

  @Override
  public PluginKey getNotificationKey() {
    return this.plugin.getKey();
  }

  @Override
  public NotificationInfo processNotification(NotificationContext ctx) {
    return plugin.buildNotification(ctx);
  }

  @Override
  public String toString() {
    return "NotificationCommand[" + plugin.getKey().getId() + "]";
  }

}
