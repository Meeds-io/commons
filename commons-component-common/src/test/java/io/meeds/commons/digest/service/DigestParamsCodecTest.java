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
import java.util.List;
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
    assertNull(DigestParamsCodec.serialize(Map.of("tooLong", "x".repeat(DigestParamsCodec.PARAM_VALUE_MAX_LENGTH + 1))));
  }

  @Test
  public void testOversizedJsonLosesItsLongestValuesAndKeepsTheIds() {
    // A task notification: a dozen long values and two ids
    Map<String, String> params = new LinkedHashMap<>();
    params.put("taskId", "42");
    for (int i = 0; i < 12; i++) {
      params.put("long" + i, "v".repeat(250));
    }
    params.put("creator", "john");
    String json = DigestParamsCodec.serialize(params);
    assertTrue(json.length() <= DigestParamsCodec.PARAMS_MAX_LENGTH);
    Map<String, String> stored = DigestParamsCodec.parse(json);
    assertEquals("42", stored.get("taskId"));
    assertEquals("john", stored.get("creator"));
    assertTrue(stored.size() < params.size());
  }

  @Test
  public void testAValueOfExactlyTheMaximumLengthIsKept() {
    // 255 is the last length a value may have, 256 is already refused
    String atTheLimit = "x".repeat(DigestParamsCodec.PARAM_VALUE_MAX_LENGTH);
    Map<String, String> stored = DigestParamsCodec.parse(DigestParamsCodec.serialize(Map.of("noteId", atTheLimit)));
    assertEquals(atTheLimit, stored.get("noteId"));
  }

  @Test
  public void testTheLongestValuesAreTheOnesDroppedAndInThatOrder() {
    // Ten values of distinct lengths, the longest in the middle of the
    // insertion order so that dropping the first or the last one would not
    // give this result. The JSON is 2 363 characters long: dropping l3 (255
    // characters, the longest) leaves 2 100, dropping l8 (254) leaves 1 838,
    // and nothing else has to go
    Map<String, String> params = new LinkedHashMap<>();
    params.put("taskId", "42");
    params.put("l1", "v".repeat(248));
    params.put("l2", "v".repeat(250));
    params.put("l3", "v".repeat(255));
    params.put("l4", "v".repeat(252));
    params.put("l5", "v".repeat(249));
    params.put("l6", "v".repeat(251));
    params.put("l7", "v".repeat(253));
    params.put("l8", "v".repeat(254));
    params.put("l9", "v".repeat(247));
    params.put("creator", "john");

    String json = DigestParamsCodec.serialize(params);

    assertTrue(json.length() <= DigestParamsCodec.PARAMS_MAX_LENGTH);
    Map<String, String> stored = DigestParamsCodec.parse(json);
    assertEquals(List.of("taskId", "l1", "l2", "l4", "l5", "l6", "l7", "l9", "creator"), List.copyOf(stored.keySet()));
    assertEquals("42", stored.get("taskId"));
    assertEquals("john", stored.get("creator"));
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
