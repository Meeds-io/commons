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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.cometd.bayeux.server.LocalSession;
import org.cometd.bayeux.server.ServerSession;
import org.cometd.oort.Oort;
import org.cometd.oort.Seti;
import org.cometd.server.BayeuxServerImpl;
import org.cometd.server.http.JSONHttpTransport;
import org.junit.Test;

public class EXoContinuationBayeuxTest {

  private static final String CHANNEL = "/eXo/Application/Test/document/**";

  @Test
  public void testAChannelObservedBeforeOortIsSetIsObservedOnceItIs() {
    EXoContinuationBayeux bayeux = new EXoContinuationBayeux();
    Oort oort = mock(Oort.class);

    // A service starts before the CometD servlet sets Oort
    bayeux.observeChannel(CHANNEL);
    bayeux.setOort(oort);

    verify(oort).observeChannel(CHANNEL);
  }

  @Test
  public void testAChannelObservedOnceOortIsSetIsObservedAtOnce() {
    EXoContinuationBayeux bayeux = new EXoContinuationBayeux();
    Oort oort = mock(Oort.class);
    bayeux.setOort(oort);

    bayeux.observeChannel(CHANNEL);

    verify(oort).observeChannel(CHANNEL);
  }

  @Test
  public void testTheUsersConnectedToAnotherNodeAreConnectedUsers() {
    EXoContinuationBayeux bayeux = new EXoContinuationBayeux();
    Seti seti = mock(Seti.class);
    when(seti.getUserIds()).thenReturn(Set.of("john"));
    bayeux.setSeti(seti);

    assertTrue(bayeux.getConnectedUserIds().contains("john"));
  }

  @Test
  public void testAMessageOortRepublishedIsRelayedAndAClientsIsNot() {
    EXoContinuationBayeux bayeux = new EXoContinuationBayeux();
    ServerSession oortSession = mock(ServerSession.class);
    ServerSession clientSession = mock(ServerSession.class);
    Oort oort = mock(Oort.class);
    when(oort.isOort(oortSession)).thenReturn(true);
    assertFalse(bayeux.isRelayed(oortSession));

    bayeux.setOort(oort);

    assertTrue(bayeux.isRelayed(oortSession));
    assertFalse(bayeux.isRelayed(clientSession));
  }

  @Test
  public void testABroadcastReachesTheSubscribersOfAPatternThatMatchesItsChannel() throws Exception {
    EXoContinuationBayeux bayeux = new EXoContinuationBayeux();
    bayeux.setOption(BayeuxServerImpl.TRANSPORTS_OPTION, JSONHttpTransport.class.getName());
    bayeux.start();
    try {
      // As another node's Oort client, subscribed to the pattern only
      List<Object> received = new CopyOnWriteArrayList<>();
      LocalSession otherNode = bayeux.newLocalSession("otherNode");
      otherNode.handshake();
      otherNode.getChannel(CHANNEL).subscribe((channel, message) -> received.add(message.getData()));

      bayeux.sendBroadcastMessage("/eXo/Application/Test/document/42", "saved", null);

      assertEquals(List.of("saved"), received);
    } finally {
      bayeux.stop();
    }
  }

  @Test
  public void testAnActionOnOortRunsOnceOortIsSetAndAtOnceAfter() {
    EXoContinuationBayeux bayeux = new EXoContinuationBayeux();
    Oort oort = mock(Oort.class);
    List<Oort> before = new CopyOnWriteArrayList<>();
    List<Oort> after = new CopyOnWriteArrayList<>();

    bayeux.onOort(before::add);
    assertEquals(List.of(), before);
    bayeux.setOort(oort);
    bayeux.onOort(after::add);

    assertEquals(List.of(oort), before);
    assertEquals(List.of(oort), after);
  }

  @Test
  public void testAFailedActionOnOortLeavesTheOthersRun() {
    EXoContinuationBayeux bayeux = new EXoContinuationBayeux();
    Oort oort = mock(Oort.class);
    List<Oort> ran = new CopyOnWriteArrayList<>();
    bayeux.onOort(o -> {
      throw new IllegalStateException("failed");
    });
    bayeux.onOort(ran::add);

    bayeux.setOort(oort);

    assertEquals(List.of(oort), ran);
  }
}
