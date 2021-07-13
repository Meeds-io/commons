package org.exoplatform.mfa.storage.entity;

import org.exoplatform.commons.api.persistence.ExoEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity(name = "RevocationRequestEntity")
@ExoEntity
@Table(name = "MFA_REVOCATION_REQUEST")
@NamedQueries({
    @NamedQuery(name = "RevocationRequestEntity.countByUsernameAndType", query = "SELECT count(*) "
        + "FROM RevocationRequestEntity revocationRequest "
        + "WHERE revocationRequest.username = :username "
        + "AND revocationRequest.type = :type"),
    @NamedQuery(name = "RevocationRequestEntity.deleteByUsernameAndType", query = "DELETE "
        + "FROM RevocationRequestEntity revocationRequest "
        + "WHERE revocationRequest.username = :username "
        + "AND revocationRequest.type = :type"),
    @NamedQuery(name = "RevocationRequestEntity.deleteById", query = "DELETE "
        + "FROM RevocationRequestEntity revocationRequest "
        + "WHERE revocationRequest.id = :id")
    ,
    @NamedQuery(name = "RevocationRequestEntity.findById", query = "SELECT revocationRequest "
        + "FROM RevocationRequestEntity revocationRequest "
        + "WHERE revocationRequest.id = :id")
})
public class RevocationRequestEntity {

  @Id
  @SequenceGenerator(name = "SEQ_REVOCATION_REQUEST_ID", sequenceName = "SEQ_REVOCATION_REQUEST_ID")
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_REVOCATION_REQUEST_ID")
  @Column(name = "ID")
  private Long id;

  @Column(name = "USERNAME", nullable = false)
  private String username;

  @Column(name = "TYPE")
  private String type;

  public RevocationRequestEntity(Long id, String username, String type) {
    this.id = id;
    this.username = username;
    this.type = type;
  }

  public RevocationRequestEntity() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}
