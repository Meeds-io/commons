/*
 *
 *  * Copyright (C) 2003-2017 eXo Platform SAS.
 *  *
 *  * This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Affero General Public License
 *  as published by the Free Software Foundation; either version 3
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see<http://www.gnu.org/licenses/>.
 *
 */

package org.exoplatform.settings.jpa;

import static org.exoplatform.commons.api.settings.data.Context.USER;
import static org.exoplatform.settings.jpa.JPAPluginSettingServiceImpl.NOTIFICATION_CHANNEL_STATUS_MODIFIED;
import static org.exoplatform.settings.jpa.JPAPluginSettingServiceImpl.NOTIFICATION_PLUGIN_STATUS_MODIFIED;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.channel.ChannelManager;
import org.exoplatform.commons.api.notification.model.PluginInfo;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.service.setting.PluginSettingService;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.commons.api.persistence.DataInitializer;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.notification.impl.AbstractService;
import org.exoplatform.commons.notification.job.NotificationJob;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;

public class JPAUserSettingServiceImpl extends AbstractService implements UserSettingService {
  private static final Log     LOG                = ExoLogger.getLogger(JPAUserSettingServiceImpl.class);

  /** Setting Scope on Common Setting **/
  public static final Scope    NOTIFICATION_SCOPE = Scope.APPLICATION.id("NOTIFICATION");

  public static final String   NAME_PATTERN       = "exo:{CHANNELID}Channel";

  private OrganizationService  organizationService;

  private SettingService       settingService;

  private ChannelManager       channelManager;

  private PluginSettingService pluginSettingService;

  private UserSetting          defaultSetting;

  private ListenerService      listenerService;

  /**
   * This service must depend on DataInitializer to make sure data
   * structure is created before initializing it
   * 
   * @param organizationService {@link OrganizationService}
   * @param settingService {@link SettingService}
   * @param channelManager {@link ChannelManager}
   * @param pluginSettingService {@link PluginSettingService}
   * @param dataInitializer {@link DataInitializer}
   * @param listenerService {@link ListenerService}
   * @throws Exception 
   */
  public JPAUserSettingServiceImpl(OrganizationService organizationService,
                                   SettingService settingService,
                                   ChannelManager channelManager,
                                   PluginSettingService pluginSettingService,
                                   DataInitializer dataInitializer,
                                   ListenerService listenerService) throws Exception { // NOSONAR
    this.organizationService = organizationService;
    this.settingService = settingService;
    this.channelManager = channelManager;
    this.pluginSettingService = pluginSettingService;
    this.listenerService = listenerService;
    listenerService.addListener(NOTIFICATION_CHANNEL_STATUS_MODIFIED, new Listener<String, Object>() {
      @Override
      public void onEvent(Event<String, Object> event) throws Exception {
        clearDefaultSetting();
      }
    });
    listenerService.addListener(NOTIFICATION_PLUGIN_STATUS_MODIFIED, new Listener<String, Object>() {
      @Override
      public void onEvent(Event<String, Object> event) throws Exception {
        clearDefaultSetting();
      }
    });
    this.organizationService.addListenerPlugin(new UserEventListener() {
      @Override
      public void postSave(User user, boolean isNew) throws Exception {
        initDefaultSettings(user.getUserName());
      }
    });
  }

  @Override
  public void save(UserSetting model) {
    String userId = model.getUserId();
    String dailys = NotificationUtils.listToString(model.getDailyPlugins(), VALUE_PATTERN);
    String weeklys = NotificationUtils.listToString(model.getWeeklyPlugins(), VALUE_PATTERN);
    String channelActives = NotificationUtils.listToString(model.getChannelActives(), VALUE_PATTERN);

    // Notification scope

    // Save plugins active
    Set<String> channels = model.getAllChannelPlugins().keySet();
    for (String channelId : channels) {
      saveUserSetting(userId,
                      NOTIFICATION_SCOPE,
                      getChannelProperty(channelId),
                      NotificationUtils.listToString(model.getPlugins(channelId), VALUE_PATTERN));
    }
    saveUserSetting(userId, NOTIFICATION_SCOPE, EXO_DAILY, dailys);
    saveUserSetting(userId, NOTIFICATION_SCOPE, EXO_WEEKLY, weeklys);
    saveUserSetting(userId, NOTIFICATION_SCOPE, EXO_IS_ACTIVE, channelActives);
    if (model.getLastReadDate() > 0) {
      saveLastReadDate(userId, model.getLastReadDate());
    }

    // Global scope
    saveUserSetting(userId, Scope.GLOBAL, EXO_IS_ENABLED, "" + model.isEnabled());

    broadcastEvent(USER_NOTIFICATION_MODIFIED_EVENT, model);
  }

  @Override
  public void setUserEnabled(String username, boolean enabled) {
    saveUserSetting(username, Scope.GLOBAL, EXO_IS_ENABLED, "" + enabled);
  }

