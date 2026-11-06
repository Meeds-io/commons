/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
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
package org.exoplatform.commons.notification.impl.service.storage.cache;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.notification.impl.service.storage.cache.model.WebNotifInfoCacheKey;
import org.exoplatform.services.cache.ClusterCachedObjectSelector;
import org.exoplatform.services.cache.ObjectCacheInfo;

/**
 * Selects the web notifications sent to a user, which are removed from every node of a cluster.
 */
public class WebNotificationsOfUserSelector extends ClusterCachedObjectSelector<WebNotifInfoCacheKey, NotificationInfo> {

  private static final long serialVersionUID = -4163720958146617319L;

  private final String      userId;

  public WebNotificationsOfUserSelector(String userId) {
    this.userId = userId;
  }

  @Override
  public boolean select(WebNotifInfoCacheKey key, ObjectCacheInfo<? extends NotificationInfo> ocinfo) {
    NotificationInfo notification = ocinfo.get();
    return notification != null && userId.equals(notification.getTo());
  }

  @Override
  protected Object[] criteria() {
    return new Object[] { userId };
  }
}
