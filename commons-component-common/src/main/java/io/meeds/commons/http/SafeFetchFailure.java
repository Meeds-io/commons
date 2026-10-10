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

/**
 * Why {@link SafeHttpFetcher} read nothing usable from a URL. Each value names
 * a decision a caller can act on or report; none carries the URL, which may
 * embed a secret.
 */
public enum SafeFetchFailure {

  /** Not a usable absolute URL. */
  INVALID_URL("The URL is not usable"),

  /** A scheme outside the allowed set. */
  SCHEME_NOT_ALLOWED("The URL scheme is not allowed"),

  /** A port outside the allowed set. */
  PORT_NOT_ALLOWED("The URL port is not allowed"),

  /** A user name or password in the URL. */
  CREDENTIALS_IN_URL("The URL carries credentials"),

  /** The host resolves to an address the platform must not reach. */
  REFUSED_ADDRESS("The URL points at an address the platform may not reach"),

  /** The host resolves to nothing. */
  UNRESOLVABLE("The URL host resolves to nothing"),

  /** No answer could be read. */
  UNREACHABLE("The URL could not be reached"),

  /** The answer took too long. */
  TIMEOUT("The URL did not answer in time"),

  /**
   * An answer the read cannot use: a status outside 2xx, a 304 to a read that
   * sent no validator, or a redirect without a {@code Location}.
   */
  HTTP_ERROR("The URL answered with an error status"),

  /** The answer is larger than allowed. */
  TOO_LARGE("The URL answered with a body larger than allowed"),

  /** More redirects than followed. */
  TOO_MANY_REDIRECTS("The URL redirects more times than allowed"),

  /** The answer declares a content type outside the accepted set. */
  CONTENT_TYPE_NOT_ALLOWED("The URL answered with a content type that is not accepted");

  private final String description;

  /**
   * Builds a failure.
   *
   * @param description the fixed phrase reported for it
   */
  SafeFetchFailure(String description) {
    this.description = description;
  }

  /**
   * @return the fixed phrase reported for this failure, never a URL
   */
  public String getDescription() {
    return description;
  }

}
