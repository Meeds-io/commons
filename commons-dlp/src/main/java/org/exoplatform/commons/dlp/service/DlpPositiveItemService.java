package org.exoplatform.commons.dlp.service;

import org.exoplatform.commons.dlp.domain.DlpPositiveItemEntity;

import java.util.List;

public interface DlpPositiveItemService {

    List<DlpPositiveItemEntity> getDlpItems();

    void addDlpItem(DlpPositiveItemEntity dlpPositiveItemEntity);
    
    void deleteDlpItem(Long itemId);
    
    DlpPositiveItemEntity getDlpItemByUUID(String fileId);
}
