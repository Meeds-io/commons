/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.commons.api.websocket;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;

public abstract class AbstractEndpoint extends Endpoint implements MessageHandler.Whole<String>,
    MessageHandler.Partial<String> {
  protected volatile Session _session;

  private ExtensibleWSFilter filter;

  @Override
  public void onOpen(Session wsSession, EndpointConfig config) {
    _session = wsSession;
    wsSession.addMessageHandler(this);
    getFilter().onOpen(wsSession, config, this);
  }

  @Override
  public void onClose(Session wsSession, CloseReason closeReason) {
    _session = wsSession;
    getFilter().onClose(wsSession, closeReason, this);
  }

  @Override
  public void onError(Session wsSession, Throwable failure) {
    _session = wsSession;
    getFilter().onError(wsSession, failure, this);
  }

  @Override
  public void onMessage(String message) {
    getFilter().onMessage(message, this);
  }

  @Override
  public void onMessage(String message, boolean arg1) {
    getFilter().onMessage(message, arg1, this);
  }

  private ExtensibleWSFilter getFilter() {
    if (filter == null) {
      PortalContainer container = RootContainer.getInstance().getPortalContainer(PortalContainer.getCurrentPortalContainerName());
      filter = (ExtensibleWSFilter) container.getComponentInstanceOfType(ExtensibleWSFilter.class);
    }
    return filter;
  }

  protected abstract void doOpen(Session wsSession, EndpointConfig config);

  protected abstract void doClose(Session wsSession, CloseReason closeReason);

  protected abstract void doError(Session wsSession, Throwable failure);

  protected abstract void doMessage(Session wsSession, String message);

  protected abstract void doMessage(Session wsSession, String message, boolean arg1);
}
