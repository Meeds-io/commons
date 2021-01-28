package org.exoplatform.commons.dlp.service.impl;

import org.exoplatform.commons.dlp.connector.DlpServiceConnector;
import org.exoplatform.commons.dlp.dao.DlpPositiveItemDAO;
import org.exoplatform.commons.dlp.domain.DlpPositiveItemEntity;
import org.exoplatform.commons.dlp.dto.DlpPositiveItem;
import org.exoplatform.commons.dlp.processor.DlpOperationProcessor;
import org.exoplatform.commons.dlp.service.DlpPositiveItemService;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;

import java.util.ArrayList;
import java.util.List;

public class DlpPositiveItemServiceImpl implements DlpPositiveItemService {

  private static final Log LOG =
      ExoLogger.getLogger(DlpPositiveItemServiceImpl.class);

  private final DlpPositiveItemDAO dlpPositiveItemDAO;

  private DlpOperationProcessor dlpOperationProcessor;

  private ListenerService listenerService;

  private OrganizationService organizationService;

  public DlpPositiveItemServiceImpl(DlpPositiveItemDAO dlpPositiveItemDAO,
                                    OrganizationService organizationService,
                                    ListenerService listenerService,
                                    DlpOperationProcessor dlpOperationProcessor) {
    this.dlpPositiveItemDAO = dlpPositiveItemDAO;
    this.organizationService = organizationService;
    this.listenerService = listenerService;
    this.dlpOperationProcessor = dlpOperationProcessor;
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
      long startTime = System.currentTimeMillis();
      dlpPositiveItemDAO.delete(dlpPositiveItemEntity);
      try {
        listenerService.broadcast(new Event("dlp.listener.event.delete.document", null, dlpPositiveItemEntity.getReference()));
      } catch (Exception e) {
        LOG.error("Error when broadcasting delete file event", e);
      }
      long endTime = System.currentTimeMillis();
      long totalTime = endTime - startTime;
      LOG.info("service={} operation={} parameters=\"fileName:{}\" status=ok " + "duration_ms={}",
               DlpOperationProcessor.DLP_FEATURE,
               "dlpPositiveItemDeletion",
               dlpPositiveItemEntity.getTitle(),
               totalTime);
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

  private List<DlpPositiveItem> fillDlpPositiveItemsFromEntities(List<DlpPositiveItemEntity> dlpPositiveItemEntities) throws
                                                                                                                      Exception {
    List<DlpPositiveItem> dlpPositiveItems = new ArrayList<>();
    for (DlpPositiveItemEntity dlpPositiveItemEntity : dlpPositiveItemEntities) {
      DlpPositiveItem dlpPositiveItem = fillDlpPositiveItemFromEntity(dlpPositiveItemEntity);
      dlpPositiveItems.add(dlpPositiveItem);
    }
    return dlpPositiveItems;
  }

  private DlpPositiveItem fillDlpPositiveItemFromEntity(DlpPositiveItemEntity dlpPositiveItemEntity) throws Exception {
    DlpPositiveItem dlpPositiveItem = new DlpPositiveItem();
    DlpServiceConnector dlpServiceConnector = (DlpServiceConnector) dlpOperationProcessor.getConnectors().get(dlpPositiveItemEntity.getType());
    dlpPositiveItem.setId(dlpPositiveItemEntity.getId());
    dlpPositiveItem.setType(dlpPositiveItemEntity.getType());
    dlpPositiveItem.setKeywords(dlpPositiveItemEntity.getKeywords());
    dlpPositiveItem.setAuthor(dlpPositiveItemEntity.getAuthor());
    User user = organizationService.getUserHandler()
                                     .findUserByName(dlpPositiveItemEntity.getAuthor());
    if (user!=null) {
      dlpPositiveItem.setAuthorDisplayName(user.getDisplayName());
      dlpPositiveItem.setIsExternal(dlpServiceConnector.checkExternal(dlpPositiveItemEntity.getAuthor()));
    }
    dlpPositiveItem.setTitle(dlpPositiveItemEntity.getTitle());
    dlpPositiveItem.setItemUrl(dlpServiceConnector.getItemUrl(dlpPositiveItemEntity.getReference()));
    dlpPositiveItem.setDetectionDate(dlpPositiveItemEntity.getDetectionDate().getTimeInMillis());
    return dlpPositiveItem;
  }
}
