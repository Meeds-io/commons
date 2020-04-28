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
package org.exoplatform.commons.notification;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.commons.api.notification.NotificationMessageUtils;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.service.storage.WebNotificationStorage;
import org.exoplatform.commons.notification.plugin.PluginTest;
import org.exoplatform.commons.testing.BaseCommonsTestCase;
import org.exoplatform.component.test.*;
import org.exoplatform.container.ExoContainerContext;

public abstract class BaseNotificationTestCase extends BaseCommonsTestCase {

  protected static final String    NOTIFICATIONS = "notifications";

  protected List<String>           userIds       = new ArrayList<>();

  protected WebNotificationStorage storage;

  @Override
  protected void beforeClass() {
    super.beforeClass();
    ExoContainerContext.setCurrentContainer(getContainer());
    storage = getContainer().getComponentInstanceOfType(WebNotificationStorage.class);
  }

  protected NotificationInfo makeWebNotificationInfo(String userId) {
    NotificationInfo info = NotificationInfo.instance();
    info.key(new PluginKey(PluginTest.ID));
    info.setTitle("The title");
    info.setFrom("mary");
    info.setTo(userId);
    info.with(NotificationMessageUtils.SHOW_POPOVER_PROPERTY.getKey(), "true")
        .with(NotificationMessageUtils.READ_PORPERTY.getKey(), "false")
        .with("activityId", "TheActivityId")
        .with("accessLink", "http://fsdfsdf.com/fsdfsf");
    return info;
  }
}
