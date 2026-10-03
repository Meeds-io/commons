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
import java.util.Set;

/**
 * What a {@link SafeHttpFetcher} may read and how far it goes: the schemes and
 * ports a URL may use, whether internal addresses may be reached at all, the
 * content types accepted, the most bytes read, the most redirects followed,
 * the timeouts, and the HTTP client's own bounds. Built with
 * {@link SafeFetchPolicyBuilder}; immutable.
 * <p>
 * The {@linkplain #getResolver() resolver} and the two exemptions are the
 * seams of the tests, so that a stub server on loopback can stand in for a
 * public one while every other internal address stays refused; a production
 * policy leaves them at their defaults.
 */
public final class SafeFetchPolicy {

  /** The scheme set a policy may draw from: the HTTP client speaks nothing else. */
  public static final Set<String>  HTTP_SCHEMES          = Set.of("http", "https");

  /** The ports a URL may reach unless the policy says otherwise. */
  public static final Set<Integer> DEFAULT_PORTS         = Set.of(80, 443, 8080, 8443);

  /** Largest body read unless the policy says otherwise, in bytes. */
  public static final long         DEFAULT_MAX_BYTES     = 10L * 1024 * 1024;

  /** Most redirects followed unless the policy says otherwise. */
  public static final int          DEFAULT_MAX_REDIRECTS = 5;

  /** Longest wait for a connection unless the policy says otherwise. */
  public static final Duration     DEFAULT_CONNECT       = Duration.ofSeconds(10);

  /** Longest wait between two reads unless the policy says otherwise. */
  public static final Duration     DEFAULT_READ          = Duration.ofSeconds(20);

  /** Longest read of a URL, redirects included, unless the policy says otherwise. */
  public static final Duration     DEFAULT_TOTAL         = Duration.ofSeconds(30);

  /** The name of a fetcher built without one; names its deadline thread. */
  public static final String       DEFAULT_NAME          = "meeds-safe-http-fetcher";

  /** The User-Agent sent unless the policy says otherwise. */
  public static final String       DEFAULT_USER_AGENT    = "Meeds-Platform-Fetcher/1.0";

  private final String             name;

  private final String             userAgent;

  private final Set<String>        allowedSchemes;

  private final Set<Integer>       allowedPorts;

  private final boolean            anyPortAllowed;

  private final boolean            internalAddressesAllowed;

  private final Set<String>        acceptedContentTypes;

  private final long               maxBytes;

  private final int                maxRedirects;

  private final Duration           connectTimeout;

  private final Duration           readTimeout;

  private final Duration           totalTimeout;

  private final int                maxConnectionsTotal;

  private final int                maxConnectionsPerRoute;

  private final HostResolver       resolver;

  private final Set<String>        exemptHosts;

  private final Set<InetAddress>   exemptAddresses;

  /**
   * Builds a policy from its builder, which validated every value.
   *
   * @param builder the builder
   */
  SafeFetchPolicy(SafeFetchPolicyBuilder builder) {
    this.name = builder.getName();
    this.userAgent = builder.getUserAgent();
    this.allowedSchemes = Set.copyOf(builder.getAllowedSchemes());
    this.allowedPorts = Set.copyOf(builder.getAllowedPorts());
    this.anyPortAllowed = builder.isAnyPortAllowed();
    this.internalAddressesAllowed = builder.isInternalAddressesAllowed();
    this.acceptedContentTypes = Set.copyOf(builder.getAcceptedContentTypes());
    this.maxBytes = builder.getMaxBytes();
    this.maxRedirects = builder.getMaxRedirects();
    this.connectTimeout = builder.getConnectTimeout();
    this.readTimeout = builder.getReadTimeout();
    this.totalTimeout = builder.getTotalTimeout();
    this.maxConnectionsTotal = builder.getMaxConnectionsTotal();
    this.maxConnectionsPerRoute = builder.getMaxConnectionsPerRoute();
    this.resolver = builder.getResolver();
    this.exemptHosts = Set.copyOf(builder.getExemptHosts());
    this.exemptAddresses = Set.copyOf(builder.getExemptAddresses());
  }

  /**
   * @return a builder holding the defaults
   */
  public static SafeFetchPolicyBuilder builder() {
    return new SafeFetchPolicyBuilder();
  }

  /**
   * @return the fetcher's name, which names its deadline thread
   */
  public String getName() {
    return name;
  }

  /**
   * @return the User-Agent sent with every request
   */
  public String getUserAgent() {
    return userAgent;
  }

  /**
   * @return the schemes a URL may use, lower-case, within {@link #HTTP_SCHEMES}
   */
  public Set<String> getAllowedSchemes() {
    return allowedSchemes;
  }

  /**
   * @return the ports a URL may reach, implicit default ports included; empty
   *         when {@link #isAnyPortAllowed()}
   */
  public Set<Integer> getAllowedPorts() {
    return allowedPorts;
  }

  /**
   * @return whether a URL may reach any port
   */
  public boolean isAnyPortAllowed() {
    return anyPortAllowed;
  }

  /**
   * @return whether internal addresses may be reached: a deployment's opt-out,
   *         false by default
   */
  public boolean isInternalAddressesAllowed() {
    return internalAddressesAllowed;
  }

  /**
   * @return the media types an answer may declare, lower-case and without
   *         parameters; empty when any is accepted
   */
  public Set<String> getAcceptedContentTypes() {
    return acceptedContentTypes;
  }

  /**
   * @return the largest body read, in bytes, unless the request says otherwise
   */
  public long getMaxBytes() {
    return maxBytes;
  }

  /**
   * @return the most redirects followed
   */
  public int getMaxRedirects() {
    return maxRedirects;
  }

  /**
   * @return the longest wait for a connection
   */
  public Duration getConnectTimeout() {
    return connectTimeout;
  }

  /**
   * @return the longest wait between two reads
   */
  public Duration getReadTimeout() {
    return readTimeout;
  }

  /**
   * @return the longest read of a URL, redirects included
   */
  public Duration getTotalTimeout() {
    return totalTimeout;
  }

  /**
   * @return the most connections the fetcher keeps open
   */
  public int getMaxConnectionsTotal() {
    return maxConnectionsTotal;
  }

  /**
   * @return the most connections the fetcher keeps open to one host
   */
  public int getMaxConnectionsPerRoute() {
    return maxConnectionsPerRoute;
  }

  /**
   * @return name resolution; the JDK's unless a test says otherwise
   */
  public HostResolver getResolver() {
    return resolver;
  }

  /**
   * @return host names whose addresses are read as public; empty in production
   */
  public Set<String> getExemptHosts() {
    return exemptHosts;
  }

  /**
   * @return addresses read as public; empty in production
   */
  public Set<InetAddress> getExemptAddresses() {
    return exemptAddresses;
  }

}
