package org.exoplatform.commons.dlp.service;

import org.exoplatform.commons.dlp.domain.RestoredDlpItemEntity;
import org.exoplatform.commons.dlp.dto.RestoredDlpItem;

public interface RestoredDlpItemService {

  void addRestoredDlpItem(RestoredDlpItemEntity restoredDlpItemEntity);

  void deleteRestoredDlpItem(Long itemId);

  RestoredDlpItem getRestoredDlpItemByReference(String itemReference) throws Exception;
}
