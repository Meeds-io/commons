package org.exoplatform.commons.dlp.processor.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.utils.CommonsUtils;

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

  private static final String   DLP_KEYWORDS = "exo.dlp.keywords";

  private static final Context  DLP_CONTEXT = Context.GLOBAL.id("DlpKeywords");

  private static final Scope    DLP_SCOPE = Scope.APPLICATION.id("DlpKeywords");

  private static final String   EXO_DLP_KEYWORDS = "exo:dlpKeywords";

  // Service
  private final DlpOperationDAO dlpOperationDAO;
  
  
  private Integer batchNumber = BATCH_NUMBER_DEFAULT;

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
  public String getKeywords() {
    SettingService settingService = CommonsUtils.getService(SettingService.class);
    SettingValue<?> settingValue = settingService.get(DLP_CONTEXT, DLP_SCOPE, EXO_DLP_KEYWORDS);
    return settingValue != null ? settingValue.getValue().toString() : System.getProperty(DLP_KEYWORDS);
  }

  @Override
  public void setKeywords(String keywords) {
    SettingService settingService = CommonsUtils.getService(SettingService.class);
    settingService.set(DLP_CONTEXT, DLP_SCOPE, EXO_DLP_KEYWORDS, SettingValue.create(keywords));
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
      int totalProcessed = 0;
      int offset = 0;
      long total = dlpOperationDAO.count();
      while (offset<total) {
        LOGGER.debug("Process Bulk DLP operation (offset : {}, total {})", offset, total);
        int stayingQueue=processBulk(offset);
        int processedOperations=batchNumber-stayingQueue;
        total=dlpOperationDAO.count();
        offset+=stayingQueue;
        totalProcessed=totalProcessed+processedOperations;
        LOGGER.debug("DLP Operation processed : {} elements removed from queue, {} staying in queue, {} total elements in queue"
                         + " after operation", processedOperations, stayingQueue, total);
      }
      LOGGER.info("Dlp Operation Processor proceed {} queue elements, {} elements staying in queue", totalProcessed,
                  offset);
    } catch (Exception e) {
      LOGGER.error("Error when processing bulk",e);
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
  
  public Integer getBatchNumber() {
    return batchNumber;
  }
  
  public void setBatchNumber(Integer batchNumber) {
    this.batchNumber = batchNumber;
  }
  
  private boolean isInterrupted() {
    if (Thread.currentThread().isInterrupted()) {
      LOGGER.debug("Thread running dlp queue processing has been interrupted. Please wait until the service exists cleanly...");
      this.interrupted = true;
    }
    return this.interrupted;
  }

  private int processBulk(int offset) {

    Map<String, List<DlpOperation>> dlpQueueSorted = new HashMap<>();

    // Get BATCH_NUMBER (default = 1000) first dlp operations
    List<DlpOperation> dlpOperations = dlpOperationDAO.findAllFirstWithOffset(offset,batchNumber);
    LOGGER.debug("{} DLP operations found",dlpOperations.size());

    // Get all Dlp operations and order them per type in map:
    // <Operation, <Type, List<DlpOperation>>>
    for (DlpOperation dlpOperation : dlpOperations) {
      putDlpOperationInMemoryQueue(dlpOperation, dlpQueueSorted);
    }
    
    int nbElementsStayingInQueue = dlpOperations.size();

    // Process dlp operation
    for (String entityType : dlpQueueSorted.keySet()) {
      DlpServiceConnector connector = getConnectors().get(entityType);
      List<DlpOperation> dlpOperationsList = dlpQueueSorted.get(entityType);
      if (dlpOperationsList == null || dlpOperationsList.isEmpty()) {
        continue;
      }
      LOGGER.debug("Will proceed to DLP operation list for type {}, size {}", entityType, dlpOperationsList.size());
      Iterator<DlpOperation> dlpOperationsIterator = dlpOperationsList.iterator();
      while (dlpOperationsIterator.hasNext()) {
        if (isInterrupted()) {
          throw new RuntimeException("Dlp queue processing interrupted");
        }
        DlpOperation dlpOperation = dlpOperationsIterator.next();
        LOGGER.debug("Call processItem for dlpOperation {}", dlpOperation.toString());
        if (connector.processItem(dlpOperation.getEntityId())) {
          dlpOperationDAO.delete(dlpOperation);
          nbElementsStayingInQueue--;
        }
      }
    }
    return nbElementsStayingInQueue;
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
