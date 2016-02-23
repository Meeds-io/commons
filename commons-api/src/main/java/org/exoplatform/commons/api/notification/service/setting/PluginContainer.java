/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
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
package org.exoplatform.commons.api.notification.service.setting;

import java.util.List;

import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationChildPlugin;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Aug 18, 2013  
 */
public interface PluginContainer {

  /**
   * Gets plugin by NotificationKey
   * @param key
   * @return
   */
  BaseNotificationPlugin getPlugin(PluginKey key);
  
  /**
   * Gets all children plugin keys in plugin
   *
   * @param parentKey the parent plugin key
   * @return The list of children plugins
   */
  List<PluginKey> getChildPluginKeys(PluginKey parentKey);
  

  /**
   * Register plugin in Container
   * @param plugin
   */
  void addPlugin(BaseNotificationPlugin plugin);
  
  /**
   * Register child plugin in Container
   * @param childPlugin
   */
  void addChildPlugin(AbstractNotificationChildPlugin childPlugin);

  /**
   * Removes plugin in Container
   * @param key
   * @return
   */
  boolean remove(PluginKey key);
  
  /**
   * @return
   */
  List<String> getDefaultActivePlugins();
}
