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
package org.exoplatform.commons.search.index.impl;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.search.dao.IndexingOperationDAO;
import org.exoplatform.commons.search.domain.IndexingOperation;
import org.exoplatform.commons.search.domain.OperationType;
import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 7/22/15
 */
public class QueueIndexingService implements IndexingService {

  private static final Log LOG = ExoLogger.getExoLogger(QueueIndexingService.class);

  private final IndexingOperationDAO indexingOperationDAO;

  public QueueIndexingService(IndexingOperationDAO indexingOperationDAO) {
    this.indexingOperationDAO = indexingOperationDAO;
  }

  @Override
  public void init(String connectorName) {
    addToIndexingQueue(connectorName, null, OperationType.INIT);
  }

  @Override
  public void index(String connectorName, String id) {
    if (StringUtils.isBlank(id)) {
      throw new IllegalArgumentException("Id is null");
    }
    addToIndexingQueue(connectorName, id, OperationType.CREATE);
  }

  @Override
  public void reindex(String connectorName, String id) {
    if (StringUtils.isBlank(id)) {
      throw new IllegalArgumentException("Id is null");
    }
    addToIndexingQueue(connectorName, id, OperationType.UPDATE);
  }

  @Override
  public void unindex(String connectorName, String id) {
    if (StringUtils.isBlank(id)) {
      throw new IllegalArgumentException("Id is null");
    }
    addToIndexingQueue(connectorName, id, OperationType.DELETE);
  }

  @Override
  public void reindexAll(String connectorName) {
    addToIndexingQueue(connectorName, null, OperationType.REINDEX_ALL);
  }

  @Override
  public void unindexAll(String connectorName) {
    addToIndexingQueue(connectorName, null, OperationType.DELETE_ALL);
  }

  @Override
  public void clearQueue() {
    indexingOperationDAO.deleteAll();
  }

  @Override
  public void clearQueue(String entityType) {
    indexingOperationDAO.deleteAllByEntityType(entityType);
  }

  /**
   * Add a new operation to the create queue
   * @param connectorName Name of the connector
   * @param entityId id of the document
   * @param operation operation to the create {create, update, delete, init}
   * @LevelAPI Experimental
   */
  private void addToIndexingQueue(String connectorName, String entityId, OperationType operation) {
    if (operation==null) {
      throw new IllegalArgumentException("Operation cannot be null");
    }
    /*
    if (!getConnectors().containsKey(connectorName)) {
      throw new IllegalStateException("Connector ["+connectorName+"] has not been registered.");
    }*/
    switch (operation) {
      //A new type of document need to be initialise
      case INIT: indexingOperationDAO.create(getIndexingOperation(connectorName, OperationType.INIT, entityId));
        break;
      //A new entity need to be indexed
      case CREATE: indexingOperationDAO.create(getIndexingOperation(connectorName, OperationType.CREATE, entityId));
        break;
      //An existing entity need to be updated in the create
      case UPDATE: indexingOperationDAO.create(getIndexingOperation(connectorName, OperationType.UPDATE, entityId));
        break;
      //An existing entity need to be deleted from the create
      case DELETE: indexingOperationDAO.create(getIndexingOperation(connectorName, OperationType.DELETE, entityId));
        break;
      //All entities of a specific type need to be deleted
      case DELETE_ALL: indexingOperationDAO.create(getIndexingOperation(connectorName, OperationType.DELETE_ALL, entityId));
        break;
      //All entities of a specific type need to be reindexed
      case REINDEX_ALL: indexingOperationDAO.create(getIndexingOperation(connectorName, OperationType.REINDEX_ALL, entityId));
        break;
      default:
        throw new IllegalArgumentException(operation+" is not an accepted operation for the Indexing Queue");
    }
  }

  private IndexingOperation getIndexingOperation (String connector, OperationType operation, String entityId) {
    IndexingOperation indexingOperation = new IndexingOperation();
    indexingOperation.setEntityType(connector);
    indexingOperation.setOperation(operation);
    if (entityId != null) indexingOperation.setEntityId(entityId);
    return indexingOperation;
  }

  /**
   * Clear the indexQueue
   * @LevelAPI Experimental
   */
  public void clearIndexingQueue() {
    indexingOperationDAO.deleteAll();
  }

  /**
   * get the number of operations in indexQueue
   * @LevelAPI Experimental
   */
  public Long getNumberOperations() {
    return indexingOperationDAO.count();
  }

  public List<IndexingOperation> getOperations(int offset, int limit) {
    return indexingOperationDAO.findAll(offset, limit);
  }

  public IndexingOperation getOperation(String operationId) {
    return indexingOperationDAO.find(Long.getLong(operationId));
  }

  public void deleteAllOperations() {
    indexingOperationDAO.deleteAll();
  }

  public void deleteOperation(IndexingOperation indexingOperation) {
    indexingOperationDAO.delete(indexingOperation);
  }
}
