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

/**
 * Each addon contributes the digest categories it owns by declaring a bean of
 * this type. The registered providers are the single source of truth for the
 * categories the user can choose, and for the notification types each category
 * covers. When an addon is not installed, its category simply does not exist.
 */
public interface DigestCategoryProvider {

  /**
   * @return the category identifier, stored in the user settings and in the
   *         waiting digest items, for example "spaces"
   */
  String getId();

  /**
   * @return the resource bundle key of the category label, displayed to the
   *         user
   */
  String getLabelKey();

  /**
   * @return the display order of the category, in the settings panel and in the
   *         digest email
   */
  int getOrder();

  /**
   * @return the identifiers of the notification plugins this category covers,
   *         for example SpaceInvitationPlugin
   */
  List<String> getPluginIds();

}
