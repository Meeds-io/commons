package org.exoplatform.commons.dlp.dto;

import java.util.List;

public class DlpPositiveItemList {

    private List<DlpPositiveItem> dlpPositiveItems;

    private int offset;

    private int limit;

    private int size;

    public List<DlpPositiveItem> getDlpPositiveItems() {
        return dlpPositiveItems;
    }

    public void setDlpPositiveItems(List<DlpPositiveItem> dlpPositiveItems) {
        this.dlpPositiveItems = dlpPositiveItems;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
