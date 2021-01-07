package org.exoplatform.commons.dlp.dao;

import org.exoplatform.commons.api.persistence.GenericDAO;
import org.exoplatform.commons.dlp.domain.DlpFileEntity;

public interface DlpFileDAO extends GenericDAO<DlpFileEntity, Long> {

    DlpFileEntity findByFileId(String fileId);

}
