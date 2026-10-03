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

import org.apache.hc.client5.http.DnsResolver;

/**
 * The HTTP client's name resolution, routed through a {@link SafeAddressGuard}:
 * the connection manager calls it when it opens a connection, so the addresses
 * it answers are the ones the socket dials, and a refused name never yields a
 * socket. This is what closes the gap between a URL checked once and a
 * connection made later from a fresh, unchecked lookup.
 */
final class GuardedDnsResolver implements DnsResolver {

  private final SafeAddressGuard guard;

  /**
   * The resolver of a guard.
   *
   * @param guard what decides the addresses
   */
  GuardedDnsResolver(SafeAddressGuard guard) {
    this.guard = guard;
  }

  /**
   * Resolves a host through the guard, refusing the addresses it refuses.
   *
   * @param host the host
   * @return the allowed addresses
   * @throws UnknownHostException when unresolvable or refused
   */
  @Override
  public InetAddress[] resolve(String host) throws UnknownHostException {
    return guard.resolveAllowed(host);
  }

  /**
   * Answers the host itself once the guard allows its addresses.
   *
   * @param host the host
   * @return the host
   * @throws UnknownHostException when unresolvable or refused
   */
  @Override
  public String resolveCanonicalHostname(String host) throws UnknownHostException {
    guard.resolveAllowed(host);
    return host;
  }

}
