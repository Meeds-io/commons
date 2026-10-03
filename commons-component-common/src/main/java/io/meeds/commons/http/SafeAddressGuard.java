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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

/**
 * Decides which URLs the platform may be made to read on a user's or a
 * sender's behalf, and which addresses it may connect to.
 * <p>
 * <b>Why.</b> A URL the server fetches for someone else — a subscribed
 * calendar, a sender's logo, an image an agent was given — would otherwise make
 * the platform a client of whatever sits on its own network: an administration
 * console on loopback, a database's HTTP port on a private address, the cloud
 * metadata endpoint handing out credentials.
 * <p>
 * <b>What it decides.</b> The URL's shape — an allowed scheme, no credentials,
 * a host, an allowed port ({@link #checkTarget}) — and the addresses its host
 * resolves to ({@link #resolveAllowed}): loopback, link-local (the metadata
 * address 169.254.169.254 among them), private, carrier-grade NAT, multicast,
 * broadcast and reserved, unspecified, IETF assignments, benchmarking, IPv6
 * unique-local and site-local, and each of those spelled as an IPv4 address
 * inside an IPv6 one (mapped, compatible, NAT64, 6to4) are refused unless the
 * policy allows internal addresses.
 * <p>
 * <b>Where the address is checked.</b> {@link #resolveAllowed} is the resolver
 * of {@link SafeHttpFetcher}'s connection manager ({@link GuardedDnsResolver}),
 * so it judges the very addresses the client then connects to, for every
 * connection it opens — every redirect hop included. A name that resolved to a
 * public address when it was first seen and to a private one when the
 * connection opens (DNS rebinding) is refused at that connection, not trusted
 * from an earlier answer. What stays outside its reach: a public server that
 * itself relays the request inward, and the time a refused or unreachable
 * answer takes, which says something about a public host.
 */
public class SafeAddressGuard {

  /** Longest URL accepted. */
  public static final int        MAX_URL_LENGTH = 2048;

  private final boolean          internalAddressesAllowed;

  private final Set<String>      allowedSchemes;

  private final Set<Integer>     allowedPorts;

  private final boolean          anyPortAllowed;

  private final HostResolver     resolver;

  private final Set<String>      exemptHosts;

  private final Set<InetAddress> exemptAddresses;

  /**
   * The guard of a policy.
   *
   * @param policy what may be read
   */
  public SafeAddressGuard(SafeFetchPolicy policy) {
    this.internalAddressesAllowed = policy.isInternalAddressesAllowed();
    this.allowedSchemes = policy.getAllowedSchemes();
    this.allowedPorts = policy.getAllowedPorts();
    this.anyPortAllowed = policy.isAnyPortAllowed();
    this.resolver = policy.getResolver();
    this.exemptHosts = policy.getExemptHosts();
    this.exemptAddresses = policy.getExemptAddresses();
  }

  /**
   * Reads a URL as it was given: trimmed, the fragment dropped, then checked
   * by {@link #checkTarget}. Nothing is resolved here.
   *
   * @param url the URL as given
   * @return the URL to read
   * @throws SafeFetchException with the reason it is refused
   */
  public URI normalize(String url) throws SafeFetchException {
    String trimmed = StringUtils.trim(url);
    if (StringUtils.isBlank(trimmed) || trimmed.length() > MAX_URL_LENGTH || containsUnsafeCharacter(trimmed)) {
      throw new SafeFetchException(SafeFetchFailure.INVALID_URL);
    }
    int fragment = trimmed.indexOf('#');
    if (fragment >= 0) {
      trimmed = trimmed.substring(0, fragment);
    }
    try {
      return checkTarget(new URI(trimmed));
    } catch (URISyntaxException e) {
      throw new SafeFetchException(SafeFetchFailure.INVALID_URL);
    }
  }

  /**
   * Checks the shape of a URL about to be read — the one given, or the target
   * of a redirect: absolute, an allowed scheme, no credentials, a host, an
   * allowed port, not too long, no white space or control character.
   *
   * @param uri the URL
   * @return the URL with its scheme lower-cased
   * @throws SafeFetchException with the reason it is refused
   */
  public URI checkTarget(URI uri) throws SafeFetchException {
    if (uri == null || !uri.isAbsolute()) {
      throw new SafeFetchException(SafeFetchFailure.INVALID_URL);
    }
    String text = uri.toString();
    if (text.length() > MAX_URL_LENGTH || containsUnsafeCharacter(text)) {
      throw new SafeFetchException(SafeFetchFailure.INVALID_URL);
    }
    // The scheme speaks before the shape: file:, ftp: or javascript: is better
    // reported as the wrong scheme than as an unreadable string
    String scheme = StringUtils.lowerCase(uri.getScheme(), Locale.ROOT);
    if (!allowedSchemes.contains(scheme)) {
      throw new SafeFetchException(SafeFetchFailure.SCHEME_NOT_ALLOWED);
    }
    if (uri.isOpaque()) {
      throw new SafeFetchException(SafeFetchFailure.INVALID_URL);
    }
    if (uri.getRawUserInfo() != null || StringUtils.contains(uri.getRawAuthority(), '@')) {
      throw new SafeFetchException(SafeFetchFailure.CREDENTIALS_IN_URL);
    }
    if (StringUtils.isBlank(uri.getHost())) {
      throw new SafeFetchException(SafeFetchFailure.INVALID_URL);
    }
    int port = uri.getPort() >= 0 ? uri.getPort() : ("http".equals(scheme) ? 80 : 443);
    if (!anyPortAllowed && !allowedPorts.contains(port)) {
      throw new SafeFetchException(SafeFetchFailure.PORT_NOT_ALLOWED);
    }
    if (scheme.equals(uri.getScheme())) {
      return uri;
    }
    try {
      return new URI(scheme + text.substring(uri.getScheme().length()));
    } catch (URISyntaxException e) {
      throw new SafeFetchException(SafeFetchFailure.INVALID_URL);
    }
  }

