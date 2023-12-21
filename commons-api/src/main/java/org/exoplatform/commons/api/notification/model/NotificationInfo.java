/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Affero General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.api.notification.model;

import java.io.Serializable;
import java.util.*;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class NotificationInfo implements Serializable, Cloneable {

  private static final long   serialVersionUID      = 689981271399587101L;

  public static final String  PREFIX_ID             = "NotificationMessage";

  public static final String  FOR_ALL_USER          = "&forAllUser";

  public static final String  FOR_ALL_INTERNAL_USER = "&forAllInternalUsers";

  private String              id;

  private PluginKey           key;                                               //

  private String              from;

  private String              to;

  private int                 order;

  private Map<String, String> ownerParameter        = new HashMap<>();

  private List<String>        sendToUserIds         = new ArrayList<>();

  private List<String>        excludedUsersIds      = new ArrayList<>();

  // list users send by frequency
  private String[]            sendToDaily           = new String[] { "" };

  private String[]            sendToWeekly          = new String[] { "" };

  private long                lastModifiedDate      = System.currentTimeMillis();

  private String              title                 = "";

  private ChannelKey          channelKey;

  private Calendar            dateCreated           = Calendar.getInstance();

  private boolean             isOnPopOver           = true;

  private boolean             read                  = false;

  private boolean             resetOnBadge          = false;

  private boolean             isUpdate              = false;

  private long                spaceId               = 0l;

  @Getter
  @Setter
  private boolean             mutable               = false;

  @Getter
  @Setter
  private boolean             spaceMuted            = false;

  public static NotificationInfo instance() {
    return new NotificationInfo();
  }

  public String getId() {
    return id;
  }

  public NotificationInfo setId(String id) {
    this.id = id;
    return this;
  }

  public NotificationInfo setSendAll(boolean isSendAll) {
    if (isSendAll) {
      setSendToDaily(new String[] { FOR_ALL_USER });
      setSendToWeekly(new String[] { FOR_ALL_USER });
    } else {
      removeOnSendToDaily(FOR_ALL_USER);
      removeOnSendToWeekly(FOR_ALL_USER);
    }
    return this;
  }

  public boolean isSendAll() {
    return ArrayUtils.contains(sendToDaily, FOR_ALL_USER) || ArrayUtils.contains(sendToWeekly, FOR_ALL_USER);
  }

  public boolean isSendAllInternals() {
    return ArrayUtils.contains(sendToDaily, FOR_ALL_INTERNAL_USER) || ArrayUtils.contains(sendToWeekly, FOR_ALL_INTERNAL_USER);
  }

  public NotificationInfo setSendAllInternals(boolean isSendAllInternals) {
    if (isSendAllInternals) {
      setSendToDaily(new String[] { FOR_ALL_INTERNAL_USER });
      setSendToWeekly(new String[] { FOR_ALL_INTERNAL_USER });
    } else {
      removeOnSendToDaily(FOR_ALL_INTERNAL_USER);
      removeOnSendToWeekly(FOR_ALL_INTERNAL_USER);
    }
    return this;
  }

  public boolean isUpdate() {
    return isUpdate;
  }

  public NotificationInfo setUpdate(boolean isUpdate) {
    this.isUpdate = isUpdate;
    return this;
  }

  public PluginKey getKey() {
    return this.key;
  }

  public NotificationInfo key(PluginKey key) {
    this.key = key;
    return this;
  }

  public NotificationInfo key(String id) {
    this.key = PluginKey.key(id);
    return this;
  }

  public String getFrom() {
    return from;
  }

  public NotificationInfo setFrom(String from) {
    this.from = from;
    return this;
  }

  /**
   * Gets the title of the notification
   * 
   * @return
   */
  public String getTitle() {
    return title;
  }

  /**
   * Sets the title of the notification
   * 
   * @param title
   */
  public NotificationInfo setTitle(String title) {
    this.title = title;
    return this;
  }

  /**
   * Gets the channel key of the notification
   * 
   * @return
   */
  public ChannelKey getChannelKey() {
    return channelKey;
  }

  /**
   * Sets the channel of the notification
   * 
   * @param channelKey
   */
  public void setChannelKey(ChannelKey channelKey) {
    this.channelKey = channelKey;
  }

  /**
   * @return the to
   */
  public String getTo() {
    return to;
  }

  /**
   * @param to the to to set
   */
  public NotificationInfo setTo(String to) {
    this.to = to;
    return this;
  }

  /**
   * @return the order
   */
  public int getOrder() {
    return order;
  }

  /**
   * @param order the order to set
   */
  public NotificationInfo setOrder(int order) {
    this.order = order;
    return this;
  }

  public List<String> getSendToUserIds() {
    return sendToUserIds;
  }

  public NotificationInfo to(List<String> sendToUserIds) {
    this.sendToUserIds = sendToUserIds;
    return this;
  }

  public List<String> getExcludedUsersIds() {
    return excludedUsersIds;
  }

  public NotificationInfo to(String sendToUserId) {
    this.sendToUserIds.add(sendToUserId);
    if (to == null) {
      to = sendToUserId;
    }
    return this;
  }

  public NotificationInfo exclude(List<String> excludedUsersIds) {
    this.excludedUsersIds = excludedUsersIds;
    return this;
  }

  public NotificationInfo exclude(String excludedUserId) {
    this.excludedUsersIds.add(excludedUserId);
    return this;
  }

  public boolean isExcluded(String userId) {
    return excludedUsersIds.contains(userId);
  }

  /**
   * @return the ownerParameter
   */
  public Map<String, String> getOwnerParameter() {
    return ownerParameter;
  }

  /**
   * @return the value of ownerParameter
   */
  public String getValueOwnerParameter(String key) {
    return ownerParameter.get(key);
  }

  public long getSpaceId() {
    if (spaceId == 0) {
      String spaceIdString = getValueOwnerParameter("spaceId");
      if (StringUtils.isNotBlank(spaceIdString) && StringUtils.isNumeric(spaceIdString)) {
        spaceId = Long.parseLong(spaceIdString);
      }
    }
    return spaceId;
  }

  public NotificationInfo setSpaceId(long spaceId) {
    this.spaceId = spaceId;
    getOwnerParameter().put("spaceId", String.valueOf(spaceId));
    return this;
  }

  /**
   * @return the array ownerParameter
   */
  public String[] getArrayOwnerParameter() {
    if (ownerParameter.size() == 0)
      return new String[] { "" };

    String[] strs = ownerParameter.toString().split(", ");
    strs[0] = strs[0].replace("{", "");
    strs[strs.length - 1] = strs[strs.length - 1].replace("}", "");
    return strs;
  }

  /**
   * @param ownerParameter the ownerParameter to set
   */
  public NotificationInfo setOwnerParameter(Map<String, String> ownerParameter) {
    this.ownerParameter = ownerParameter;
    return this;
  }

  /**
   * @param key the ownerParameter key to set
   * @param value the ownerParameter value to set
   */
  public NotificationInfo with(String key, String value) {
    this.ownerParameter.put(key, value);
    return this;
  }

  public NotificationInfo end() {
    return this;
  }

  /**
   * Get the last modified date
   * 
   * @return
   */
  public long getLastModifiedDate() {
    return lastModifiedDate;
  }

  /**
   * @param lastModifiedDate
   */
  public NotificationInfo setLastModifiedDate(Calendar lastModifiedDate) {
    this.lastModifiedDate = lastModifiedDate.getTimeInMillis();
    return this;
  }

  /**
   * @param lastModifiedDate
   */
  public NotificationInfo setLastModifiedDate(long lastModifiedDate) {
    this.lastModifiedDate = lastModifiedDate;
    return this;
  }

  public Calendar getDateCreated() {
    return dateCreated;
  }

  public NotificationInfo setDateCreated(Calendar dateCreated) {
    this.dateCreated = dateCreated;
    return this;
  }

  public boolean isOnPopOver() {
    return isOnPopOver;
  }

  public NotificationInfo setOnPopOver(boolean isOnPopOver) {
    this.isOnPopOver = isOnPopOver;
    return this;
  }

  /**
   * @return the sendToDaily
   */
  public String[] getSendToDaily() {
    return sendToDaily;
  }

  /**
   * @param userIds the list userIds to set for sendToDaily
   */
  public NotificationInfo setSendToDaily(String[] userIds) {
    this.sendToDaily = userIds;
    return this;
  }

  /**
   * @param userId the userId to add into sendToDaily
   */
  public NotificationInfo setSendToDaily(String userId) {
    this.sendToDaily = addMoreItemInArray(sendToDaily, userId);
    return this;
  }

  /**
   * @param userId the userId to remove into sendToDaily
   */
  public NotificationInfo removeOnSendToDaily(String userId) {
    this.sendToDaily = removeItemInArray(sendToDaily, userId);
    return this;
  }

  /**
   * @return the sendToWeekly
   */
  public String[] getSendToWeekly() {
    return sendToWeekly;
  }

  /**
   * @param userIds the list userIds to set for sendToWeekly
   */
  public NotificationInfo setSendToWeekly(String[] userIds) {
    this.sendToWeekly = userIds;
    return this;
  }

  /**
   * @param userId the userId to add into sendToWeekly
   */
  public NotificationInfo setSendToWeekly(String userId) {
    this.sendToWeekly = addMoreItemInArray(sendToWeekly, userId);
    return this;
  }

  /**
   * @param userId the userId to remove into sendToWeekly
   */
  public NotificationInfo removeOnSendToWeekly(String userId) {
    this.sendToWeekly = removeItemInArray(sendToWeekly, userId);
    return this;
  }

  public boolean isResetOnBadge() {
    return resetOnBadge;
  }

  public NotificationInfo setResetOnBadge(boolean resetOnBadge) {
    this.resetOnBadge = resetOnBadge;
    return this;
  }

  public boolean isRead() {
    return read;
  }

  public NotificationInfo setRead(boolean read) {
    this.read = read;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder("{");
    buffer.append("providerType: ")
          .append(key)
          .append(", sendToDaily: ")
          .append(Arrays.asList(sendToDaily).toString())
          .append(", sendToWeekly: ")
          .append(Arrays.asList(sendToWeekly).toString());
    return buffer.toString();
  }

  private String[] addMoreItemInArray(String[] src, String element) {
    if (element == null || element.trim().length() == 0) {
      return src;
    }
    //
    List<String> where = new ArrayList<>();
    if (src.length > 1 || (src.length == 1 && !src[0].equals(""))) {
      where = new ArrayList<>(Arrays.asList(src));
    }
    if (!where.contains(element)) {
      where.add(element);
      return where.toArray(new String[where.size()]);
    }
    return src;
  }

  private String[] removeItemInArray(String[] src, String element) {
    if (element == null || element.trim().length() == 0) {
      return src;
    }
    //
    List<String> where = new ArrayList<>();
    if (src.length > 1 || (src.length == 1 && !src[0].equals(""))) {
      where = new ArrayList<>(Arrays.asList(src));
    }
    if (where.contains(element)) {
      where.remove(element);
      return where.toArray(new String[where.size()]);
    }
    return src;
  }

  @Override
  public NotificationInfo clone() { // NOSONAR
    return clone(false);
  }

  public NotificationInfo clone(boolean isNew) {
    return new NotificationInfo(isNew ? null : id,
                                key,
                                from,
                                to,
                                order,
                                new HashMap<>(ownerParameter),
                                sendToUserIds == null ? null : new ArrayList<>(sendToUserIds),
                                excludedUsersIds == null ? null : new ArrayList<>(excludedUsersIds),
                                arrayCopy(sendToDaily),
                                arrayCopy(sendToWeekly),
                                lastModifiedDate,
                                title,
                                channelKey,
                                dateCreated,
                                isNew || isOnPopOver,
                                !isNew && read,
                                !isNew && resetOnBadge,
                                !isNew,
                                spaceId,
                                mutable,
                                spaceMuted);
  }

  /**
   * Copy the array string, if source is empty or null, return array has one
   * empty item.
   * 
   * @param source
   * @return
   */
  private String[] arrayCopy(String[] source) {
    return (source != null && source.length > 0) ? Arrays.asList(source).toArray(new String[source.length]) : new String[] { "" };
  }

}
