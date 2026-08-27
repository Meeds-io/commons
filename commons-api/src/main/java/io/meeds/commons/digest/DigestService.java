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
package io.meeds.commons.digest;

/**
 * Service holding the settings of the digest mail notifications feature. The
 * digest is independent from the notification channels: it only relies on its
 * own settings, starting with the platform-wide administrator switch that
 * allows users to configure their digest.
 */
public interface DigestService {

  /**
   * @return true when the administrator allowed users to set digest mail
   *         notifications, false otherwise. When nothing was ever saved, the
   *         digest is not allowed.
   */
  boolean isDigestAllowed();

  /**
   * Saves the administrator switch allowing users to set digest mail
   * notifications.
   *
   * @param allowed true to allow users to configure their digest
   */
  void saveDigestAllowed(boolean allowed);

}
