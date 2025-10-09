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
package org.exoplatform.commons.search.index.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.search.dao.IndexingOperationDAO;
import org.exoplatform.commons.search.domain.IndexingOperation;
import org.exoplatform.commons.search.domain.OperationType;
import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.services.listener.ListenerService;

public class QueueIndexingService implements IndexingService {

  private final IndexingOperationDAO indexingOperationDAO;

  private final ListenerService      listenerService;

  public QueueIndexingService(IndexingOperationDAO indexingOperationDAO,
                              ListenerService listenerService) {
    this.indexingOperationDAO = indexingOperationDAO;
    this.listenerService = listenerService;
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

  /**
   * Add a new operation to the create queue
   * @param connectorName Name of the connector
   * @param entityId id of the document
   * @param operation operation to the create {create, update, delete, init}
   * @LevelAPI Experimental
   */
  private void addToIndexingQueue(String connectorName, String entityId, OperationType operation) {
    if (operation == null) {
      throw new IllegalArgumentException("Operation cannot be null");
    } else if (operation == OperationType.DELETE_ALL) {
      throw new IllegalArgumentException("Operation 'DELETE_ALL' isn't handled");
    }
    indexingOperationDAO.create(getIndexingOperation(connectorName, operation, entityId));
    listenerService.broadcast("indexing.operation.%s".formatted(operation.name().toLowerCase()), connectorName, entityId);
  }

  private IndexingOperation getIndexingOperation(String connector, OperationType operation, String entityId) {
    IndexingOperation indexingOperation = new IndexingOperation();
    indexingOperation.setEntityIndex(connector);
    indexingOperation.setOperation(operation);
    if (entityId != null) indexingOperation.setEntityId(entityId);
    return indexingOperation;
  }

  /**
   * Clear the indexQueue
   *
   * @LevelAPI Experimental
   */
  public void clearIndexingQueue() {
    indexingOperationDAO.deleteAll();
  }

  /**
   * get the number of operations in indexQueue
   * @return long for number of operations
   * @LevelAPI Experimental
   */
  public long getNumberOperations() {
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
