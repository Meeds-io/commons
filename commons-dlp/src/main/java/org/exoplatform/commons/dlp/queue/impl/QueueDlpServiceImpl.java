package org.exoplatform.commons.dlp.queue.impl;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.commons.dlp.dao.DlpOperationDAO;
import org.exoplatform.commons.dlp.domain.DlpOperation;
import org.exoplatform.commons.dlp.queue.QueueDlpService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class QueueDlpServiceImpl implements QueueDlpService {

  private static final Log LOGGER = ExoLogger.getExoLogger(QueueDlpServiceImpl.class);

  private final DlpOperationDAO dlpOperationDAO;

  public QueueDlpServiceImpl(DlpOperationDAO dlpOperationDAO) {
    this.dlpOperationDAO = dlpOperationDAO;
  }

  @Override
  public void addToQueue(String connectorName, String id) {
    if (StringUtils.isBlank(id)) {
      throw new IllegalArgumentException("Entity id is null");
    }
    dlpOperationDAO.create(getDlpOperation(connectorName, id));
    LOGGER.info("Entity with id: " + id + " and connector: " + connectorName + " has been added to Dlp Queue"); 
  }
  
  private DlpOperation getDlpOperation (String connector, String entityId) {
    DlpOperation dlpOperation = new DlpOperation();
    dlpOperation.setEntityType(connector);
    if (entityId != null) {
      dlpOperation.setEntityId(entityId);
    }
    return dlpOperation;
  }
}
