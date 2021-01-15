package org.exoplatform.commons.dlp.dao.impl;

import java.util.List;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.dlp.dao.DlpOperationDAO;
import org.exoplatform.commons.dlp.domain.DlpOperation;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;

public class DlpOperationDAOImpl extends GenericDAOJPAImpl<DlpOperation, Long> implements DlpOperationDAO {

  @Override
  @ExoTransactional
  public List<DlpOperation> findAllFirst(Integer maxResults) {
      return getEntityManager()
              .createNamedQuery("DlpOperation.findAllFirst", DlpOperation.class)
              .setMaxResults(maxResults)
              .getResultList();
  }
  
  @Override
  @ExoTransactional
  public List<DlpOperation> findByEntityIdAndType(String entityId, String entityType) {
      return getEntityManager()
              .createNamedQuery("DlpOperation.findByEntityIdAndType", DlpOperation.class)
              .setParameter("entityId", entityId)
              .setParameter("entityType", entityType)
              .getResultList();
  }
  
  @Override
  @ExoTransactional
  public int deleteByEntityId(String entityId) {
    return getEntityManager().createNamedQuery("DlpOperation.deleteByEntityId")
        .setParameter("entityId", entityId)
        .executeUpdate();
  }
  
}

