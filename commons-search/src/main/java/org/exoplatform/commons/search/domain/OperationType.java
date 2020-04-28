/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
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
  DELETE("D"),
  DELETE_ALL("X"),
  REINDEX_ALL("R");

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
