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

import org.exoplatform.commons.notification.impl.service.storage.cache.model.ListWebNotificationsData;
import org.exoplatform.commons.notification.impl.service.storage.cache.model.ListWebNotificationsKey;
import org.exoplatform.services.cache.ClusterCachedObjectSelector;
import org.exoplatform.services.cache.ObjectCacheInfo;

/**
 * Selects the lists of the web notifications of a user, which are removed from every node of a cluster.
 */
public class WebNotificationListsOfUserSelector extends
                                                ClusterCachedObjectSelector<ListWebNotificationsKey, ListWebNotificationsData> {

  private static final long serialVersionUID = 6620348715902173954L;

  private final String      userId;

  public WebNotificationListsOfUserSelector(String userId) {
    this.userId = userId;
  }

  @Override
  public boolean select(ListWebNotificationsKey key, ObjectCacheInfo<? extends ListWebNotificationsData> ocinfo) {
    return userId.equals(key.getUserId());
  }

  @Override
  protected Object[] criteria() {
    return new Object[] { userId };
  }
}
