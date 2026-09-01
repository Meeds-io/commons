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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.exoplatform.container.component.ComponentPlugin;

import io.meeds.commons.digest.plugin.DigestCategoryProvider;

/**
 * Holds the digest categories the installed addons contribute. It is a kernel
 * component on purpose: the kernel container is shared by every webapp, while
 * each webapp has its own Spring context. A category declared by an addon
 * therefore reaches the digest whatever the addon is made of, which a Spring
 * bean would not do.
 */
public class DigestCategoryRegistry {

  private static final Logger                LOG               = LoggerFactory.getLogger(DigestCategoryRegistry.class);

  private final List<DigestCategoryProvider> categoryProviders = new ArrayList<>();

  /**
   * Adds the category an addon owns. Called by the kernel for each declared
   * component-plugin.
   *
   * @param plugin the category to add
   */
  public void addCategoryProvider(ComponentPlugin plugin) {
    if (plugin instanceof DigestCategoryProvider categoryProvider) {
      categoryProviders.removeIf(registered -> registered.getId().equals(categoryProvider.getId()));
      categoryProviders.add(categoryProvider);
    } else {
      LOG.warn("The digest category plugin {} is ignored, it doesn't implement DigestCategoryProvider",
               plugin == null ? null : plugin.getName());
    }
  }

  /**
   * @return the categories of the installed addons, in display order
   */
  public List<DigestCategoryProvider> getCategoryProviders() {
    return categoryProviders.stream().sorted(Comparator.comparingInt(DigestCategoryProvider::getOrder)).toList();
  }

}
