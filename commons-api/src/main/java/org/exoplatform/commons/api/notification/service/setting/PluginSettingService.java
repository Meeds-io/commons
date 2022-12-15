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
package org.exoplatform.commons.api.notification.service.setting;

import java.util.Collections;
import java.util.List;

import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.model.ChannelKey;
import org.exoplatform.commons.api.notification.model.GroupProvider;
import org.exoplatform.commons.api.notification.model.PluginInfo;
import org.exoplatform.commons.api.notification.plugin.GroupProviderPlugin;
import org.exoplatform.commons.api.notification.plugin.config.PluginConfig;

public interface PluginSettingService {

  /**
   * Registers configuration of a plugin.
   * @param pluginConfig The plugin configuration.
   */
  void registerPluginConfig(PluginConfig pluginConfig);

  /**
   * Registers the plugin configuration of a group.
   * @param groupConfig The plugin configuration to be registered.
   */
  void registerGroupConfig(GroupProviderPlugin groupConfig);
  
  /**
   * Gets configuration of a plugin from its Id.
   * 
   * @param pluginId Id of the plugin.
   * @return The plugin configuration.
   */
  PluginConfig getPluginConfig(String pluginId);

  /**
   * Gets a list of groups containing plugins.
   * 
   * @return The list of groups.
   */
  List<GroupProvider> getGroupPlugins();

  /**
   * Saves a plugin.
   * 
   * @param pluginId Id of the saved plugin.
   * @param isActive If "true", the plugin is active. If "false", the plugin is inactive.
   */
  void saveActivePlugin(String channelId, String pluginId, boolean isActive);

  /**
   * Checks if a plugin is active or inactive.
   * 
   * @param channelId  {@link ChannelKey} identifier
   * @param pluginId Id of the plugin.
   * @return The returned value is "true" if the plugin is active or "false" if the plugin is inactive.
   */
  boolean isActive(String channelId, String pluginId);

  /**
   * Checks whether the channel is allowed on designated plugin or not
   * 
   * @param  channelId {@link ChannelKey} identifier
   * @param  pluginId  Id of the plugin.
   * @return           true if {@link AbstractChannel} is default channel or if
   *                   the channel is configured to be used in pluginId
   */
  boolean isAllowed(String channelId, String pluginId);

  /**
   * Gets all Ids of active plugins by channel.
   * 
   * @return Ids of the active plugins.
   */
  List<String> getActivePluginIds(String channelId);

  /**
   * @param  pluginId {@link PluginConfig} type
   * @return          {@link List} of active channels of Plugin
   */
  default List<String> getPluginChannels(String pluginId) {
    return Collections.emptyList();
  }

  /**
   * Gets information of all active plugins by channel.
   * 
   * @return Information of the active plugins.
   */
  List<PluginInfo> getActivePlugins(String channelId);
  /**
   * Gets information of all plugins.
   * 
   * @return Information of the plugins.
   */
  List<PluginInfo> getAllPlugins();

}
