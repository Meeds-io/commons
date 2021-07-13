package org.exoplatform.mfa.api.otp;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.mfa.api.MfaService;
import org.exoplatform.mfa.api.otp.OtpConnector;
import org.exoplatform.mfa.api.otp.OtpService;
import org.exoplatform.mfa.impl.otp.ExoOtpConnector;
import org.exoplatform.portal.branding.BrandingService;
import org.exoplatform.services.resources.ResourceBundleService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OtpServiceTest {
  private OtpService otpService;

  @Mock
  OtpConnector otpConnector;
  @Mock
  ResourceBundleService resourceBundleService;

  @Mock
  MfaService mfaService;

  @Before
  public void setUp() {
    InitParams initParams = new InitParams();
    ValueParam valueParam = new ValueParam();
    valueParam.setName("activeConnector");
    valueParam.setValue("ExoOtpConnector");
    initParams.addParam(valueParam);
    this.mfaService = mock(MfaService.class);
    this.resourceBundleService=mock(ResourceBundleService.class);
    
    this.otpService=new OtpService(initParams,mfaService,resourceBundleService);
    
    otpConnector = mock(ExoOtpConnector.class);
    doCallRealMethod().when(otpConnector).setName(anyString());
    when(otpConnector.getName()).thenCallRealMethod();
    otpConnector.setName("ExoOtpConnector");
    this.otpService.addConnector(otpConnector);

  }

  @Test
  public void testValidateToken() {
    //GIVEN
    String user = "john";
    String token = "token";
    //WHEN
    when(otpConnector.validateToken(eq(user),eq(token),any())).thenReturn(true);
    otpService.validateToken(user,token);

    //THEN
    verify(otpConnector, times(1)).validateToken(eq(user),eq(token),any());
    verify(mfaService, times(1)).deleteRevocationRequest(user,otpService.getType());
  }

  @Test
  public void testInValidateToken() {
    //GIVEN
    String user = "john";
    String token = "token";
    //WHEN
    when(otpConnector.validateToken(eq(user),eq(token),any())).thenReturn(false);
    otpService.validateToken(user,token);

    //THEN
    verify(otpConnector, times(1)).validateToken(eq(user),eq(token),any());
    verify(mfaService, times(0)).deleteRevocationRequest(user,otpService.getType());
  }

  @Test
  public void testIsMfaInitializedForUser() {
    //GIVEN

    //WHEN
    otpService.isMfaInitializedForUser("user");

    //THEN
    verify(otpConnector, times(1)).isMfaInitializedForUser("user");
  }

  @Test
  public void testGenerateSecret() {
    //GIVEN

    //WHEN
    otpService.generateSecret("user");

    //THEN
    verify(otpConnector, times(1)).generateSecret("user");
  }


  @Test
  public void testGenerateUrlFromSecret() {
    //GIVEN

    //WHEN
    otpService.generateUrlFromSecret("user","secret");

    //THEN
    verify(otpConnector, times(1)).generateUrlFromSecret("user","secret");
  }

  @Test
  public void testRemoveSecret() {
    //GIVEN

    //WHEN
    otpService.removeSecret("user");

    //THEN
    verify(otpConnector, times(1)).removeSecret("user");
  }



}
