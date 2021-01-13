package org.exoplatform.commons.dlp.service;

import org.exoplatform.commons.dlp.domain.DlpPositiveItemEntity;
import org.exoplatform.commons.dlp.dto.DlpPositiveItem;
import java.util.List;

public interface DlpPositiveItemService {

    List<DlpPositiveItem> getDlpPositivesItems(int offset, int limit) throws Exception;

    void addDlpPositiveItem(DlpPositiveItemEntity dlpPositiveItemEntity);
    
    void deleteDlpPositiveItem(Long itemId);
    
    DlpPositiveItemEntity getDlpPositiveItemByReference(String itemReference);

    Long getDlpPositiveItemsCount();
}
