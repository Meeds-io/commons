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
        @NamedQuery(name = "DlpPositiveItemEntity.getDlpItemByUUID",
                query = "SELECT q FROM DlpPositiveItemEntity q WHERE q.uuid = :uuid")
})

public class DlpPositiveItemEntity {

    @Id
    @SequenceGenerator(name = "SEQ_ITEM_ID", sequenceName = "SEQ_ITEM_ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_ITEM_ID")
    @Column(name = "ITEM_ID")
    private String id;

    @Column(name = "ITEM_UUID")
    private String uuid;
    
    @Column(name = "KEYWORDS")
    private String Keywords;

    @Column(name = "DETECTION_DATE")
    private Calendar detectionDate;

    public void setId(String id) { this.id = id; }

    public String getId() {
        return id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getKeywords() {
        return Keywords;
    }

    public void setKeywords(String keywords) {
        Keywords = keywords;
    }

    public Calendar getDetectionDate() {
        return detectionDate;
    }

    public void setDetectionDate(Calendar detectionDate) {
        this.detectionDate = detectionDate;
    }
}
