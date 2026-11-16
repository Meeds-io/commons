/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.jpa.settings.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.channel.ChannelManager;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.channel.template.TemplateProvider;
import org.exoplatform.commons.api.notification.model.ChannelKey;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.plugin.config.PluginConfig;
import org.exoplatform.commons.api.notification.service.setting.PluginSettingService;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.commons.api.persistence.DataInitializer;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.notification.channel.MailChannel;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.jpa.BaseTest;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.idm.UserImpl;
import org.exoplatform.settings.jpa.CacheUserSettingServiceImpl;
import org.exoplatform.settings.jpa.JPAPluginSettingServiceImpl;
import org.exoplatform.settings.jpa.JPAUserSettingServiceImpl;

public class JPAUserSettingServiceTest extends BaseTest {

  private static final String       GROUP_PROVIDER_ID = "groupId";

  private static final String       PLUGIN_ID         = "pluginAllowedId";

  private static final String       CHANNEL_ID        = "channelId";

  private static final PluginConfig PLUGIN_CONFIG     = new PluginConfig();
  static {
    PLUGIN_CONFIG.setPluginId(PLUGIN_ID);
    PLUGIN_CONFIG.setGroupId(GROUP_PROVIDER_ID);
    PLUGIN_CONFIG.setDefaultConfig(Collections.singletonList(UserSetting.FREQUENCY.INSTANTLY.name()));
    PLUGIN_CONFIG.addAdditionalChannel(CHANNEL_ID);
  }

  JPAUserSettingServiceImpl   userSettingService;

  JPAPluginSettingServiceImpl pluginSettingServiceImpl;

  ChannelManager              channelManager;

  OrganizationService         organizationService;

  Set<String>                 teardownUsers;

  List<String>                dispatchedUsers;

  List<String>                dispatchedPlugins;

  AbstractChannel             specificChannel = new AbstractChannel() {
                                                @Override
                                                public void registerTemplateProvider(TemplateProvider provider) {
                                                  // No
                                                  // Template
                                                  // Provider
                                                }

                                                @Override
                                                protected AbstractTemplateBuilder getTemplateBuilderInChannel(PluginKey key) {
                                                  // No
                                                  // template
                                                  // builder
                                                  return null;
                                                }

                                                @Override
                                                public ChannelKey getKey() {
                                                  return ChannelKey.key(CHANNEL_ID);
                                                }

                                                @Override
                                                public String getId() {
                                                  return CHANNEL_ID;
                                                }

                                                @Override
                                                public void dispatch(NotificationContext ctx, String userId) {
                                                  dispatchedUsers.add(userId);
                                                  dispatchedPlugins.add(ctx.getNotificationInfo().getKey().getId());
                                                }

                                                public boolean isDefaultChannel() {
                                                  return false;
                                                };
                                              };

  @Override
  public void setUp() throws Exception {
    super.setUp();
    userSettingService = getService(JPAUserSettingServiceImpl.class);
    pluginSettingServiceImpl = getService(JPAPluginSettingServiceImpl.class);
    channelManager = getService(ChannelManager.class);
    organizationService = getService(OrganizationService.class);

    pluginSettingServiceImpl.registerPluginConfig(PLUGIN_CONFIG);
    channelManager.register(specificChannel);
    dispatchedUsers = new ArrayList<>();
    dispatchedPlugins = new ArrayList<>();
    teardownUsers = new HashSet<>();
  }

  @Override
  protected void tearDown() throws Exception {
    for (String username : teardownUsers) {
      organizationService.getUserHandler().removeUser(username, false);
    }
    channelManager.unregister(specificChannel);
    pluginSettingServiceImpl.unregisterPluginConfig(PLUGIN_CONFIG);
    super.tearDown();
  }

  public void test_1_GetDefautSetting() throws Exception {
    for (int i = 0; i < 10; i++) {
      User user = new UserImpl("userTestSetting_" + i);
      organizationService.getUserHandler().createUser(user, true);
      String userName = user.getUserName();
      teardownUsers.add(userName);
      restartTransaction();
    }
    List<UserSetting> list = userSettingService.getDigestDefaultSettingForAllUser(0, 5);
    assertEquals(5, list.size());

    list = userSettingService.getDigestDefaultSettingForAllUser(0, 0);
    assertTrue(list.size() >= 10);
    Set<String> defaultDigestUsers = list.stream().map(UserSetting::getUserId).collect(Collectors.toSet());
    for (String username : teardownUsers) {
      assertTrue(defaultDigestUsers.contains(username));
    }
  }

