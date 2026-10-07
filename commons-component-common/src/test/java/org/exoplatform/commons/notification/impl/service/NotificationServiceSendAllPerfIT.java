/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
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

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

import org.exoplatform.commons.api.notification.channel.ChannelManager;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.WebNotificationFilter;
import org.exoplatform.commons.api.notification.service.NotificationCompletionService;
import org.exoplatform.commons.api.notification.service.WebNotificationService;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.notification.NotificationContextFactory;
import org.exoplatform.commons.notification.impl.AbstractService;
import org.exoplatform.commons.notification.impl.jpa.web.dao.WebNotifDAO;
import org.exoplatform.commons.notification.impl.jpa.web.dao.WebParamsDAO;
import org.exoplatform.commons.notification.impl.jpa.web.dao.WebUsersDAO;
import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.commons.testing.BaseCommonsTestCase;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.UserStatus;

/**
 * What one send-all costs on the real JPA settings store, over HSQLDB, when
 * the platform holds far more disabled accounts than users able to receive it
 * (EXO-90779: 136,196 USER settings contexts for 457 enabled users on a copy
 * of a production database). The send-all must cost the users it reaches, not
 * the settings rows of the accounts it skips.
 * <p>
 * Opt-in: failsafe runs it with {@code -Prun-its}, surefire never does. The
 * sizes can be changed with {@code -Dexo.it.sendall.enabledUsers} and
 * {@code -Dexo.it.sendall.disabledUsers}. The users are created in the
 * in-memory organization service, so the identity store costs nothing here:
 * the test counts the SQL statements the send-all runs on the settings and
 * notification stores, and logs its duration. The HSQLDB database lives as
 * long as the JVM: the users and settings it creates are not removed.
 */
@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.ROOT, path = "conf/configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.commons.component.core-local-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.settings-configuration-local.xml"),
})
public class NotificationServiceSendAllPerfIT extends BaseCommonsTestCase {

  private static final Log    LOG             = ExoLogger.getLogger(NotificationServiceSendAllPerfIT.class);

  private static final int    ENABLED_USERS   = Integer.getInteger("exo.it.sendall.enabledUsers", 200);

  private static final int    DISABLED_USERS  = Integer.getInteger("exo.it.sendall.disabledUsers", 20000);

  private static final String ENABLED_PREFIX  = "perfenabled";

  private static final String DISABLED_PREFIX = "perfdisabled";

  private static final int    BATCH_SIZE      = 500;

  private UserSettingService   userSettingService;

  private WebNotificationService webNotificationService;

  private SettingService       settingService;

  private OrganizationService  organizationService;

  private EntityManagerService entityManagerService;

  private WebNotifDAO          webNotifDAO;

  private WebParamsDAO         webParamsDAO;

