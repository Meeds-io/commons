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
package org.exoplatform.commons.search.index.impl;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.picocontainer.Startable;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.commons.search.dao.IndexingOperationDAO;
import org.exoplatform.commons.search.domain.IndexingOperation;
import org.exoplatform.commons.search.domain.OperationType;
import org.exoplatform.commons.search.es.client.*;
import org.exoplatform.commons.search.index.IndexingOperationProcessor;
import org.exoplatform.commons.search.index.IndexingServiceConnector;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS Author : Thibault Clement
 * tclement@exoplatform.com 10/12/15
 */
public class ElasticIndexingOperationProcessor extends IndexingOperationProcessor implements Startable {

  private static final Log                   LOG                                 =
                                                 ExoLogger.getExoLogger(ElasticIndexingOperationProcessor.class);

  private static final String                BATCH_NUMBER_PROPERTY_NAME          = "exo.es.indexing.batch.number";

  private static final Integer               BATCH_NUMBER_DEFAULT                = 1000;

  private static final String                REQUEST_SIZE_LIMIT_PROPERTY_NAME    = "exo.es.indexing.request.size.limit";

  /** in bytes, default=10MB **/
  private static final Integer               REQUEST_SIZE_LIMIT_DEFAULT          = 10485760;

  private static final String                REINDEXING_BATCH_SIZE_PROPERTY_NAME = "exo.es.reindex.batch.size";

  private static final int                   REINDEXING_BATCH_SIZE_DEFAULT_VALUE = 100;

  // Service
  private final IndexingOperationDAO         indexingOperationDAO;

  private final ElasticIndexingClient        elasticIndexingClient;

  private final ElasticContentRequestBuilder elasticContentRequestBuilder;

  private final ElasticIndexingAuditTrail    auditTrail;

  private final EntityManagerService         entityManagerService;

  private Integer                            batchNumber                         = BATCH_NUMBER_DEFAULT;

  private Integer                            requestSizeLimit                    = REQUEST_SIZE_LIMIT_DEFAULT;

  private ExecutorService                    executors                           = Executors.newCachedThreadPool();

  private String                             esVersion;

  private boolean                            interrupted                         = false;

  private boolean                            initialized                         = false;

  public ElasticIndexingOperationProcessor(IndexingOperationDAO indexingOperationDAO,
                                           ElasticIndexingClient elasticIndexingClient,
                                           ElasticContentRequestBuilder elasticContentRequestBuilder,
                                           ElasticIndexingAuditTrail auditTrail,
                                           EntityManagerService entityManagerService,
                                           InitParams initParams) {
    this.indexingOperationDAO = indexingOperationDAO;
    this.auditTrail = auditTrail;
    this.entityManagerService = entityManagerService;
    this.elasticIndexingClient = elasticIndexingClient;
    this.elasticContentRequestBuilder = elasticContentRequestBuilder;
    if (StringUtils.isNotBlank(PropertyManager.getProperty(BATCH_NUMBER_PROPERTY_NAME))) {
      this.batchNumber = Integer.valueOf(PropertyManager.getProperty(BATCH_NUMBER_PROPERTY_NAME));
    }
    if (StringUtils.isNotBlank(PropertyManager.getProperty(REQUEST_SIZE_LIMIT_PROPERTY_NAME))) {
      this.requestSizeLimit = Integer.valueOf(PropertyManager.getProperty(REQUEST_SIZE_LIMIT_PROPERTY_NAME));
    }
    if (initParams == null || !initParams.containsKey("es.version")) {
      throw new IllegalStateException("es.version parameter is mandatory");
    }
    this.esVersion = initParams.getValueParam("es.version").getValue();
    LOG.info("Use ES Version {}", esVersion);
  }

  @Override
  public void addConnector(IndexingServiceConnector indexingServiceConnector) {
    addConnector(indexingServiceConnector, false);
  }

  @Override
  public void addConnector(IndexingServiceConnector indexingServiceConnector, Boolean override) {
    if (getConnectors().containsKey(indexingServiceConnector.getConnectorName()) && override.equals(false)) {
      LOG.error("Impossible to add connector {}. A connector with the same name has already been registered.",
                indexingServiceConnector.getConnectorName());
    } else {
      getConnectors().put(indexingServiceConnector.getConnectorName(), indexingServiceConnector);
      LOG.info("An Indexing Connector has been added: {}", indexingServiceConnector.getConnectorName());
    }
  }