  public void testChannelEnablement() throws Exception {
    String username = "testChannelEnablement";
    User user = new UserImpl(username);
    organizationService.getUserHandler().createUser(user, false);

    UserSetting userSetting = userSettingService.get(username);
    assertNotNull(userSetting);

    Set<String> channelActives = userSetting.getChannelActives();
    assertTrue(channelActives.contains(CHANNEL_ID));
    for (String channelId : channelActives) {
      assertTrue(userSetting.isActive(channelId, PLUGIN_ID));
      assertTrue(pluginSettingServiceImpl.isActive(channelId, PLUGIN_ID));
    }

    pluginSettingServiceImpl.saveChannelStatus(CHANNEL_ID, false);
    assertFalse(pluginSettingServiceImpl.isActive(CHANNEL_ID, PLUGIN_ID));
    assertFalse(pluginSettingServiceImpl.isChannelActive(CHANNEL_ID));
    userSetting = userSettingService.get(username);
    channelActives = userSetting.getChannelActives();
    assertFalse(channelActives.contains(CHANNEL_ID));

    pluginSettingServiceImpl.saveChannelStatus(CHANNEL_ID, true);
    assertTrue(pluginSettingServiceImpl.isActive(CHANNEL_ID, PLUGIN_ID));
    assertTrue(pluginSettingServiceImpl.isChannelActive(CHANNEL_ID));
    userSetting = userSettingService.get(username);
    channelActives = userSetting.getChannelActives();
    assertTrue(channelActives.contains(CHANNEL_ID));
  }

  public void testNotAllowedChannel() throws Exception {
    String username = "userAllowedTest";
    User user = new UserImpl(username);
    organizationService.getUserHandler().createUser(user, true);

    UserSetting userSetting = userSettingService.get(username);
    assertNotNull(userSetting);

    Set<String> channelActives = userSetting.getChannelActives();
    assertTrue(channelActives.contains(CHANNEL_ID));
    for (String channelId : channelActives) {
      assertTrue(userSetting.isActive(channelId, PLUGIN_ID));
      assertTrue(pluginSettingServiceImpl.isActive(channelId, PLUGIN_ID));
    }

    List<String> allowedPlugins = userSetting.getAllChannelPlugins().get(CHANNEL_ID);
    assertEquals(Collections.singletonList(PLUGIN_ID), allowedPlugins);

    assertTrue(pluginSettingServiceImpl.isActive(CHANNEL_ID, PLUGIN_ID));
    assertTrue(pluginSettingServiceImpl.isAllowed(CHANNEL_ID, PLUGIN_ID));

    for (String channelId : channelActives) {
      pluginSettingServiceImpl.saveActivePlugin(channelId, PLUGIN_ID, false);
      assertFalse(pluginSettingServiceImpl.isActive(channelId, PLUGIN_ID));
      userSetting = userSettingService.get(username);
      assertFalse(userSetting.isActive(channelId, PLUGIN_ID));
    }

    for (String channelId : channelActives) {
      pluginSettingServiceImpl.saveActivePlugin(channelId, PLUGIN_ID, true);
      assertTrue(pluginSettingServiceImpl.isActive(channelId, PLUGIN_ID));
      userSetting = userSettingService.get(username);
      assertTrue(userSetting.isActive(channelId, PLUGIN_ID));
    }

  }

