package org.exoplatform.mfa.api.rest;

import org.apache.http.impl.bootstrap.HttpServer;
import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.mfa.api.otp.OtpService;
import org.exoplatform.mfa.rest.otp.OtpRestService;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OtpRestServiceTest {

  OtpRestService otpRestService;

  @Mock
  OtpService otpService;

  @Before
  public void setUp() {
    otpService=mock(OtpService.class);
    otpRestService = new OtpRestService(otpService);

  }

  private void startSessionAs(String username) {
    Identity identity = new Identity(username);
    ConversationState state = new ConversationState(identity);
    ConversationState.setCurrent(state);
  }

  @Test
  public void testCheckRegistration() {
    startSessionAs("john");
    when(otpService.isMfaInitializedForUser(anyString())).thenReturn(true);

    Response response = otpRestService.checkRegistration(null);
    assertEquals("{\"result\":\"true\"}", response.getEntity().toString());
  }

  @Test
  public void testGenerateSecret() {
    startSessionAs("john");
    when(otpService.isMfaInitializedForUser(anyString())).thenReturn(false);
    when(otpService.generateSecret(anyString())).thenReturn("1234567890");
    when(otpService.generateUrlFromSecret(anyString(),anyString())).thenReturn("http://urlFromSecret");

    Response response = otpRestService.generateSecret(null);
    assertEquals("{\"secret\":\"1234567890\",\"url\":\"http://urlFromSecret\"}", response.getEntity().toString());

  }

  @Test
  public void testVerifyToken() {
    startSessionAs("john");
    when(otpService.validateToken(anyString(),anyString())).thenReturn(true);

    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpSession session = mock(HttpSession.class);
    when(request.getSession()).thenReturn(session);

    Response response = otpRestService.verifyToken(request,"123456");
    assertEquals("{\"result\":\"true\"}", response.getEntity().toString());

    verify(session,times(1)).setAttribute("mfaValidated", true);


  }
}
