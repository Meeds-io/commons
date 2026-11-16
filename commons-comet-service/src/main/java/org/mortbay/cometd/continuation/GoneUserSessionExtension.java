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

import org.cometd.bayeux.server.ServerMessage;
import org.cometd.bayeux.server.ServerSession;

/**
 * Delivers no message to the session of a user disabled or deleted since the
 * handshake, on any node. It is added to each session that holds a user of the
 * store, at the handshake, because only a session's own extensions run for each
 * subscriber of a broadcast: the server-wide extensions see a broadcast once,
 * at its publication, with no target session.
 * <p>
 * It runs for a direct delivery as well as for a broadcast, so it is the one
 * check of a message's delivery. A non-meta message to such a session is
 * dropped and the session is disconnected. {@code disconnect()} removes the
 * session, queues the {@code /meta/disconnect} reply and flushes the session,
 * which wakes the held long poll: the held connection returns at once when the
 * disconnect happens on a delivery to it. Only a held connection that no
 * message reaches goes on until its next connect, which
 * {@link GoneUserExtension} ends, or until its poll timeout.
 * <p>
 * The check goes through {@link EXoContinuationBayeux#isSessionUserActive},
 * which reads the user only when the user's generation in the conversation
 * registry differs from the session's.
 */
public class GoneUserSessionExtension implements ServerSession.Extension {

  private final EXoContinuationBayeux bayeux;

  public GoneUserSessionExtension(EXoContinuationBayeux bayeux) {
    this.bayeux = bayeux;
  }

  @Override
  public ServerMessage send(ServerSession sender, ServerSession session, ServerMessage message) {
    if (session == null
        || session.isLocalSession()
        || bayeux.isSessionUserActive(session)) {
      return message;
    }
    session.disconnect();
    return null;
  }

}
