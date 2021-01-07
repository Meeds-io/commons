package org.exoplatform.commons.dlp.dao.impl;

import org.exoplatform.commons.dlp.dao.DlpFileDAO;
import org.exoplatform.commons.dlp.domain.DlpFileEntity;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

public class DlpFileDAOImpl extends GenericDAOJPAImpl<DlpFileEntity, Long> implements DlpFileDAO {

    @Override
    public DlpFileEntity findFileByUUID(String uuid) {
        TypedQuery<DlpFileEntity> query = getEntityManager()
                .createNamedQuery("DlpFileEntity.getDlpFileByUUID", DlpFileEntity.class)
                .setParameter("uuid", uuid);

        try {
            return query.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }
}
