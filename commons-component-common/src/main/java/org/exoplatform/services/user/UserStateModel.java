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
package org.exoplatform.services.user;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.Objects;


public class UserStateModel implements Serializable {
  private static final long serialVersionUID = -416451728545284362L;

  private String userId = null;
      
  private long lastActivity = 0;
  
  private String status = null;
  
  public UserStateModel() {}
  
  public UserStateModel(String userId, long lastActivity, String status) {
    this.userId = userId;
    this.lastActivity = lastActivity;
    this.status = status;
  }
  
  public String getUserId() {
    return this.userId;
  }
  
  public void setUserId(String userId) {
    this.userId = userId;
  }
  
  public long getLastActivity() {
    return this.lastActivity;
  }
  
  public void setLastActivity(long lastActivity) {
    this.lastActivity = lastActivity;
  }
  
  public String getStatus() {
    return this.status;
  }
  
  public void setStatus(String status) {
    this.status = status;
  }
  
  public UserStateModel clone() {
    return new UserStateModel(this.getUserId(), this.getLastActivity(), this.getStatus());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UserStateModel that = (UserStateModel) o;
    return lastActivity == that.lastActivity &&
            Objects.equals(userId, that.userId) &&
            Objects.equals(status, that.status);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, lastActivity, status);
  }
}
