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

import java.net.URI;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

/**
 * One read of a URL by {@link SafeHttpFetcher}: a GET, with the headers it
 * sends and how it narrows the fetcher's {@link SafeFetchPolicy}. A request
 * never widens the policy: its body limit is capped at the policy's, and its
 * media types are kept only where the policy accepts them too. Built from
 * {@link #get(URI)} and the {@code with} methods; immutable. A header value
 * carrying a line break or another control character is refused, so that no
 * value can add a header of its own.
 *
 * @param uri the URL to read
 * @param accept the {@code Accept} header sent, null for none
 * @param ifNoneMatch the {@code If-None-Match} header sent, null for none
 * @param ifModifiedSince the {@code If-Modified-Since} header sent, null for
 *          none
 * @param acceptedContentTypes the media types this read accepts, within the
 *          policy's when the policy names some; null keeps the policy's, and
 *          an empty set is refused
 * @param maxBytes the most bytes this read takes, capped at the policy's; zero
 *          or less keeps the policy's
 * @param truncateAtLimit whether a body past the limit is cut at it and
 *          returned, instead of refused: for a page whose head comes first,
 *          never for an image, useless cut
 */
public record SafeFetchRequest(URI uri,
                               String accept,
                               String ifNoneMatch,
                               String ifModifiedSince,
                               Set<String> acceptedContentTypes,
                               long maxBytes,
                               boolean truncateAtLimit) {

  /**
   * Checks the request and normalizes its media types.
   *
   * @throws IllegalArgumentException when a header value carries a control
   *           character, or the media types name none
   * @param uri the URL to read
   * @param accept the {@code Accept} header sent
   * @param ifNoneMatch the {@code If-None-Match} header sent
   * @param ifModifiedSince the {@code If-Modified-Since} header sent
   * @param acceptedContentTypes the media types accepted
   * @param maxBytes the most bytes read
   * @param truncateAtLimit whether a longer body is cut rather than refused
   */
  public SafeFetchRequest {
    Objects.requireNonNull(uri, "uri");
    requireHeaderValue(accept, "accept");
    requireHeaderValue(ifNoneMatch, "ifNoneMatch");
    requireHeaderValue(ifModifiedSince, "ifModifiedSince");
    if (acceptedContentTypes != null) {
      acceptedContentTypes = SafeFetchPolicyBuilder.normalizeContentTypes(acceptedContentTypes);
      if (acceptedContentTypes.isEmpty()) {
        throw new IllegalArgumentException("acceptedContentTypes names at least one media type");
      }
    }
  }

  /**
   * A plain GET under the fetcher's policy.
   *
   * @param uri the URL to read
   * @return the request
   */
  public static SafeFetchRequest get(URI uri) {
    return new SafeFetchRequest(uri, null, null, null, null, 0, false);
  }

  /**
   * The same read with an {@code Accept} header.
   *
   * @param acceptHeader the header value
   * @return the request
   */
  public SafeFetchRequest withAccept(String acceptHeader) {
    return new SafeFetchRequest(uri, acceptHeader, ifNoneMatch, ifModifiedSince, acceptedContentTypes, maxBytes, truncateAtLimit);
  }

  /**
   * The same read made conditional on the validators of a previous one; a
   * server answering 304 gives a {@linkplain SafeFetchResponse#notModified()
   * not-modified} response.
   *
   * @param etag the entity tag of the previous read, or null
   * @param lastModified the {@code Last-Modified} of the previous read, or null
   * @return the request
   * @throws IllegalArgumentException when a value carries a control character
   */
  public SafeFetchRequest withValidators(String etag, String lastModified) {
    return new SafeFetchRequest(uri, accept, etag, lastModified, acceptedContentTypes, maxBytes, truncateAtLimit);
  }

  /**
   * The same read accepting these media types only, within the policy's when
   * the policy names some: a type the policy refuses stays refused.
   *
   * @param contentTypes the media types, without parameters, case ignored; at
   *          least one
   * @return the request
   * @throws IllegalArgumentException when the set names no media type
   */
  public SafeFetchRequest withAcceptedContentTypes(Collection<String> contentTypes) {
    return new SafeFetchRequest(uri,
                                accept,
                                ifNoneMatch,
                                ifModifiedSince,
                                Set.copyOf(Objects.requireNonNull(contentTypes, "contentTypes")),
                                maxBytes,
                                truncateAtLimit);
  }

  /**
   * The same read taking at most this many bytes, or the policy's limit when
   * that is lower.
   *
   * @param limit the limit, positive
   * @return the request
   */
  public SafeFetchRequest withMaxBytes(long limit) {
    if (limit <= 0) {
      throw new IllegalArgumentException("maxBytes is positive");
    }
    return new SafeFetchRequest(uri, accept, ifNoneMatch, ifModifiedSince, acceptedContentTypes, limit, truncateAtLimit);
  }

  /**
   * The same read, a body past the limit cut at it and returned rather than
   * refused.
   *
   * @return the request
   */
  public SafeFetchRequest truncatedAtLimit() {
    return new SafeFetchRequest(uri, accept, ifNoneMatch, ifModifiedSince, acceptedContentTypes, maxBytes, true);
  }

  /**
   * Refuses a header value carrying a line break or another control
   * character, which would end the header and start another.
   *
   * @param value the value, null for none
   * @param field its name, for the refusal
   */
  private static void requireHeaderValue(String value, String field) {
    if (value == null) {
      return;
    }
    for (int i = 0; i < value.length(); i++) {
      if (Character.isISOControl(value.charAt(i))) {
        throw new IllegalArgumentException(field + " carries a control character");
      }
    }
  }

}
