package org.exoplatform.commons.dlp.dao;

import org.exoplatform.commons.api.persistence.GenericDAO;
import org.exoplatform.commons.dlp.domain.RestoredDlpItemEntity;

public interface RestoredDlpItemDAO extends GenericDAO<RestoredDlpItemEntity, Long> {

  RestoredDlpItemEntity findRestoredDlpItemByReference(String itemReference);
}