  /**
   * Resolves a host and refuses it when ANY of its addresses is one the platform
   * must not reach — a name answering one public and one loopback address is a
   * name reaching loopback. This is the HTTP client's resolver: its answer is
   * what the client connects to.
   *
   * @param host the host, brackets of an IPv6 literal allowed
   * @return the addresses, every one allowed
   * @throws RefusedAddressException when an address is refused
   * @throws UnknownHostException when the host resolves to nothing
   */
  public InetAddress[] resolveAllowed(String host) throws UnknownHostException {
    String bare = StringUtils.removeEnd(StringUtils.removeStart(StringUtils.trim(host), "["), "]");
    if (StringUtils.isBlank(bare)) {
      throw new UnknownHostException("No host");
    }
    InetAddress[] addresses = resolver.resolve(bare);
    if (addresses == null || addresses.length == 0) {
      throw new UnknownHostException("The host resolves to nothing");
    }
    if (!internalAddressesAllowed && !exemptHosts.contains(bare)) {
      for (InetAddress address : addresses) {
        if (isBlocked(address) && !exemptAddresses.contains(address)) {
          throw new RefusedAddressException();
        }
      }
    }
    return addresses;
  }

  /**
   * Whether one address is outside what a fetched URL may point at.
   * <p>
   * The JDK predicates cover the any-local, loopback, link-local, site-local
   * (RFC 1918 in IPv4, fec0::/10 in IPv6) and multicast addresses; the byte
   * inspection covers the rest, and the IPv4 address an IPv6 address may carry,
   * which is how {@code ::ffff:127.0.0.1} or {@code 64:ff9b::a9fe:a9fe} would
   * otherwise walk past a loopback or metadata check.
   *
   * @param address one resolved address
   * @return true when the platform must not reach it
   */
  public static boolean isBlocked(InetAddress address) {
    if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
        || address.isSiteLocalAddress() || address.isMulticastAddress()) {
      return true;
    }
    byte[] bytes = address.getAddress();
    if (bytes.length == 4) {
      return isBlockedIpv4(bytes);
    }
    if (bytes.length != 16) {
      return true;
    }
    if ((bytes[0] & 0xFE) == 0xFC) {
      // fc00::/7, unique local addresses, which isSiteLocalAddress does not know
      return true;
    }
    byte[] embedded = embeddedIpv4(bytes);
    return embedded != null && isBlockedIpv4(embedded);
  }

  /**
   * Whether four bytes name an IPv4 address the platform must not reach.
   *
   * @param bytes an IPv4 address
   * @return true when refused
   */
  private static boolean isBlockedIpv4(byte[] bytes) {
    int first = bytes[0] & 0xFF;
    int second = bytes[1] & 0xFF;
    return first == 0                                        // 0.0.0.0/8, "this network"
        || first == 10                                       // RFC 1918
        || first == 127                                      // loopback
        || first >= 224                                      // multicast, reserved, broadcast
        || (first == 100 && second >= 64 && second <= 127)   // RFC 6598 carrier-grade NAT
        || (first == 169 && second == 254)                   // link-local, cloud metadata
        || (first == 172 && second >= 16 && second <= 31)    // RFC 1918
        || (first == 192 && second == 168)                   // RFC 1918
        || (first == 192 && second == 0 && (bytes[2] & 0xFF) == 0) // IETF protocol assignments
        || (first == 198 && (second == 18 || second == 19)); // benchmarking
  }

  /**
   * The IPv4 address carried by an IPv6 one, when it carries one: IPv4-mapped
   * {@code ::ffff:a.b.c.d}, IPv4-compatible {@code ::a.b.c.d}, NAT64
   * {@code 64:ff9b::a.b.c.d} and 6to4 {@code 2002:aabb:ccdd::}.
   *
   * @param bytes an IPv6 address
   * @return the four bytes of the IPv4 address, or null when none
   */
  private static byte[] embeddedIpv4(byte[] bytes) {
    if ((bytes[0] & 0xFF) == 0x20 && (bytes[1] & 0xFF) == 0x02) {
      return new byte[] { bytes[2], bytes[3], bytes[4], bytes[5] };
    }
    boolean nat64 = bytes[0] == 0 && (bytes[1] & 0xFF) == 0x64 && (bytes[2] & 0xFF) == 0xFF && (bytes[3] & 0xFF) == 0x9B;
    for (int i = nat64 ? 4 : 0; i < 10; i++) {
      if (bytes[i] != 0) {
        return null; // NOSONAR null is "carries none"
      }
    }
    boolean mapped = (bytes[10] & 0xFF) == 0xFF && (bytes[11] & 0xFF) == 0xFF;
    boolean zeroes = bytes[10] == 0 && bytes[11] == 0;
    if ((nat64 && !zeroes) || (!mapped && !zeroes)) {
      return null; // NOSONAR as above
    }
    return new byte[] { bytes[12], bytes[13], bytes[14], bytes[15] };
  }

  /**
   * Whether a URL carries white space or a control character, which no usable
   * URL does and which a request line, a header or a log line must never
   * receive.
   *
   * @param url the URL
   * @return true when it does
   */
  private static boolean containsUnsafeCharacter(String url) {
    for (int i = 0; i < url.length(); i++) {
      char c = url.charAt(i);
      if (Character.isWhitespace(c) || Character.isISOControl(c)) {
        return true;
      }
    }
    return false;
  }

}
