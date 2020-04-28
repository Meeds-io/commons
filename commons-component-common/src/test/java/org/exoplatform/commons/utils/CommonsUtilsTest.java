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
package org.exoplatform.commons.utils;

import org.exoplatform.commons.testing.BaseCommonsTestCase;

public class CommonsUtilsTest extends BaseCommonsTestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    System.clearProperty(CommonsUtils.CONFIGURED_DOMAIN_URL_KEY);
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
    System.setProperty(CommonsUtils.CONFIGURED_DOMAIN_URL_KEY, "http://localhost:8080");
  }

  public void testGetRestContextName() {
    assertEquals("rest", CommonsUtils.getRestContextName());
  }
  
  public void testGetCurrentDomain() {
    try {
      CommonsUtils.getCurrentDomain();
      assertFalse(true);
    } catch (NullPointerException e) {
      assertEquals("Get the domain is unsuccessfully. Please, add configuration domain on " +
      "configuration.properties file with key: " + CommonsUtils.CONFIGURED_DOMAIN_URL_KEY, e.getMessage());
    }
    System.setProperty(CommonsUtils.CONFIGURED_DOMAIN_URL_KEY, "http://exoplatfom.com");
    assertEquals("http://exoplatfom.com", CommonsUtils.getCurrentDomain());
  }
}
