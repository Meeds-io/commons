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

import java.util.Collections;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One line of a digest email, built by the addon owning the notification type.
 * It holds text only, never HTML: the wording is a translation key of the
 * addon notification bundle, its placeholders {0}, {1}... are replaced by the
 * given arguments in order (plain replacement, the same convention as the
 * other notification bundles: no quoting rule, apostrophes stay single), and
 * the layout is the job of the email template. The arguments are escaped by
 * the digest before being put in the email, the addon gives them raw.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DigestLine {

  /**
   * The key of the wording in the notification bundle of the addon, for example
   * digest.line.SpaceInvitationPlugin={0} invited you to join {1}
   */
  private String       labelKey;

  /** The values of the placeholders {0}, {1}... of the wording, raw text */
  private List<String> args = Collections.emptyList();

  /** The absolute link to the object the line is about */
  private String       url;

  /**
   * @param labelKey the wording key
   * @param args the values of its placeholders, in order
   * @return the line, without link yet: see {@link #withUrl(String)}
   */
  public static DigestLine of(String labelKey, String... args) {
    return new DigestLine(labelKey, List.of(args), null);
  }

  public DigestLine withUrl(String url) {
    this.url = url;
    return this;
  }

}
