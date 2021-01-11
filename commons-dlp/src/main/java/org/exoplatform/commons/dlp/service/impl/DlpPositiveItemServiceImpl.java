package org.exoplatform.commons.dlp.service.impl;

import org.exoplatform.commons.dlp.dao.DlpPositiveItemDAO;
import org.exoplatform.commons.dlp.domain.DlpPositiveItemEntity;
import org.exoplatform.commons.dlp.dto.DlpPositiveItem;
import org.exoplatform.commons.dlp.dto.DlpPositiveItemList;
import org.exoplatform.commons.dlp.service.DlpPositiveItemService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DlpPositiveItemServiceImpl implements DlpPositiveItemService {

    private final DlpPositiveItemDAO dlpPositiveItemDAO;

    private static final Log LOG =
            ExoLogger.getLogger(DlpPositiveItemServiceImpl.class);

    public DlpPositiveItemServiceImpl(DlpPositiveItemDAO dlpPositiveItemDAO) {
        this.dlpPositiveItemDAO = dlpPositiveItemDAO;
    }


    @Override
    public DlpPositiveItemList getDlpPositivesItems(int offset, int limit) throws Exception {
        DlpPositiveItemList dlpPositiveItemList = new DlpPositiveItemList();
        List<DlpPositiveItemEntity> dlpPositiveItemEntities = dlpPositiveItemDAO.findAll();
        if (limit <= 0) {
            limit = dlpPositiveItemEntities.size();
        }
        dlpPositiveItemEntities = dlpPositiveItemEntities.stream().skip(offset).limit(limit).collect(Collectors.toList());
        List<DlpPositiveItem> dlpPositiveItems = fillDlpPositiveItemFromEntities(dlpPositiveItemEntities);
        dlpPositiveItemList.setDlpPositiveItems(dlpPositiveItems);
        dlpPositiveItemList.setSize(dlpPositiveItemEntities.size());
        dlpPositiveItemList.setLimit(limit);
        return dlpPositiveItemList;
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


    private List<DlpPositiveItem> fillDlpPositiveItemFromEntities(List<DlpPositiveItemEntity> dlpPositiveItemEntities) throws Exception {
        List<DlpPositiveItem> dlpPositiveItems = new ArrayList<>();
        for (DlpPositiveItemEntity dlpPositiveItemEntity : dlpPositiveItemEntities) {
            DlpPositiveItem dlpPositiveItem = new DlpPositiveItem();
            dlpPositiveItem.setId(dlpPositiveItemEntity.getId());
            dlpPositiveItem.setType(dlpPositiveItemEntity.getType());
            dlpPositiveItem.setKeywords(dlpPositiveItemEntity.getKeywords());
            dlpPositiveItem.setAuthor(dlpPositiveItemEntity.getAuthor());
            IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
            Identity userIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, dlpPositiveItemEntity.getAuthor());
            dlpPositiveItem.setAuthorFullName(userIdentity.getProfile().getFullName());
            dlpPositiveItem.setTitle(dlpPositiveItemEntity.getTitle());
            dlpPositiveItem.setDetectionDate(dlpPositiveItemEntity.getDetectionDate().getTimeInMillis());
            dlpPositiveItems.add(dlpPositiveItem);
        }
        return dlpPositiveItems;
    }
}
