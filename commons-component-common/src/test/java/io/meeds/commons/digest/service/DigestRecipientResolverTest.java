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
package io.meeds.commons.digest.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import org.exoplatform.commons.api.notification.plugin.NotificationPluginUtils;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.UserStatus;

class DigestRecipientResolverTest {

  private static final String ADDRESS = "john@example.com";

  private final OrganizationService organizationService = mock(OrganizationService.class);

  private final UserHandler userHandler = mock(UserHandler.class);

  private final DigestRecipientResolver resolver = new DigestRecipientResolver(organizationService);

  @Test
  void testDisabledAccountWithAnAddressAsUsernameGetsNoDigest() throws Exception {
    try (MockedStatic<NotificationPluginUtils> utils = Mockito.mockStatic(NotificationPluginUtils.class)) {
      mockAccount(ADDRESS, false);
      utils.when(() -> NotificationPluginUtils.getTo(ADDRESS)).thenReturn(ADDRESS);
      assertNull(resolver.getEmail(ADDRESS));
    }
  }

  @Test
  void testEnabledAccountKeepsItsDigest() throws Exception {
    try (MockedStatic<NotificationPluginUtils> utils = Mockito.mockStatic(NotificationPluginUtils.class)) {
      mockAccount(ADDRESS, true);
      utils.when(() -> NotificationPluginUtils.getTo(ADDRESS)).thenReturn("John<" + ADDRESS + ">");
      assertEquals("John<" + ADDRESS + ">", resolver.getEmail(ADDRESS));
    }
  }

  @Test
  void testAddressThatIsNoAccountKeepsBeingResolvedAsAnAddress() throws Exception {
    try (MockedStatic<NotificationPluginUtils> utils = Mockito.mockStatic(NotificationPluginUtils.class)) {
      mockAccount(ADDRESS, null);
      utils.when(() -> NotificationPluginUtils.getTo(ADDRESS)).thenReturn(ADDRESS);
      assertEquals(ADDRESS, resolver.getEmail(ADDRESS));
    }
  }

  @Test
  void testUnreadableAccountStatusIsAnErrorNotAMail() throws Exception {
    try (MockedStatic<NotificationPluginUtils> utils = Mockito.mockStatic(NotificationPluginUtils.class)) {
      when(organizationService.getUserHandler()).thenReturn(userHandler);
      when(userHandler.findUserByName(ADDRESS, UserStatus.ANY)).thenThrow(new Exception("db down"));
      utils.when(() -> NotificationPluginUtils.getTo(ADDRESS)).thenReturn(ADDRESS);
      IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> resolver.getEmail(ADDRESS));
      assertEquals("db down", thrown.getCause().getMessage());
    }
  }

  @Test
  void testAccountStatusIsReadInsideARequestLifecycle() throws Exception {
    OrganizationService lifecycleOrganizationService =
                                                     mock(OrganizationService.class,
                                                          withSettings().extraInterfaces(ComponentRequestLifecycle.class));
    when(lifecycleOrganizationService.getUserHandler()).thenReturn(userHandler);
    User user = mock(User.class);
    when(user.isEnabled()).thenReturn(false);
    when(userHandler.findUserByName(ADDRESS, UserStatus.ANY)).thenReturn(user);
    try (MockedStatic<NotificationPluginUtils> utils = Mockito.mockStatic(NotificationPluginUtils.class);
         MockedStatic<RequestLifeCycle> lifecycle = Mockito.mockStatic(RequestLifeCycle.class)) {
      utils.when(() -> NotificationPluginUtils.getTo(ADDRESS)).thenReturn(ADDRESS);
      assertNull(new DigestRecipientResolver(lifecycleOrganizationService).getEmail(ADDRESS));
      lifecycle.verify(() -> RequestLifeCycle.begin((ComponentRequestLifecycle) lifecycleOrganizationService));
      lifecycle.verify(RequestLifeCycle::end);
    }
  }

  /** @param enabled null for a username that is no account */
  private void mockAccount(String username, Boolean enabled) throws Exception {
    when(organizationService.getUserHandler()).thenReturn(userHandler);
    User user = null;
    if (enabled != null) {
      user = mock(User.class);
      when(user.isEnabled()).thenReturn(enabled);
    }
    when(userHandler.findUserByName(username, UserStatus.ANY)).thenReturn(user);
  }

}
