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
 * One read of a URL by {@link SafeHttpFetcher}: a GET, with what it may say
 * and accept beyond the fetcher's {@link SafeFetchPolicy}. Built from
 * {@link #get(URI)} and the {@code with} methods; immutable.
 *
 * @param uri the URL to read
 * @param accept the {@code Accept} header sent, null for none
 * @param ifNoneMatch the {@code If-None-Match} header sent, null for none
 * @param ifModifiedSince the {@code If-Modified-Since} header sent, null for
 *          none
 * @param acceptedContentTypes the media types this read accepts, replacing the
 *          policy's; null keeps the policy's, empty accepts any
 * @param maxBytes the most bytes this read takes, replacing the policy's; zero
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
    acceptedContentTypes = acceptedContentTypes == null ? null : SafeFetchPolicyBuilder.normalizeContentTypes(acceptedContentTypes);
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
   */
  public SafeFetchRequest withValidators(String etag, String lastModified) {
    return new SafeFetchRequest(uri, accept, etag, lastModified, acceptedContentTypes, maxBytes, truncateAtLimit);
  }

  /**
   * The same read accepting these media types only.
   *
   * @param contentTypes the media types, without parameters, case ignored;
   *          empty accepts any
   * @return the request
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
   * The same read taking at most this many bytes.
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

}
