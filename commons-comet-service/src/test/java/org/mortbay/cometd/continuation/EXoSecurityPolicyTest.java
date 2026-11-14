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
import static org.mockito.Mockito.when;

import org.cometd.bayeux.server.ServerChannel;
import org.cometd.bayeux.server.ServerMessage;
import org.cometd.bayeux.server.ServerSession;
import java.util.Map;

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
}
