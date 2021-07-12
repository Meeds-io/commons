package org.exoplatform.mfa.rest.entities;

import java.util.HashMap;
import java.util.Map;

public class RevocationRequestEntity {

  private long id;

  private String username;

  private String type;

  public RevocationRequestEntity(long id, String username, String type) {
    this.id = id;
    this.username = username;
    this.type = type;
  }

  public Map<String, Object> asMap() {
    Map<String, Object> result = new HashMap<>();
    result.put("id",id);
    result.put("username",username);
    result.put("type",type);
    return result;

  }
}
