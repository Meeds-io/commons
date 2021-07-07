package org.exoplatform.mfa.storage.dao;

import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.mfa.storage.entity.RevocationRequestEntity;

import javax.persistence.TypedQuery;

public class RevocationRequestDAO extends GenericDAOJPAImpl<RevocationRequestEntity, Long> {

  public long countByUsernameAndType(String username, String type) {
    TypedQuery<Long> query = getEntityManager().createNamedQuery("RevocationRequestEntity.countByUsernameAndType", Long.class);
    query.setParameter("username", username);
    query.setParameter("type", type);
    return query.getSingleResult();
  }

}
