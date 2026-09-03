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
package io.meeds.commons.digest.model;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A notification waiting in the digest of one recipient, as the addons see it
 * when they are asked to build its email line: ids only, the names, titles and
 * links are looked up fresh at send time.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DigestItem {

  private long                id;

  /** The recipient */
  private String              userId;

  /** The notification type */
  private String              pluginId;

  private String              category;

  /** When the notification happened */
  private Instant             date;

  /**
   * The parameters the notification plugin stored, the same names as in the
   * notification: spaceId, activityId, taskId...
   */
  private Map<String, String> params = Collections.emptyMap();

  public String getParam(String name) {
    return params == null ? null : params.get(name);
  }

}
