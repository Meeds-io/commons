package org.exoplatform.mfa.api.rest;

import org.exoplatform.mfa.api.MfaService;
import org.exoplatform.mfa.api.otp.OtpService;
import org.exoplatform.mfa.rest.mfa.MfaRestService;
import org.exoplatform.mfa.rest.otp.OtpRestService;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MfaRestServiceTest {

  MfaRestService mfaRestService;

  @Mock
  MfaService mfaService;

  @Before
  public void setUp() {
    mfaService=mock(MfaService.class);
    mfaRestService = new MfaRestService(mfaService);

  }

  private void startSessionAs(String username) {
    Identity identity = new Identity(username);
    ConversationState state = new ConversationState(identity);
    ConversationState.setCurrent(state);
  }

  @Test
  public void testGetMfaSystem() {
    when(mfaService.getMfaSystem()).thenReturn("otp");

    Response response = mfaRestService.getMfaSystem();

    assertEquals("{\"mfaSystem\":\"otp\"}",response.getEntity().toString());

  }

  @Test
  public void testAddNonExistingRevocationRequest() {
    String user = "john";
    String mfaSystem = "otp";
    startSessionAs(user);
    when(mfaService.addRevocationRequest(user, mfaSystem)).thenReturn(true);
    Response response = mfaRestService.addRevocationRequest(mfaSystem);
    assertEquals(200,response.getStatus());
    assertEquals("{\"result\":\"true\"}",response.getEntity().toString());
    verify(mfaService,times(1)).addRevocationRequest(user, mfaSystem);
  }

  @Test
  public void testAddExistingRevocationRequest() {
    String user = "john";
    String mfaSystem = "otp";
    startSessionAs(user);
    when(mfaService.addRevocationRequest(user, mfaSystem)).thenReturn(false);
    Response response = mfaRestService.addRevocationRequest(mfaSystem);
    assertEquals(200,response.getStatus());
    assertEquals("{\"result\":\"false\"}",response.getEntity().toString());

    verify(mfaService,times(1)).addRevocationRequest(user, mfaSystem);
  }
}
