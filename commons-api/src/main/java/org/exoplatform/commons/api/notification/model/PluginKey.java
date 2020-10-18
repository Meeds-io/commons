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
package org.exoplatform.commons.api.notification.model;

import java.io.Serializable;

import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;

public final class PluginKey implements Serializable {
  private static final long serialVersionUID = 1L;

  private final String id;
  
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
  
  public String getId() {
    return this.id;
  }
  
  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PluginKey)) {
      return false;
    }

    PluginKey that = (PluginKey) o;

    if (id != null ? !id.equals(that.id) : that.id != null) {
      return false;
    }

    return true;
  }
  @Override
  public int hashCode() {
    return (id != null ? id.hashCode() : 0);
  }
  
  @Override
  public String toString() {
      return "NotificationPluginKey[id=" + id + "]";
  }

}
