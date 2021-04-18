package org.exoplatform.mfa.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.mfa.api.MfaService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.filter.Filter;

public class MfaFilter implements Filter {
  
  private static final Log LOG = ExoLogger.getLogger(MfaFilter.class);
  
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest httpServletRequest = (HttpServletRequest) request;
    HttpServletResponse httpServletResponse = (HttpServletResponse) response;
    HttpSession session = httpServletRequest.getSession();
    ExoContainer container = PortalContainer.getInstance();
    MfaService mfaService = container.getComponentInstanceOfType(MfaService.class);

    String requestUri = httpServletRequest.getRequestURI();
    if (mfaService.isProtectedUri(requestUri)) {
      if ((session.getAttribute("mfaValidated")==null || !(boolean)session.getAttribute("mfaValidated")) && mfaService.canAccess(requestUri)) {
          LOG.info("Mfa Filter must redirect on page to fill token");
          httpServletResponse.sendRedirect("/portal/dw/mfa-access?initialUri=" + requestUri);
          return;
        }
      }

    chain.doFilter(request, response);


  }
}
