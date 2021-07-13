package org.exoplatform.mfa.storage.dto;

import java.io.Serializable;

public class RevocationRequest implements Serializable {

  private static final long serialVersionUID = -1200232799295442412L;

  private Long id;

  private String user;
  private String type;

  public RevocationRequest(Long id, String user, String type) {
    this.id = id;
    this.user = user;
    this.type = type;
  }

  public RevocationRequest() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return "RevocationRequest{" +
        "id=" + id +
        ", user='" + user + '\'' +
        ", type='" + type + '\'' +
        '}';
  }
}
