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
 * The class defines a simple entry
 * @LevelAPI Experimental
 */
public class SimpleEntry extends SearchEntry{
  private static final String TITLE = "title";
  private static final String EXCERPT = "excerpt";
  private static final String URL = "url";
  /**
   * Get title
   * @return String
   * @LevelAPI Experimental
   */
  public String getTitle() {
    return (String)content.get(TITLE);
  }

  /**
   * Set title
   * @param title
   * @LevelAPI Experimental
   */
  public void setTitle(String title) {
    content.put(TITLE, title);
  }

  /**
   * Get excerpt
   * @return String
   * @LevelAPI Experimental
   */
  public String getExcerpt() {
    return (String)content.get(EXCERPT);
  }

  /**
   * Set excerpt
   * @param excerpt
   * @LevelAPI Experimental
   */
  public void setExcerpt(String excerpt) {
    content.put(EXCERPT, excerpt);
  }
  
  /**
   * Get URL
   * @return String
   * @LevelAPI Experimental
   */
  public String getUrl() {
    return (String)content.get(URL);
  }

  /**
   * Set URL
   * @param url
   * @LevelAPI Experimental
   */
  public void setUrl(String url) {
    content.put(URL, url);
  }
}
