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
package org.exoplatform.commons.api.notification.model;

import java.util.*;
import java.util.Map.Entry;

/**
 * User setting notification
 */
public class UserSetting {
  public static String EMAIL_CHANNEL = "MAIL_CHANNEL";

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

  private List<String> channelActives;

  private Calendar     lastUpdateTime;

  private String       userId;

  private Map<String, List<String>> channelPlugins;

  private List<String> dailyPlugins;

  private List<String> weeklyPlugins;

  private long lastReadDate = 0;
  
  private boolean isEnabled = true;

  public UserSetting() {
    this.channelActives = new ArrayList<String>();
    this.channelPlugins = new HashMap<String, List<String>>();
    //
    this.dailyPlugins = new ArrayList<String>();
    this.weeklyPlugins = new ArrayList<String>();
    this.lastUpdateTime = Calendar.getInstance();
    this.isEnabled = true;
  }
  
  public static UserSetting getInstance() {
    return new UserSetting();
  }

  /**
   * Get the last read date
   * @return
   */
  public long getLastReadDate() {
    return lastReadDate;
  }

  /**
   * Set last read date
   * @param lastReadDate
   */
  public void setLastReadDate(long lastReadDate) {
    this.lastReadDate = lastReadDate;
  }

  /**
   * @return
   */
  public List<String> getChannelActives() {
    return channelActives == null ? channelActives = new ArrayList<String>() : channelActives;
  }
  
  /**
   * @return
   */
  public boolean isChannelActive(String channelId) {
    return channelActives != null && channelActives.contains(channelId);
  }

  /**
   * @param channelId
   */
  public void setChannelActive(String channelId) {
    if (!isChannelActive(channelId)) {
      if (channelActives == null) {
        channelActives = new ArrayList<String>();
        channelActives.add(channelId);
      } else if (!channelActives.contains(channelId)) {
        channelActives.add(channelId);
      }
    }
  }

  /**
   * @param channelId
   */
  public void removeChannelActive(String channelId) {
    if(isChannelActive(channelId)) {
      channelActives.remove(channelId);
    }
  }
  
  /**
   * @param channelActives
   */
  public void setChannelActives(List<String> channelActives) {
    this.channelActives = channelActives;
  }

  /**
   * @return the userId
   */
  public String getUserId() {
    return userId;
  }

  /**
   * @param userId the userId to set
   */
  public UserSetting setUserId(String userId) {
    this.userId = userId;
    return this;
  }

  /**
   * @return the lastUpdateTime
   */
  public Calendar getLastUpdateTime() {
    return lastUpdateTime;
  }

  /**
   * @param lastUpdateTime the lastUpdateTime to set
   */
  public UserSetting setLastUpdateTime(Calendar lastUpdateTime) {
    this.lastUpdateTime = lastUpdateTime;
    return this;
  }

  /**
   * @param channelPlugins the channelPlugins to set
   */
  public void setAllChannelPlugins(Map<String, List<String>> channelPlugins) {
    this.channelPlugins = channelPlugins;
  }
  
  /**
   * @return the all channelPlugins
   */
  public Map<String, List<String>> getAllChannelPlugins() {
    return channelPlugins;
  }

  /**
   * @return the channelPlugins
   */
  public List<String> getPlugins(String channelId) {
    List<String> channelPlugins = this.channelPlugins.get(channelId);
    if (channelPlugins == null) {
      channelPlugins = new ArrayList<String>();
      this.channelPlugins.put(channelId, channelPlugins);
    }
    return channelPlugins;
  }

  /**
   * @param channelId
   * @param pluginIds
   */
  public void setChannelPlugins(String channelId, List<String> pluginIds) {
    this.channelPlugins.put(channelId, pluginIds);
  }

  /**
   * Add the pluginId by channel
   * @param channelId
   * @param pluginId
   */
  public void addChannelPlugin(String channelId, String pluginId) {
    List<String> plugins = getPlugins(channelId);
    if (!plugins.contains(pluginId)) {
      plugins.add(pluginId);
    }
  }
  
  /**
   * remove the pluginId on channel
   * @param channelId
   * @param pluginId
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
   * Checks the user's setting for the channel and the plugin
   * if it's active, it's instantly including the email channel.
   * 
   * @param pluginId
   * @return
   */
  public boolean isActive(String channelId, String pluginId) {
    return (getPlugins(channelId).contains(pluginId));
  }
  
  /**
   * @param pluginId
   * @return
   */
  public boolean isInDaily(String pluginId) {
    return (dailyPlugins.contains(pluginId)) ? true : false;
  }

  /**
   * @param pluginId
   * @return
   */
  public boolean isInWeekly(String pluginId) {
    return (weeklyPlugins.contains(pluginId)) ? true : false;
  }

  private void addProperty(List<String> providers, String pluginId) {
    if (providers.contains(pluginId) == false) {
      providers.add(pluginId);
    }
  }
  
  @Override
  public UserSetting clone() {
    UserSetting setting = getInstance();
    setting.setChannelActives(new ArrayList<String>(channelActives));
    setting.setDailyPlugins(new ArrayList<String>(dailyPlugins));
    setting.setWeeklyPlugins(new ArrayList<String>(weeklyPlugins));
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
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
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
    return Objects.hash(channelActives, lastUpdateTime, userId, channelPlugins, dailyPlugins, weeklyPlugins, lastReadDate, isEnabled);
  }
}
