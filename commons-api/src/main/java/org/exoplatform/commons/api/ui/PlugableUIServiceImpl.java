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
package org.exoplatform.commons.api.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlugableUIServiceImpl implements PlugableUIService {
  private Map<String, List<BaseUIPlugin>> plugins = new HashMap<>();

  @Override
  public void addPlugin(BaseUIPlugin plugin) {
    List<BaseUIPlugin> lst = plugins.get(plugin.getType());
    if (lst == null) {
      lst = new ArrayList<>();
      plugins.put(plugin.getType(), lst);
    }
    lst.add(plugin);
  }

  @Override
  public List<BaseUIPlugin> getPlugins(String type) {
    return plugins.get(type);
  }

  @Override
  public List<Response> render(RenderContext renderContext) {
    List<BaseUIPlugin> plugins = getPlugins(renderContext.getPluginType());
    List<Response> response = new ArrayList<>();
    if (plugins != null) {
      for (BaseUIPlugin plugin : plugins) {      
        response.add(plugin.render(renderContext));
      }      
    }
    return response;
  }

  @Override
  public Response processAction(ActionContext actionContext) {
    List<BaseUIPlugin> plugins = getPlugins(actionContext.getPluginType());
    if (plugins != null) {
      for (BaseUIPlugin plugin : plugins) {
        Response response = plugin.processAction(actionContext);
        if (response != null) {
          return response;
        }        
      }
    }
    return null;
  }
}
