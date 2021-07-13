package org.exoplatform.mfa.storage.dao;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.mfa.storage.entity.RevocationRequestEntity;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

public class RevocationRequestDAO extends GenericDAOJPAImpl<RevocationRequestEntity, Long> {

  @ExoTransactional
  public long countByUsernameAndType(String username, String type) {
    TypedQuery<Long> query = getEntityManager().createNamedQuery("RevocationRequestEntity.countByUsernameAndType", Long.class);
    query.setParameter("username", username);
    query.setParameter("type", type);
    return query.getSingleResult();
  }

  @ExoTransactional
  public void deleteByUsernameAndType(String username, String type) {
    Query query = getEntityManager().createNamedQuery("RevocationRequestEntity.deleteByUsernameAndType");
    query.setParameter("username", username);
    query.setParameter("type", type);
    query.executeUpdate();
  }

  @ExoTransactional
  public void deleteById(Long id) {
    Query query = getEntityManager().createNamedQuery("RevocationRequestEntity.deleteById");
    query.setParameter("id",id);
    query.executeUpdate();
  }

  @ExoTransactional
  public RevocationRequestEntity findById(Long id) {
    TypedQuery<RevocationRequestEntity> query = getEntityManager().createNamedQuery("RevocationRequestEntity.findById",
                                                                                    RevocationRequestEntity.class);
    query.setParameter("id",id);
    return query.getSingleResult();
  }



}
