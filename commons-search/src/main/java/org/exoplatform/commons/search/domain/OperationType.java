package org.exoplatform.commons.search.domain;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 * exo@exoplatform.com
 * 9/15/15
 */
public enum OperationType {
  INIT("I"),
  CREATE("C"),
  UPDATE("U"),
  DELETE("D");

  private final String operationId;

  OperationType(String operationId) {
    this.operationId = operationId;
  }

  public String getOperationId() {
    return operationId;
  }

  public static OperationType getById(String operation) {
    for (OperationType type : OperationType.values()) {
      if (type.getOperationId().equals(operation)) {
        return type;
      }
    }
    return null;
  }
}
