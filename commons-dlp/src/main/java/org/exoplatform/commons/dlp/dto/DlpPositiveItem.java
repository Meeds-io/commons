package org.exoplatform.commons.dlp.dto;

public class DlpPositiveItem {

    private Long   id;

    private String title;

    private String author;
    
    private String type;

    private String keywords;

    private String refernece;

    private String downloadUrl;

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

    public String getRefernece() { return refernece; }

    public void setRefernece(String refernece) { this.refernece = refernece; }

    public String getDownloadUrl() { return downloadUrl; }

    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
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
