package org.exoplatform.commons.dlp.dao.impl;

import org.exoplatform.commons.dlp.dao.DlpPositiveItemDAO;
import org.exoplatform.commons.dlp.domain.DlpPositiveItemEntity;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

public class DlpPositiveItemDAOImpl extends GenericDAOJPAImpl<DlpPositiveItemEntity, Long> implements DlpPositiveItemDAO {

    @Override
    public DlpPositiveItemEntity findItemByUUID(String uuid) {
        TypedQuery<DlpPositiveItemEntity> query = getEntityManager()
                .createNamedQuery("DlpPositiveItemEntity.getDlpItemByUUID", DlpPositiveItemEntity.class)
                .setParameter("uuid", uuid);

        try {
            return query.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }
}
