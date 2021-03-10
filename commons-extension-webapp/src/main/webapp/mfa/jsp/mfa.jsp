<%--

    Copyright (C) 2009 eXo Platform SAS.

    This is free software; you can redistribute it and/or modify it
    under the terms of the GNU Lesser General Public License as
    published by the Free Software Foundation; either version 2.1 of
    the License, or (at your option) any later version.

    This software is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this software; if not, write to the Free
    Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
    02110-1301 USA, or see the FSF site: http://www.fsf.org.

--%>

<%@ page import="org.exoplatform.container.PortalContainer"%>
<%@ page import="org.exoplatform.services.resources.ResourceBundleService"%>
<%@ page import="org.exoplatform.portal.resource.SkinService"%>
<%@ page import="java.util.ResourceBundle"%>
<%@ page import="org.exoplatform.services.organization.User"%>
<%@ page import="org.exoplatform.services.organization.impl.UserImpl" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.exoplatform.web.controller.QualifiedName" %>
<%@ page import="org.exoplatform.web.login.recovery.PasswordRecoveryService" %>
<%@ page import="org.exoplatform.portal.resource.SkinConfig" %>
<%@ page import="java.util.Collection" %>
<%@ page import="java.util.Locale" %>
<%@ page import="org.exoplatform.commons.utils.I18N" %>
<%@ page import="org.exoplatform.portal.config.UserPortalConfigService" %>
<%@ page import="org.exoplatform.portal.resource.config.tasks.PortalSkinTask" %>
<%@ page import="org.gatein.portal.controller.resource.ResourceRequestHandler" %>
<%@ page import="org.gatein.common.text.EntityEncoder"%>

<%@ page language="java" %>
<%
  String contextPath = request.getContextPath() ;
  String initialUri = (String)request.getParameter("initialUri");
  String mfaPath = "/portal/mfa";

  PortalContainer portalContainer = PortalContainer.getCurrentInstance(session.getServletContext());
  SkinService skinService = PortalContainer.getCurrentInstance(session.getServletContext())
              .getComponentInstanceOfType(SkinService.class);

  UserPortalConfigService userPortalConfigService = portalContainer.getComponentInstanceOfType(UserPortalConfigService.class);
  String skinName = userPortalConfigService.getDefaultPortalSkinName();
  String loginCssPath = skinService.getSkin("portal/login", skinName).getCSSPath()+"?v="+ResourceRequestHandler.VERSION;
  String coreCssPath = skinService.getPortalSkin(PortalSkinTask.DEFAULT_MODULE_NAME, skinName).getCSSPath()+"?v="+ResourceRequestHandler.VERSION;
  String brandingCss = "/rest/v1/platform/branding/css?v="+ResourceRequestHandler.VERSION;

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Multiple Factor Authentication</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <link rel="shortcut icon" type="image/x-icon"  href="<%=contextPath%>/favicon.ico" />
    <link id="brandingSkin" rel="stylesheet" type="text/css" href="<%=brandingCss%>">

    <link href="<%=coreCssPath%>" rel="stylesheet" type="text/css"/>
    <link href="<%=loginCssPath%>" rel="stylesheet" type="text/css"/>
    <script type="text/javascript" src="/eXoResources/javascript/jquery-3.2.1.js"></script>
    <script type="text/javascript" src="/eXoResources/javascript/eXo/webui/FormValidation.js"></script>
</head>
<body class="modal-open">
<div class="uiPopupWrapper">
    <div class="UIPopupWindow uiPopup uiForgotPassword UIDragObject NormalStyle">
        <div class="popupHeader ClearFix">
            <span class="popupTitle center">Multiple Factor Authentication</span>
        </div>
        <div class="popupContent">
            <form name="mfaForm" action="<%=mfaPath%>" method="post" style="margin: 0px;" autocomplete="off">
                <div class="content">
                    <div class="form-horizontal">
                        <div class="control-group">
                            <label class="control-label">Token :</label>
                            <div class="controls">
                                <input class="username" data-validation="require" name="token" type="text" />
                            </div>
                        </div>
                    </div>
                </div>
                <% if (initialUri != null) {
                  initialUri = EntityEncoder.FULL.encode(initialUri);
                %>
                <input type="hidden" name="initialUri" value="<%=initialUri%>"/>

                <% } %>
                <input type="hidden" name="action" value="checkMfaToken"/>

                <div id="UIPortalLoginFormAction" class="uiAction">
                    <button type="submit" class="btn btn-primary">Valider</button>

                </div>
            </form>
        </div>
    </div>
</div>
</body>
</html>
