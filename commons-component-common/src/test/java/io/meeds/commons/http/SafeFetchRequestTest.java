/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Pins what a read refuses to be built with: a header value able to add a
 * header of its own, and a set of media types naming none.
 */
class SafeFetchRequestTest {

  private static final URI URL = URI.create("https://example.org/cal.ics");

  /**
   * A line break or another control character in a header value is refused,
   * whichever header carries it; a plain value and no value at all are kept.
   */
  @Test
  void aHeaderValueWithAControlCharacterIsRefused() {
    SafeFetchRequest request = SafeFetchRequest.get(URL);
    for (String value : new String[] { "\"v1\"\r\nX-Injected: 1", "\"v1\"\nX-Injected: 1", "\"v1\"\rX", "a\u0000b" }) {
      assertThrows(IllegalArgumentException.class, () -> request.withValidators(value, null), value);
      assertThrows(IllegalArgumentException.class, () -> request.withValidators(null, value), value);
      assertThrows(IllegalArgumentException.class, () -> request.withAccept(value), value);
    }
    SafeFetchRequest conditional = request.withAccept("text/calendar").withValidators("\"v1\"", "Mon, 14 Sep 2026 10:00:00 GMT");
    assertEquals("text/calendar", conditional.accept());
    assertEquals("\"v1\"", conditional.ifNoneMatch());
    assertEquals("Mon, 14 Sep 2026 10:00:00 GMT", conditional.ifModifiedSince());
    SafeFetchRequest none = request.withValidators(null, null);
    assertNull(none.ifNoneMatch());
    assertNull(none.ifModifiedSince());
  }

  /**
   * A set of media types naming none — empty, or blank entries only — is
   * refused rather than read as accepting any; the types named are kept
   * normalized.
   */
  @Test
  void aSetOfMediaTypesNamingNoneIsRefused() {
    SafeFetchRequest request = SafeFetchRequest.get(URL);
    Set<String> empty = Set.of();
    Set<String> blank = Set.of(" ");
    assertThrows(IllegalArgumentException.class, () -> request.withAcceptedContentTypes(empty));
    assertThrows(IllegalArgumentException.class, () -> request.withAcceptedContentTypes(blank));
    assertEquals(Set.of("image/png"), request.withAcceptedContentTypes(Set.of("Image/PNG; q=1")).acceptedContentTypes());
    assertNull(request.acceptedContentTypes(), "no set keeps the policy's");
  }

}
