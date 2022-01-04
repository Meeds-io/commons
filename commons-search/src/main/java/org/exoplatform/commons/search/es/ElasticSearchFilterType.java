/* 
* Copyright (C) 2003-2015 eXo Platform SAS.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see http://www.gnu.org/licenses/ .
*/
package org.exoplatform.commons.search.es;

/**
 * Filter type that can be added by search connectors in
 * ES queries (seeElasticSearchServiceConnector#getAdditionalFilters(java.util.List)).
 * Type FILTER_CUSTOM allows to define any type of filter by providing the full content of the filter.
 */
public enum  ElasticSearchFilterType {
  FILTER_BY_TERM("term"),
  FILTER_EXIST("exist"),
  FILTER_NOT_EXIST("notExist"),
  FILTER_CUSTOM("custom"),
  FILTER_MY_WORK_DOCS("myWork"),
  FILTER_MATADATAS("metaDatas");

  private final String filterType;

  ElasticSearchFilterType(String filterType) {
    this.filterType = filterType;
  }

  @Override
  public String toString() {
    return filterType;
  }

}

