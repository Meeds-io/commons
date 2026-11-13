/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
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
package org.exoplatform.commons.notification.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class ElementCacheKeyTest {

  @Test
  void testKeysBuiltFromTheSameInputsAreEqual() {
    assertEquals(new ElementCacheKey("plugin", "fr"), new ElementCacheKey("plugin", "fr"));
    assertEquals(new ElementCacheKey("plugin", "fr").hashCode(), new ElementCacheKey("plugin", "fr").hashCode());
    assertEquals(new ElementCacheKey("en"), new ElementCacheKey("en"));
    assertEquals(new ElementCacheKey("en").hashCode(), new ElementCacheKey("en").hashCode());
  }

  @Test
  void testKeysDifferByPluginOrLanguage() {
    assertNotEquals(new ElementCacheKey("plugin", "fr"), new ElementCacheKey("plugin", "en"));
    assertNotEquals(new ElementCacheKey("plugin", "fr"), new ElementCacheKey("other", "fr"));
    assertNotEquals(new ElementCacheKey("fr"), new ElementCacheKey("plugin", "fr"));
  }
}
