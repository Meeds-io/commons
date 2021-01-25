package org.exoplatform.commons.dlp.dto;

public class DlpPositiveItem {

    private Long   id;

    private String title;

    private String author;
    
    private String authorDisplayName;
    
    private String type;

    private String keywords;

    private String reference;

    private String   itemUrl;

    private Long   detectionDate;

    public DlpPositiveItem() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getReference() { return reference; }

    public void setReference(String reference) { this.reference = reference; }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getItemUrl() {
        return itemUrl;
    }

    public void setItemUrl(String itemUrl) {
        this.itemUrl = itemUrl;
    }

    public String getAuthorDisplayName() {
        return authorDisplayName;
    }

    public void setAuthorDisplayName(String authorDisplayName) {
        this.authorDisplayName = authorDisplayName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public Long getDetectionDate() {
        return detectionDate;
    }

    public void setDetectionDate(Long detectionDate) {
        this.detectionDate = detectionDate;
    }
}
