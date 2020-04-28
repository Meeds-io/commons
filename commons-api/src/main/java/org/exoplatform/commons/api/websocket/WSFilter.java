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
import javax.websocket.EndpointConfig;
import javax.websocket.Session;

import org.exoplatform.commons.api.websocket.ExtensibleWSFilter.ExtensibleFilterChain;

public interface WSFilter {
  
  public void onOpen(Session wsSession, EndpointConfig config, ExtensibleFilterChain chain);

  public void onClose(Session wsSession, CloseReason closeReason, ExtensibleFilterChain chain);

  public void onError(Session wsSession, Throwable failure, ExtensibleFilterChain chain);

  public void onMessage(Session wsSession, Object data, ExtensibleFilterChain chain);
  
  public void onMessage(Session wsSession, Object arg0, boolean arg1, ExtensibleFilterChain chain);
}
