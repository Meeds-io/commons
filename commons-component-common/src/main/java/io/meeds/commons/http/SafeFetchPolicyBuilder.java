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
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

/**
 * Builds a {@link SafeFetchPolicy}, starting from the defaults and refusing
 * what no policy may hold: a scheme the HTTP client does not speak, a port
 * outside 1-65535, a non-positive limit or timeout.
 */
public final class SafeFetchPolicyBuilder {

  /** The most digits a port has: longer, it is above 65535 or padded. */
  private static final int MAX_PORT_DIGITS          = 5;

  private String           name                     = SafeFetchPolicy.DEFAULT_NAME;

  private String           userAgent                = SafeFetchPolicy.DEFAULT_USER_AGENT;

  private Set<String>      allowedSchemes           = SafeFetchPolicy.HTTP_SCHEMES;

  private Set<Integer>     allowedPorts             = SafeFetchPolicy.DEFAULT_PORTS;

  private boolean          anyPortAllowed;

  private boolean          internalAddressesAllowed;

  private Set<String>      acceptedContentTypes     = Set.of();

  private long             maxBytes                 = SafeFetchPolicy.DEFAULT_MAX_BYTES;

  private int              maxRedirects             = SafeFetchPolicy.DEFAULT_MAX_REDIRECTS;

  private Duration         connectTimeout           = SafeFetchPolicy.DEFAULT_CONNECT;

  private Duration         readTimeout              = SafeFetchPolicy.DEFAULT_READ;

  private Duration         totalTimeout             = SafeFetchPolicy.DEFAULT_TOTAL;

  private int              maxConnectionsTotal      = 20;

  private int              maxConnectionsPerRoute   = 2;

  private HostResolver     resolver                 = InetAddress::getAllByName;

  private Set<String>      exemptHosts              = Set.of();

  private Set<InetAddress> exemptAddresses          = Set.of();

  /**
   * A builder holding the defaults; {@link SafeFetchPolicy#builder()} is the
   * way to one.
   */
  SafeFetchPolicyBuilder() {
    // the defaults are the field initializers
  }

  /**
   * Names the fetcher, and so its deadline thread.
   *
   * @param fetcherName the name
   * @return this builder
   */
  public SafeFetchPolicyBuilder name(String fetcherName) {
    this.name = requireText(fetcherName, "name");
    return this;
  }

  /**
   * Sets the User-Agent sent with every request.
   *
   * @param agent the header value
   * @return this builder
   */
  public SafeFetchPolicyBuilder userAgent(String agent) {
    this.userAgent = requireText(agent, "userAgent");
    return this;
  }

  /**
   * Sets the schemes a URL may use, within {@link SafeFetchPolicy#HTTP_SCHEMES}.
   *
   * @param schemes the schemes, case ignored
   * @return this builder
   */
  public SafeFetchPolicyBuilder allowedSchemes(Collection<String> schemes) {
    Set<String> lowered = new LinkedHashSet<>();
    for (String scheme : requireNonEmpty(schemes, "allowedSchemes")) {
      String normalized = StringUtils.lowerCase(StringUtils.trim(scheme), Locale.ROOT);
      if (!SafeFetchPolicy.HTTP_SCHEMES.contains(normalized)) {
        throw new IllegalArgumentException("A fetch policy allows http and https only");
      }
      lowered.add(normalized);
    }
    this.allowedSchemes = lowered;
    return this;
  }

  /**
   * Allows https only.
   *
   * @return this builder
   */
  public SafeFetchPolicyBuilder httpsOnly() {
    return allowedSchemes(Set.of("https"));
  }

  /**
   * Sets the ports a URL may reach, the implicit default ports included.
   *
   * @param ports the ports, each in 1-65535
   * @return this builder
   */
  public SafeFetchPolicyBuilder allowedPorts(Collection<Integer> ports) {
    Set<Integer> checked = new LinkedHashSet<>();
    for (Integer port : requireNonEmpty(ports, "allowedPorts")) {
      if (port == null || port < 1 || port > 65535) {
        throw new IllegalArgumentException("A port is between 1 and 65535");
      }
      checked.add(port);
    }
    this.allowedPorts = checked;
    this.anyPortAllowed = false;
    return this;
  }

  /**
   * Reads a comma-separated list of ports, ignoring what is not one — a word, a
   * number outside 1-65535, however many digits it has — as a deployment
   * property states them; falls back to the given set when the list names no
   * usable port.
   *
   * @param ports the list, such as {@code 80,443,8443}
   * @param fallback the ports when the list names none
   * @return this builder
   */
  public SafeFetchPolicyBuilder allowedPorts(String ports, Collection<Integer> fallback) {
    Set<Integer> parsed = new LinkedHashSet<>();
    Arrays.stream(StringUtils.split(StringUtils.defaultString(ports), ','))
          .map(String::trim)
          .filter(entry -> StringUtils.isNumeric(entry) && entry.length() <= MAX_PORT_DIGITS)
          .map(Integer::parseInt)
          .filter(port -> port > 0 && port <= 65535)
          .forEach(parsed::add);
    return allowedPorts(parsed.isEmpty() ? fallback : parsed);
  }

