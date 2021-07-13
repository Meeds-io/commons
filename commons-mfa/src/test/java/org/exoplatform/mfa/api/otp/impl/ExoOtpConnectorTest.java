package org.exoplatform.mfa.api.otp.impl;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.mfa.api.otp.OtpConnector;
import org.exoplatform.mfa.impl.otp.CodeGenerator;
import org.exoplatform.mfa.impl.otp.ExoOtpConnector;
import org.exoplatform.portal.branding.BrandingService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ExoOtpConnectorTest {

  @Mock
  BrandingService brandingService;

  @Mock
  SettingService settingService;

  String defaultIssuer = "Test Platform";
  String defaultUser = "john";
  private static final String OTP_SECRET = "otpSecret";
  private static final String OTP_SECRET_CHECKED = "otpSecretChecked";

  @Before
  public void setUp() {
    settingService = mock(SettingService.class);
    brandingService = mock(BrandingService.class);
    when(brandingService.getCompanyName()).thenReturn(defaultIssuer);
  }

  @Test
  public void testIsMfaInitializedForUserWithoutSettingsStored() {
    //GIVEN
    //no secret stored for this user
    when(settingService.get(new Context(Context.USER.getName(), defaultUser), Scope.APPLICATION, OTP_SECRET)).thenReturn(null);

    InitParams initParams=new InitParams();
    OtpConnector exoOtpConnector = new ExoOtpConnector(initParams, settingService, brandingService);

    //when
    boolean isMfaInitializedForUser = exoOtpConnector.isMfaInitializedForUser(defaultUser);

    //then
    assertFalse(isMfaInitializedForUser);
  }

  @Test
  public void testIsMfaInitializedForUserWithSecretButNotValidated() {
    //GIVEN
    //Secret stored for this user
    when(settingService.get(new Context(Context.USER.getName(), defaultUser), Scope.APPLICATION, OTP_SECRET))
        .thenReturn(new SettingValue("123456789"));
    // but not validated
    when(settingService.get(new Context(Context.USER.getName(), defaultUser), Scope.APPLICATION, OTP_SECRET_CHECKED))
        .thenReturn(null);
    InitParams initParams=new InitParams();
    OtpConnector exoOtpConnector = new ExoOtpConnector(initParams, settingService, brandingService);

    //when
    boolean isMfaInitializedForUser = exoOtpConnector.isMfaInitializedForUser(defaultUser);

    //then
    assertFalse(isMfaInitializedForUser);
  }

  @Test
  public void testIsMfaInitializedForUserWithSecretAndValidated() {
    //GIVEN
    //Secret stored for this user
    when(settingService.get(new Context(Context.USER.getName(), defaultUser), Scope.APPLICATION, OTP_SECRET))
        .thenReturn(new SettingValue("123456789"));
    // but not validated
    when(settingService.get(new Context(Context.USER.getName(), defaultUser), Scope.APPLICATION, OTP_SECRET_CHECKED))
        .thenReturn(new SettingValue("true"));
    InitParams initParams=new InitParams();
    OtpConnector exoOtpConnector = new ExoOtpConnector(initParams, settingService, brandingService);

    //when
    boolean isMfaInitializedForUser = exoOtpConnector.isMfaInitializedForUser(defaultUser);

    //then
    assertTrue(isMfaInitializedForUser);
  }

  @Test
  public void testGenerateSecret() {
    //GIVEN

    InitParams initParams=new InitParams();
    OtpConnector exoOtpConnector = new ExoOtpConnector(initParams, settingService, brandingService);

    //when
    String secret = exoOtpConnector.generateSecret(defaultUser);


    //then
    verify(settingService, times(1))
        .set(eq(new Context(Context.USER.getName(), defaultUser)), eq(Scope.APPLICATION), eq(OTP_SECRET),any());
    verify(settingService, times(1))
        .set(eq(new Context(Context.USER.getName(), defaultUser)), eq(Scope.APPLICATION), eq(OTP_SECRET_CHECKED),any());
  }

  @Test
  public void testGenerateUrl() {
    //GIVEN
    InitParams initParams=new InitParams();
    OtpConnector exoOtpConnector = new ExoOtpConnector(initParams, settingService, brandingService);

    //when
    String url = exoOtpConnector.generateUrlFromSecret(defaultUser,"1234567890");


    //then
    String expected="otpauth://totp/"+defaultUser+"?secret=1234567890&issuer="+defaultIssuer.replace(" ","%20")+"&algorithm=SHA1"
        + "&digits=6"
        + "&period=30";
    assertEquals(expected,url);
  }

  @Test
  public void testValidateTokenSuccess() {
    //GIVEN
    InitParams initParams=new InitParams();
    //Secret stored for this user
    when(settingService.get(new Context(Context.USER.getName(), defaultUser), Scope.APPLICATION, OTP_SECRET))
        .thenReturn(new SettingValue("123456789"));
    //Secret stored for this user
    when(settingService.get(new Context(Context.USER.getName(), defaultUser), Scope.APPLICATION, OTP_SECRET))
        .thenReturn(new SettingValue("123456789"));
    // but not validated
    when(settingService.get(new Context(Context.USER.getName(), defaultUser), Scope.APPLICATION, OTP_SECRET_CHECKED))
        .thenReturn(null);

    String instantExpected = "2021-07-01T10:00:00Z";
    Clock clock = Clock.fixed(Instant.parse(instantExpected), ZoneId.of("UTC"));

    OtpConnector exoOtpConnector = new ExoOtpConnector(initParams, settingService, brandingService);

    //when
    //819132 is the token calculated with secret=123456789 at instant 2021-07-01T10:00:00Z
    boolean result = exoOtpConnector.validateToken(defaultUser,"819132", clock);

    //then
    assertTrue(result);
    ArgumentCaptor<SettingValue> settingValueArgumentCaptor=ArgumentCaptor.forClass(SettingValue.class);;

    verify(settingService, times(1))
        .set(eq(new Context(Context.USER.getName(), defaultUser)), eq(Scope.APPLICATION), eq(OTP_SECRET_CHECKED),
             settingValueArgumentCaptor.capture());
    assertTrue(Boolean.parseBoolean((String)settingValueArgumentCaptor.getValue().getValue()));
  }

  @Test
  public void testRemoveSecret() {
    //GIVEN

    InitParams initParams=new InitParams();
    OtpConnector exoOtpConnector = new ExoOtpConnector(initParams, settingService, brandingService);

    //when
    exoOtpConnector.removeSecret(defaultUser);


    //then
    verify(settingService, times(1))
        .remove(new Context(Context.USER.getName(), defaultUser), Scope.APPLICATION,OTP_SECRET);
    verify(settingService, times(1))
        .remove(new Context(Context.USER.getName(), defaultUser), Scope.APPLICATION, OTP_SECRET_CHECKED);
  }
}
