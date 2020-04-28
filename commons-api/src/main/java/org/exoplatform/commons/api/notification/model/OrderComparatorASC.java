/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.commons.api.notification.model;

import java.util.Comparator;

import org.exoplatform.commons.api.notification.model.GroupProvider;
import org.exoplatform.commons.api.notification.model.PluginInfo;
import org.exoplatform.commons.api.notification.plugin.config.PluginConfig;

public class OrderComparatorASC implements Comparator<Object> {
  @Override
  public int compare(Object o1, Object o2) {
    if (o1 instanceof GroupProvider && o2 instanceof GroupProvider) {
      Integer order1 = ((GroupProvider) o1).getOrder();
      Integer order2 = ((GroupProvider) o2).getOrder();
      return order1.compareTo(order2);
    }
    if (o1 instanceof PluginConfig && o2 instanceof PluginConfig) {
      Integer order1 = Integer.parseInt(((PluginConfig) o1).getOrder());
      Integer order2 = Integer.parseInt(((PluginConfig) o2).getOrder());
      return order1.compareTo(order2);
    }
    if (o1 instanceof PluginInfo && o2 instanceof PluginInfo) {
      Integer order1 = ((PluginInfo) o1).getOrder();
      Integer order2 = ((PluginInfo) o2).getOrder();
      return order1.compareTo(order2);
    }
    return 0;
  }
}
