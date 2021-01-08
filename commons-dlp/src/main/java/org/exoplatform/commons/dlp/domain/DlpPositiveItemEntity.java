package org.exoplatform.commons.dlp.domain;

import org.exoplatform.commons.api.persistence.ExoEntity;

import javax.persistence.*;
import java.util.Calendar;

/**
 * Entity for Dlp positive item. Created by The eXo Platform SAS Author :
 * eXoPlatform exo@exoplatform.com
 */
@Entity(name = "DlpPositiveItemEntity")
@ExoEntity
@Table(name = "DLP_POSITIVE_ITEMS")
@NamedQueries({
        @NamedQuery(name = "DlpPositiveItemEntity.findDlpPositiveItemByReference",
                query = "SELECT q FROM DlpPositiveItemEntity q WHERE q.reference = :itemReference")
})

public class DlpPositiveItemEntity {

    @Id
    @SequenceGenerator(name = "SEQ_DLP_POSITIVE_ITEM_ID", sequenceName = "SEQ_DLP_POSITIVE_ITEM_ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_DLP_POSITIVE_ITEM_ID")
    @Column(name = "ITEM_ID")
    private String id;

    @Column(name = "ITEM_REFERENCE")
    private String reference;
    
    @Column(name = "ITEM_TYPE")
    private String type;

    @Column(name = "KEYWORDS")
    private String keywords;

    @Column(name = "DETECTION_DATE")
    private Calendar detectionDate;
    
    public String getId() {
        return id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
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

    public Calendar getDetectionDate() {
        return detectionDate;
    }

    public void setDetectionDate(Calendar detectionDate) {
        this.detectionDate = detectionDate;
    }
}
