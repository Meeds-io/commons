package org.exoplatform.commons.dlp.dto;

public class RestoredDlpItem {

  private Long detectionDate;

  private Long id;

  private String reference;

  private String restoredUrl;

  public RestoredDlpItem() {
  }

  public Long getDetectionDate() {
    return detectionDate;
  }

  public void setDetectionDate(Long detectionDate) {
    this.detectionDate = detectionDate;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  public String getRestoredUrl() { return restoredUrl; }

  public void setRestoredUrl(String restoredUrl) { this.restoredUrl = restoredUrl; }
}
