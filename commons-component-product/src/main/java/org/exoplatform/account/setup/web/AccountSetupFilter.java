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
package org.exoplatform.account.setup.web;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.web.filter.Filter;

/**
 * @author <a href="fbradai@exoplatform.com">Fbradai</a>
 */
public class AccountSetupFilter implements Filter {
  private static final String PLF_PLATFORM_EXTENSION_SERVLET_CTX = "/commons-extension";

  private static final String ACCOUNT_SETUP_SERVLET              = "/accountSetup";

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest httpServletRequest = (HttpServletRequest) request;
    HttpServletResponse httpServletResponse = (HttpServletResponse) response;

    ExoContainer container = PortalContainer.getInstance();
    AccountSetupService accountSetupService = container.getComponentInstanceOfType(AccountSetupService.class);

    boolean setupDone = accountSetupService.mustSkipAccountSetup();

    String requestUri = httpServletRequest.getRequestURI();
    boolean isRestUri = requestUri.contains(container.getContext().getRestContextName());
    if (!setupDone && !isRestUri) {
      ServletContext platformExtensionContext = httpServletRequest.getSession()
                                                                  .getServletContext()
                                                                  .getContext(PLF_PLATFORM_EXTENSION_SERVLET_CTX);
      platformExtensionContext.getRequestDispatcher(ACCOUNT_SETUP_SERVLET).forward(httpServletRequest, httpServletResponse);
      return;
    }
    chain.doFilter(request, response);
  }
}