  /**
   * Lets a URL reach any port. Stated, never implied: an empty port set is
   * refused, not read as this.
   *
   * @return this builder
   */
  public SafeFetchPolicyBuilder anyPort() {
    this.anyPortAllowed = true;
    this.allowedPorts = Set.of();
    return this;
  }

  /**
   * Lets the fetcher reach internal addresses: a deployment's opt-out.
   *
   * @param allowed whether they may be reached
   * @return this builder
   */
  public SafeFetchPolicyBuilder internalAddressesAllowed(boolean allowed) {
    this.internalAddressesAllowed = allowed;
    return this;
  }

  /**
   * Sets the media types an answer may declare; an answer declaring another,
   * or none, is refused before its body is read. Empty, the default, accepts
   * any. A request may narrow this set, never widen it.
   *
   * @param contentTypes the media types, without parameters, case ignored
   * @return this builder
   */
  public SafeFetchPolicyBuilder acceptedContentTypes(Collection<String> contentTypes) {
    this.acceptedContentTypes = normalizeContentTypes(contentTypes);
    return this;
  }

  /**
   * Sets the largest body read, in bytes: a ceiling a request may lower, never
   * raise.
   *
   * @param bytes the limit, positive
   * @return this builder
   */
  public SafeFetchPolicyBuilder maxBytes(long bytes) {
    if (bytes <= 0) {
      throw new IllegalArgumentException("maxBytes is positive");
    }
    this.maxBytes = bytes;
    return this;
  }

  /**
   * Sets the most redirects followed; zero follows none.
   *
   * @param redirects the limit, zero or more
   * @return this builder
   */
  public SafeFetchPolicyBuilder maxRedirects(int redirects) {
    if (redirects < 0) {
      throw new IllegalArgumentException("maxRedirects is zero or more");
    }
    this.maxRedirects = redirects;
    return this;
  }

  /**
   * Sets the longest wait for a connection.
   *
   * @param timeout the wait, positive
   * @return this builder
   */
  public SafeFetchPolicyBuilder connectTimeout(Duration timeout) {
    this.connectTimeout = requirePositive(timeout, "connectTimeout");
    return this;
  }

  /**
   * Sets the longest wait between two reads.
   *
   * @param timeout the wait, positive
   * @return this builder
   */
  public SafeFetchPolicyBuilder readTimeout(Duration timeout) {
    this.readTimeout = requirePositive(timeout, "readTimeout");
    return this;
  }

  /**
   * Sets the longest read of a URL, redirects included, enforced by cancelling
   * the request.
   *
   * @param timeout the deadline, positive
   * @return this builder
   */
  public SafeFetchPolicyBuilder totalTimeout(Duration timeout) {
    this.totalTimeout = requirePositive(timeout, "totalTimeout");
    return this;
  }

  /**
   * Sets the HTTP client's connection pool bounds.
   *
   * @param total the most connections kept open, positive
   * @param perRoute the most connections kept open to one host, positive
   * @return this builder
   */
  public SafeFetchPolicyBuilder maxConnections(int total, int perRoute) {
    if (total <= 0 || perRoute <= 0 || perRoute > total) {
      throw new IllegalArgumentException("Connection bounds are positive, per route at most the total");
    }
    this.maxConnectionsTotal = total;
    this.maxConnectionsPerRoute = perRoute;
    return this;
  }

  /**
   * Replaces name resolution: the seam of the tests, a table of names. Every
   * address it answers is still judged by the guard, so it cannot open what
   * the policy refuses.
   *
   * @param hostResolver the resolver
   * @return this builder
   */
  public SafeFetchPolicyBuilder resolver(HostResolver hostResolver) {
    this.resolver = Objects.requireNonNull(hostResolver, "resolver");
    return this;
  }

  /**
   * @return the policy
   */
  public SafeFetchPolicy build() {
    return new SafeFetchPolicy(this);
  }

  /**
   * Reads the addresses of these host names as public, whatever they are: the
   * seam of the tests, so that a stub on loopback answers for a "public" name
   * while the same stub under any other name stays refused. Package-private,
   * so that no production policy can exempt a name: a test outside this
   * package reaches it through a helper of its own in this package. Empty in
   * production.
   *
   * @param hosts the host names
   * @return this builder
   */
  SafeFetchPolicyBuilder exemptHosts(Collection<String> hosts) {
    this.exemptHosts = Set.copyOf(Objects.requireNonNull(hosts, "exemptHosts"));
    return this;
  }

