package org.exoplatform.commons.dlp.service.impl;

import org.exoplatform.commons.dlp.dao.DlpPositiveItemDAO;
import org.exoplatform.commons.dlp.domain.DlpPositiveItemEntity;
import org.exoplatform.commons.dlp.service.DlpPositiveItemService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.util.List;

public class DlpPositiveItemServiceImpl implements DlpPositiveItemService {

    private final DlpPositiveItemDAO dlpPositiveItemDAO;

    private static final Log LOG =
            ExoLogger.getLogger(DlpPositiveItemServiceImpl.class);

    public DlpPositiveItemServiceImpl(DlpPositiveItemDAO dlpPositiveItemDAO) {
        this.dlpPositiveItemDAO = dlpPositiveItemDAO;
    }


    @Override
    public List<DlpPositiveItemEntity> getDlpPositivesItems() {
        return dlpPositiveItemDAO.findAll();
    }

    @Override
    public void addDlpPositiveItem(DlpPositiveItemEntity dlpPositiveItemEntity) {
        dlpPositiveItemDAO.create(dlpPositiveItemEntity);
    }

    @Override
    public void deleteDlpPositiveItem(Long itemId) {
        DlpPositiveItemEntity dlpPositiveItemEntity = dlpPositiveItemDAO.find(itemId);
        if (dlpPositiveItemEntity != null) {
            dlpPositiveItemDAO.delete(dlpPositiveItemEntity);
        } else {
            LOG.warn("The DlpItem's {} not found.", itemId);
        }
    }

    @Override
    public DlpPositiveItemEntity getDlpPositiveItemByReference(String itemReference) {
        return dlpPositiveItemDAO.findDlpPositiveItemByReference(itemReference);
    }
}
