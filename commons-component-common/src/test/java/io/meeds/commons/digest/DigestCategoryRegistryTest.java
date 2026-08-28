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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.container.xml.ValuesParam;

import io.meeds.commons.digest.plugin.DigestCategoryPlugin;
import io.meeds.commons.digest.plugin.DigestCategoryProvider;

/**
 * Checks how an addon declares the digest category it owns, and what the
 * digest does with the declarations it receives.
 */
public class DigestCategoryRegistryTest {

  @Test
  public void testCategoriesAreReturnedInDisplayOrder() {
    DigestCategoryRegistry registry = new DigestCategoryRegistry();
    registry.addCategoryProvider(categoryPlugin("process", 60, "CreateRequestPlugin"));
    registry.addCategoryProvider(categoryPlugin("spaces", 10, "SpaceInvitationPlugin"));
    registry.addCategoryProvider(categoryPlugin("news", 30, "PostNewsNotificationPlugin"));

    assertEquals(List.of("spaces", "news", "process"),
                 registry.getCategoryProviders().stream().map(DigestCategoryProvider::getId).toList());
  }

  @Test
  public void testNoCategoryWithoutAnyAddon() {
    assertTrue(new DigestCategoryRegistry().getCategoryProviders().isEmpty());
  }

  @Test
  public void testTheSameCategoryIsDeclaredOnlyOnce() {
    DigestCategoryRegistry registry = new DigestCategoryRegistry();
    registry.addCategoryProvider(categoryPlugin("news", 30, "PostNewsNotificationPlugin"));
    registry.addCategoryProvider(categoryPlugin("news", 30, "PublishNewsNotificationPlugin"));

    assertEquals(1, registry.getCategoryProviders().size());
    assertEquals(List.of("PublishNewsNotificationPlugin"), registry.getCategoryProviders().get(0).getPluginIds());
  }

  @Test
  public void testAPluginThatIsNotACategoryIsIgnored() {
    DigestCategoryRegistry registry = new DigestCategoryRegistry();
    registry.addCategoryProvider(null);
    assertTrue(registry.getCategoryProviders().isEmpty());
  }

  @Test
  public void testTheLabelKeyIsDeducedFromTheIdWhenNotDeclared() {
    InitParams params = new InitParams();
    params.addParam(valueParam("id", "tasks"));
    params.addParam(valuesParam("TaskAssignPlugin"));

    DigestCategoryPlugin plugin = new DigestCategoryPlugin(params);
    assertEquals("digest.category.tasks", plugin.getLabelKey());
    assertEquals(0, plugin.getOrder());
  }

  @Test
  public void testACategoryWithoutIdIsRefused() {
    InitParams params = new InitParams();
    params.addParam(valuesParam("TaskAssignPlugin"));
    assertThrows(IllegalArgumentException.class, () -> new DigestCategoryPlugin(params));
  }

  /**
   * A category covering nothing would show the user a choice that never brings
   * him anything.
   */
  @Test
  public void testACategoryWithoutNotificationPluginIsRefused() {
    InitParams params = new InitParams();
    params.addParam(valueParam("id", "tasks"));
    assertThrows(IllegalArgumentException.class, () -> new DigestCategoryPlugin(params));
  }

  private DigestCategoryPlugin categoryPlugin(String id, int order, String pluginId) {
    InitParams params = new InitParams();
    params.addParam(valueParam("id", id));
    params.addParam(valueParam("labelKey", "digest.category." + id));
    params.addParam(valueParam("order", String.valueOf(order)));
    params.addParam(valuesParam(pluginId));
    return new DigestCategoryPlugin(params);
  }

  private ValueParam valueParam(String name, String value) {
    ValueParam valueParam = new ValueParam();
    valueParam.setName(name);
    valueParam.setValue(value);
    return valueParam;
  }

  private ValuesParam valuesParam(String... pluginIds) {
    ValuesParam valuesParam = new ValuesParam();
    valuesParam.setName("pluginIds");
    valuesParam.setValues(new ArrayList<>(List.of(pluginIds)));
    return valuesParam;
  }

}