  /**
   * Handle the Indexing queue Get all data in the indexing queue, transform
   * them to ES requests, send requests to ES This method is ONLY called by the
   * job scheduler. This method is not annotated with @ExoTransactional because
   * we don't want it to be executed in one transaction. A request lifecycle is
   * started and ended for all jobs, it is done by
   * org.exoplatform.services.scheduler.impl.JobEnvironmentConfigListener. It
   * means that we have 1 entity manager per job execution. Because of that, we
   * have to take care of cleaning the persistence context regularly to avoid to
   * have too big sessions and bad performances. This method is synchronized to
   * make sure the queue is processed by only one thread at a time, since the
   * indexing queue does not support multi-thread processing for the moment.
   */
  @Override
  public synchronized void process() {
    if (!this.initialized) {
      LOG.debug("Skip ES queue processing until service is properly initialized");
      return;
    }
    this.interrupted = false;
    try {
      // Loop until the number of data retrieved from indexing queue is less
      // than
      // BATCH_NUMBER (default = 1000)
      int processedOperations;
      do {
        processedOperations = processBulk();
      } while (processedOperations >= batchNumber);
    } finally {
      if (this.interrupted) {
        LOG.info("Indexing queue processing interruption done");
      }
    }
  }

  /**
   * Set the indexing process as interrupted in order to terminate it as soon as
   * possible without finishing the whole process. Since the indexing process
   * can take time (for a reindexAll operation for example), it allows to
   * interrupt it gracefully (without killing the thread).
   */
  @Override
  public void interrupt() {
    LOG.info("Indexing queue processing has been interrupted. Please wait until the service exists cleanly...");
    this.interrupted = true;
  }

  private boolean isInterrupted() {
    if (Thread.currentThread().isInterrupted()) {
      LOG.info("Thread running indexing queue processing has been interrupted. Please wait until the service exists cleanly...");
      this.interrupted = true;
    }
    return this.interrupted;
  }

  private int processBulk() {
    Map<OperationType, Map<String, List<IndexingOperation>>> indexingQueueSorted = new EnumMap<>(OperationType.class);
    List<IndexingOperation> indexingOperations;
    long maxIndexingOperationId = 0;

    // Get BATCH_NUMBER (default = 1000) first indexing operations
    indexingOperations = indexingOperationDAO.findAllFirst(batchNumber);
    if (indexingOperations.isEmpty()) {
      return 0;
    }

    // Get all Indexing operations and order them per operation and type in map:
    // <Operation, <Type, List<IndexingOperation>>>
    for (IndexingOperation indexingOperation : indexingOperations) {
      putIndexingOperationInMemoryQueue(indexingOperation, indexingQueueSorted);
      // Get the max ID of IndexingOperation of the bulk
      if (maxIndexingOperationId < indexingOperation.getId()) {
        maxIndexingOperationId = indexingOperation.getId();
      }
    }

    processInit(indexingQueueSorted);
    processCUD(indexingQueueSorted);

    if (isInterrupted()) {
      throw new IllegalStateException("Indexing queue processing interrupted");
    }

    // Removes the processed IDs from the “indexing queue” table that have
    // timestamp older than the timestamp of
    // start of processing
    indexingOperationDAO.deleteAllIndexingOperationsHavingIdLessThanOrEqual(maxIndexingOperationId);

    // clear entity manager content after each bulk
    entityManagerService.getEntityManager().clear();

    return indexingOperations.size();
  }

  /**
   * Add an indexing operation to the Temporary inMemory IndexingQueue
   * 
   * @param indexingOperation the operation to add to the Temporary inMemory
   *          IndexingQueue
   * @param indexingQueueSorted Temporary inMemory IndexingQueue
   */
  private void putIndexingOperationInMemoryQueue(IndexingOperation indexingOperation,
                                                 Map<OperationType, Map<String, List<IndexingOperation>>> indexingQueueSorted) {
    // Check if the Indexing Operation map already contains a specific operation
    if (!indexingQueueSorted.containsKey(indexingOperation.getOperation())) {
      // If not add a new operation in the map
      indexingQueueSorted.put(indexingOperation.getOperation(), new HashMap<>());
    }
    // Check if the operation map already contains a specific type
    if (!indexingQueueSorted.get(indexingOperation.getOperation()).containsKey(indexingOperation.getEntityIndex())) {
      // If not add a new type for the operation above
      indexingQueueSorted.get(indexingOperation.getOperation())
                         .put(indexingOperation.getEntityIndex(),
                              new ArrayList<>());
    }
    // Add the indexing operation in the specific Operation -> Type
    indexingQueueSorted.get(indexingOperation.getOperation()).get(indexingOperation.getEntityIndex()).add(indexingOperation);
  }

