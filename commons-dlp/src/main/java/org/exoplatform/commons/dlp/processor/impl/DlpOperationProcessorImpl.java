package org.exoplatform.commons.dlp.processor.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.picocontainer.Startable;

import org.exoplatform.commons.dlp.connector.DlpServiceConnector;
import org.exoplatform.commons.dlp.dao.DlpOperationDAO;
import org.exoplatform.commons.dlp.domain.DlpOperation;
import org.exoplatform.commons.dlp.processor.DlpOperationProcessor;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class DlpOperationProcessorImpl extends DlpOperationProcessor implements Startable {

  private static final Log      LOGGER               = ExoLogger.getExoLogger(DlpOperationProcessorImpl.class);

  private static final Integer  BATCH_NUMBER_DEFAULT = 1000;

  // Service
  private final DlpOperationDAO dlpOperationDAO;

  private Integer               batchNumber          = BATCH_NUMBER_DEFAULT;

  private ExecutorService       executors            = Executors.newCachedThreadPool();

  private boolean               interrupted          = false;

  private boolean               initialized          = false;

  public DlpOperationProcessorImpl(DlpOperationDAO dlpOperationDAO) {
    this.dlpOperationDAO = dlpOperationDAO;
  }

  @Override
  public void start() {
    this.initialized = true;
  }

  @Override
  public void stop() {
    executors.shutdownNow();
  }

  @Override
  public synchronized void process() {
    if (!this.initialized) {
      LOGGER.debug("Skip Dlp queue processing until service is properly initialized");
      return;
    }
    this.interrupted = false;
    try {
      // Loop until the number of data retrieved from dlp queue is less than
      // BATCH_NUMBER (default = 1000)
      int processedOperations;
      do {
        processedOperations = processBulk();
      } while (processedOperations >= batchNumber);
    } finally {
      if (this.interrupted) {
        LOGGER.debug("Dlp queue processing interruption done");
      }
    }
  }

  @Override
  public void interrupt() {
    LOGGER.debug("Dlp queue processing has been interrupted. Please wait until the service exists cleanly...");
    this.interrupted = true;
  }

  private boolean isInterrupted() {
    if (Thread.currentThread().isInterrupted()) {
      LOGGER.debug("Thread running dlp queue processing has been interrupted. Please wait until the service exists cleanly...");
      this.interrupted = true;
    }
    return this.interrupted;
  }

  private int processBulk() {

    Map<String, List<DlpOperation>> dlpQueueSorted = new HashMap<>();
    long maxDlpOperationId = 0;

    // Get BATCH_NUMBER (default = 1000) first dlp operations
    List<DlpOperation> dlpOperations = dlpOperationDAO.findAllFirst(batchNumber);

    // Get all Dlp operations and order them per type in map:
    // <Operation, <Type, List<DlpOperation>>>
    for (DlpOperation dlpOperation : dlpOperations) {
      putDlpOperationInMemoryQueue(dlpOperation, dlpQueueSorted);
      // Get the max ID of DlpOperation of the bulk
      if (maxDlpOperationId < dlpOperation.getId()) {
        maxDlpOperationId = dlpOperation.getId();
      }
    }

    // Process dlp operation
    for (String entityType : dlpQueueSorted.keySet()) {
      DlpServiceConnector connector = (DlpServiceConnector) getConnectors().get(entityType);
      List<DlpOperation> dlpOperationsList = dlpQueueSorted.get(entityType);
      if (dlpOperationsList == null || dlpOperationsList.isEmpty()) {
        continue;
      }
      Iterator<DlpOperation> dlpOperationsIterator = dlpOperationsList.iterator();
      while (dlpOperationsIterator.hasNext()) {
        if (isInterrupted()) {
          throw new RuntimeException("Dlp queue processing interrupted");
        }
        DlpOperation dlpOperation = dlpOperationsIterator.next();
        if (connector.processItem(dlpOperation.getEntityId())) {
          dlpOperationDAO.delete(dlpOperation);
        }
      }
    }
    return dlpOperations.size();
  }

  /**
   * Add an dlp operation to the Temporary inMemory DlpQueue
   * 
   * @param dlpOperation the operation to add to the Temporary inMemory DlpQueue
   * @param dlpQueueSorted Temporary inMemory DlpQueue
   */
  private void putDlpOperationInMemoryQueue(DlpOperation dlpOperation,
                                            Map<String, List<DlpOperation>> dlpQueueSorted) {
    // Check if the operation map already contains a specific type
    if (!dlpQueueSorted.containsKey(dlpOperation.getEntityType())) {
      // If not add a new type for the operation above
      dlpQueueSorted.put(dlpOperation.getEntityType(), new ArrayList<DlpOperation>());
    }
    // Add the dlp operation in the specific Operation -> Type
    dlpQueueSorted.get(dlpOperation.getEntityType()).add(dlpOperation);
  }
}
