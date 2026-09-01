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

import java.util.List;

import io.meeds.commons.digest.model.DigestUserSettings;
import io.meeds.commons.digest.plugin.DigestCategoryProvider;

/**
 * Service holding the settings of the digest mail notifications feature. The
 * digest is independent from the notification channels: it only relies on its
 * own settings, the platform wide administrator switch and the choices of each
 * user.
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

  /**
   * @param username the user to read the choices of
   * @return the digest choices of the user, both frequencies off with no
   *         category when he never saved any. The categories of the addons
   *         uninstalled since his last save are left out.
   */
  DigestUserSettings getUserSettings(String username);

  /**
   * Saves the digest choices of a user and keeps the digest sender job work
   * list in sync: the user is enrolled when he enables a frequency, and removed
   * when both frequencies end up off. A frequency switched on starts covering
   * the notifications received from now on.
   *
   * @param username the user saving his choices
   * @param settings the chosen frequencies and their categories
   * @param timeZone the user profile timezone, used to send the digest at the
   *                 right local hour, the server one is used when null
   * @throws IllegalArgumentException when a frequency is enabled with no
   *           category. The categories that no installed addon provides are
   *           left out instead of being rejected, so that uninstalling an addon
   *           never keeps a user from saving his choices.
   */
  void saveUserSettings(String username, DigestUserSettings settings, String timeZone);

  /**
   * @return the categories the user can choose from, in display order. Only the
   *         categories of the installed addons are returned.
   */
  List<DigestCategoryProvider> getCategories();

}
