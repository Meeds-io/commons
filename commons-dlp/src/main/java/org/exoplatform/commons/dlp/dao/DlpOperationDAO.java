package org.exoplatform.commons.dlp.dao;

import java.util.List;

import org.exoplatform.commons.api.persistence.GenericDAO;
import org.exoplatform.commons.dlp.domain.DlpOperation;

public interface DlpOperationDAO extends GenericDAO<DlpOperation, Long> {

  List<DlpOperation> findAllFirst(Integer maxResults);
  
  List<DlpOperation> findByEntityIdAndType(String entityId, String entityType);

}

