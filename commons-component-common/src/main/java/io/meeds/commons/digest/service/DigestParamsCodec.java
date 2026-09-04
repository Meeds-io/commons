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

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * Writes and reads back the PARAMS column of a waiting item: a small JSON
 * object of strings, ids only. Written at capture, read at send time.
 */
public final class DigestParamsCodec {

  /**
   * A value longer than this is no identifier: it is not stored, the email
   * text is built fresh at send time anyway
   */
  static final int PARAM_VALUE_MAX_LENGTH = 255;

  /**
   * The size of the PARAMS column: when the JSON is too long, the longest
   * values go first, one by one, until it fits. The ids are the shortest values
   * of a notification, they are the last to go.
   */
  static final int PARAMS_MAX_LENGTH      = 2000;

  private DigestParamsCodec() {
    // utility
  }

  /**
   * @param ownerParameters the parameters the notification plugin stored
   * @return the JSON to store, null when there is nothing worth storing
   */
  public static String serialize(Map<String, String> ownerParameters) {
    if (ownerParameters == null || ownerParameters.isEmpty()) {
      return null;
    }
    Map<String, String> kept = new LinkedHashMap<>();
    ownerParameters.forEach((key, value) -> {
      if (StringUtils.isNotBlank(key) && value != null && value.length() <= PARAM_VALUE_MAX_LENGTH) {
        kept.put(key, value);
      }
    });
    String json = toJson(kept);
    // An oversized JSON would make the whole insert fail: the long values (a
    // description, a list of names) go first, the ids stay and send time
    // rebuilds everything from them anyway
    while (json.length() > PARAMS_MAX_LENGTH && !kept.isEmpty()) {
      String longest = kept.entrySet()
                           .stream()
                           .max(Comparator.comparingInt(entry -> entry.getValue().length()))
                           .map(Map.Entry::getKey)
                           .orElseThrow();
      kept.remove(longest);
      json = toJson(kept);
    }
    return kept.isEmpty() ? null : json;
  }

  private static String toJson(Map<String, String> params) {
    StringBuilder json = new StringBuilder("{");
    params.forEach((key, value) -> json.append(json.length() > 1 ? "," : "")
                                       .append('"')
                                       .append(escape(key))
                                       .append("\":\"")
                                       .append(escape(value))
                                       .append('"'));
    return json.append('}').toString();
  }

  /**
   * @param name a parameter name
   * @param value its value
   * @return the fragment a stored JSON contains for this parameter, used to
   *         find the items about one object
   */
  public static String fragment(String name, String value) {
    return "\"" + escape(name) + "\":\"" + escape(value) + "\"";
  }

  /**
   * @param json the stored PARAMS, as written by {@link #serialize(Map)}
   * @return the parameters, in storage order, empty when nothing was stored or
   *         when the content is not what this codec writes
   */
  public static Map<String, String> parse(String json) {
    Map<String, String> params = new LinkedHashMap<>();
    if (StringUtils.isBlank(json) || json.charAt(0) != '{' || json.charAt(json.length() - 1) != '}') {
      return params;
    }
    int position = 1;
    int end = json.length() - 1;
    while (position < end) {
      if (json.charAt(position) == ',') {
        position++;
        continue;
      }
      StringBuilder key = new StringBuilder();
      position = readString(json, position, key);
      if (position < 0 || position >= end || json.charAt(position) != ':') {
        return params;
      }
      StringBuilder value = new StringBuilder();
      position = readString(json, position + 1, value);
      if (position < 0) {
        return params;
      }
      params.put(key.toString(), value.toString());
    }
    return params;
  }

  private static int readString(String json, int position, StringBuilder out) {
    if (position >= json.length() || json.charAt(position) != '"') {
      return -1;
    }
    position++;
    while (position < json.length()) {
      char character = json.charAt(position);
      if (character == '"') {
        return position + 1;
      } else if (character == '\\' && position + 1 < json.length()) {
        char escaped = json.charAt(position + 1);
        if (escaped == 'u' && position + 5 < json.length()) {
          out.append((char) Integer.parseInt(json.substring(position + 2, position + 6), 16));
          position += 6;
        } else {
          out.append(escaped);
          position += 2;
        }
      } else {
        out.append(character);
        position++;
      }
    }
    return -1;
  }

  private static String escape(String value) {
    StringBuilder escaped = new StringBuilder(value.length());
    for (char character : value.toCharArray()) {
      if (character == '"' || character == '\\') {
        escaped.append('\\');
        escaped.append(character);
      } else if (character < 0x20) {
        escaped.append(String.format("\\u%04x", (int) character));
      } else {
        escaped.append(character);
      }
    }
    return escaped.toString();
  }

}
