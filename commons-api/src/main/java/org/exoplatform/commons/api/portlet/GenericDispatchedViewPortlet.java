package org.exoplatform.commons.api.portlet;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Set;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.portal.webui.application.UIPortlet;

/**
 * This is a generic and simple Portlet class that dispatches the view to a
 * JSP/HTML file. It allows to access Portlet preferences from HTTP Request
 * attributes too in render phase.
 */
public class GenericDispatchedViewPortlet extends GenericPortlet {

  private String  viewDispatchedPath;

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
    request.setAttribute("portletStorageId", UIPortlet.getCurrentUIPortlet().getStorageId());
    prd.include(request, response);
  }
}
