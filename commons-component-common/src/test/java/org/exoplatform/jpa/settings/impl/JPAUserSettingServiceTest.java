package org.exoplatform.jpa.settings.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.channel.ChannelManager;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.channel.template.TemplateProvider;
import org.exoplatform.commons.api.notification.model.ChannelKey;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.plugin.config.PluginConfig;
import org.exoplatform.commons.notification.channel.MailChannel;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.jpa.BaseTest;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.idm.UserImpl;
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
      userSettingService.initDefaultSettings(userName);
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
    userSettingService.initDefaultSettings(username);

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
    organizationService.getUserHandler().createUser(user, false);
    userSettingService.initDefaultSettings(username);

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
}
