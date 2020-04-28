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
package org.exoplatform.commons.api.search;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.api.search.data.SearchContext;
import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;

import java.util.Collection;

/**
 * Is extended by all SearchService connectors, and allows to build configuration needed by a list of connectors that is used for the Unified Search.
 * 
 */
public abstract class SearchServiceConnector extends BaseComponentPlugin {
  private String searchType; //search type name
  private String displayName; //for use when rendering
  private boolean enable = true;
  private boolean enabledForAnonymous = false;
  
  /**
   * Gets a search type.
   * @return The string.
   * @LevelAPI Experimental
   */
  public String getSearchType() {
    return searchType;
  }

  /**
   * Sets a search type.
   * @param searchType The search type to be set.
   * @LevelAPI Experimental
   */
  public void setSearchType(String searchType) {
    this.searchType = searchType;
  }

  /**
   * Gets a display name.
   * @return The string.
   * @LevelAPI Experimental
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * Sets a display name.
   * @param displayName The display name to be set.
   * @LevelAPI Experimental
   */
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }
  
  /**
   * is enable by default
   */
  public boolean isEnable() {
    return enable;
  }

  /**
   * set enable by default
   */
  public void setEnable(boolean enable) {
    this.enable = enable;
  }
  
  public boolean isEnabledForAnonymous() {
    return enabledForAnonymous;
  }
  
  public void setEnabledForAnonymous(boolean enabledForAnonymous) {
    this.enabledForAnonymous = enabledForAnonymous;
  }

  /**
   * Initializes a search service connector. The constructor is default that connectors must implement.
   * @param initParams The parameters which are used for initializing the search service connector from configuration.
   * @LevelAPI Experimental
   */
  public SearchServiceConnector(InitParams initParams) {
    PropertiesParam param = initParams.getPropertiesParam("constructor.params");
    this.searchType = param.getProperty("searchType");
    this.displayName = param.getProperty("displayName");
    if (StringUtils.isNotBlank(param.getProperty("enable"))) this.setEnable(Boolean.parseBoolean(param.getProperty("enable")));
  }

  /**
   * Returns a collection of search results from the connectors. 
   * The connectors must implement this search method, with the parameters below.
   * @param context The search context.
   * @param query The query statement.
   * @param sites Specified sites where the search is performed (for example Acme, or Intranet).
   * @param offset The start point from which the search results are returned.
   * @param limit The limitation number of search results.
   * @param sort The sorting criteria (title, relevancy and date).
   * @param order The sorting order (ascending and descending).
   * @return The collection of search results.
   * @LevelAPI Experimental 
   */
  public abstract Collection<SearchResult> search(SearchContext context, String query, Collection<String> sites, int offset, int limit, String sort, String order);
}
