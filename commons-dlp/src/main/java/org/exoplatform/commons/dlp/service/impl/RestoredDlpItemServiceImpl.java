package org.exoplatform.commons.dlp.service.impl;

import org.exoplatform.commons.dlp.dao.RestoredDlpItemDAO;
import org.exoplatform.commons.dlp.domain.RestoredDlpItemEntity;
import org.exoplatform.commons.dlp.dto.RestoredDlpItem;
import org.exoplatform.commons.dlp.service.RestoredDlpItemService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.util.Calendar;

public class RestoredDlpItemServiceImpl implements RestoredDlpItemService {

  private static final Log LOG = ExoLogger.getLogger(RestoredDlpItemServiceImpl.class);

  private final RestoredDlpItemDAO restoredDlpItemDAO;

  public RestoredDlpItemServiceImpl(RestoredDlpItemDAO restoredDlpItemDAO) {
    this.restoredDlpItemDAO = restoredDlpItemDAO;
  }

  @Override
  public void addRestoredDlpItem(RestoredDlpItemEntity restoredDlpItemEntity) {
    RestoredDlpItemEntity existedEntity = restoredDlpItemDAO.findRestoredDlpItemByReference(restoredDlpItemEntity.getReference());
    if (existedEntity != null) {
      existedEntity.setDetectionDate(Calendar.getInstance());
      restoredDlpItemDAO.update(existedEntity);
    } else {
      restoredDlpItemDAO.create(restoredDlpItemEntity);
    }
  }

  @Override
  public void deleteRestoredDlpItem(Long itemId) {
    RestoredDlpItemEntity restoredDlpItemEntity = restoredDlpItemDAO.find(itemId);
    if (restoredDlpItemEntity != null) {
      restoredDlpItemDAO.delete(restoredDlpItemEntity);
    }
  }

  @Override
  public RestoredDlpItem getRestoredDlpItemByReference(String itemReference) {
    RestoredDlpItemEntity restoredDlpItemEntity = restoredDlpItemDAO.findRestoredDlpItemByReference(itemReference);
    try {
      return fillRestoredDlpItemFromEntity(restoredDlpItemEntity);
    } catch (Exception e) {
      LOG.warn("The restored Dlp Item's {} not found.", itemReference);
      return null;
    }
  }

  private RestoredDlpItem fillRestoredDlpItemFromEntity(RestoredDlpItemEntity restoredDlpItemEntity) throws Exception {
    RestoredDlpItem restoredDlpItem = new RestoredDlpItem();
    restoredDlpItem.setId(restoredDlpItemEntity.getId());
    restoredDlpItem.setReference(restoredDlpItemEntity.getReference());
    restoredDlpItem.setDetectionDate(restoredDlpItemEntity.getDetectionDate().getTimeInMillis());
    return restoredDlpItem;
  }
}
