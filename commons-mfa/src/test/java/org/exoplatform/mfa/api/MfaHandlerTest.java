package org.exoplatform.mfa.api;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.web.ControllerContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class MfaHandlerTest {
  
  private MfaHandler mfaHandler;
  
  @Before
  public void setUp() {
    this.mfaHandler=new MfaHandler();
  }
  
  @Test
  public void testExecute() {
    ControllerContext context = Mockito.mock(ControllerContext.class);
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    HttpSession session = Mockito.mock(HttpSession.class);
    Mockito.when(request.getSession()).thenReturn(session);
  
    Mockito.when(request.getParameter("token")).thenReturn("123456");
  
    ConversationState state = Mockito.mock(ConversationState.class);
    Identity identity = Mockito.mock(Identity.class);
    Mockito.when(identity.getUserId()).thenReturn("john");
    Mockito.when(state.getIdentity()).thenReturn(identity);
    ConversationState.setCurrent(state);
    
    Mockito.when(context.getRequest()).thenReturn(request);
    Mockito.when(context.getResponse()).thenReturn(response);
    
    MfaService mfaService = Mockito.mock(MfaService.class);
    Mockito.when(mfaService.validateToken(Mockito.anyString(),Mockito.anyString())).thenReturn(true);
    ExoContainerContext.getCurrentContainer().registerComponentInstance(mfaService);
  
    try {
      mfaHandler.execute(context);
    } catch (Exception e) {
      e.printStackTrace();
    }
    Mockito.verify(session,Mockito.times(1)).setAttribute("mfaValidated","123456");
    
  }
}
