package org.exoplatform.mfa.filter;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.web.filter.Filter;

public class MfaFilter implements Filter {
  
  private static String MFA_URI = "/portal/dw/mfa-access";
  private static final Log          LOG     = ExoLogger.getLogger(MfaFilter.class);
  private List<String> excludedUrls = new ArrayList<>(
      Arrays.asList("/portal/skins",
                    "/portal/scripts",
                    "/portal/javascript",
                    "/portal/rest",
                    "/portal/service-worker.js",
                    MFA_URI
      )
  );
  
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest httpServletRequest = (HttpServletRequest) request;
    HttpServletResponse httpServletResponse = (HttpServletResponse) response;
    HttpSession session = httpServletRequest.getSession();
    ExoContainer container = PortalContainer.getInstance();
    MfaService mfaService = container.getComponentInstanceOfType(MfaService.class);

    String requestUri = httpServletRequest.getRequestURI();
    if (httpServletRequest.getRemoteUser() != null && mfaService.isMfaFeatureActivated() &&
        excludedUrls.stream().noneMatch(s -> requestUri.startsWith(s)) &&
        (mfaService.isProtectedUri(requestUri) ||
            mfaService.currentUserIsInProtectedGroup(ConversationState.getCurrent().getIdentity()))) {
      if (shouldAuthenticateFromSession(session)) {
        LOG.debug("Mfa Filter must redirect on page to fill token");
        httpServletResponse.sendRedirect(MFA_URI+"?initialUri=" + requestUri);
        return;
      }
    }

    chain.doFilter(request, response);

  }

  private boolean shouldAuthenticateFromSession(HttpSession session) {
    if (session.getAttribute("mfaValidated") == null) return true;
    if (!(boolean) session.getAttribute("mfaValidated")) return true;
    Instant expiration = (Instant)session.getAttribute("mfaExpiration");
    if (expiration!=null &&
        expiration.isBefore(Instant.now())) return true;
    return false;
  }
}
