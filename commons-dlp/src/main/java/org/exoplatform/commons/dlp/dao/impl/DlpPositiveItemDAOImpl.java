package org.exoplatform.commons.dlp.dao.impl;

import org.exoplatform.commons.dlp.dao.DlpPositiveItemDAO;
import org.exoplatform.commons.dlp.domain.DlpPositiveItemEntity;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

public class DlpPositiveItemDAOImpl extends GenericDAOJPAImpl<DlpPositiveItemEntity, Long> implements DlpPositiveItemDAO {

    @Override
    public DlpPositiveItemEntity findDlpPositiveItemByReference(String itemReference) {
        TypedQuery<DlpPositiveItemEntity> query = getEntityManager()
                .createNamedQuery("DlpPositiveItemEntity.findDlpPositiveItemByReference", DlpPositiveItemEntity.class)
                .setParameter("itemReference", itemReference);

        try {
            return query.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    public List<DlpPositiveItemEntity> getDlpPositiveItems(int offset, int limit) {
        TypedQuery<DlpPositiveItemEntity> query = null;
        query = getEntityManager().createNamedQuery("DlpPositiveItemEntity.getDlpPositiveItems",
                DlpPositiveItemEntity.class);
        if (offset > 0) {
            query.setFirstResult(offset);
        }
        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return query.getResultList();
    }
}
