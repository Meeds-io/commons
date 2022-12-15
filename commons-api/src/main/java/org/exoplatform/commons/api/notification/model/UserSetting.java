/*
 * Copyright (C) 2003-2019 eXo Platform SAS.
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

import java.util.*;
import java.util.Map.Entry;

/**
 * User setting notification
 */
public class UserSetting {
  public static final String EMAIL_CHANNEL = "MAIL_CHANNEL";

  public enum FREQUENCY {
    INSTANTLY, DAILY, WEEKLY;

    public static FREQUENCY getFrequecy(String name) {
      for (int i = 0; i < values().length; ++i) {
        if (values()[i].name().equalsIgnoreCase(name)) {
          return values()[i];
        }
      }
      return null;
    }
  }

  private Set<String>               channelActives;

  private Calendar                  lastUpdateTime;

  private String                    userId;

  private Map<String, List<String>> channelPlugins;

  private List<String>              dailyPlugins;

  private List<String>              weeklyPlugins;

  private long                      lastReadDate = 0;

  private boolean                   isEnabled    = true;

  public UserSetting() {
    this.channelActives = new HashSet<>();
    this.channelPlugins = new HashMap<>();
    //
    this.dailyPlugins = new ArrayList<>();
    this.weeklyPlugins = new ArrayList<>();
    this.lastUpdateTime = Calendar.getInstance();
    this.isEnabled = true;
  }

  public static UserSetting getInstance() {
    return new UserSetting();
  }

  public long getLastReadDate() {
    return lastReadDate;
  }

  public void setLastReadDate(long lastReadDate) {
    this.lastReadDate = lastReadDate;
  }

  public Set<String> getChannelActives() {
    if (channelActives == null) {
      channelActives = new HashSet<>();
    }
    return channelActives;
  }

  public boolean isChannelGloballyActive(String channelId) {
    return isEnabled && channelActives != null && channelActives.contains(channelId);
  }

  public boolean isChannelActive(String channelId, String pluginId) {
    return isEnabled && channelPlugins != null && channelPlugins.containsKey(channelId) && channelPlugins.get(channelId).contains(pluginId);
  }

  public void setChannelActive(String channelId) {
    if (channelActives == null) {
      channelActives = new HashSet<>();
    }
    channelActives.add(channelId);
  }

  public void removeChannelActive(String channelId) {
    channelActives.remove(channelId);
  }

  public void setChannelActives(Set<String> channelActives) {
    this.channelActives = channelActives;
  }

  public String getUserId() {
    return userId;
  }

  public UserSetting setUserId(String userId) {
    this.userId = userId;
    return this;
  }

  public Calendar getLastUpdateTime() {
    return lastUpdateTime;
  }

  public UserSetting setLastUpdateTime(Calendar lastUpdateTime) {
    this.lastUpdateTime = lastUpdateTime;
    return this;
  }

  public void setAllChannelPlugins(Map<String, List<String>> channelPlugins) {
    this.channelPlugins = channelPlugins;
  }

  public Map<String, List<String>> getAllChannelPlugins() {
    return channelPlugins;
  }

  public List<String> getPlugins(String channelId) {
    return channelPlugins.computeIfAbsent(channelId, key -> new ArrayList<>());
  }

  public void setChannelPlugins(String channelId, List<String> pluginIds) {
    this.channelPlugins.put(channelId, pluginIds);
  }

  /**
   * Add the pluginId by channel
   * 
   * @param channelId Channel identifier
   * @param  pluginId Plugin identifier
   */
  public void addChannelPlugin(String channelId, String pluginId) {
    List<String> plugins = getPlugins(channelId);
    if (!plugins.contains(pluginId)) {
      plugins.add(pluginId);
    }
  }

  /**
   * remove the pluginId on channel
   * 
   * @param channelId Channel identifier
   * @param pluginId  Plugin identifier
   */
  public void removeChannelPlugin(String channelId, String pluginId) {
    List<String> plugins = getPlugins(channelId);
    if (plugins.contains(pluginId)) {
      plugins.remove(pluginId);
    }
  }

  /**
   * @return the dailyPlugins
   */
  public List<String> getDailyPlugins() {
    return dailyPlugins;
  }