  /**
   * Process all the Create / Update / Delete operations
   * 
   * @param indexingQueueSorted Temporary inMemory IndexingQueue
   */
  private void processCUD(Map<OperationType, Map<String, List<IndexingOperation>>> indexingQueueSorted) {
    // Initialize bulk request for CUD operations
    StringBuilder bulkRequest = new StringBuilder();

    // Process Delete document operation
    if (indexingQueueSorted.containsKey(OperationType.DELETE)) {
      Map<String, List<IndexingOperation>> deleteIndexingOperationsMap = indexingQueueSorted.get(OperationType.DELETE);
      for (String entityIndex : deleteIndexingOperationsMap.keySet()) {
        ElasticIndexingServiceConnector connector = (ElasticIndexingServiceConnector) getConnectors().get(entityIndex);
        if (connector == null) {
          continue;
        }
        List<IndexingOperation> deleteIndexingOperationsList = deleteIndexingOperationsMap.get(entityIndex);
        if (deleteIndexingOperationsList == null || deleteIndexingOperationsList.isEmpty()) {
          continue;
        }
        Iterator<IndexingOperation> deleteIndexingOperationsIterator = deleteIndexingOperationsList.iterator();
        while (deleteIndexingOperationsIterator.hasNext()) {
          if (isInterrupted()) {
            return;
          }
          IndexingOperation deleteIndexQueue = deleteIndexingOperationsIterator.next();
          try {
            String deleteDocumentRequestContent = elasticContentRequestBuilder.getDeleteDocumentRequestContent(connector,
                                                                                                               deleteIndexQueue.getEntityId());
            if (deleteDocumentRequestContent != null) {
              bulkRequest.append(deleteDocumentRequestContent);
            }
            // Choose operation to delete from Queue one by one instead
          } catch (Exception e) {
            LOG.warn("Error while *deleting* index entry of entity, type = " + entityIndex + ", id ="
                + (deleteIndexQueue == null ? null : deleteIndexQueue.getEntityId()) + ", cause:", e);
          } finally {
            // Remove the delete operations from the map
            indexingQueueSorted.remove(OperationType.DELETE, deleteIndexQueue.getEntityId());
          }

          // Delete added indexation operation from queue even if the request
          // fails
          deleteIndexingOperationsIterator.remove();

          // Remove the object from other create or update operations planned
          // before the timestamp of the delete operation
          deleteOperationsByEntityIdForTypesBefore(new OperationType[] { OperationType.CREATE },
                                                   indexingQueueSorted,
                                                   deleteIndexQueue);
          deleteOperationsByEntityIdForTypes(new OperationType[] { OperationType.UPDATE }, indexingQueueSorted, deleteIndexQueue);
          // Check if the bulk request limit size is already reached
          bulkRequest = checkBulkRequestSizeReachedLimitation(bulkRequest);
        }
      }
    }

    // Process Create document operation
    if (indexingQueueSorted.containsKey(OperationType.CREATE)) {
      Map<String, List<IndexingOperation>> createIndexingOperationsMap = indexingQueueSorted.get(OperationType.CREATE);
      for (String entityIndex : createIndexingOperationsMap.keySet()) {
        ElasticIndexingServiceConnector connector = (ElasticIndexingServiceConnector) getConnectors().get(entityIndex);
        if (connector == null) {
          continue;
        }
        List<IndexingOperation> createIndexingOperationsList = createIndexingOperationsMap.get(entityIndex);
        if (createIndexingOperationsList == null || createIndexingOperationsList.isEmpty()) {
          continue;
        }
        Iterator<IndexingOperation> createIndexingOperationsIterator = createIndexingOperationsList.iterator();
        while (createIndexingOperationsIterator.hasNext()) {
          if (isInterrupted()) {
            return;
          }
          IndexingOperation createIndexQueue = createIndexingOperationsIterator.next();
          try {
            if (connector.isNeedIngestPipeline()) {
              String singleRequestOperation =
                                            elasticContentRequestBuilder.getCreatePipelineDocumentRequestContent(connector,
                                                                                                                 createIndexQueue.getEntityId());
              if (singleRequestOperation != null) {
                elasticIndexingClient.sendCreateDocOnPipeline(connector.getIndexAlias(),
                                                              createIndexQueue.getEntityId(),
                                                              connector.getPipelineName(),
                                                              singleRequestOperation);
              }
              // Delete this single operation since it's not indexed in bulk
              indexingQueueSorted.remove(OperationType.CREATE, createIndexQueue.getEntityId());
              indexingQueueSorted.remove(OperationType.UPDATE, createIndexQueue.getEntityId());
            } else {
              String singleRequestOperation =
                                            elasticContentRequestBuilder.getCreateDocumentRequestContent(connector,
                                                                                                         createIndexQueue.getEntityId());
              if (singleRequestOperation != null) {
                bulkRequest.append(singleRequestOperation);
              }
            }
          } catch (Exception e) {
            LOG.warn("Error while *creating* index entry of entity, type = " + entityIndex + ", id ="
                + (createIndexQueue == null ? null : createIndexQueue.getEntityId()) + ", cause:", e);
          } finally {

            // Delete added indexation operation from queue even if the request
            // fails
            createIndexingOperationsIterator.remove();

            // Remove the object from other update operations for this entityId
            deleteOperationsByEntityIdForTypes(new OperationType[] { OperationType.UPDATE },
                                               indexingQueueSorted,
                                               createIndexQueue);

            // Delete this single operation since it's not indexed in bulk
            indexingQueueSorted.remove(OperationType.CREATE, createIndexQueue.getEntityId());
          }

          // Check if the bulk request limit size is already reached
          bulkRequest = checkBulkRequestSizeReachedLimitation(bulkRequest);
        }
      }
    }

    // Process Update document operation
    if (indexingQueueSorted.containsKey(OperationType.UPDATE)) {
      Map<String, List<IndexingOperation>> updateIndexingOperationsMap = indexingQueueSorted.get(OperationType.UPDATE);
      for (String entityIndex : updateIndexingOperationsMap.keySet()) {
        ElasticIndexingServiceConnector connector = (ElasticIndexingServiceConnector) getConnectors().get(entityIndex);
        if (connector == null) {
          continue;
        }
        List<IndexingOperation> updateIndexingOperationsList = updateIndexingOperationsMap.get(entityIndex);
        if (updateIndexingOperationsList == null || updateIndexingOperationsList.isEmpty()) {
          continue;
        }
        Iterator<IndexingOperation> updateIndexingOperationsIterator = updateIndexingOperationsList.iterator();
        while (updateIndexingOperationsIterator.hasNext()) {
          if (isInterrupted()) {
            return;
          }
          IndexingOperation updateIndexQueue = updateIndexingOperationsIterator.next();
          try {
            if (connector.isNeedIngestPipeline()) {
              String singleRequestOperation =
                                            elasticContentRequestBuilder.getCreatePipelineDocumentRequestContent(connector,
                                                                                                                 updateIndexQueue.getEntityId());
              if (singleRequestOperation != null) {
                elasticIndexingClient.sendCreateDocOnPipeline(connector.getIndexAlias(),
                                                              updateIndexQueue.getEntityId(),
                                                              connector.getPipelineName(),
                                                              singleRequestOperation);
              }
            } else {
              String singleRequestOperation =
                                            elasticContentRequestBuilder.getUpdateDocumentRequestContent(connector,
                                                                                                         updateIndexQueue.getEntityId());
              if (singleRequestOperation != null) {
                bulkRequest.append(singleRequestOperation);
              }
            }
          } catch (Exception e) {
            LOG.warn("Error while *updating* index entry of entity, type = " + entityIndex + ", id ="
                + (updateIndexQueue == null ? null : updateIndexQueue.getEntityId()) + ", cause:", e);
          } finally {
            // Delete this single operation since it's not indexed in bulk
            indexingQueueSorted.remove(OperationType.UPDATE, updateIndexQueue.getEntityId());
          }

          // Delete added indexation operation from queue even if the request
          // fails
          updateIndexingOperationsIterator.remove();

          // Check if the bulk request limit size is already reached
          bulkRequest = checkBulkRequestSizeReachedLimitation(bulkRequest);
        }
      }
    }

    if (bulkRequest.length() > 0 && !isInterrupted()) {
      elasticIndexingClient.sendCUDRequest(bulkRequest.toString());
    }
  }

