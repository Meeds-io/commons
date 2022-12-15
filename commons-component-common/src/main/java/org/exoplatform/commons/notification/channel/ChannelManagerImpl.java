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
package org.exoplatform.commons.notification.channel;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.picocontainer.Startable;

import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.channel.ChannelManager;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.channel.template.TemplateProvider;
import org.exoplatform.commons.api.notification.lifecycle.AbstractNotificationLifecycle;
import org.exoplatform.commons.api.notification.model.ChannelKey;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.notification.template.TemplateUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import groovy.text.GStringTemplateEngine;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform thanhvc@exoplatform.com
 * Dec 12, 2014
 */
public class ChannelManagerImpl implements ChannelManager, Startable {
  /** logger */
  private static final Log                       LOG             = ExoLogger.getLogger(ChannelManagerImpl.class);

  /** Defines the channels: key = channelId and Channel */
  private final Map<ChannelKey, AbstractChannel> allChannels     = new HashMap<>();

  private List<TemplateProvider>                 providers       = new LinkedList<>();

  private GStringTemplateEngine                  gTemplateEngine = new GStringTemplateEngine();

  @Override
  public void register(AbstractChannel channel) {
    allChannels.put(ChannelKey.key(channel.getId()), channel);
  }

  @Override
  public void unregister(AbstractChannel channel) {
    allChannels.remove(channel.getKey());
  }

  @Override
  public void registerTemplateProvider(TemplateProvider provider) {
    providers.add(provider);
  }

  @Override
  public void registerOverrideTemplateProvider(TemplateProvider provider) {
    providers.add(provider);
    AbstractChannel channel = allChannels.get(provider.getChannelKey());
    if (channel != null) {
      channel.registerTemplateProvider(addTemplateEngine(provider));
    } else {
      LOG.warn("Register the new TemplateProvider is unsucessful");
    }
  }

  @Override
  public AbstractChannel getChannel(ChannelKey key) {
    return allChannels.get(key);
  }

  @Override
  public AbstractNotificationLifecycle getLifecycle(ChannelKey key) {
    return getChannel(key).getLifecycle();
  }

  @Override
  public int sizeChannels() {
    return allChannels.size();
  }

  @Override
  public List<AbstractChannel> getDefaultChannels() {
    return getChannels().stream()
                        .filter(AbstractChannel::isDefaultChannel)
                        .collect(Collectors.toList());
  }

  @Override
  public List<AbstractChannel> getSpecificChannels() {
    return getChannels().stream()
        .filter(channel -> !channel.isDefaultChannel())
        .collect(Collectors.toList());
  }

  @Override
  public List<AbstractChannel> getChannels() {
    List<AbstractChannel> channels = new LinkedList<>();
    AbstractChannel emailChannel = getChannel(ChannelKey.key(MailChannel.ID));
    if (emailChannel != null) {
      channels.add(emailChannel);
    }
    for (AbstractChannel channel : this.allChannels.values()) {
      if (MailChannel.ID.equals(channel.getId())) {
        continue;
      }
      channels.add(channel);
    }
    return Collections.unmodifiableList(channels);
  }

  @Override
  public void start() {
    for (TemplateProvider provider : providers) {
      AbstractChannel channel = allChannels.get(provider.getChannelKey());
      if (channel != null) {
        channel.registerTemplateProvider(addTemplateEngine(provider));
      } else {
        LOG.warn("Register the new TemplateProvider is unsucessful");
      }
    }
  }

  @Override
  public void stop() {
    // Nothing to do
  }

  /**
   * Makes the template file to Groovy template. It's ready to generate the
   * message. Why needs to do this way? - TemplateBuilder and TemplateProvider
   * can be extensible or override. So only final list to make the template. -
   * Don't waste time to build the template for useless template.
   * 
   * @param  provider
   * @return
   */
  private TemplateProvider addTemplateEngine(TemplateProvider provider) {
    Map<PluginKey, AbstractTemplateBuilder> builders = provider.getTemplateBuilder();
    Map<PluginKey, String> configs = provider.getTemplateFilePathConfigs();
    for (Entry<PluginKey, String> entry : configs.entrySet()) {
      PluginKey pluginKey = entry.getKey();
      String templatePath = entry.getValue();
      if (templatePath != null && templatePath.length() > 0) {
        try {
          AbstractTemplateBuilder builder = builders.get(pluginKey);
          if (builder != null) {
            String template = TemplateUtils.loadGroovyTemplate(templatePath);
            builder.setTemplateEngine(gTemplateEngine.createTemplate(template));
          }
        } catch (Exception e) {
          LOG.warn("Failed to build groovy template engine for: " + pluginKey.getId() + " templatePath: " + templatePath, e);
        }
      }
    }
    return provider;
  }
}
