package org.exoplatform.commons.dlp.service.impl;

import org.exoplatform.commons.dlp.dao.DlpPositiveItemDAO;
import org.exoplatform.commons.dlp.domain.DlpPositiveItemEntity;
import org.exoplatform.commons.dlp.dto.DlpPositiveItem;
import org.exoplatform.commons.dlp.service.DlpPositiveItemService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;

import java.util.ArrayList;
import java.util.List;

public class DlpPositiveItemServiceImpl implements DlpPositiveItemService {

    private final DlpPositiveItemDAO dlpPositiveItemDAO;

    private static final Log LOG =
            ExoLogger.getLogger(DlpPositiveItemServiceImpl.class);

    public DlpPositiveItemServiceImpl(DlpPositiveItemDAO dlpPositiveItemDAO) {
        this.dlpPositiveItemDAO = dlpPositiveItemDAO;
    }


    @Override
    public List<DlpPositiveItem> getDlpPositivesItems(int offset, int limit) throws Exception {
        List<DlpPositiveItemEntity> dlpPositiveItemEntities = dlpPositiveItemDAO.getDlpPositiveItems(offset, limit);
        return fillDlpPositiveItemsFromEntities(dlpPositiveItemEntities);
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
    public DlpPositiveItem getDlpPositiveItemByReference(String itemReference) throws Exception {
        DlpPositiveItemEntity dlpPositiveItemEntity = dlpPositiveItemDAO.findDlpPositiveItemByReference(itemReference);
        return fillDlpPositiveItemFromEntity(dlpPositiveItemEntity);
    }

    @Override
    public Long getDlpPositiveItemsCount() {
        return dlpPositiveItemDAO.count();
    }


    private List<DlpPositiveItem> fillDlpPositiveItemsFromEntities(List<DlpPositiveItemEntity> dlpPositiveItemEntities) throws Exception {
        List<DlpPositiveItem> dlpPositiveItems = new ArrayList<>();
        for (DlpPositiveItemEntity dlpPositiveItemEntity : dlpPositiveItemEntities) {
            DlpPositiveItem dlpPositiveItem = fillDlpPositiveItemFromEntity(dlpPositiveItemEntity);
            dlpPositiveItems.add(dlpPositiveItem);
        }
        return dlpPositiveItems;
    }

    private DlpPositiveItem fillDlpPositiveItemFromEntity(DlpPositiveItemEntity dlpPositiveItemEntity) throws Exception {
        DlpPositiveItem dlpPositiveItem = new DlpPositiveItem();
        dlpPositiveItem.setId(dlpPositiveItemEntity.getId());
        dlpPositiveItem.setType(dlpPositiveItemEntity.getType());
        dlpPositiveItem.setKeywords(dlpPositiveItemEntity.getKeywords());
        dlpPositiveItem.setAuthor(dlpPositiveItemEntity.getAuthor());
        dlpPositiveItem.setTitle(dlpPositiveItemEntity.getTitle());
        dlpPositiveItem.setDetectionDate(dlpPositiveItemEntity.getDetectionDate().getTimeInMillis());
        return dlpPositiveItem;
    }
}
