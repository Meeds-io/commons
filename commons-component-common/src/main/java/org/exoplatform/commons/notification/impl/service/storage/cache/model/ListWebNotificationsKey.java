/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
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
package org.exoplatform.commons.notification.impl.service.storage.cache.model;

import org.exoplatform.commons.api.notification.model.WebNotificationFilter;
import org.exoplatform.commons.notification.impl.service.storage.cache.CacheKey;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ListWebNotificationsKey extends WebNotificationFilter implements CacheKey {

  private static final long serialVersionUID = -6483807024658392464L;

  private int               offset;

  private int               limit;

  public ListWebNotificationsKey(WebNotificationFilter filter, int offset, int limit) {
    super(filter.getPluginKeys(),
          filter.getUserId(),
          filter.getLimitDay(),
          filter.isOnPopover(),
          filter.getIsRead(),
          filter.getParameter());
    this.offset = offset;
    this.limit = limit;
  }

}
