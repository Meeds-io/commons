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
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * What {@link SafeHttpFetcher} read from a URL: a 2xx answer with its body, or
 * a 304 to a conditional read.
 *
 * @param status the HTTP status answered
 * @param notModified whether the server answered 304: nothing changed since
 *          the validators sent
 * @param truncated whether the body was cut at the request's limit, as the
 *          request allowed
 * @param body the body read; empty for an answer without one, null when not
 *          modified
 * @param contentType the {@code Content-Type} header answered, as sent, or
 *          null
 * @param headers the first value of every header answered, by name, case
 *          ignored; a value carrying a control character other than a tab
 *          is left out, as a request would refuse to send it
 * @param uri the URL the answer came from, after the redirects followed
 */
public record SafeFetchResponse(int status,
                                boolean notModified,
                                boolean truncated,
                                byte[] body,
                                String contentType,
                                Map<String, String> headers,
                                URI uri) {

  /**
   * Keeps the headers in a case-insensitive, unmodifiable map.
   *
   * @param status the HTTP status answered
   * @param notModified whether the server answered 304
   * @param truncated whether the body was cut at the limit
   * @param body the body read
   * @param contentType the {@code Content-Type} answered
   * @param headers the headers answered
   * @param uri the URL the answer came from
   */
  public SafeFetchResponse {
    Map<String, String> byName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    if (headers != null) {
      byName.putAll(headers);
    }
    headers = Collections.unmodifiableMap(byName);
  }

  /**
   * The first value of a header answered.
   *
   * @param name the header name, case ignored
   * @return the value, or null when the header was not answered or its value
   *         carried a control character other than a tab
   */
  public String header(String name) {
    return name == null ? null : headers.get(name);
  }

  /**
   * The media type answered: the {@code Content-Type} without its parameters,
   * lower-cased.
   *
   * @return the media type, or null when none was answered
   */
  public String mediaType() {
    return SafeFetchPolicyBuilder.mediaTypeOf(contentType);
  }

  /**
   * Compares by content: the body is an array.
   *
   * @param other the other object
   * @return true when every component is equal, the body byte by byte
   */
  @Override
  public boolean equals(Object other) {
    return this == other
        || (other instanceof SafeFetchResponse that
            && status == that.status
            && notModified == that.notModified
            && truncated == that.truncated
            && Arrays.equals(body, that.body)
            && Objects.equals(contentType, that.contentType)
            && Objects.equals(headers, that.headers)
            && Objects.equals(uri, that.uri));
  }

  /**
   * Hashes by content, the body included.
   *
   * @return the hash
   */
  @Override
  public int hashCode() {
    return Objects.hash(status, notModified, truncated, Arrays.hashCode(body), contentType, headers, uri);
  }

  /**
   * Describes the answer by its status, size and type; never the body, never
   * the URL, which may embed a secret.
   *
   * @return the description
   */
  @Override
  public String toString() {
    return "SafeFetchResponse[status=%d, notModified=%s, truncated=%s, bytes=%d, contentType=%s]".formatted(status,
                                                                                                           notModified,
                                                                                                           truncated,
                                                                                                           body == null ? 0 : body.length,
                                                                                                           contentType);
  }

}
