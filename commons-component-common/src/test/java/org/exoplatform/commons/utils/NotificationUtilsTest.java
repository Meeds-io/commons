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
package org.exoplatform.commons.utils;

import static org.exoplatform.commons.api.notification.NotificationConstants.CALENDAR_ACTIVITY;

import java.util.List;

import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.testing.BaseCommonsTestCase;

public class NotificationUtilsTest extends BaseCommonsTestCase {

  public void testAddToListFromEmptyString() {
    try {
      List<String> emptyStringList = NotificationUtils.stringToList("");
      assertTrue(emptyStringList.add("test"));
    } catch (UnsupportedOperationException unsupportedOperationException) {
      fail("Fail to add an item to the list due to UnsupportedOperationException of add operation");
    }
  }
  
  public void testRemoveLinkTitle() {
    String title = "<a href=\"http://exoplatform.github.io/\" target=\"_blank\">http://exoplatform.github.io/</a>";
    String newTitle = "<span class=\"user-name text-bold\">http://exoplatform.github.io/</span>";
    assertEquals(newTitle, NotificationUtils.removeLinkTitle(title));
    
    title = "MHM&amp;#39s B-day Party";
    assertEquals("MHM&#39s B-day Party", NotificationUtils.getNotificationActivityTitle(title, CALENDAR_ACTIVITY));
    
    title = "MHM&amp;#39s B-day Party";
    assertEquals("MHM&amp;#39s B-day Party", NotificationUtils.getNotificationActivityTitle(title, ""));
  }
}