  /**
   * @param dailyPlugins the dailyPlugins to set
   */
  public void setDailyPlugins(List<String> dailyPlugins) {
    this.dailyPlugins = dailyPlugins;
  }

  /**
   * @return the weeklyPlugins
   */
  public List<String> getWeeklyPlugins() {
    return weeklyPlugins;
  }

  /**
   * @param weeklyPlugins the weeklyPlugins to set
   */
  public void setWeeklyPlugins(List<String> weeklyPlugins) {
    this.weeklyPlugins = weeklyPlugins;
  }

  /**
   * @param pluginId the provider's id to add
   * @param frequencyType {@link FREQUENCY} of notification plugin
   */
  public void addPlugin(String pluginId, FREQUENCY frequencyType) {
    if (frequencyType.equals(FREQUENCY.DAILY)) {
      addProperty(dailyPlugins, pluginId);
      weeklyPlugins.remove(pluginId);
    } else if (frequencyType.equals(FREQUENCY.WEEKLY)) {
      addProperty(weeklyPlugins, pluginId);
      dailyPlugins.remove(pluginId);
    } else if (frequencyType.equals(FREQUENCY.INSTANTLY)) {
      addChannelPlugin(EMAIL_CHANNEL, pluginId);
    }
  }

  public void removePlugin(String pluginId, FREQUENCY frequencyType) {
    if (frequencyType.equals(FREQUENCY.DAILY)) {
      weeklyPlugins.remove(pluginId);
    } else if (frequencyType.equals(FREQUENCY.WEEKLY)) {
      dailyPlugins.remove(pluginId);
    } else if (frequencyType.equals(FREQUENCY.INSTANTLY)) {
      removeChannelPlugin(EMAIL_CHANNEL, pluginId);
    }
  }

  /**
   * Checks the user's setting for the channel and the plugin if it's active,
   * it's instantly including the email channel.
   * 
   * @param channelId Channel identifier
   * @param  pluginId Plugin identifier
   * @return true if active, else false
   */
  public boolean isActive(String channelId, String pluginId) {
    return isEnabled && getPlugins(channelId).contains(pluginId);
  }

  public boolean isInDaily(String pluginId) {
    return isEnabled && dailyPlugins.contains(pluginId);
  }

  public boolean isInWeekly(String pluginId) {
    return isEnabled && weeklyPlugins.contains(pluginId);
  }

  private void addProperty(List<String> providers, String pluginId) {
    if (!providers.contains(pluginId)) {
      providers.add(pluginId);
    }
  }

  @Override
  public UserSetting clone() { // NOSONAR
    UserSetting setting = getInstance();
    setting.setChannelActives(new HashSet<>(channelActives));
    setting.setDailyPlugins(new ArrayList<>(dailyPlugins));
    setting.setWeeklyPlugins(new ArrayList<>(weeklyPlugins));
    //
    for (Entry<String, List<String>> entry : channelPlugins.entrySet()) {
      setting.getPlugins(entry.getKey()).addAll(entry.getValue());
    }
    setting.setUserId(userId);
    return setting;
  }

  @Override
  public String toString() {
    return "UserSetting : {userId : " + userId + "}";
  }

  public boolean isEnabled() {
    return isEnabled;
  }

  public void setEnabled(boolean isEnabled) {
    this.isEnabled = isEnabled;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    UserSetting that = (UserSetting) o;
    return lastReadDate == that.lastReadDate &&
        isEnabled == that.isEnabled &&
        Objects.equals(channelActives, that.channelActives) &&
        Objects.equals(lastUpdateTime, that.lastUpdateTime) &&
        Objects.equals(userId, that.userId) &&
        Objects.equals(channelPlugins, that.channelPlugins) &&
        Objects.equals(dailyPlugins, that.dailyPlugins) &&
        Objects.equals(weeklyPlugins, that.weeklyPlugins);
  }

  @Override
  public int hashCode() {
    return Objects.hash(channelActives,
                        lastUpdateTime,
                        userId,
                        channelPlugins,
                        dailyPlugins,
                        weeklyPlugins,
                        lastReadDate,
                        isEnabled);
  }
}
