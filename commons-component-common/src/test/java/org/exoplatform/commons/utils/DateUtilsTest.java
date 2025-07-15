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

import java.util.Locale;
import java.util.TimeZone;

import org.junit.Test;

import org.exoplatform.commons.testing.BaseCommonsTestCase;
import org.exoplatform.container.ExoContainerContext;

public class DateUtilsTest extends BaseCommonsTestCase {

  public void testGetTimeZone() {
    assertEquals(TimeZone.getTimeZone("GMT"), DateUtils.getTimeZone("GMT"));
    assertEquals(TimeZone.getTimeZone("Africa/Tunis"), DateUtils.getTimeZone("Africa/Tunis"));
    assertEquals(TimeZone.getTimeZone(""), DateUtils.getTimeZone(""));
    try {
      DateUtils.getTimeZone(null);
      fail();
    } catch (IllegalArgumentException exp) {

    }
  }

  @Test
  public void testGetRelativeTimeLabel() {
    ExoContainerContext.setCurrentContainer(getContainer());

    assertEquals("less than a minute ago", DateUtils.getRelativeTimeLabel(Locale.ENGLISH, System.currentTimeMillis() - 30L));
    assertEquals("about a month ago", DateUtils.getRelativeTimeLabel(Locale.ENGLISH, System.currentTimeMillis() - 3000000000L));
    assertEquals("about 2 months ago",
                 DateUtils.getRelativeTimeLabel(Locale.ENGLISH, System.currentTimeMillis() - 7000000000L));
    assertEquals("about 3 months ago",
                 DateUtils.getRelativeTimeLabel(Locale.ENGLISH, System.currentTimeMillis() - 10000000000L));
  }

}
