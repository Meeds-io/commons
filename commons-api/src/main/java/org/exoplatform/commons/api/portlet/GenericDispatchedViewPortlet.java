package org.exoplatform.commons.api.portlet;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Set;

import javax.portlet.*;

import org.apache.commons.lang3.StringUtils;
import org.gatein.portal.controller.resource.ResourceScope;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.application.UIPortlet;
import org.exoplatform.web.application.JavascriptManager;
import org.exoplatform.web.application.RequestContext;

/**
 * This is a generic and simple Portlet class that dispatches the view to a
 * JSP/HTML file. It allows to access Portlet preferences from HTTP Request
 * attributes too in render phase.
 */
public class GenericDispatchedViewPortlet extends GenericPortlet {

  private String  viewDispatchedPath;

  private String  jsModule;

  private String  jsToAppend;

  private boolean useJSManagerLoading;

  @Override
  public void init(PortletConfig config) throws PortletException {
    super.init(config);
    viewDispatchedPath = config.getInitParameter("portlet-view-dispatched-file-path");
    String useJSManager = config.getInitParameter("use-js-manager");
    if (StringUtils.isNotBlank(useJSManager)) {
      useJSManagerLoading = Boolean.parseBoolean(useJSManager);
      jsModule = config.getInitParameter("js-manager-jsModule");
      if (StringUtils.isBlank(jsModule)) {
        jsModule = ResourceScope.PORTLET + "/" + getPortletContext().getPortletContextName() + "/"
            + getPortletConfig().getPortletName();
      }
      jsToAppend = config.getInitParameter("js-manager-javascript-content");
      if (StringUtils.isBlank(jsToAppend)) {
        String alias = getPortletName();
        jsToAppend = alias + " && " + alias + ".init && " + alias + ".init();";
      } else if (!StringUtils.endsWith(jsToAppend.trim(), ";")) {
        jsToAppend = jsToAppend + ";";
      }
    }
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
    if (useJSManagerLoading) {
      PortalRequestContext portalRequestContext = (PortalRequestContext) RequestContext.getCurrentInstance();
      JavascriptManager javascriptManager = portalRequestContext.getJavascriptManager();
      String alias = getPortletName();
      javascriptManager.require(jsModule, alias).addScripts(jsToAppend);
    }
  }
}
