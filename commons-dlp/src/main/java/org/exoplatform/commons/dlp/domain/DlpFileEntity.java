package org.exoplatform.commons.dlp.domain;

import org.exoplatform.commons.api.persistence.ExoEntity;

import javax.persistence.*;
import java.util.Calendar;

/**
 * Entity for Dlp File. Created by The eXo Platform SAS Author :
 * eXoPlatform exo@exoplatform.com
 */
@Entity(name = "DlpFileEntity")
@ExoEntity
@Table(name = "DLP_FILES")
@NamedQueries({
        @NamedQuery(name = "DlpFileEntity.getDlpFileByUUID",
                query = "SELECT q FROM DlpFileEntity q WHERE q.uuid = :uuid")
})

public class DlpFileEntity {

    @Id
    @SequenceGenerator(name = "SEQ_FILE_ID", sequenceName = "SEQ_FILE_ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_FILE_ID")
    @Column(name = "FILE_ID")
    private String id;

    @Column(name = "FILE_UUID")
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
