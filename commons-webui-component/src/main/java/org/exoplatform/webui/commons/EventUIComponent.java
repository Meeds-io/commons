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
package org.exoplatform.webui.commons;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * 15 Nov 2010  
 */
public class EventUIComponent {

  public enum EVENTTYPE {
    EVENT, URL
  };

  private String    Id;

  private String    eventName;

  private EVENTTYPE type = EVENTTYPE.EVENT;

  public EventUIComponent() {
    super();
  }
  
  public EventUIComponent(String id, String eventName) {
    super();
    Id = id;
    this.eventName = eventName;
  }

  public EventUIComponent(String id, String eventName, EVENTTYPE type) {
    super();
    Id = id;
    this.eventName = eventName;
    this.type = type;
  }

  public String getId() {
    return Id;
  }

  public void setId(String id) {
    Id = id;
  }

  public String getEventName() {
    return eventName;
  }

  public void setEventName(String eventName) {
    this.eventName = eventName;
  }

  public EVENTTYPE getType() {
    return type;
  }

  public void setType(EVENTTYPE type) {
    this.type = type;
  }
}