  /**
   * Process all the requests for “init of the ES create mapping” (Operation
   * type = I) in the indexing queue (if any)
   * 
   * @param indexingQueueSorted Temporary inMemory IndexingQueue
   */
  private void processInit(Map<OperationType, Map<String, List<IndexingOperation>>> indexingQueueSorted) {
    if (indexingQueueSorted.containsKey(OperationType.INIT)) {
      for (String entityIndex : indexingQueueSorted.get(OperationType.INIT).keySet()) {
        IndexingServiceConnector connector = getConnectors().get(entityIndex);
        if (isInterrupted() || connector == null) {
          return;
        }
        sendInitRequests(connector);
      }
      indexingQueueSorted.remove(OperationType.INIT);
    }
  }

  private void deleteOperationsByEntityIdForTypesBefore(OperationType[] operations,
                                                        Map<OperationType, Map<String, List<IndexingOperation>>> indexingQueueSorted,
                                                        IndexingOperation indexQueue) {
    for (OperationType operation : operations) {
      if (indexingQueueSorted.containsKey(operation)) {
        if (indexingQueueSorted.get(operation).containsKey(indexQueue.getEntityIndex())) {
          for (Iterator<IndexingOperation> iterator = indexingQueueSorted.get(operation)
                                                                         .get(indexQueue.getEntityIndex())
                                                                         .iterator(); iterator.hasNext();) {
            IndexingOperation indexingOperation = iterator.next();
            // Check Id higher than the Id of the CUD indexing queue, the index
            // queue is removed
            if ((indexQueue.getId() > indexingOperation.getId())
                && indexingOperation.getEntityId().equals(indexQueue.getEntityId())) {
              iterator.remove();
            }
          }
        }
      }
    }
  }

