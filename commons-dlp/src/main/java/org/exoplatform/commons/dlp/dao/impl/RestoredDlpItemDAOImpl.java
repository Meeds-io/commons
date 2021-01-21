package org.exoplatform.commons.dlp.dao.impl;

import org.exoplatform.commons.dlp.dao.RestoredDlpItemDAO;
import org.exoplatform.commons.dlp.domain.RestoredDlpItemEntity;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

public class RestoredDlpItemDAOImpl extends GenericDAOJPAImpl<RestoredDlpItemEntity, Long> implements RestoredDlpItemDAO {

  @Override
  public RestoredDlpItemEntity findRestoredDlpItemByReference(String itemReference) {
    TypedQuery<RestoredDlpItemEntity> query = getEntityManager()
        .createNamedQuery("RestoredDlpItemEntity.findRestoredDlpItemByReference", RestoredDlpItemEntity.class)
        .setParameter("itemReference", itemReference);

    try {
      return query.getSingleResult();
    } catch (NoResultException ex) {
      return null;
    }
  }
}
