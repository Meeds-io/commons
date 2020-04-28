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

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class MockResourceBundle extends ResourceBundle {

  protected Map<String, Object> content;

  public MockResourceBundle(Map<String, Object> content)
  {
     this.content = new HashMap<String, Object>(content);
  }

  protected Object handleGetObject(String key)
  {
     if (key == null)
     {
        throw new IllegalArgumentException("null hey is not allowed");
     }
     return content.get(key);
  }

  public Enumeration<String> getKeys()
  {
     return Collections.enumeration(content.keySet());
  }

  public void put(String key, String value) {
    content.put(key, value);
  }

}