  @Override
  public UserSetting get(String userId) {// NOSONAR
    UserSetting userSettings = getDefaultSettings();
    userSettings.setUserId(userId);
    userSettings.setEnabled(isUserEnabled(userId));

    Map<Scope, Map<String, SettingValue<String>>> userNotificationSettings = settingService.getSettingsByContext(USER.id(userId));
    if (userNotificationSettings == null || userNotificationSettings.isEmpty()) {
      return userSettings;
    }
    List<AbstractChannel> allChannels = channelManager.getChannels();
    Map<String, AbstractChannel> channelPluginByName = allChannels.stream()
                                                                  .collect(Collectors.toMap(channel -> getChannelProperty(channel.getId()),
                                                                                            Function.identity()));

    // Global Settings
    if (userSettings.isEnabled()
        && userNotificationSettings.containsKey(Scope.GLOBAL)
        && userNotificationSettings.get(Scope.GLOBAL).containsKey(EXO_IS_ENABLED)) {
      SettingValue<String> enabledSetting = userNotificationSettings.get(Scope.GLOBAL).get(EXO_IS_ENABLED);
      boolean isEnabled = enabledSetting != null
          && StringUtils.isNotBlank(enabledSetting.getValue())
          && Boolean.valueOf(enabledSetting.getValue());
      userSettings.setEnabled(isEnabled);
    }
    if (userNotificationSettings.containsKey(NOTIFICATION_SCOPE)) {
      Set<String> channelActives = userSettings.getChannelActives();
      List<String> dailyPlugins = userSettings.getDailyPlugins();
      List<String> weeklyPlugins = userSettings.getWeeklyPlugins();

      // Notification settings
      Map<String, SettingValue<String>> notificationSettings = userNotificationSettings.get(NOTIFICATION_SCOPE);
      for (Map.Entry<String, SettingValue<String>> settingValue : notificationSettings.entrySet()) {
        String key = settingValue.getKey();
        String value = settingValue.getValue().getValue();
        if (value == null) {
          continue;
        }
        if (EXO_IS_ACTIVE.equals(key)) {
          userSettings.setChannelActives(getSetFromValue(value, channelActives));
          channelActives.forEach(channelId -> {
            if (!userSettings.getChannelActives().contains(channelId)
                && getUserSetting(userId,
                                  NOTIFICATION_SCOPE,
                                  getChannelProperty(channelId)) == null) {
              userSettings.getChannelActives().add(channelId);
            }
          });
        } else if (EXO_LAST_READ_DATE.equals(key)) {
          userSettings.setLastReadDate(Long.parseLong(value));
        } else if (EXO_DAILY.equals(key)) {
          userSettings.setDailyPlugins(getArrayListValue(value, dailyPlugins));
        } else if (EXO_WEEKLY.equals(key)) {
          userSettings.setWeeklyPlugins(getArrayListValue(value, weeklyPlugins));
        } else if (channelPluginByName.containsKey(key)) {
          AbstractChannel channel = channelPluginByName.get(key);
          userSettings.setChannelPlugins(channel.getId(), getArrayListValue(value, new ArrayList<>()));
        } else if (PropertyManager.isDevelopping()) {
          LOG.warn("A setting was found for user {}, but not considered", userId);
        } else {
          LOG.debug("A setting was found for user {}, but not considered", userId);
        }
      }
    }
    return userSettings;
  }

  @Override
  public void initDefaultSettings(String userName) {
    try {
      fillDefaultSettingsOfUser(userName);
    } catch (Exception e) {
      LOG.error("Failed to init default settings for user " + userName, e);
    }
  }

  @Override
  public void initDefaultSettings(User[] users) {
    for (User user : users) {
      String userName = user.getUserName();
      try {
        fillDefaultSettingsOfUser(userName);
      } catch (Exception e) {
        LOG.error("Failed to init default settings for user " + userName, e);
      }
    }
  }

  @Override
  public UserSetting getDefaultSettings() { // NOSONAR
    if (defaultSetting == null || PropertyManager.isDevelopping()) {
      defaultSetting = UserSetting.getInstance();
      List<String> activeChannels = getDefaultSettingActiveChannels();
      if (CollectionUtils.isEmpty(activeChannels)) {
        for (AbstractChannel channel : channelManager.getChannels()) {
          if (pluginSettingService.isChannelActive(channel.getId())) {
            defaultSetting.setChannelActive(channel.getId());
          }
        }
      } else {
        defaultSetting.getChannelActives().addAll(activeChannels);
      }
      //
      List<PluginInfo> plugins = pluginSettingService.getAllPlugins();
      for (PluginInfo pluginInfo : plugins) {
        List<String> pluginChannels = pluginSettingService.getPluginChannels(pluginInfo.getType());
        for (String defaultConf : pluginInfo.getDefaultConfig()) {
          for (String channelId : pluginChannels) {
            if (UserSetting.FREQUENCY.getFrequecy(defaultConf) == UserSetting.FREQUENCY.INSTANTLY) {
              defaultSetting.addChannelPlugin(channelId, pluginInfo.getType());
            } else {
              defaultSetting.addPlugin(pluginInfo.getType(), UserSetting.FREQUENCY.getFrequecy(defaultConf));
            }
          }
        }
      }
    }
    return defaultSetting.clone();
  }

