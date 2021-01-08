package org.exoplatform.commons.dlp.service;

import org.exoplatform.commons.dlp.domain.DlpPositiveItemEntity;

import java.util.List;

public interface DlpPositiveItemService {

    List<DlpPositiveItemEntity> getDlpPositivesItems();

    void addDlpPositiveItem(DlpPositiveItemEntity dlpPositiveItemEntity);
    
    void deleteDlpPositiveItem(Long itemId);
    
    DlpPositiveItemEntity getDlpPositiveItemByReference(String itemReference);
}