  public void testDisabledUser() throws Exception {
    User u = CommonsUtils.getService(OrganizationService.class).getUserHandler().createUserInstance("binh");
    u.setEmail("email@test");
    u.setFirstName("first");
    u.setLastName("last");
    u.setPassword("pwdADDSomeSaltToBeCompliantWithSomeIS00");
    CommonsUtils.getService(OrganizationService.class).getUserHandler().createUser(u, true);

    String pluginId = "TestPlugin";
    userSettingService.save(createUserSetting("binh", Arrays.asList(pluginId), null, null));
    UserSetting userSetting = userSettingService.get("binh");
    assertTrue(userSetting.isEnabled());
    assertTrue(userSetting.isChannelActive(MailChannel.ID, pluginId));

    // disable user "binh"
    CommonsUtils.getService(OrganizationService.class).getUserHandler().setEnabled("binh", false, true);
    userSetting = userSettingService.get("binh");
    assertFalse(userSetting.isEnabled());
    assertFalse(userSetting.isChannelActive(MailChannel.ID, pluginId));

    // enable user "root" but not change the active channel status
    CommonsUtils.getService(OrganizationService.class).getUserHandler().setEnabled("binh", true, true);
    userSetting = userSettingService.get("binh");
    assertTrue(userSetting.isEnabled());
    assertTrue(userSetting.isChannelActive(MailChannel.ID, pluginId));

    CommonsUtils.getService(OrganizationService.class).getUserHandler().removeUser("binh", false);
    assertNull(CommonsUtils.getService(OrganizationService.class).getUserHandler().findUserByName("binh"));

  }

  private UserSetting createUserSetting(String userId, List<String> instantly, List<String> daily, List<String> weekly) {
    UserSetting model = new UserSetting();
    model.setUserId(userId);
    model.setChannelActive(MailChannel.ID);
    model.setDailyPlugins(daily);
    model.setChannelPlugins(MailChannel.ID, instantly);
    model.setWeeklyPlugins(weekly);
    return model;
  }

  public void testChannelDefaultValue() throws Exception {
    String username = "testChannelDefaultValue";
    User user = new UserImpl(username);
    organizationService.getUserHandler().createUser(user, false);

    UserSetting userSetting = userSettingService.get(username);
    assertNotNull(userSetting);

    //set channel default value to false for users
    pluginSettingServiceImpl.saveChannelDefaultValue(CHANNEL_ID, false);

    //channel is still active
    assertTrue(pluginSettingServiceImpl.isChannelActive(CHANNEL_ID));

    //user setting is still default settings
    userSetting = userSettingService.get(username);
    Set<String> channelActives = userSetting.getChannelActives();
    assertFalse(channelActives.contains(CHANNEL_ID));
  }

  /**
   * Two nodes share the settings store, and the events are local to a node:
   * a plugin status written through the first is seen by the default settings
   * of the second.
   */
  public void testAPluginStatusChangedOnAnotherNodeChangesTheDefaultSettings() throws Exception {
    JPAUserSettingServiceImpl otherNode = otherNodeUserSettingService();
    JPAPluginSettingServiceImpl writerNode = otherNodePluginSettingService();
    assertTrue(otherNode.getDefaultSettings().getPlugins(CHANNEL_ID).contains(PLUGIN_ID));

    writerNode.saveActivePlugin(CHANNEL_ID, PLUGIN_ID, false);
    assertFalse(otherNode.getDefaultSettings().getPlugins(CHANNEL_ID).contains(PLUGIN_ID));

    writerNode.saveActivePlugin(CHANNEL_ID, PLUGIN_ID, true);
    assertTrue(otherNode.getDefaultSettings().getPlugins(CHANNEL_ID).contains(PLUGIN_ID));
  }

  /**
   * A channel status written on another node reaches the default settings, and
   * the cached user settings derived from the old defaults are discarded.
   */
  public void testAChannelStatusChangedOnAnotherNodeReachesTheCachedUserSettings() throws Exception {
    JPAUserSettingServiceImpl otherNode = otherNodeUserSettingService();
    CacheUserSettingServiceImpl cachedOtherNode = new CacheUserSettingServiceImpl(getService(CacheService.class),
                                                                                  mock(ListenerService.class),
                                                                                  otherNode);
    JPAPluginSettingServiceImpl writerNode = otherNodePluginSettingService();
    String username = "userWithNoStoredSetting";
    writerNode.saveChannelDefaultValue(CHANNEL_ID, true);
    cachedOtherNode.clearDefaultSetting();
    assertTrue(cachedOtherNode.get(username).getChannelActives().contains(CHANNEL_ID));
    assertTrue(cachedOtherNode.getDefaultSettings().getChannelActives().contains(CHANNEL_ID));

    try {
      writerNode.saveChannelStatus(CHANNEL_ID, false);
      assertFalse(cachedOtherNode.getDefaultSettings().getChannelActives().contains(CHANNEL_ID));
      assertFalse(cachedOtherNode.get(username).getChannelActives().contains(CHANNEL_ID));
    } finally {
      writerNode.saveChannelStatus(CHANNEL_ID, true);
    }
    assertTrue(cachedOtherNode.get(username).getChannelActives().contains(CHANNEL_ID));
  }

