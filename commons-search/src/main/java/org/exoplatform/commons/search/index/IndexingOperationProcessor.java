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
package org.exoplatform.commons.search.index;

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * 10/12/15
 */
public abstract class IndexingOperationProcessor {

  private static final Log LOG = ExoLogger.getExoLogger(IndexingOperationProcessor.class);

  private static final ThreadLocal<String> CURRENT_TENANT_NAME = new ThreadLocal<>();

  private Map<String, IndexingServiceConnector> connectors = new HashMap<String, IndexingServiceConnector>();

  public static String getCurrentTenantName() {
    return CURRENT_TENANT_NAME.get();
  }

  public static void setCurrentTenantName(String tenantName) {
    CURRENT_TENANT_NAME.set(tenantName);
  }

  /**
   * Add Indexing Connector to the service
   * @param indexingServiceConnector the indexing connector to add
   * @LevelAPI Experimental
   */
  public void addConnector (IndexingServiceConnector indexingServiceConnector) {
    addConnector(indexingServiceConnector, false);
  }

  /**
   * Add Indexing Connector to the service
   * @param indexingServiceConnector the indexing connector to add
   * @param override equal true if we can override an existing connector, false otherwise
   * @LevelAPI Experimental
   */
  public void addConnector (IndexingServiceConnector indexingServiceConnector, Boolean override) {
    if (connectors.containsKey(indexingServiceConnector.getType()) && override.equals(false)) {
      LOG.error("Impossible to add connector {}. A connector with the same name has already been registered.",
          indexingServiceConnector.getType());
    } else {
      connectors.put(indexingServiceConnector.getType(), indexingServiceConnector);
      LOG.info("An Indexing Connector has been added: {}", indexingServiceConnector.getType());
    }
  }

  /**
   * Gets all current connectors
   * @return Connectors
   * @LevelAPI Experimental
   */
  public Map<String, IndexingServiceConnector> getConnectors() {
    return connectors;
  }

  /**
   * Index all document in the indexing queue
   * @LevelAPI Experimental
   */
  public abstract void process();

  /**
   * Interrupt the indexing queue process
   */
  public abstract void interrupt();
}
