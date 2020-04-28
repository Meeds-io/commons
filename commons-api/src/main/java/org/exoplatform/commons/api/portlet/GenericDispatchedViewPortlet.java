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
package org.exoplatform.commons.api.portlet;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Set;

import javax.portlet.*;

import org.apache.commons.lang3.StringUtils;

/**
 * This is a generic and simple Portlet class that dispatches the view to a
 * JSP/HTML file.
 * 
 * It allows to access Portlet preferences from  HTTP Request attributes too in render phase.
 */
public class GenericDispatchedViewPortlet extends GenericPortlet {

  private String viewDispatchedPath;

  @Override
  public void init(PortletConfig config) throws PortletException {
    super.init(config);
    viewDispatchedPath = config.getInitParameter("portlet-view-dispatched-file-path");
    if (StringUtils.isBlank(viewDispatchedPath)) {
      throw new IllegalStateException("Portlet init parameter 'portlet-view-dispatched-file-path' is mandatory");
    }
  }

  @Override
  protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
    PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher(viewDispatchedPath);
    PortletPreferences preferences = request.getPreferences();
    if (preferences != null) {
      Set<Entry<String, String[]>> preferencesEntries = preferences.getMap().entrySet();
      for (Entry<String, String[]> entry : preferencesEntries) {
        request.setAttribute(entry.getKey(), entry.getValue());
      }
    }
    prd.include(request, response);
  }
}
