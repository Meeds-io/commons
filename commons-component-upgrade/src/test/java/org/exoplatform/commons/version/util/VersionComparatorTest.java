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
package org.exoplatform.commons.version.util;

import junit.framework.TestCase;

public class VersionComparatorTest extends TestCase {

  public void testIsBefore() {
    assertTrue(VersionComparator.isBefore("2.1", "2.2"));
    assertFalse(VersionComparator.isBefore("2.2", "2.1"));
    assertFalse(VersionComparator.isBefore("2.1", "2.1"));
    assertFalse(VersionComparator.isBefore("2.1", ""));
    assertTrue(VersionComparator.isBefore("", "2.2"));
    assertTrue(VersionComparator.isBefore("5.0.0-M32", "5.0-RC1"));
  }

  public void testIsAfter() {
    assertTrue(VersionComparator.isAfter("5.0.0-GA", "5.0.0-M32"));
    assertTrue(VersionComparator.isAfter("2.2", "2.1"));
    assertTrue(VersionComparator.isAfter("4.0.0-relooking-SNAPSHOT", "2.3.10-SNAPSHOT"));
    assertFalse(VersionComparator.isAfter("2.1", "2.2"));
    assertFalse(VersionComparator.isAfter("2.1", "2.1"));
    assertFalse(VersionComparator.isAfter("", "2.1"));
    assertTrue(VersionComparator.isAfter("2.2", ""));
  }

  public void testIsSame() {
    assertTrue(VersionComparator.isSame("2.1", "2.1"));
    assertFalse(VersionComparator.isSame("2.1", "2.2"));
    assertFalse(VersionComparator.isSame("2.1", ""));
    assertFalse(VersionComparator.isSame("", "2.2"));
  }
}
