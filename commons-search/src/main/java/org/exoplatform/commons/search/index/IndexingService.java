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
package org.exoplatform.commons.search.index;

public interface IndexingService {

  /**
   * Add a init operation to the indexing queue to init the index
   * @param connectorName Name of the connector
   * @LevelAPI Experimental
   */
  void init(String connectorName);

  /**
   * Add a create operation to the indexing queue
   * @param connectorName Name of the connector
   * @param id id of the document
   * @LevelAPI Experimental
   */
  void index(String connectorName, String id);

  /**
   * Add a update operation to the indexing queue
   * @param connectorName Name of the connector
   * @param id id of the document
   * @LevelAPI Experimental
   */
  void reindex(String connectorName, String id);

  /**
   * Add a delete operation to the indexing queue
   * @param connectorName Name of the connector
   * @param id id of the document
   * @LevelAPI Experimental
   */
  void unindex(String connectorName, String id);
}
