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

import java.util.ArrayList;
import java.util.List;


public class PluginInfo {

  private String       type;

  private int          order         = 0;

  private String       resourceBundleKey;
  
  private String       bundlePath;

  private List<String> channelActives;

  private List<String> defaultConfig = new ArrayList<String>();

  public PluginInfo() {
    channelActives = new ArrayList<String>();
  }

  /**
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * @param type the id to set
   */
  public PluginInfo setType(String type) {
    this.type = type;
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
  public PluginInfo setOrder(int order) {
    this.order = order;
    return this;
  }

  /**
   * @return the defaultConfig
   */
  public List<String> getDefaultConfig() {
    return defaultConfig;
  }

  /**
   * @param defaultConfig the defaultConfig to set
   */
  public void setDefaultConfig(List<String> defaultConfig) {
    this.defaultConfig = defaultConfig;
  }

  /**
   * @return the resourceBundleKey
   */
  public String getResourceBundleKey() {
    return resourceBundleKey;
  }

  /**
   * @param resourceBundleKey the resourceBundleKey to set
   */
  public PluginInfo setResourceBundleKey(String resourceBundleKey) {
    this.resourceBundleKey = resourceBundleKey;
    return this;
  }
  
  /**
   * @return the bundlePath
   */
  public String getBundlePath() {
    return bundlePath;
  }

  /**
   * @param bundlePath the bundlePath to set
   */
  public PluginInfo setBundlePath(String bundlePath) {
    this.bundlePath = bundlePath;
    return this;
  }

  /**
   * @return
   */
  public List<String> getAllChannelActive() {
    return channelActives;
  }

  /**
   * @return
   */
  public boolean isChannelActive(String channelId) {
    return channelActives.contains(channelId);
  }

  /**
   * @param channelId
   */
  public PluginInfo setChannelActive(String channelId) {
    channelActives.add(channelId);
    return this;
  }
  
  /**
   * @param channelActives
   */
  public PluginInfo setChannelActives(List<String> channelActives) {
    this.channelActives = new ArrayList<String>(channelActives);
    return this;
  }

  public PluginInfo end() {
    return this;
  }
  
  @Override
 public String toString() {
   return this.type;
 }
 
 @Override
 public boolean equals(final Object o) {
   if (this == o) {
     return true;
   }
   if (!(o instanceof PluginInfo)) {
     return false;
   }
   //
   PluginInfo that = (PluginInfo) o;
   if (type != null ? !type.equals(that.type) : that.type != null) {
     return false;
   }
   return true;
 }

 @Override
 public int hashCode() {
   int result = super.hashCode();
   result = 31 * result + (type != null ? type.hashCode() : 0);
   return result;
 }
}
