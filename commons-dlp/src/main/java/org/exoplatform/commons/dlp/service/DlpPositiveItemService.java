package org.exoplatform.commons.dlp.service;

import org.exoplatform.commons.dlp.domain.DlpPositiveItemEntity;
import org.exoplatform.commons.dlp.dto.DlpPositiveItem;
import java.util.List;

public interface DlpPositiveItemService {

    DlpPositiveItem getDlpPositiveItemById(Long itemId) throws Exception;

    List<DlpPositiveItem> getDlpPositivesItems(int offset, int limit) throws Exception;

    void addDlpPositiveItem(DlpPositiveItemEntity dlpPositiveItemEntity);
    
    void deleteDlpPositiveItem(Long itemId);

    void restoreDlpPositiveItem(Long itemId);

    DlpPositiveItem getDlpPositiveItemByReference(String itemReference) throws Exception;

    Long getDlpPositiveItemsCount();
}
