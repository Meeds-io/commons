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
package org.exoplatform.settings.jpa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.channel.ChannelManager;
import org.exoplatform.commons.api.notification.model.ChannelKey;
import org.exoplatform.commons.api.notification.model.GroupProvider;
import org.exoplatform.commons.api.notification.model.OrderComparatorASC;
import org.exoplatform.commons.api.notification.model.PluginInfo;
import org.exoplatform.commons.api.notification.plugin.GroupProviderPlugin;
import org.exoplatform.commons.api.notification.plugin.config.GroupConfig;
import org.exoplatform.commons.api.notification.plugin.config.PluginConfig;
import org.exoplatform.commons.api.notification.service.setting.PluginSettingService;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.notification.impl.AbstractService;
import org.exoplatform.commons.utils.MailUtils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class JPAPluginSettingServiceImpl extends AbstractService implements PluginSettingService {

  public static final String         NOTIFICATION_CHANNEL_STATUS_MODIFIED = "notification.channel.status.modified";

  private static final Log           LOG                                  = ExoLogger.getLogger(JPAPluginSettingServiceImpl.class);

  private static final long          SPECIFIC_CHANNEL_VERSION             = 1;

  private static final Scope         NOTIFICATION_SCOPE                   = Scope.APPLICATION.id("NotificationSettings");

  private static final Context       CHANNEL_CONTEXT                      = Context.GLOBAL.id("NotificationChannelSetting");

  private static final Scope         CHANNEL_SCOPE                        = Scope.APPLICATION.id("NotificationChannelSetting");

  private static final String        NAME_SPACES                          = "exo:";

  private List<PluginConfig>         pluginConfigs                        = new ArrayList<>();

  private Map<String, GroupProvider> groupPluginMap                       = new ConcurrentHashMap<>();

  private SettingService             settingService;

  private ChannelManager             channelManager;

  private ListenerService            listenerService;

  public JPAPluginSettingServiceImpl(SettingService settingService,
                                     ChannelManager channelManager,
                                     ListenerService listenerService) {
    this.settingService = settingService;
    this.channelManager = channelManager;
    this.listenerService = listenerService;
  }

  @Override
  public void registerPluginConfig(PluginConfig pluginConfig) {
    pluginConfigs.add(pluginConfig);
    Collections.sort(pluginConfigs, new OrderComparatorASC());
    if (!pluginConfig.isChildPlugin()) {
      PluginInfo pluginInfo = new PluginInfo();
      pluginInfo.setType(pluginConfig.getPluginId())
                .setOrder(Integer.valueOf(pluginConfig.getOrder()))
                .setResourceBundleKey(pluginConfig.getResourceBundleKey())
                .setBundlePath(pluginConfig.getBundlePath())
                .setDefaultConfig(pluginConfig.getDefaultConfig());
      // all channels
      String groupId = pluginConfig.getGroupId();
      GroupConfig gConfig = pluginConfig.getGroupConfig();
      if (gConfig != null) {
        groupId = gConfig.getId();
      }
      //
      if (groupPluginMap.containsKey(groupId)) {
        groupPluginMap.get(groupId).addPluginInfo(pluginInfo);
      } else if (groupId != null && groupId.length() > 0) {
        GroupProvider groupProvider = new GroupProvider(groupId);
        groupProvider.addPluginInfo(pluginInfo);
        if (gConfig != null) {
          groupProvider.setOrder(Integer.valueOf(gConfig.getOrder()));
          groupProvider.setResourceBundleKey(gConfig.getResourceBundleKey());
        }
        groupPluginMap.put(groupId, groupProvider);
      }
    }
  }

  public void unregisterPluginConfig(PluginConfig pluginConfig) {
    pluginConfigs.remove(pluginConfig);
    List<PluginInfo> pluginInfos = groupPluginMap.get(pluginConfig.getGroupId()).getPluginInfos();
    PluginInfo pluginInfo = pluginInfos.stream()
                                       .filter(info -> StringUtils.equals(info.getType(), pluginConfig.getPluginId()))
                                       .findFirst()
                                       .orElse(null);
    if (pluginInfo != null) {
      pluginInfos.remove(pluginInfo);
    }
  }

  @Override
  public void registerGroupConfig(GroupProviderPlugin groupConfigPlg) {
    for (GroupConfig gconfig : groupConfigPlg.getGroupProviders()) {
      GroupProvider groupProvider = new GroupProvider(gconfig.getId());
      groupProvider.setOrder(Integer.valueOf(gconfig.getOrder()));
      groupProvider.setResourceBundleKey(gconfig.getResourceBundleKey());
      if (groupPluginMap.containsKey(gconfig.getId())) {
        groupProvider.setPluginInfos(groupPluginMap.get(gconfig.getId()).getPluginInfos());
      }
      groupPluginMap.put(gconfig.getId(), groupProvider);
    }
  }

  @Override
  public PluginConfig getPluginConfig(String pluginId) {
    for (PluginConfig pluginConfig : pluginConfigs) {
      if (pluginConfig.getPluginId().equals(pluginId)) {
        return pluginConfig;
      }
    }
    return null;
  }

  @Override
  public List<GroupProvider> getGroupPlugins() {
    List<GroupProvider> groupProviders = new ArrayList<>();
    for (GroupProvider groupPlugin : groupPluginMap.values()) {
      groupProviders.add(groupPlugin);
    }
    Collections.sort(groupProviders, new OrderComparatorASC());
    return groupProviders;
  }

  @Override
  public void saveActivePlugin(String channelId, String pluginId, boolean isActive) {
    List<String> current = getPluginChannels(pluginId);
    if (isActive) {
      if (!current.contains(channelId)) {
        current.add(channelId);
        saveActivePlugins(pluginId, current);
      }
    } else if (current.contains(channelId)) {
      current.remove(channelId);
      saveActivePlugins(pluginId, current);
    }
  }

  @Override
  public void saveChannelStatus(String channelId, boolean enable) {
    settingService.set(CHANNEL_CONTEXT, CHANNEL_SCOPE, channelId, SettingValue.create(String.valueOf(enable)));
    try {
      listenerService.broadcast(NOTIFICATION_CHANNEL_STATUS_MODIFIED, channelId, null);
    } catch (Exception e) {
      LOG.warn("Error broadcasting channel status modification", e);
    }
  }

  @Override
  public void saveEmailSender(String name, String email) {
    if (!NotificationUtils.isValidNotificationSenderName(name)) {
      throw new IllegalArgumentException("invalidSenderName");
    }
    if (!NotificationUtils.isValidEmailAddresses(email)) {
      throw new IllegalArgumentException("invalidSenderEmail");
    }
    settingService.set(org.exoplatform.commons.api.settings.data.Context.GLOBAL,
                       Scope.GLOBAL,
                       MailUtils.SENDER_NAME_PARAM,
                       SettingValue.create(name));
    settingService.set(org.exoplatform.commons.api.settings.data.Context.GLOBAL,
                       Scope.GLOBAL,
                       MailUtils.SENDER_EMAIL_PARAM,
                       SettingValue.create(email));
  }

  @Override
  public String getEmailSenderName() {
    return MailUtils.getSenderName();
  }

  @Override
  public String getEmailSenderEmail() {
    return MailUtils.getSenderEmail();
  }

  @Override
  public boolean isChannelActive(String channelId) {
    SettingValue<?> activeSettingValue = settingService.get(CHANNEL_CONTEXT, CHANNEL_SCOPE, channelId);
    return activeSettingValue == null || activeSettingValue.getValue() == null || StringUtils.equals(activeSettingValue.getValue().toString(), "true");
  }

  @Override
  public boolean isActive(String channelId, String pluginId) {
    return isAllowed(channelId, pluginId) && getPluginChannels(pluginId).contains(channelId);
  }

  @Override
  public boolean isAllowed(String channelId, String pluginId) {
    AbstractChannel channel = channelManager.getChannel(ChannelKey.key(channelId));
    PluginConfig pluginConfig = getPluginConfig(pluginId);
    return channel != null && pluginConfig != null
        && isChannelActive(channelId)
        && (channel.isDefaultChannel() || pluginConfig.getAdditionalChannels().contains(channelId));
  }

  @Override
  public List<String> getPluginChannels(String pluginId) {
    List<String> defaultChannels = new ArrayList<>();
    for (AbstractChannel channel : channelManager.getDefaultChannels()) {
      defaultChannels.add(channel.getId());
    }
    List<AbstractChannel> specificChannels = channelManager.getSpecificChannels();
    for (AbstractChannel channel : specificChannels) {
      defaultChannels.remove(channel.getId());
    }

    List<String> activeChannels;
    SettingValue<?> pluginActiveChannelsSetting = settingService.get(Context.GLOBAL,
                                                                     Scope.GLOBAL.id(null),
                                                                     (NAME_SPACES + pluginId));
    if (pluginActiveChannelsSetting == null || pluginActiveChannelsSetting.getValue() == null) {
      activeChannels = defaultChannels;
    } else {
      String values = String.valueOf(pluginActiveChannelsSetting.getValue());
      activeChannels = NotificationUtils.stringToList(getValues(values));
    }
    PluginConfig pluginConfig = pluginConfigs.stream()
                                             .filter(config -> StringUtils.equals(config.getPluginId(), pluginId))
                                             .findFirst()
                                             .orElse(null);
    if (pluginConfig != null) {
      List<String> additionalChannels = pluginConfig.getAdditionalChannels();
      if (CollectionUtils.isNotEmpty(additionalChannels)) {
        defaultChannels.addAll(additionalChannels);
        if (!isPluginSettingVersion(pluginId, SPECIFIC_CHANNEL_VERSION)) {
          activeChannels.addAll(additionalChannels);
        }
      }
    }

    @SuppressWarnings("unchecked")
    List<String> allowedDefaultChannels = (List<String>) CollectionUtils.intersection(defaultChannels, activeChannels);
    Set<String> pluginChannels = new HashSet<>(allowedDefaultChannels);
    return new ArrayList<>(pluginChannels);
  }

  @Override
  public List<String> getActivePluginIds(String channelId) {
    Set<String> activePluginIds = new HashSet<>();
    Iterator<PluginConfig> pluginsIterator = pluginConfigs.iterator();
    while (pluginsIterator.hasNext()) {
      PluginConfig pluginConfig = pluginsIterator.next();
      if (!pluginConfig.isChildPlugin() && isActive(channelId, pluginConfig.getPluginId())) {
        activePluginIds.add(pluginConfig.getPluginId());
      }
    }
    return Collections.unmodifiableList(new ArrayList<String>(activePluginIds));
  }

  @Override
  public List<PluginInfo> getActivePlugins(String channelId) {
    Set<PluginInfo> activePlugins = new HashSet<>();
    for (GroupProvider groupPlugin : groupPluginMap.values()) {
      for (PluginInfo pluginInfo : groupPlugin.getPluginInfos()) {
        if (isActive(channelId, pluginInfo.getType())) {
          activePlugins.add(pluginInfo);
        }
      }
    }
    return Collections.unmodifiableList(new ArrayList<>(activePlugins));
  }

  @Override
  public List<PluginInfo> getAllPlugins() {
    Set<PluginInfo> activePlugins = new HashSet<>();
    for (GroupProvider groupPlugin : groupPluginMap.values()) {
      for (PluginInfo pluginInfo : groupPlugin.getPluginInfos()) {
        activePlugins.add(pluginInfo);
      }
    }
    return Collections.unmodifiableList(new ArrayList<PluginInfo>(activePlugins));
  }

  private void saveActivePlugins(String pluginId, List<String> channels) {
    String activeChannelsString = NotificationUtils.listToString(channels, VALUE_PATTERN);
    settingService.set(Context.GLOBAL,
                       Scope.GLOBAL.id(null),
                       (NAME_SPACES + pluginId),
                       SettingValue.create(activeChannelsString));
    savePluginSettingVersion(pluginId);
    clearCache();
  }

  private void savePluginSettingVersion(String pluginId) {
    settingService.set(Context.GLOBAL,
                       NOTIFICATION_SCOPE,
                       (NAME_SPACES + pluginId),
                       SettingValue.create(String.valueOf(SPECIFIC_CHANNEL_VERSION)));
  }

  private boolean isPluginSettingVersion(String pluginId, long settingVersion) {
    SettingValue<?> settingValue = settingService.get(Context.GLOBAL,
                                                      NOTIFICATION_SCOPE,
                                                      (NAME_SPACES + pluginId));
    return settingValue != null && settingValue.getValue() != null
        && Long.parseLong(settingValue.getValue().toString()) >= settingVersion;
  }

  private void clearCache() {
    ExoContainerContext.getService(UserSettingService.class).clearDefaultSetting();
  }

}
