package org.exoplatform.commons.dlp.service.impl;

import org.exoplatform.commons.dlp.dao.DlpFileDAO;
import org.exoplatform.commons.dlp.domain.DlpFileEntity;
import org.exoplatform.commons.dlp.service.DlpFileService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.util.List;

public class DlpFileServiceImpl implements DlpFileService {

    private final DlpFileDAO dlpFileDAO;

    private static final Log LOG =
            ExoLogger.getLogger(DlpFileServiceImpl.class);

    public DlpFileServiceImpl(DlpFileDAO dlpFileDAO) {
        this.dlpFileDAO = dlpFileDAO;
    }


    @Override
    public List<DlpFileEntity> getDlpFiles() {
        return dlpFileDAO.findAll();
    }

    @Override
    public void addDlpFile(DlpFileEntity dlpFile) {
        dlpFileDAO.create(dlpFile);
    }

    @Override
    public void deleteDlpFile(Long fileId) {
        DlpFileEntity dlpFileEntity = dlpFileDAO.find(fileId);
        if (dlpFileEntity != null) {
            dlpFileDAO.delete(dlpFileEntity);
        } else {
            LOG.warn("The DlpFile's {} not found.", fileId);
        }
    }

    @Override
    public DlpFileEntity getDlpFileByUUID(String uuid) {
        return dlpFileDAO.findFileByUUID(uuid);
    }
}
