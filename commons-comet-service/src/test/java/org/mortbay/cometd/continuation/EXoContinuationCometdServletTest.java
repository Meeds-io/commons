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

import java.util.Map;

import org.cometd.bayeux.server.ServerMessage;
import org.cometd.bayeux.server.ServerSession;
import org.cometd.oort.Oort;
import org.mockito.Mockito;

import org.junit.jupiter.api.Test;

class EXoContinuationCometdServletTest {

  @Test
  void testTheClusterIsTrustedOnlyWithItsOwnSecret() {
    assertTrue(EXoContinuationCometdServlet.isOortTrusted(true, "a-random-shared-value"));
    assertFalse(EXoContinuationCometdServlet.isOortTrusted(false, "a-random-shared-value"));
    assertFalse(EXoContinuationCometdServlet.isOortTrusted(true, null));
    assertFalse(EXoContinuationCometdServlet.isOortTrusted(true, ""));
    assertFalse(EXoContinuationCometdServlet.isOortTrusted(true, "  "));
    assertFalse(EXoContinuationCometdServlet.isOortTrusted(true, "exo"));
    assertFalse(EXoContinuationCometdServlet.isOortTrusted(true, " exo "));
  }

  @Test
  void testTheTrustIsSetOnTheBayeuxFromTheSecretTheOortHolds() {
    EXoContinuationBayeux bayeux = new EXoContinuationBayeux();
    Oort oort = Mockito.mock(Oort.class);
    Mockito.when(oort.getSecret()).thenReturn("a-random-shared-value");
    assertTrue(EXoContinuationCometdServlet.applyOortTrust(bayeux, oort, true));
    assertTrue(bayeux.isOortTrusted());
    assertFalse(EXoContinuationCometdServlet.applyOortTrust(bayeux, oort, false));
    assertFalse(bayeux.isOortTrusted());
    Mockito.when(oort.getSecret()).thenReturn("exo");
    assertFalse(EXoContinuationCometdServlet.applyOortTrust(bayeux, oort, true));
    assertFalse(bayeux.isOortTrusted());
    assertFalse(EXoContinuationCometdServlet.applyOortTrust(bayeux, null, true));
  }

  @Test
  void testATrustedNodeAcceptsThePeerItsOortRecognizes() {
    EXoContinuationBayeux bayeux = new EXoContinuationBayeux();
    Oort oort = Mockito.mock(Oort.class);
    ServerMessage handshake = Mockito.mock(ServerMessage.class);
    Mockito.when(oort.getSecret()).thenReturn("a-random-shared-value");
    Mockito.when(handshake.getExt()).thenReturn(Map.of(Oort.EXT_OORT_FIELD, Map.of()));
    Mockito.when(oort.isOortHandshake(handshake)).thenReturn(true);

    EXoContinuationCometdServlet.applyOortTrust(bayeux, oort, true);

    assertTrue(new EXoContinuationBayeux.EXoSecurityPolicy(bayeux).canHandshake(bayeux,
                                                                               Mockito.mock(ServerSession.class),
                                                                               handshake));
  }
}
