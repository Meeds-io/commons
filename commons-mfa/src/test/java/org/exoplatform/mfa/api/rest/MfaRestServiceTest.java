package org.exoplatform.mfa.api.rest;

import org.exoplatform.mfa.api.MfaService;
import org.exoplatform.mfa.api.otp.OtpService;
import org.exoplatform.mfa.rest.mfa.MfaRestService;
import org.exoplatform.mfa.rest.otp.OtpRestService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
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

  @Test
  public void testGetMfaSystem() {
    when(mfaService.getMfaSystem()).thenReturn("otp");

    Response response = mfaRestService.getMfaSystem();

    assertEquals("{\"mfaSystem\":\"otp\"}",response.getEntity().toString());

  }
}
