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
package io.meeds.commons.digest.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

/**
 * What is written at capture must be read back identically at send time.
 */
public class DigestParamsCodecTest {

  @Test
  public void testRoundTrip() {
    Map<String, String> params = new LinkedHashMap<>();
    params.put("spaceId", "42");
    params.put("title", "He said \"hi\" \\ bye");
    params.put("multiline", "a\nb\tc");
    String json = DigestParamsCodec.serialize(params);
    assertEquals(params, DigestParamsCodec.parse(json));
  }

  @Test
  public void testFragmentMatchesTheSerializedForm() {
    Map<String, String> params = Map.of("spaceId", "42");
    assertTrue(DigestParamsCodec.serialize(params).contains(DigestParamsCodec.fragment("spaceId", "42")));
  }

  @Test
  public void testNothingWorthStoringGivesNull() {
    assertNull(DigestParamsCodec.serialize(null));
    assertNull(DigestParamsCodec.serialize(Map.of()));
    assertEquals("{}", DigestParamsCodec.serialize(Map.of("tooLong", "x".repeat(DigestParamsCodec.PARAM_VALUE_MAX_LENGTH + 1))));
  }

  @Test
  public void testOversizedJsonIsDroppedNotTruncated() {
    Map<String, String> params = new LinkedHashMap<>();
    for (int i = 0; i < 20; i++) {
      params.put("p" + i, "v".repeat(200));
    }
    assertNull(DigestParamsCodec.serialize(params));
  }

  @Test
  public void testParseToleratesGarbage() {
    assertTrue(DigestParamsCodec.parse(null).isEmpty());
    assertTrue(DigestParamsCodec.parse("").isEmpty());
    assertTrue(DigestParamsCodec.parse("not json").isEmpty());
    assertTrue(DigestParamsCodec.parse("{\"unterminated").isEmpty());
    assertEquals(Map.of("a", "1"), DigestParamsCodec.parse("{\"a\":\"1\",\"broken\"}"));
  }

}
