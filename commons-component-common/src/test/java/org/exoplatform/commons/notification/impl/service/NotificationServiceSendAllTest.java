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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.hibernate.Session;

import org.exoplatform.commons.api.notification.channel.ChannelManager;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.WebNotificationFilter;
import org.exoplatform.commons.api.notification.service.NotificationCompletionService;
import org.exoplatform.commons.api.notification.service.WebNotificationService;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.commons.notification.NotificationContextFactory;
import org.exoplatform.commons.notification.impl.jpa.web.dao.WebNotifDAO;
import org.exoplatform.commons.notification.impl.jpa.web.dao.WebParamsDAO;
import org.exoplatform.commons.notification.impl.jpa.web.dao.WebUsersDAO;
import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.commons.testing.BaseCommonsTestCase;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;

/**
 * A send-all over more users than one page, delivered by the real web channel
 * into the real JPA storage, on the real kernel EntityManager of the thread
 * (EXO-90779). The entities counted are the web notifications it persists: in
 * this container the settings DAOs are in-memory, so the settings reads the
 * clear exists for in production do not reach JPA here. The users are enabled
 * users of the in-memory organization service, which a send-all walks.
 */
@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.ROOT, path = "conf/configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.commons.component.core-local-configuration.xml"),
})
public class NotificationServiceSendAllTest extends BaseCommonsTestCase {

  private static final int       USERS_COUNT = 250;

  private static final String    USER_PREFIX = "sendall";

  private UserSettingService     userSettingService;

  private WebNotificationService webNotificationService;

  private EntityManagerService   entityManagerService;

  private WebNotifDAO            webNotifDAO;

  private WebParamsDAO           webParamsDAO;

  private WebUsersDAO            webUsersDAO;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    userSettingService = getService(UserSettingService.class);
    webNotificationService = getService(WebNotificationService.class);
    entityManagerService = getService(EntityManagerService.class);
    webNotifDAO = getService(WebNotifDAO.class);
    webParamsDAO = getService(WebParamsDAO.class);
    webUsersDAO = getService(WebUsersDAO.class);
    begin();
    cleanWebNotifications();
    UserHandler userHandler = getService(OrganizationService.class).getUserHandler();
    for (int i = 0; i < USERS_COUNT; i++) {
      User user = userHandler.createUserInstance(USER_PREFIX + i);
      user.setEmail(USER_PREFIX + i + "@test.local");
      userHandler.createUser(user, true);
    }
    restartTransaction();
  }

  @Override
  public void tearDown() throws Exception {
    cleanWebNotifications();
    end();
    super.tearDown();
  }

  /**
   * On the notification thread pool, the kernel EntityManager of the thread
   * ends emptied instead of holding the entities of every user, and every user
   * still gets the notification. That the clear follows each page is pinned by
   * NotificationServiceImplTest.
   */
  public void testSendAllOnThePoolKeepsThePersistenceContextBounded() throws Exception {
    notificationService(true).process(sendAllNotification());

    assertTrue("Managed entities after the send-all: " + managedEntitiesCount(), managedEntitiesCount() < USERS_COUNT);
    assertReceivedByEveryUser();
  }

  /**
   * Explicit recipients, the members of a large space, leave the kernel
   * EntityManager of the pool thread emptied too, and every one of them gets
   * the notification. That they are handed page by page, with a clear after
   * each page, is pinned by NotificationServiceImplTest.
   */
  public void testExplicitRecipientsOnThePoolKeepThePersistenceContextBounded() throws Exception {
    List<String> users = IntStream.range(0, USERS_COUNT).mapToObj(i -> USER_PREFIX + i).toList();
    notificationService(true).process(NotificationInfo.instance().key("TestPlugin").to(new ArrayList<>(users)));

    assertTrue("Managed entities after the notification: " + managedEntitiesCount(), managedEntitiesCount() < USERS_COUNT);
    assertReceivedByEveryUser();
  }

  /**
   * A disabled user named as a recipient gets nothing, through the cached user
   * settings the lifecycles read, while the enabled recipient named with it does.
   */
  public void testADisabledRecipientReceivesNothing() throws Exception {
    String disabledUser = USER_PREFIX + "disabled";
    UserHandler userHandler = getService(OrganizationService.class).getUserHandler();
    userHandler.createUser(userHandler.createUserInstance(disabledUser), true);
    userHandler.setEnabled(disabledUser, false, true);
    restartTransaction();

    notificationService(true).process(NotificationInfo.instance()
                                                      .key("TestPlugin")
                                                      .to(new ArrayList<>(List.of(USER_PREFIX + 0, disabledUser))));

    assertEquals(1, webNotificationService.getNotificationInfos(new WebNotificationFilter(USER_PREFIX + 0), 0, 10).size());
    assertEquals(0, webNotificationService.getNotificationInfos(new WebNotificationFilter(disabledUser), 0, 10).size());
  }

  /**
   * Outside the notification pool, the EntityManager belongs to the caller: it
   * is not emptied, and ends holding the entities of every user walked.
   */
  public void testSendAllOutsideThePoolLeavesThePersistenceContextToTheCaller() throws Exception {
    notificationService(false).process(sendAllNotification());

    assertTrue("Managed entities after the send-all: " + managedEntitiesCount(), managedEntitiesCount() >= USERS_COUNT);
    assertReceivedByEveryUser();
  }

  private NotificationServiceImpl notificationService(boolean poolThread) {
    // Every user setting is reloaded from the settings storage, not the cache,
    // as when the platform has more users than the cache holds
    userSettingService.clearDefaultSetting();
    entityManagerService.getEntityManager().clear();

    NotificationCompletionService completionService = mock(NotificationCompletionService.class);
    when(completionService.isPoolThread()).thenReturn(poolThread);
    return new NotificationServiceImpl(getService(ChannelManager.class),
                                       userSettingService,
                                       getService(OrganizationService.class),
                                       getService(NotificationContextFactory.class),
                                       getService(ListenerService.class),
                                       completionService,
                                       entityManagerService);
  }

  private NotificationInfo sendAllNotification() {
    return NotificationInfo.instance().key("TestPlugin").setSendAll(true);
  }

  private int managedEntitiesCount() {
    return entityManagerService.getEntityManager().unwrap(Session.class).getStatistics().getEntityCount();
  }

  private void assertReceivedByEveryUser() {
    for (int i = 0; i < USERS_COUNT; i++) {
      String username = USER_PREFIX + i;
      assertEquals("Web notifications of " + username,
                   1,
                   webNotificationService.getNotificationInfos(new WebNotificationFilter(username), 0, 10).size());
    }
  }

  private void cleanWebNotifications() {
    webUsersDAO.deleteAll();
    webParamsDAO.deleteAll();
    restartTransaction();
    webNotifDAO.deleteAll();
    restartTransaction();
  }

}
