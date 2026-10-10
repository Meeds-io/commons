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

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Resolves a host name to its addresses, for {@link SafeAddressGuard}: the
 * JDK's resolver in production, a table in the tests.
 */
@FunctionalInterface
public interface HostResolver {

  /**
   * Resolves a host.
   *
   * @param host the host, an IPv6 literal without brackets
   * @return every address the host resolves to
   * @throws UnknownHostException when it resolves to nothing
   */
  InetAddress[] resolve(String host) throws UnknownHostException;

}
