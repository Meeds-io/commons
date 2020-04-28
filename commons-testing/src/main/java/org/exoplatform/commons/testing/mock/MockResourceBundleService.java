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
package org.exoplatform.commons.testing.mock;

import java.util.Locale;
import java.util.ResourceBundle;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.services.resources.Query;
import org.exoplatform.services.resources.ResourceBundleData;
import org.exoplatform.services.resources.ResourceBundleService;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class MockResourceBundleService implements ResourceBundleService {

  public ResourceBundleData createResourceBundleDataInstance() {

    return null;
  }

  public PageList<ResourceBundleData> findResourceDescriptions(Query q) {

    return null;
  }

  public ResourceBundle getResourceBundle(String name, Locale locale) {

    return null;
  }

  public ResourceBundle getResourceBundle(String[] name, Locale locale) {

    return null;
  }

  public ResourceBundle getResourceBundle(String name, Locale locale, ClassLoader cl) {

    return null;
  }

  public ResourceBundle getResourceBundle(String[] name, Locale locale, ClassLoader cl) {

    return null;
  }

  public ResourceBundleData getResourceBundleData(String id) {

    return null;
  }

  public String[] getSharedResourceBundleNames() {

    return null;
  }

  public ResourceBundleData removeResourceBundleData(String id) {

    return null;
  }

  public void saveResourceBundle(ResourceBundleData data) {
  }

}
