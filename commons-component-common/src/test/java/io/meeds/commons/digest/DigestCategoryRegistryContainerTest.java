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

import org.exoplatform.jpa.BaseTest;

import io.meeds.commons.digest.plugin.DigestCategoryProvider;

/**
 * Checks that a category declared in a portal configuration, the way every
 * addon declares its own, really reaches the registry of the container. The
 * Kernel only applies the external plugins of the container owning the
 * component: a registry declared at the root level would start fine and stay
 * silently empty, and the drawer would show no category at all.
 */
public class DigestCategoryRegistryContainerTest extends BaseTest {

  public void testACategoryDeclaredInAPortalConfigurationReachesTheRegistry() {
    DigestCategoryRegistry categoryRegistry = getService(DigestCategoryRegistry.class);
    assertNotNull("The registry must be owned by the portal container", categoryRegistry);

    List<DigestCategoryProvider> categories = categoryRegistry.getCategoryProviders();
    assertEquals(1, categories.size());
    assertEquals("testCategory", categories.get(0).getId());
    assertEquals(List.of("TestPlugin"), categories.get(0).getPluginIds());
  }

}
