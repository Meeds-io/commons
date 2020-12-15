package org.exoplatform.commons.dlp.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.exoplatform.commons.api.persistence.ExoEntity;

@Entity
@ExoEntity
@Table(name = "DLP_QUEUE")
@NamedQueries({
    @NamedQuery(name = "DlpOperation.deleteAllDlpOperationsHavingIdLessThanOrEqual",
        query = "DELETE FROM DlpOperation q WHERE q.id <= :id"),
    @NamedQuery(name = "DlpOperation.findAllFirst",
        query = "SELECT q FROM DlpOperation q ORDER BY q.id")
})
public class DlpOperation implements Serializable {

  @Id
  @SequenceGenerator(name="SEQ_DLP_QUEUE_ID", sequenceName="SEQ_DLP_QUEUE_ID")
  @GeneratedValue(strategy=GenerationType.AUTO, generator="SEQ_DLP_QUEUE_ID")
  @Column(name = "OPERATION_ID")
  private Long id;

  @Column(name = "ENTITY_TYPE")
  private String entityType;

  @Column(name = "ENTITY_ID")
  private String entityId;

  //The timestamp is managed by the DB and cannot be set or get
  //It's only use for querying timestamp based dlp operations
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "OPERATION_TIMESTAMP", insertable = false, updatable = false)
  private Date timestamp;

  public DlpOperation() {
  }

  public DlpOperation(String entityId, String entityType) {
    this.entityId = entityId;
    this.entityType = entityType;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getEntityId() {
    return entityId;
  }

  public void setEntityId(String entityId) {
    this.entityId = entityId;
  }

  public String getEntityType() {
    return entityType;
  }

  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DlpOperation that = (DlpOperation) o;

    if (entityId != null ? !entityId.equals(that.entityId) : that.entityId != null) return false;
    if (entityType != null ? !entityType.equals(that.entityType) : that.entityType != null) return false;
    if (id != null ? !id.equals(that.id) : that.id != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (entityType != null ? entityType.hashCode() : 0);
    result = 31 * result + (entityId != null ? entityId.hashCode() : 0);
    return result;
  }
}

