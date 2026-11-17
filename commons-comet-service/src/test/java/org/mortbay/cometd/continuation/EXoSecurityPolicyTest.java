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
package org.mortbay.cometd.continuation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.cometd.bayeux.server.ServerChannel;
import org.cometd.bayeux.server.ServerMessage;
import org.cometd.bayeux.server.ServerSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.cometd.oort.Oort;
import org.cometd.oort.Seti;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EXoSecurityPolicyTest {

  @Mock
  private Oort                                        oort;

  @Mock
  private ServerSession                               client;

  @Mock
  private ServerMessage                               message;

  @Mock
  private ServerChannel                               channel;

  @Mock
  private Seti                                        seti;

  private EXoContinuationBayeux                       bayeux;

  private EXoContinuationBayeux.EXoSecurityPolicy     policy;

  @BeforeEach
  void setUp() {
    bayeux = new EXoContinuationBayeux();
    bayeux.setOort(oort);
    bayeux.setSeti(seti);
    policy = new EXoContinuationBayeux.EXoSecurityPolicy(bayeux);
    when(message.getExt()).thenReturn(Map.of(Oort.EXT_OORT_FIELD, Map.of(Oort.EXT_COMET_URL_FIELD, "")));
    when(client.getId()).thenReturn("clientId");
    when(oort.isOortHandshake(message)).thenReturn(true);
    when(oort.isOort(client)).thenReturn(true);
  }

  @Test
  void testAnOortHandshakeIsRefusedUnlessTheClusterIsTrusted() {
    assertFalse(policy.canHandshake(bayeux, client, message));
    bayeux.setOortTrusted(true);
    assertTrue(policy.canHandshake(bayeux, client, message));
  }

  @Test
  void testAnOortSessionCannotSubscribeUnlessTheClusterIsTrusted() {
    assertFalse(policy.canSubscribe(bayeux, client, channel, message));
    bayeux.setOortTrusted(true);
    assertTrue(policy.canSubscribe(bayeux, client, channel, message));
  }

  @Test
  void testATrustedClusterStillRefusesAHandshakeOortDoesNotRecognize() {
    bayeux.setOortTrusted(true);
    when(oort.isOortHandshake(message)).thenReturn(false);
    when(oort.isOort(client)).thenReturn(false);
    assertFalse(policy.canHandshake(bayeux, client, message));
    assertFalse(policy.canSubscribe(bayeux, client, channel, message));
  }

  @Test
  void testNoOortRefusesWithoutFailing() {
    bayeux.setOort(null);
    bayeux.setOortTrusted(true);
    assertFalse(policy.canHandshake(bayeux, client, message));
    assertFalse(policy.canSubscribe(bayeux, client, channel, message));
  }

  @Test
  void testAUserHandshakeCarryingTheClusterExtensionNeedsTheClusterSecret() {
    String token = bayeux.getUserToken("john");
    when(message.get("exoId")).thenReturn("john");
    when(message.get("exoToken")).thenReturn(token);
    when(oort.isOortHandshake(message)).thenReturn(false);
    bayeux.setOortTrusted(true);

    assertFalse(policy.canHandshake(bayeux, client, message));

    when(message.getExt()).thenReturn(null);
    assertTrue(policy.canHandshake(bayeux, client, message));
  }

  @Test
  void testSessionsAddedAndRemovedConcurrentlyLeaveNoUserBehind() throws Exception {
    int users = 50;
    List<ServerSession> sessions = new ArrayList<>();
    List<ServerMessage> handshakes = new ArrayList<>();
    for (int i = 0; i < users; i++) {
      String userId = "concurrent-user-" + i;
      ServerSession session = mock(ServerSession.class);
      when(session.getId()).thenReturn("concurrent-session-" + i);
      ServerMessage handshake = mock(ServerMessage.class);
      when(handshake.get("exoId")).thenReturn(userId);
      when(handshake.get("exoToken")).thenReturn(bayeux.getUserToken(userId));
      sessions.add(session);
      handshakes.add(handshake);
    }
    ExecutorService threads = Executors.newFixedThreadPool(5);
    try {
      for (int round = 0; round < 200; round++) {
        List<Future<?>> tasks = new ArrayList<>();
        tasks.add(threads.submit(() -> {
          for (int i = 0; i < users; i++) {
            assertTrue(policy.canHandshake(bayeux, sessions.get(i), handshakes.get(i)));
          }
        }));
        for (int remover = 0; remover < 2; remover++) {
          tasks.add(threads.submit(() -> {
            for (int i = users - 1; i >= 0; i--) {
              policy.removed(sessions.get(i), null, false);
            }
          }));
        }
        for (int reader = 0; reader < 2; reader++) {
          tasks.add(threads.submit(() -> {
            for (int i = 0; i < users * 4; i++) {
              bayeux.getConnectedUserIds().forEach(userId -> assertTrue(userId != null));
            }
          }));
        }
        for (Future<?> task : tasks) {
          // Fails with the exception a thread met, a concurrent modification among them
          task.get(30, TimeUnit.SECONDS);
        }
        for (int i = 0; i < users; i++) {
          policy.removed(sessions.get(i), null, false);
        }
        assertEquals(0,
                     bayeux.getConnectedUserIds().stream().filter(userId -> userId.startsWith("concurrent-user-")).count());
      }
    } finally {
      threads.shutdownNow();
    }
  }

  @Test
  void testASessionThatStartsWhileAnotherOfTheSameUserEndsIsKept() throws Exception {
    String userId = "two-sessions-user";
    ServerSession ending = mock(ServerSession.class);
    when(ending.getId()).thenReturn("ending-session");
    ServerSession starting = mock(ServerSession.class);
    when(starting.getId()).thenReturn("starting-session");
    ServerMessage handshake = mock(ServerMessage.class);
    when(handshake.get("exoId")).thenReturn(userId);
    when(handshake.get("exoToken")).thenReturn(bayeux.getUserToken(userId));
    ExecutorService threads = Executors.newFixedThreadPool(2);
    try {
      for (int round = 0; round < 5000; round++) {
        assertTrue(policy.canHandshake(bayeux, ending, handshake));
        CyclicBarrier together = new CyclicBarrier(2);
        Future<?> end = threads.submit(() -> {
          together.await();
          policy.removed(ending, null, false);
          return null;
        });
        Future<?> start = threads.submit(() -> {
          together.await();
          assertTrue(policy.canHandshake(bayeux, starting, handshake));
          return null;
        });
        end.get(30, TimeUnit.SECONDS);
        start.get(30, TimeUnit.SECONDS);
        assertTrue(bayeux.isSubscribed(userId, "starting-session"), "round " + round);
        assertTrue(bayeux.getConnectedUserIds().contains(userId), "round " + round);
        policy.removed(starting, null, false);
      }
    } finally {
      threads.shutdownNow();
    }
  }
}