  @Override
  public List<UserSetting> getDigestSettingForAllUser(NotificationContext notificationContext, int offset, int limit) {
    List<UserSetting> models = new ArrayList<>();
    Boolean isWeekly = notificationContext.value(NotificationJob.JOB_WEEKLY);
    String frequency = EXO_DAILY;
    if (isWeekly != null && isWeekly.booleanValue()) {
      frequency = EXO_WEEKLY;
    }

    try {
      boolean continueSearching = true;
      while (models.size() < limit && continueSearching) {
        List<Context> contexts = settingService.getContextsByTypeAndScopeAndSettingName(Context.USER.getName(),
                                                                                        NOTIFICATION_SCOPE.getName(),
                                                                                        NOTIFICATION_SCOPE.getId(),
                                                                                        frequency,
                                                                                        offset,
                                                                                        limit);
        continueSearching = contexts.size() == limit;
        for (Context context : contexts) {
          String username = context.getId();
          UserSetting userSetting = get(username);
          if (userSetting != null && userSetting.isEnabled()) {
            models.add(userSetting);
          }
        }
      }
    } catch (Exception e) {
      LOG.error("Failed to get all " + frequency + " users have notification messages", e);
    }
    return models;
  }

  @Override
  public List<UserSetting> getDigestDefaultSettingForAllUser(int offset, int limit) {
    List<UserSetting> users = new ArrayList<>();
    try {
      // Get all users not having EXO_DAILY setting stored in DB.
      // Not having this setting assumes that users uses default settings
      // and haven't changed their notification settings.
      Set<String> userNames = settingService.getEmptyContextsByTypeAndScopeAndSettingName(Context.USER.getName(),
                                                                                          NOTIFICATION_SCOPE.getName(),
                                                                                          NOTIFICATION_SCOPE.getId(),
                                                                                          EXO_DAILY,
                                                                                          offset,
                                                                                          limit);
      for (String userName : userNames) {
        users.add(new UserSetting().setUserId(userName).setLastUpdateTime(Calendar.getInstance()));
      }
    } catch (Exception e) {
      LOG.error("Failed to get default daily users have notification messages", e);
    }
    return users;
  }

  @Override
  public void saveLastReadDate(String userId, Long time) {
    settingService.set(USER.id(userId), NOTIFICATION_SCOPE, EXO_LAST_READ_DATE, SettingValue.create(time));
  }

  @Override
  public void clearDefaultSetting() {
    this.defaultSetting = null;
  }

  private String getChannelProperty(String channelId) {
    return NAME_PATTERN.replace("{CHANNELID}", channelId);
  }

  private SettingValue<?> getUserSetting(String userId, Scope scope, String key) {
    return settingService.get(USER.id(userId), scope, key);
  }

  private void saveUserSetting(String userId, Scope scope, String key, String value) {
    settingService.set(USER.id(userId), scope, key, SettingValue.create(value));
  }

  private List<String> getArrayListValue(String value, List<String> defaultValue) {
    if (StringUtils.isNotBlank(value) && !"false".equals(value)) {
      if ("true".equals(value)) {
        value = UserSetting.EMAIL_CHANNEL;
      }
      return NotificationUtils.stringToList(getValues(value));
    }
    return defaultValue;
  }

  private Set<String> getSetFromValue(String value, Set<String> defaultValue) {
    if (StringUtils.isNotBlank(value) && !"false".equals(value)) {
      if ("true".equals(value)) {
        value = UserSetting.EMAIL_CHANNEL;
      }
      return NotificationUtils.stringToSet(getValues(value));
    }
    return defaultValue;
  }

  private List<String> getDefaultSettingActiveChannels() {
    String activeChannels = System.getProperty("exo.notification.channels", "");
    List<String> activeChannelsList = activeChannels.isEmpty() ? Collections.emptyList() : Arrays.asList(activeChannels.split(","));
    return activeChannelsList.stream().filter(channelId -> pluginSettingService.isChannelActive(channelId)).toList();
  }

  private void fillDefaultSettingsOfUser(String username) {
    settingService.save(Context.USER.id(username));
  }

  private boolean isUserEnabled(String userId) {
    try {
      User user = organizationService.getUserHandler().findUserByName(userId);
      return user != null && user.isEnabled();
    } catch (Exception e) {
      LOG.warn("Error getting user status from IDM store. Consider it as enabled.", e);
      return true;
    }
  }

  private void broadcastEvent(String eventName, UserSetting model) {
    try {
      listenerService.broadcast(eventName, model.getUserId(), model);
    } catch (Exception e) {
      LOG.warn("Error broadcasting modification on User Notification Settings '{}'", model, e);
    }
  }

}