  private WebUsersDAO          webUsersDAO;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    userSettingService = getService(UserSettingService.class);
    webNotificationService = getService(WebNotificationService.class);
    settingService = getService(SettingService.class);
    organizationService = getService(OrganizationService.class);
    entityManagerService = getService(EntityManagerService.class);
    webNotifDAO = getService(WebNotifDAO.class);
    webParamsDAO = getService(WebParamsDAO.class);
    webUsersDAO = getService(WebUsersDAO.class);
    begin();
    cleanWebNotifications();
  }

  @Override
  public void tearDown() throws Exception {
    cleanWebNotifications();
    end();
    super.tearDown();
  }

  /**
   * A send-all is run over the enabled users alone, then again once the
   * disabled accounts exist, disabled the way an administrator disables them.
   * Each one reaches every enabled user created here once and no disabled
   * account, and the disabled accounts add less than one SQL statement per
   * hundred of them to the second: walking their settings rows costs at least
   * one statement each.
   */
  public void testSendAllCostsTheEnabledUsersNotTheDisabledAccounts() throws Exception {
    createUsers(ENABLED_PREFIX, ENABLED_USERS, true);
    int enabledUsers = organizationService.getUserHandler().findAllUsers(UserStatus.ENABLED).getSize();
    Measure reference = measureSendAll();
    assertReceivedByTheEnabledUsersOnly(1, enabledUsers);

    createUsers(DISABLED_PREFIX, DISABLED_USERS, false);
    assertDisabledInSettings(DISABLED_PREFIX + 0);
    assertDisabledInSettings(DISABLED_PREFIX + (DISABLED_USERS - 1));
    Measure measure = measureSendAll();

    LOG.info("Send-all to {} enabled users: {} SQL statements in {} ms alone, {} SQL statements in {} ms with {} disabled accounts",
             enabledUsers,
             reference.statements(),
             reference.millis(),
             measure.statements(),
             measure.millis(),
             DISABLED_USERS);
    assertReceivedByTheEnabledUsersOnly(2, enabledUsers);
    long addedStatements = measure.statements() - reference.statements();
    assertTrue("The " + DISABLED_USERS + " disabled accounts added " + addedStatements + " SQL statements to the send-all",
               addedStatements < DISABLED_USERS / 100);
  }

  private Measure measureSendAll() throws Exception {
    // Every user setting is read from the settings store, as when the platform
    // has more users than the user settings cache holds
    userSettingService.clearDefaultSetting();
    restartTransaction();
    entityManagerService.getEntityManager().clear();

    Statistics statistics = entityManagerService.getEntityManager()
                                                .getEntityManagerFactory()
                                                .unwrap(SessionFactory.class)
                                                .getStatistics();
    statistics.setStatisticsEnabled(true);
    statistics.clear();
    long start = System.currentTimeMillis();
    notificationService().process(NotificationInfo.instance().key("TestPlugin").setSendAll(true));
    long millis = System.currentTimeMillis() - start;
    long statements = statistics.getPrepareStatementCount();
    statistics.setStatisticsEnabled(false);
    restartTransaction();
    return new Measure(statements, millis);
  }

  private NotificationServiceImpl notificationService() {
    // On the notification pool, as in production: the persistence context is
    // emptied after each page
    NotificationCompletionService completionService = mock(NotificationCompletionService.class);
    when(completionService.isPoolThread()).thenReturn(true);
    return new NotificationServiceImpl(getService(ChannelManager.class),
                                       userSettingService,
                                       organizationService,
                                       getService(NotificationContextFactory.class),
                                       getService(ListenerService.class),
                                       completionService,
                                       entityManagerService);
  }

  private void createUsers(String prefix, int count, boolean enabled) throws Exception {
    UserHandler userHandler = organizationService.getUserHandler();
    for (int i = 0; i < count; i++) {
      String username = prefix + i;
      User user = userHandler.createUserInstance(username);
      user.setFirstName(username);
      user.setLastName(username);
      user.setEmail(username + "@test.local");
      user.setPassword("password");
      userHandler.createUser(user, true);
      if (!enabled) {
        userHandler.setEnabled(username, false, true);
      }
      if (i % BATCH_SIZE == BATCH_SIZE - 1) {
        restartTransaction();
        entityManagerService.getEntityManager().clear();
      }
    }
    restartTransaction();
    entityManagerService.getEntityManager().clear();
  }

  /**
   * Every enabled user created here has the notifications of every send-all so
   * far, and no more web notifications exist than the enabled users of the
   * platform have received: none went to a disabled account.
   */
  private void assertReceivedByTheEnabledUsersOnly(int sendAllCount, int enabledUsers) {
    for (int i = 0; i < ENABLED_USERS; i++) {
      String username = ENABLED_PREFIX + i;
      assertEquals("Web notifications of " + username,
                   sendAllCount,
                   webNotificationService.getNotificationInfos(new WebNotificationFilter(username), 0, 10).size());
    }
    long webNotifications = webUsersDAO.count();
    assertTrue("Web notifications after " + sendAllCount + " send-alls: " + webNotifications,
               webNotifications <= (long) sendAllCount * enabledUsers);
  }

  private void assertDisabledInSettings(String username) {
    SettingValue<?> enabled = settingService.get(Context.USER.id(username), Scope.GLOBAL, AbstractService.EXO_IS_ENABLED);
    assertNotNull("Enabled flag of " + username, enabled);
    assertEquals("Enabled flag of " + username, "false", String.valueOf(enabled.getValue()));
  }

  private void cleanWebNotifications() {
    webUsersDAO.deleteAll();
    webParamsDAO.deleteAll();
    restartTransaction();
    webNotifDAO.deleteAll();
    restartTransaction();
  }

  private record Measure(long statements, long millis) {
  }

}
