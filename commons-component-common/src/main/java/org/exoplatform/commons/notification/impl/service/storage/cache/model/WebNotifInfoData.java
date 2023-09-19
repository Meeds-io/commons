/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.exoplatform.commons.notification.impl.service.storage.cache.model;

import org.exoplatform.commons.api.notification.model.ChannelKey;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;

import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EqualsAndHashCode
public class WebNotifInfoData implements Serializable {
  private static final long serialVersionUID = 1L;

  private String              id;
  private PluginKey           key;                                           
  private String              from           = "";
  private String              to;
  private int                 order;
  private Map<String, String> ownerParameter = new HashMap<>();
  private List<String>        sendToUserIds  = new ArrayList<>();
  private String[]            sendToDaily;
  private String[]            sendToWeekly;
  private long                lastModifiedDate;
  private String              title = "";
  private ChannelKey          channelKey;
  private boolean             onPopOver;
  private boolean             read;

  public WebNotifInfoData(NotificationInfo notificationInfo) {
    this.id = notificationInfo.getId();
    this.key = notificationInfo.getKey();                                          
    this.from = notificationInfo.getFrom();
    this.to = notificationInfo.getTo();
    this.order = notificationInfo.getOrder();
    this.ownerParameter = notificationInfo.getOwnerParameter();
    this.sendToUserIds = notificationInfo.getSendToUserIds();
    this.sendToDaily = notificationInfo.getSendToDaily();
    this.sendToWeekly = notificationInfo.getSendToWeekly();
    this.lastModifiedDate = notificationInfo.getLastModifiedDate();
    this.title = notificationInfo.getTitle();
    this.channelKey = notificationInfo.getChannelKey();
    this.onPopOver = notificationInfo.isOnPopOver();
    this.read = notificationInfo.isRead();
  }
  
  public NotificationInfo build() {
    NotificationInfo notificationInfo = new NotificationInfo();
    notificationInfo.setId(this.id);
    notificationInfo.key(this.key);                                          
    notificationInfo.setFrom(this.from);
    notificationInfo.setTo(this.to);
    notificationInfo.setOrder(this.order);
    notificationInfo.setOwnerParameter(this.ownerParameter);
    notificationInfo.to(this.sendToUserIds);
    notificationInfo.setSendToDaily(this.sendToDaily);
    notificationInfo.setSendToWeekly(this.sendToWeekly);
    notificationInfo.setLastModifiedDate(this.lastModifiedDate);
    notificationInfo.setTitle(this.title);
    notificationInfo.setChannelKey(this.channelKey);
    notificationInfo.setOnPopOver(onPopOver);
    notificationInfo.setRead(read);
    return notificationInfo;
  }

  public String getTo() {
    return to;
  }

  public WebNotifInfoData updateRead(boolean isRead) {
    this.read = isRead;
    return this;
  }

  public WebNotifInfoData updateShowPopover(boolean isShow) {
    this.onPopOver = isShow;
    return this;
  }

}
