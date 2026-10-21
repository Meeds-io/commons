/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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
package io.meeds.commons.http;

import java.net.UnknownHostException;

/**
 * Thrown by {@link SafeAddressGuard#resolveAllowed} for a host resolving to an
 * address the platform must not reach. An {@link UnknownHostException} so that
 * it travels out of the HTTP client's connection code like a resolution
 * failure, and a type of its own so that it is told apart from one.
 */
public class RefusedAddressException extends UnknownHostException {

  private static final long serialVersionUID = 4139583107263351862L;

  /**
   * Builds the exception; the message never names the host or the address.
   */
  public RefusedAddressException() {
    super("The URL points at an address the platform may not reach");
  }

}
