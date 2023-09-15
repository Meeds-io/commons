/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
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

package org.exoplatform.commons.notification.impl.service.storage.cache;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.notification.impl.service.storage.cache.model.ListWebNotificationsData;
import org.exoplatform.commons.notification.impl.service.storage.cache.model.ListWebNotificationsKey;
import org.exoplatform.services.cache.CachedObjectSelector;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.ObjectCacheInfo;

public class WebNotificationInfosSelector implements CachedObjectSelector<ListWebNotificationsKey, Object> {
  private final NotificationInfo notificationInfo;
  
  public WebNotificationInfosSelector(NotificationInfo notificationInfo) {
    this.notificationInfo = notificationInfo;
  }
  
  @Override
  public boolean select(ListWebNotificationsKey key, ObjectCacheInfo<? extends Object> ocinfo) {
    if (key != null && key.getUserId() != null) {
      return key.getUserId().equals(notificationInfo.getTo());
    }
    return false;
  }

  @Override
  public void onSelect(ExoCache<? extends ListWebNotificationsKey, ? extends Object> cache,
      ListWebNotificationsKey key, ObjectCacheInfo<? extends Object> ocinfo) throws Exception {
    Object data = ocinfo.get();
    if (data instanceof ListWebNotificationsData listData) {
      if (key.isOnPopover()) {
        listData.removeByValue(notificationInfo.getId());
      } else {
        listData.clear();          
      }  
    }
  }

}
