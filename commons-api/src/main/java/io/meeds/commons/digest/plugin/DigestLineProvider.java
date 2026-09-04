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
package io.meeds.commons.digest.plugin;

import java.util.List;

import io.meeds.commons.digest.model.DigestItem;
import io.meeds.commons.digest.model.DigestLine;

/**
 * Builds the digest email lines of the notification types an addon owns. The
 * digest stores ids only, so at send time it asks the addon for each waiting
 * item: the addon loads the object by its id and returns the line, or nothing
 * when the object doesn't exist any more, in which case the line is left out
 * of the email and not counted.
 */
public interface DigestLineProvider {

  /**
   * @return the ids of the notification types this provider builds the lines
   *         of
   */
  List<String> getPluginIds();

  /**
   * @param item the waiting notification, ids only
   * @param context the recipient, his language and his timezone
   * @return the line to put in the email, or null when the object it is about
   *         doesn't exist any more
   */
  DigestLine buildLine(DigestItem item, DigestLineContext context);

}
