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
package org.exoplatform.commons.api.notification.model;

import java.io.Serializable;

import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;

import lombok.Data;

@Data
public final class PluginKey implements Serializable {

  private static final long serialVersionUID = -8398874553307926320L;

  private final String      id;

  public PluginKey(BaseNotificationPlugin plugin) {
    this(plugin.getId());
  }

  public PluginKey(String id) {
    this.id = id;
  }

  public static PluginKey key(BaseNotificationPlugin plugin) {
    return new PluginKey(plugin);
  }

  public static PluginKey key(String id) {
    return new PluginKey(id);
  }

}
