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

import org.exoplatform.services.listener.Asynchronous;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.user.UserStateModel;
import org.exoplatform.services.user.UserStateWSMessage;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;

import static org.exoplatform.services.user.UserStateService.*;

@Asynchronous
public class UserLogoutListener extends Listener<ConversationRegistry, ConversationState> {

  private final ContinuationService continuationService;

  public UserLogoutListener(ContinuationService continuationService) {
    this.continuationService = continuationService;
  }

  @Override
  public void onEvent(Event<ConversationRegistry, ConversationState> event) {
    ConversationState state = event.getData();
    ConversationRegistry conversationRegistry = event.getSource();
    String userId = getUserIdFromState(state);

    if (userId != null && conversationRegistry.getStateKeys(userId).isEmpty()) {
      UserStateModel offlineState = new UserStateWSMessage(new UserStateModel(userId, 0, STATUS_OFFLINE), USER_STATUS_UPDATED);
      continuationService.sendBroadcastMessage(COMETD_CHANNEL, offlineState);
    }
  }

  private String getUserIdFromState(ConversationState state) {
    if (state == null || state.getIdentity() == null) {
      return null;
    }
    return state.getIdentity().getUserId();
  }
}
