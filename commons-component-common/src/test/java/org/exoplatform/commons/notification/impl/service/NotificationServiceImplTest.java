/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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
package org.exoplatform.commons.notification.impl.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.channel.ChannelManager;
import org.exoplatform.commons.api.notification.lifecycle.AbstractNotificationLifecycle;
import org.exoplatform.commons.api.notification.model.ChannelKey;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.plugin.config.PluginConfig;
import org.exoplatform.commons.api.notification.service.NotificationCompletionService;
import org.exoplatform.commons.api.notification.service.setting.PluginSettingService;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.commons.notification.NotificationContextFactory;
import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserStatus;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotificationServiceImplTest {

  private static final String           PLUGIN_ID   = "TestPlugin";

  private static final int              USERS_COUNT = 250;

  private static final List<String>     USERS       = IntStream.range(0, USERS_COUNT).mapToObj(i -> "user" + i).toList();

  @Mock
  private ChannelManager                channelManager;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private NotificationContextFactory    notificationContextFactory;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private OrganizationService           organizationService;

  @Mock
  private NotificationCompletionService completionService;

  @Mock
  private EntityManagerService          entityManagerService;

  @Mock
  private EntityManager                 entityManager;

  @Mock
  private EntityTransaction             transaction;

  @Mock
  private PluginSettingService          pluginSettingService;

  @Mock
  private ListAccess<User>              enabledUsers;

  @Mock
  private UserSettingService            userSettingService;

  @Mock
  private AbstractNotificationLifecycle mailLifecycle;

  @Mock
  private AbstractNotificationLifecycle webLifecycle;

  private MockedStatic<CommonsUtils>    commonsUtils;

  private NotificationServiceImpl       notificationService;

  @BeforeEach
  void setUp() throws Exception {
    commonsUtils = mockStatic(CommonsUtils.class);
    commonsUtils.when(() -> CommonsUtils.getService(PluginSettingService.class)).thenReturn(pluginSettingService);

    AbstractChannel mailChannel = channel("MAIL_CHANNEL");
    AbstractChannel webChannel = channel("WEB_CHANNEL");
    when(channelManager.getChannels()).thenReturn(List.of(mailChannel, webChannel));
    when(channelManager.getLifecycle(ChannelKey.key("MAIL_CHANNEL"))).thenReturn(mailLifecycle);
    when(channelManager.getLifecycle(ChannelKey.key("WEB_CHANNEL"))).thenReturn(webLifecycle);
    when(pluginSettingService.isActive(anyString(), eq(PLUGIN_ID))).thenReturn(true);

    when(organizationService.getUserHandler().findAllUsers(UserStatus.ENABLED)).thenReturn(enabledUsers);
    when(enabledUsers.getSize()).thenReturn(USERS_COUNT);
    when(enabledUsers.load(anyInt(), anyInt())).thenAnswer(invocation -> {
      int index = invocation.getArgument(0);
      int length = invocation.getArgument(1);
      // As the organization service does, past the size of the list
      if (index + length > USERS_COUNT) {
        throw new IllegalArgumentException("Try to get more than number users can retrieve");
      }
      return users(USERS.subList(index, index + length));
    });

    when(completionService.isPoolThread()).thenReturn(true);
    when(entityManagerService.getEntityManager()).thenReturn(entityManager);
    when(entityManager.getTransaction()).thenReturn(transaction);

    notificationService = new NotificationServiceImpl(channelManager,
                                                      userSettingService,
                                                      organizationService,
                                                      notificationContextFactory,
                                                      mock(ListenerService.class),
                                                      completionService,
                                                      entityManagerService);
  }

  @AfterEach
  void tearDown() {
    commonsUtils.close();
  }

  /**
   * Each page of enabled users is loaded once, the last one within the size of
   * the list, and handed to every channel before the next page is loaded; the
   * persistence context is emptied after each page.
   */
  @Test
  void testSendAllHandsEachPageToEveryChannelThenClearsThePersistenceContext() throws Exception {
    notificationService.process(sendAllNotification());

    InOrder inOrder = inOrder(enabledUsers, mailLifecycle, webLifecycle, entityManager);
    for (int offset = 0; offset < USERS_COUNT; offset += 100) {
      String[] page = page(offset);
      inOrder.verify(enabledUsers).load(offset, page.length);
      inOrder.verify(mailLifecycle).process(any(NotificationContext.class), eq(page));
      inOrder.verify(webLifecycle).process(any(NotificationContext.class), eq(page));
      inOrder.verify(entityManager).clear();
    }
    verify(enabledUsers, times(3)).load(anyInt(), anyInt());
    verify(entityManager, times(3)).clear();
  }

  /**
   * A send-all walks the enabled users of the organization service, never the
   * disabled ones: their settings are not even loaded.
   */
  @Test
  void testSendAllWalksTheEnabledUsersOnly() throws Exception {
    notificationService.process(sendAllNotification());

    verify(organizationService.getUserHandler()).findAllUsers(UserStatus.ENABLED);
    verify(organizationService.getUserHandler(), never()).findAllUsers(UserStatus.ANY);
    verify(organizationService.getUserHandler(), never()).findAllUsers(UserStatus.DISABLED);
  }

  /**
   * A user listed twice, as the organization service does when it completes a
   * page filtered on the status with the last user it found, is handed once.
   */
  @Test
  void testAUserListedTwiceIsHandedOnce() throws Exception {
    User[] paddedPage = users(List.of("user0", "user1", "user1", "user1"));
    when(enabledUsers.getSize()).thenReturn(4);
    when(enabledUsers.load(0, 4)).thenReturn(paddedPage);

    notificationService.process(sendAllNotification());

    verify(mailLifecycle).process(any(NotificationContext.class), eq(new String[] { "user0", "user1" }));
    verify(webLifecycle).process(any(NotificationContext.class), eq(new String[] { "user0", "user1" }));
  }

  /**
   * A user listed again on the next page, as the organization service does when
   * it completes a short page with the users that follow, is handed once.
   */
  @Test
  void testAUserListedAgainOnTheNextPageIsHandedOnce() throws Exception {
    User[] repeatedUser = users(List.of("user99"));
    when(enabledUsers.getSize()).thenReturn(101);
    when(enabledUsers.load(100, 1)).thenReturn(repeatedUser);

    notificationService.process(sendAllNotification());

    verify(mailLifecycle).process(any(NotificationContext.class), eq(page(0)));
    verify(webLifecycle).process(any(NotificationContext.class), eq(page(0)));
    verify(mailLifecycle, times(1)).process(any(NotificationContext.class), any(String[].class));
    verify(webLifecycle, times(1)).process(any(NotificationContext.class), any(String[].class));
  }

  /**
   * When the users cannot be listed, no channel is given anything and the
   * notification ends in error.
   */
  @Test
  void testAFailingUsersListingIsRaised() throws Exception {
    IllegalStateException error = new IllegalStateException("Users store failure");
    when(organizationService.getUserHandler().findAllUsers(UserStatus.ENABLED)).thenThrow(error);

    NotificationInfo notification = sendAllNotification();
    assertSame(error, assertThrows(IllegalStateException.class, () -> notificationService.process(notification)));

    verify(mailLifecycle, never()).process(any(NotificationContext.class), any(String[].class));
    verify(webLifecycle, never()).process(any(NotificationContext.class), any(String[].class));
  }

  /**
   * A clear would discard the pending changes of a transaction still active.
   */
  @Test
  void testSendAllLeavesThePersistenceContextOfAnActiveTransaction() throws Exception {
    when(transaction.isActive()).thenReturn(true);

    notificationService.process(sendAllNotification());

    verify(webLifecycle, times(3)).process(any(NotificationContext.class), any(String[].class));
    verify(entityManager, never()).clear();
  }

  /**
   * Outside the notification pool, the EntityManager belongs to the caller of
   * the notification.
   */
  @Test
  void testSendAllOutsideThePoolLeavesThePersistenceContext() throws Exception {
    when(completionService.isPoolThread()).thenReturn(false);

    notificationService.process(sendAllNotification());

    verify(webLifecycle, times(3)).process(any(NotificationContext.class), any(String[].class));
    verify(entityManager, never()).clear();
  }

  /**
   * A channel that fails is not given the next pages, the other channels still
   * get every page, and the notification ends in error.
   */
  @Test
  void testAChannelFailingOnAPageIsDroppedAndTheErrorIsRaised() {
    IllegalStateException error = new IllegalStateException("Mail channel failure");
    doThrow(error).when(mailLifecycle).process(any(NotificationContext.class), any(String[].class));

    NotificationInfo notification = sendAllNotification();
    assertSame(error, assertThrows(IllegalStateException.class, () -> notificationService.process(notification)));

    verify(mailLifecycle, times(1)).process(any(NotificationContext.class), any(String[].class));
    verify(webLifecycle, times(3)).process(any(NotificationContext.class), any(String[].class));
  }

  @Test
  void testAnInactiveChannelIsNotGivenTheSendAll() throws Exception {
    when(pluginSettingService.isActive("MAIL_CHANNEL", PLUGIN_ID)).thenReturn(false);

    notificationService.process(sendAllNotification());

    verify(mailLifecycle, never()).process(any(NotificationContext.class), any(String[].class));
    verify(webLifecycle, times(3)).process(any(NotificationContext.class), any(String[].class));
  }

  /**
   * External users of a send-all to internals, and excluded users, are left
   * out of the page every channel receives.
   */
  @Test
  void testEveryChannelGetsThePageWithoutExternalAndExcludedUsers() throws Exception {
    UserProfile externalProfile = mock(UserProfile.class);
    when(externalProfile.getAttribute(UserProfile.OTHER_KEYS[2])).thenReturn("true");
    when(organizationService.getUserProfileHandler().findUserProfileByName("user1")).thenReturn(externalProfile);

    notificationService.process(NotificationInfo.instance().key(PLUGIN_ID).setSendAllInternals(true).exclude("user2"));

    String[] firstPage = USERS.subList(0, 100)
                              .stream()
                              .filter(user -> !user.equals("user1") && !user.equals("user2"))
                              .toArray(String[]::new);
    verify(mailLifecycle).process(any(NotificationContext.class), eq(firstPage));
    verify(webLifecycle).process(any(NotificationContext.class), eq(firstPage));
    verify(organizationService.getUserProfileHandler(), times(USERS_COUNT)).findUserProfileByName(anyString());
  }

  /**
   * A user who muted the space of a mutable plugin's notification is left out
   * of the page every channel receives.
   */
  @Test
  void testEveryChannelGetsThePageWithoutTheUsersWhoMutedTheSpace() throws Exception {
    PluginConfig pluginConfig = mock(PluginConfig.class);
    when(pluginConfig.isMutable()).thenReturn(true);
    when(pluginSettingService.getPluginConfig(PLUGIN_ID)).thenReturn(pluginConfig);
    UserSetting mutedSetting = mock(UserSetting.class);
    when(mutedSetting.isSpaceMuted(1L)).thenReturn(true);
    UserSetting defaultSetting = mock(UserSetting.class);
    when(userSettingService.get(anyString())).thenReturn(defaultSetting);
    when(userSettingService.get("user3")).thenReturn(mutedSetting);

    notificationService.process(sendAllNotification().setSpaceId(1L));

    String[] firstPage = USERS.subList(0, 100).stream().filter(user -> !user.equals("user3")).toArray(String[]::new);
    verify(mailLifecycle).process(any(NotificationContext.class), eq(firstPage));
    verify(webLifecycle).process(any(NotificationContext.class), eq(firstPage));
  }

  /**
   * A lifecycle leaves its last per-recipient clone in the shared context:
   * every channel, on every page, still starts from the notification itself.
   */
  @Test
  void testEveryChannelStartsEachPageFromTheNotification() throws Exception {
    List<NotificationInfo> received = new ArrayList<>();
    doAnswer(invocation -> {
      NotificationContext ctx = invocation.getArgument(0);
      received.add(ctx.getNotificationInfo());
      ctx.setNotificationInfo(ctx.getNotificationInfo().clone().setTo("lastRecipient"));
      return null;
    }).when(mailLifecycle).process(any(NotificationContext.class), any(String[].class));
    doAnswer(invocation -> {
      NotificationContext ctx = invocation.getArgument(0);
      received.add(ctx.getNotificationInfo());
      ctx.setNotificationInfo(ctx.getNotificationInfo().clone(true).setTo("lastRecipient"));
      return null;
    }).when(webLifecycle).process(any(NotificationContext.class), any(String[].class));
    NotificationInfo notification = sendAllNotification();

    notificationService.process(notification);

    assertEquals(6, received.size());
    received.forEach(info -> assertSame(notification, info));
  }

  @Test
  void testNoActiveChannelWalksNoUser() throws Exception {
    when(pluginSettingService.isActive(anyString(), eq(PLUGIN_ID))).thenReturn(false);

    notificationService.process(sendAllNotification());

    verify(organizationService.getUserHandler(), never()).findAllUsers(any(UserStatus.class));
  }

  /**
   * A few explicit recipients are one page: every active channel gets it, no
   * user is walked, and the persistence context is emptied once.
   */
  @Test
  void testExplicitRecipientsGoToEveryActiveChannel() throws Exception {
    notificationService.process(NotificationInfo.instance().key(PLUGIN_ID).to(List.of("john", "mary")));

    verify(mailLifecycle).process(any(NotificationContext.class), eq(new String[] { "john", "mary" }));
    verify(webLifecycle).process(any(NotificationContext.class), eq(new String[] { "john", "mary" }));
    verify(organizationService.getUserHandler(), never()).findAllUsers(any(UserStatus.class));
    verify(entityManager, times(1)).clear();
  }

  /**
   * Many explicit recipients, the members of a large space, go page by page to
   * every channel, with the persistence context emptied after each page.
   */
  @Test
  void testExplicitRecipientsGoPageByPageToEveryChannel() throws Exception {
    notificationService.process(NotificationInfo.instance().key(PLUGIN_ID).to(USERS));

    InOrder inOrder = inOrder(mailLifecycle, webLifecycle, entityManager);
    for (int offset = 0; offset < USERS_COUNT; offset += 100) {
      String[] page = page(offset);
      inOrder.verify(mailLifecycle).process(any(NotificationContext.class), eq(page));
      inOrder.verify(webLifecycle).process(any(NotificationContext.class), eq(page));
      inOrder.verify(entityManager).clear();
    }
    verify(entityManager, times(3)).clear();
  }

  @Test
  void testAChannelFailingOnAPageOfRecipientsIsDroppedAndTheErrorIsRaised() {
    IllegalStateException error = new IllegalStateException("Mail channel failure");
    doThrow(error).when(mailLifecycle).process(any(NotificationContext.class), any(String[].class));

    NotificationInfo notification = NotificationInfo.instance().key(PLUGIN_ID).to(USERS);
    assertSame(error, assertThrows(IllegalStateException.class, () -> notificationService.process(notification)));

    verify(mailLifecycle, times(1)).process(any(NotificationContext.class), any(String[].class));
    verify(webLifecycle, times(3)).process(any(NotificationContext.class), any(String[].class));
  }

  private NotificationInfo sendAllNotification() {
    return NotificationInfo.instance().key(PLUGIN_ID).setSendAll(true);
  }

  private String[] page(int offset) {
    return USERS.subList(offset, Math.min(offset + 100, USERS_COUNT)).toArray(new String[0]);
  }

  private User[] users(List<String> usernames) {
    return usernames.stream().map(username -> {
      User user = mock(User.class);
      when(user.getUserName()).thenReturn(username);
      return user;
    }).toArray(User[]::new);
  }

  private AbstractChannel channel(String id) {
    AbstractChannel channel = mock(AbstractChannel.class);
    when(channel.getId()).thenReturn(id);
    return channel;
  }

}
