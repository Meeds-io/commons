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
package org.exoplatform.services.user;

import java.util.*;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;
import org.mortbay.cometd.continuation.EXoContinuationBayeux;

import org.exoplatform.commons.testing.BaseCommonsTestCase;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.security.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class UserStateServiceTest extends BaseCommonsTestCase {

  private String              SUPER_USER = "root";

  private ContinuationService continuationService;

  private UserStateService    userStateService;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    //
    loginUser(SUPER_USER, false);

    continuationService = mock(ContinuationService.class);
    userStateService = new UserStateService(continuationService,
                                            getService(CacheService.class),
                                            getService(SettingService.class));
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    userStateService.clearCache();
  }

  public void testGetUserState() throws Exception {
    //
    loginUser("mary", true);
    doNothing().when(continuationService).sendBroadcastMessage(anyString(), any(), anyString());
    when(continuationService.isPresent("mary")).thenReturn(true);

    assertEquals(UserStateService.DEFAULT_STATUS, userStateService.getUserState("mary").getStatus());

    //
    loginUser("demo", false);
    when(continuationService.isPresent("demo")).thenReturn(true);

    // get status of user Mary by current user Demo
    assertEquals(UserStateService.DEFAULT_STATUS, userStateService.getUserState("demo").getStatus());
    assertEquals(UserStateService.DEFAULT_STATUS, userStateService.getUserState("mary").getStatus());

    when(continuationService.isPresent("demo")).thenReturn(false);
    assertEquals(UserStateService.STATUS_OFFLINE, userStateService.getUserState("demo").getStatus());
  }

  public void testOnline() throws Exception {
    long date = new Date().getTime();
    UserStateModel userModel = new UserStateModel(SUPER_USER,
                                                  date,
                                                  UserStateService.STATUS_OFFLINE);
    userStateService.save(userModel);
    when(continuationService.getConnectedUserIds()).thenReturn(Collections.singleton(SUPER_USER));

    //
    List<UserStateModel> onlines = userStateService.online();
    assertEquals(0, onlines.size());

    String status = "doNotDisturb";
    userStateService.saveStatus(SUPER_USER, status);
    onlines = userStateService.online();
    assertEquals(1, onlines.size());
    assertEquals(SUPER_USER, onlines.get(0).getUserId());
    assertNotNull(onlines.get(0).getLastActivity());
    assertEquals(status, onlines.get(0).getStatus());
  }

  public void testLastLogin() {
    assertNull(userStateService.lastLogin());
    loginUser("user1", true);
    when(continuationService.getConnectedUserIds()).thenReturn(Collections.singleton("user1"));
    assertEquals("user1", userStateService.lastLogin().getUserId());

    loginUser("user2", true);
    when(continuationService.getConnectedUserIds()).thenReturn(new LinkedHashSet<>(Arrays.asList("user1", "user2")));
    assertEquals("user2", userStateService.lastLogin().getUserId());
  }

  public void testIsOnline() throws Exception {
    when(continuationService.isPresent(SUPER_USER)).thenReturn(true);

    assertTrue(userStateService.isOnline(SUPER_USER));

    assertFalse(userStateService.isOnline("demo"));

    when(continuationService.isPresent("demo")).thenReturn(true);

    assertTrue(userStateService.isOnline("demo"));
  }

  private void loginUser(String userId, boolean hasPing) {
    Collection<MembershipEntry> membershipEntries = new ArrayList<MembershipEntry>();
    MembershipEntry membershipEntry = new MembershipEntry("/platform/administrators", "*");
    membershipEntries.add(membershipEntry);
    Identity identity = new Identity(userId, membershipEntries);
    ConversationState state = new ConversationState(identity);
    ConversationState.setCurrent(state);
    //
    if (hasPing) {
      userStateService.saveStatus(userId, UserStateService.DEFAULT_STATUS);
    }
  }
}