  /**
   * Reads these addresses as public, whatever name they come from: the seam of
   * the tests, so that a stub on 127.0.0.1 can be reached while 127.0.0.2 and
   * every other internal address stay refused. Package-private, as
   * {@link #exemptHosts}. Empty in production.
   *
   * @param addresses the addresses
   * @return this builder
   */
  SafeFetchPolicyBuilder exemptAddresses(Collection<InetAddress> addresses) {
    this.exemptAddresses = Set.copyOf(Objects.requireNonNull(addresses, "exemptAddresses"));
    return this;
  }

  /**
   * Lower-cases media types and drops their parameters, so that an answer's
   * {@code Content-Type} compares by its media type alone.
   *
   * @param contentTypes the media types, null for none
   * @return the normalized set
   */
  static Set<String> normalizeContentTypes(Collection<String> contentTypes) {
    Set<String> normalized = new HashSet<>();
    if (contentTypes != null) {
      for (String contentType : contentTypes) {
        String mediaType = mediaTypeOf(contentType);
        if (mediaType != null) {
          normalized.add(mediaType);
        }
      }
    }
    return normalized;
  }

  /**
   * The media type of a {@code Content-Type} value: what stands before the
   * first {@code ;}, trimmed and lower-cased.
   *
   * @param contentType the header value
   * @return the media type, null when the value is blank
   */
  static String mediaTypeOf(String contentType) {
    String mediaType = StringUtils.lowerCase(StringUtils.trim(StringUtils.substringBefore(contentType, ";")), Locale.ROOT);
    return StringUtils.isBlank(mediaType) ? null : mediaType;
  }

  /**
   * @return the name
   */
  String getName() {
    return name;
  }

  /**
   * @return the User-Agent
   */
  String getUserAgent() {
    return userAgent;
  }

  /**
   * @return the schemes
   */
  Set<String> getAllowedSchemes() {
    return allowedSchemes;
  }

  /**
   * @return the ports
   */
  Set<Integer> getAllowedPorts() {
    return allowedPorts;
  }

  /**
   * @return whether any port is allowed
   */
  boolean isAnyPortAllowed() {
    return anyPortAllowed;
  }

  /**
   * @return whether internal addresses are allowed
   */
  boolean isInternalAddressesAllowed() {
    return internalAddressesAllowed;
  }

  /**
   * @return the accepted media types
   */
  Set<String> getAcceptedContentTypes() {
    return acceptedContentTypes;
  }

  /**
   * @return the body limit
   */
  long getMaxBytes() {
    return maxBytes;
  }

  /**
   * @return the redirect limit
   */
  int getMaxRedirects() {
    return maxRedirects;
  }

  /**
   * @return the connect timeout
   */
  Duration getConnectTimeout() {
    return connectTimeout;
  }

  /**
   * @return the read timeout
   */
  Duration getReadTimeout() {
    return readTimeout;
  }

  /**
   * @return the deadline
   */
  Duration getTotalTimeout() {
    return totalTimeout;
  }

  /**
   * @return the pool's total bound
   */
  int getMaxConnectionsTotal() {
    return maxConnectionsTotal;
  }

  /**
   * @return the pool's per-route bound
   */
  int getMaxConnectionsPerRoute() {
    return maxConnectionsPerRoute;
  }

  /**
   * @return the resolver
   */
  HostResolver getResolver() {
    return resolver;
  }

  /**
   * @return the exempt host names
   */
  Set<String> getExemptHosts() {
    return exemptHosts;
  }

  /**
   * @return the exempt addresses
   */
  Set<InetAddress> getExemptAddresses() {
    return exemptAddresses;
  }

  /**
   * A text value that is not blank.
   *
   * @param value the value
   * @param field its name, for the refusal
   * @return the value
   */
  private static String requireText(String value, String field) {
    if (StringUtils.isBlank(value)) {
      throw new IllegalArgumentException(field + " is not blank");
    }
    return value;
  }

  /**
   * A collection that is not empty.
   *
   * @param values the collection
   * @param field its name, for the refusal
   * @param <T> the element type
   * @return the collection
   */
  private static <T> Collection<T> requireNonEmpty(Collection<T> values, String field) {
    if (values == null || values.isEmpty()) {
      throw new IllegalArgumentException(field + " is not empty");
    }
    return values;
  }

  /**
   * A duration that is positive.
   *
   * @param duration the duration
   * @param field its name, for the refusal
   * @return the duration
   */
  private static Duration requirePositive(Duration duration, String field) {
    if (duration == null || duration.isZero() || duration.isNegative()) {
      throw new IllegalArgumentException(field + " is positive");
    }
    return duration;
  }

}
