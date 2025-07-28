/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2025 Meeds Association contact@meeds.io
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
package org.exoplatform.listener;

import org.exoplatform.services.listener.Event;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.user.UserStateWSMessage;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.exoplatform.services.user.UserStateService.COMETD_CHANNEL;
import static org.exoplatform.services.user.UserStateService.STATUS_OFFLINE;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserLogoutListenerTest {

  @Mock
  private ContinuationService                            continuationService;

  @InjectMocks
  private UserLogoutListener                             listener;

  @Mock
  private Event<ConversationRegistry, ConversationState> event;

  @Mock
  private ConversationState                              conversationState;

  @Mock
  private Identity                                       identity;

  @Mock
  private ConversationRegistry                           conversationRegistry;

  @Captor
  private ArgumentCaptor<UserStateWSMessage>             wsMessageCaptor;

  @Test
  public void testBroadcastOfflineStatus() {
    String userId = "john";
    when(event.getData()).thenReturn(conversationState);
    when(event.getSource()).thenReturn(conversationRegistry);
    when(conversationState.getIdentity()).thenReturn(identity);
    when(identity.getUserId()).thenReturn(userId);
    when(conversationRegistry.getStateKeys(userId)).thenReturn(Collections.emptyList());

    listener.onEvent(event);

    verify(continuationService).sendBroadcastMessage(eq(COMETD_CHANNEL), wsMessageCaptor.capture());
    UserStateWSMessage message = wsMessageCaptor.getValue();
    assertNotNull(message);

    assertEquals("user-status-updated", message.getWsEventName());

    assertEquals(userId, message.getUserId());
    assertEquals(STATUS_OFFLINE, message.getStatus());

    reset(continuationService);

    when(event.getData()).thenReturn(conversationState);
    when(conversationState.getIdentity()).thenReturn(identity);
    when(identity.getUserId()).thenReturn(null);
    listener.onEvent(event);
    verifyNoInteractions(continuationService);
  }
}
