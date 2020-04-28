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
package org.exoplatform.commons.api.indexing.data;

/**
 * The class defines structure of id that specifies a unique entry
 * @LevelAPI Experimental
 */
public class SearchEntryId {
  protected String collection;
  protected String type;
  protected String name;
    
  /**
   * Constructor creates entry id
   * @param collection
   * @param type
   * @param name
   * @LevelAPI Experimental
   */
  public SearchEntryId(String collection, String type, String name) {
    this.collection = collection;
    this.type = type;
    this.name = name;
  }
  
  /**
   * Get collection
   * @return String
   * @LevelAPI Experimental
   */
  public String getCollection() {
    return collection;
  }

  /**
   * Get type
   * @return String
   * @LevelAPI Experimental
   */
  public String getType() {
    return type;
  }

  /**
   * Get name
   * @return String
   * @LevelAPI Experimental
   */
  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return collection + "/" + type + "/" + name;
  }

}
