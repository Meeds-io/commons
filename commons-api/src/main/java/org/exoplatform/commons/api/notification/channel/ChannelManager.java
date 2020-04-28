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
package org.exoplatform.commons.api.notification.channel;

import java.util.List;

import org.exoplatform.commons.api.notification.channel.template.TemplateProvider;
import org.exoplatform.commons.api.notification.lifecycle.AbstractNotificationLifecycle;
import org.exoplatform.commons.api.notification.model.ChannelKey;

public interface ChannelManager {

  /**
   * Register new channel
   * 
   * @param channel
   */
  void register(AbstractChannel channel);

  /**
   * Unregister the specified channel
   * 
   * @param channel
   */
  void unregister(AbstractChannel channel);

  /**
   * Register the Template provider
   * 
   * @param provider
   */
  void registerTemplateProvider(TemplateProvider provider);
  
  /**
   * Register and override the Template provider
   * 
   * @param provider
   */
  void registerOverrideTemplateProvider(TemplateProvider provider);

  /**
   * Gets the channel by the specified key
   * @param key the channel key
   * @return
   */
  AbstractChannel getChannel(ChannelKey key);

  /**
   * Gets list of the channels
   * @return
   */
  List<AbstractChannel> getChannels();

  /**
   * Gets the lifecycle by the ChannelKey
   * @param key
   * @return
   */
  AbstractNotificationLifecycle getLifecycle(ChannelKey key);

  /**
   * Gets size of channels has been registered
   * 
   * @return
   */
  int sizeChannels();
}
