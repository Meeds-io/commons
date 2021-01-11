package org.exoplatform.commons.dlp.dto;

import java.io.Serializable;
import java.util.List;

public class DlpPositiveItemList implements Serializable {

    private List<DlpPositiveItem> dlpPositiveItems;

    private long limit;

    private long size;

    public List<DlpPositiveItem> getDlpPositiveItems() {
        return dlpPositiveItems;
    }

    public void setDlpPositiveItems(List<DlpPositiveItem> dlpPositiveItems) {
        this.dlpPositiveItems = dlpPositiveItems;
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

}


