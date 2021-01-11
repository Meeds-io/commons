package org.exoplatform.commons.dlp.service;

import org.exoplatform.commons.dlp.domain.DlpPositiveItemEntity;
import org.exoplatform.commons.dlp.dto.DlpPositiveItemList;

public interface DlpPositiveItemService {

    DlpPositiveItemList getDlpPositivesItems(int offset, int limit) throws Exception;

    void addDlpPositiveItem(DlpPositiveItemEntity dlpPositiveItemEntity);
    
    void deleteDlpPositiveItem(Long itemId);
    
    DlpPositiveItemEntity getDlpPositiveItemByReference(String itemReference);
}
