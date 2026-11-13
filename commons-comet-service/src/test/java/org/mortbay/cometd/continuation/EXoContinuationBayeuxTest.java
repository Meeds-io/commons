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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.cometd.oort.Oort;
import org.cometd.oort.Seti;
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
}