  private void deleteOperationsByEntityIdForTypes(OperationType[] operations,
                                                  Map<OperationType, Map<String, List<IndexingOperation>>> indexingQueueSorted,
                                                  IndexingOperation indexQueue) {
    for (OperationType operation : operations) {
      if (indexingQueueSorted.containsKey(operation)) {
        if (indexingQueueSorted.get(operation).containsKey(indexQueue.getEntityIndex())) {
          for (Iterator<IndexingOperation> iterator = indexingQueueSorted.get(operation)
                                                                         .get(indexQueue.getEntityIndex())
                                                                         .iterator(); iterator.hasNext();) {
            IndexingOperation indexingOperation = iterator.next();
            if (indexingOperation.getEntityId().equals(indexQueue.getEntityId())) {
              iterator.remove();
            }
          }
        }
      }
    }
  }

  private void sendInitRequests(IndexingServiceConnector indexingServiceConnector) {
    ElasticIndexingServiceConnector connector = (ElasticIndexingServiceConnector) indexingServiceConnector;

    String indexAlias = connector.getIndexAlias();
    String index = connector.getCurrentIndex();
    boolean useAlias = true;
    if (index == null) {
      index = indexAlias;
      useAlias = false;
    }

    // Send request to create index
    boolean newlyCreated =
                         elasticIndexingClient.sendCreateIndexRequest(index,
                                                                      elasticContentRequestBuilder.getCreateIndexRequestContent(connector),
                                                                      connector.getMapping());
    if (newlyCreated && useAlias) {
      elasticIndexingClient.sendCreateIndexAliasRequest(index, null, indexAlias);
    }

    if (connector.isNeedIngestPipeline()) {
      elasticIndexingClient.sendCreateAttachmentPipelineRequest(index,
                                                                connector.getPipelineName(),
                                                                connector.getAttachmentProcessor());
    }
  }

  /**
   * If the bulk request already reached a size limitation, the bulk request
   * need to be sent immediately
   *
   * @param bulkRequest to analyze
   * @return
   */
  private StringBuilder checkBulkRequestSizeReachedLimitation(StringBuilder bulkRequest) {
    if (bulkRequest.length() >= requestSizeLimit) {
      elasticIndexingClient.sendCUDRequest(bulkRequest.toString());
      // return an empty bulk request
      return new StringBuilder();
    } else {
      return bulkRequest;
    }
  }

  public Integer getBatchNumber() {
    return batchNumber;
  }

  public void setBatchNumber(Integer batchNumber) {
    this.batchNumber = batchNumber;
  }

  public Integer getRequestSizeLimit() {
    return requestSizeLimit;
  }

  public void setRequestSizeLimit(Integer requestSizeLimit) {
    this.requestSizeLimit = requestSizeLimit;
  }

  @Override
  public void start() {
    try {
      String version = elasticIndexingClient.sendGetESVersion();
      if (version == null || !version.startsWith(this.esVersion + ".")) {
        LOG.error("Expected Version of ES version is " + this.esVersion + " but was " + version
            + ". If this is a compatible version, you can configure 'exo.es.version.minor' to delete this error message.");
      }
      // ES index and type need to be created for all registered connectors
      initConnectors();

      this.initialized = true;
    } catch (Exception e) {
      LOG.error("Error while initializing ES connectors", e);
    }
  }

  @Override
  public void stop() {
    executors.shutdownNow();
  }

  public void setInitialized(boolean initialized) {
    this.initialized = initialized;
  }

  private void initConnectors() {
    for (Map.Entry<String, IndexingServiceConnector> entry : getConnectors().entrySet()) {
      sendInitRequests(entry.getValue());
    }
  }

}
