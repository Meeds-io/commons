package org.exoplatform.mfa.api;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.web.ControllerContext;
import org.exoplatform.web.WebRequestHandler;

public class MfaHandler extends WebRequestHandler {
  
  public static final String NAME = "mfa";
  public static final String REQ_PARAM_INITIAL_URI = "initialUri";
  
  
  @Override
  public String getHandlerName() {
    return NAME;
  }
  
  @Override
  public boolean execute(ControllerContext context) throws Exception {
    HttpServletRequest req = context.getRequest();
    HttpServletResponse res = context.getResponse();
    MfaService mfaService = getService(MfaService.class);
  
    String token = req.getParameter("token");
    ConversationState state = ConversationState.getCurrent();
    if (mfaService.validateToken(state.getIdentity().getUserId(),token)) {
      req.getSession().setAttribute("mfaValidated",token);
    }
    String initialUri = req.getParameter(REQ_PARAM_INITIAL_URI);
    res.sendRedirect(initialUri);
    
    return true;
  
  }
  
  protected boolean dispatch(String path, ServletContext context, HttpServletRequest req, HttpServletResponse res)  throws ServletException, IOException {
    RequestDispatcher dispatcher = context.getRequestDispatcher(path);
    if (dispatcher != null) {
      dispatcher.forward(req, res);
      return true;
    } else {
      return false;
    }
  }
  
  @Override
  protected boolean getRequiresLifeCycle() {
    return false;
  }
  
  private <T> T getService(Class<T> clazz) {
    return ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(clazz);
  }
}