  /**
   * A reader that meets the defaults while another thread rebuilds them never
   * gets a half-built instance.
   */
  public void testDefaultSettingsRebuiltConcurrentlyAreNeverReadHalfBuilt() throws Exception {
    AbstractChannel channel = mock(AbstractChannel.class);
    when(channel.getId()).thenReturn("concurrentChannel");
    ChannelManager channels = mock(ChannelManager.class);
    when(channels.getChannels()).thenReturn(List.of(channel));
    AtomicReference<String> stamp = new AtomicReference<>("s1");
    SettingService settingsStore = mock(SettingService.class);
    when(settingsStore.get(any(), any(), any())).thenAnswer(i -> SettingValue.create(stamp.get()));
    CountDownLatch rebuilding = new CountDownLatch(1);
    CountDownLatch release = new CountDownLatch(1);
    AtomicBoolean blockNext = new AtomicBoolean(false);
    PluginSettingService plugins = mock(PluginSettingService.class);
    when(plugins.isChannelActive("concurrentChannel")).thenAnswer(i -> {
      if (blockNext.getAndSet(false)) {
        rebuilding.countDown();
        release.await(30, TimeUnit.SECONDS);
      }
      return true;
    });
    JPAUserSettingServiceImpl service = new JPAUserSettingServiceImpl(mock(OrganizationService.class),
                                                                      settingsStore,
                                                                      channels,
                                                                      plugins,
                                                                      getService(DataInitializer.class),
                                                                      mock(ListenerService.class));
    assertTrue(service.getDefaultSettings().getChannelActives().contains("concurrentChannel"));

    stamp.set("s2");
    blockNext.set(true);
    Thread rebuilder = new Thread(service::getDefaultSettings);
    rebuilder.start();
    try {
      assertTrue(rebuilding.await(30, TimeUnit.SECONDS));
      assertTrue(service.getDefaultSettings().getChannelActives().contains("concurrentChannel"));
    } finally {
      release.countDown();
      rebuilder.join(30000);
    }
  }

  /**
   * A user setting loaded while the defaults change is derived from the old
   * ones, and is not kept in the cache.
   */
  public void testAUserSettingLoadedWhileTheDefaultsChangeIsNotCached() throws Exception {
    JPAUserSettingServiceImpl store = mock(JPAUserSettingServiceImpl.class);
    String username = "userLoadedDuringDefaultsChange";
    AtomicReference<String> stamp = new AtomicReference<>("s1");
    when(store.getDefaultSettingsStamp()).thenAnswer(i -> stamp.get());
    when(store.get(username)).thenAnswer(i -> {
      stamp.set("s2");
      return new UserSetting();
    });
    CacheService cacheService = getService(CacheService.class);
    CacheUserSettingServiceImpl cached = new CacheUserSettingServiceImpl(cacheService,
                                                                         mock(ListenerService.class),
                                                                         store);

    cached.get(username);

    assertNull(cacheService.getCacheInstance(CacheUserSettingServiceImpl.CACHE_NAME).get(username));
  }

  private JPAUserSettingServiceImpl otherNodeUserSettingService() throws Exception {
    return new JPAUserSettingServiceImpl(mock(OrganizationService.class),
                                         getService(SettingService.class),
                                         channelManager,
                                         pluginSettingServiceImpl,
                                         getService(DataInitializer.class),
                                         mock(ListenerService.class));
  }

  private JPAPluginSettingServiceImpl otherNodePluginSettingService() {
    JPAPluginSettingServiceImpl service = new JPAPluginSettingServiceImpl(getService(SettingService.class),
                                                                          channelManager,
                                                                          mock(ListenerService.class));
    service.registerPluginConfig(PLUGIN_CONFIG);
    return service;
  }
}
