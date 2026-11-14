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
package org.exoplatform.ws.frameworks.cometd.transport;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import javax.annotation.security.DenyAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;

@ExtendWith(MockitoExtension.class)
class RESTContinuationServiceTest {

  private static final String     CHANNEL = "/eXo/Application/web/NotificationMessage";

  @Mock
  private ContinuationService     continuationService;

  private RESTContinuationService service;

  @BeforeEach
  void setUp() {
    service = new RESTContinuationService(continuationService);
  }

  @AfterEach
  void tearDown() {
    ConversationState.setCurrent(null);
  }

  /**
   * The ws method access filter enforces these roles before the method runs:
   * this pins the annotations against the role-removed and role-weakened
   * mutants, which a direct call of the method cannot kill.
   */
  @Test
  void testEachOperationRequiresItsRole() throws Exception {
    assertRoles("getToken", new String[] { "users" }, String.class);
    assertRoles("isSubscribed", new String[] { "users" }, String.class, String.class);
    assertDenied("hasChannel", String.class);
    assertDenied("sendMessage", DelegateMessage.class);
    assertDenied("sendBroadcastMessage", DelegateMessage.class);
  }

  private void assertDenied(String methodName, Class<?>... parameterTypes) throws Exception {
    assertNotNull(RESTContinuationService.class.getMethod(methodName, parameterTypes).getAnnotation(DenyAll.class),
                  methodName);
  }

  @Test
  void testGetTokenAnswersTheSignedInUserOwnTokenNotStorable() {
    signIn("john", "users");
    when(continuationService.getUserToken("john")).thenReturn("johnToken");

    Response response = service.getToken("john");

    assertEquals(200, response.getStatus());
    assertEquals("johnToken", response.getEntity());
    assertNotStorable(response);
  }

  @Test
  void testGetTokenOfAnotherUserIsForbidden() {
    signIn("john", "users");
    assertEquals(403, service.getToken("mary").getStatus());
    signIn("root", "users", "administrators");
    assertEquals(403, service.getToken("mary").getStatus());
    verify(continuationService, never()).getUserToken(anyString());
  }

  @Test
  void testGetTokenWithoutIdentityIsForbidden() {
    assertEquals(403, service.getToken("john").getStatus());
    ConversationState.setCurrent(new ConversationState(null));
    assertEquals(403, service.getToken("john").getStatus());
    verify(continuationService, never()).getUserToken(anyString());
  }

  @Test
  void testIsSubscribedIsLimitedToTheSignedInUser() {
    signIn("john", "users");
    assertEquals(403, service.isSubscribed("mary", CHANNEL).getStatus());
    verify(continuationService, never()).isSubscribe(anyString(), anyString());

    when(continuationService.isSubscribe("john", CHANNEL)).thenReturn(true);
    Response response = service.isSubscribed("john", CHANNEL);
    assertEquals(200, response.getStatus());
    assertEquals("true", response.getEntity());
    assertNotStorable(response);
  }

  /**
   * The operation is closed over HTTP ({@code @DenyAll}): this pins its body
   * for a direct call only.
   */
  @Test
  void testHasChannelIsNotStorable() {
    when(continuationService.hasChannel(CHANNEL)).thenReturn(true);
    Response response = service.hasChannel(CHANNEL);
    assertEquals(200, response.getStatus());
    assertEquals("true", response.getEntity());
    assertNotStorable(response);
  }

  /**
   * The operation is closed over HTTP ({@code @DenyAll}): this pins its body
   * for a direct call only.
   */
  @Test
  void testSendMessagesDelegateToTheService() {
    assertEquals(200, service.sendMessage(new DelegateMessage(CHANNEL, "mary", "hello", "1")).getStatus());
    verify(continuationService).sendMessage("mary", CHANNEL, "hello", "1");
    assertEquals(200, service.sendBroadcastMessage(new DelegateMessage(CHANNEL, "hello", "2")).getStatus());
    verify(continuationService).sendBroadcastMessage(CHANNEL, "hello", "2");
  }

  private void assertRoles(String methodName, String[] roles, Class<?>... parameterTypes) throws Exception {
    RolesAllowed rolesAllowed = RESTContinuationService.class.getMethod(methodName, parameterTypes)
                                                             .getAnnotation(RolesAllowed.class);
    assertNotNull(rolesAllowed, methodName);
    assertArrayEquals(roles, rolesAllowed.value(), methodName);
  }

  private void assertNotStorable(Response response) {
    CacheControl cacheControl = (CacheControl) response.getMetadata().getFirst(HttpHeaders.CACHE_CONTROL);
    assertNotNull(cacheControl);
    assertTrue(cacheControl.isNoStore());
    assertTrue(cacheControl.isPrivate());
  }

  private void signIn(String userId, String... roles) {
    ConversationState.setCurrent(new ConversationState(new Identity(userId, Collections.emptyList(), List.of(roles))));
  }
}
