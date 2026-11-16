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

import org.cometd.bayeux.Channel;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.ServerMessage;
import org.cometd.bayeux.server.ServerSession;

/**
 * Ends the session of a user disabled or deleted since the handshake, on any
 * node, at its next connect, the request every open client sends again at each
 * long poll. The delivery of a message to such a session is checked by
 * {@link GoneUserSessionExtension}, which runs for each subscriber of a
 * broadcast, as a server-wide extension does not.
 * <p>
 * The check goes through {@link EXoContinuationBayeux#isSessionUserActive},
 * which reads the user only when the user's generation in the conversation
 * registry differs from the session's. A session that holds no user of the
 * store and a local session pass unchanged.
 */
public class GoneUserExtension implements BayeuxServer.Extension {

  private final EXoContinuationBayeux bayeux;

  public GoneUserExtension(EXoContinuationBayeux bayeux) {
    this.bayeux = bayeux;
  }

  @Override
  public boolean rcvMeta(ServerSession session, ServerMessage.Mutable message) {
    if (session == null
        || session.isLocalSession()
        || !Channel.META_CONNECT.equals(message.getChannel())
        || bayeux.isSessionUserActive(session)) {
      return true;
    }
    session.disconnect();
    return false;
  }

}
