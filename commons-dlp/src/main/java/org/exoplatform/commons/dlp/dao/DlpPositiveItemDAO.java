package org.exoplatform.commons.dlp.dao;

import org.exoplatform.commons.api.persistence.GenericDAO;
import org.exoplatform.commons.dlp.domain.DlpPositiveItemEntity;

import java.util.List;

public interface DlpPositiveItemDAO extends GenericDAO<DlpPositiveItemEntity, Long> {

    DlpPositiveItemEntity findDlpPositiveItemByReference(String itemReference);

    List<DlpPositiveItemEntity> getDlpPositiveItems(int offset, int limit);
}
