package org.exoplatform.mfa.api.otp;

import org.exoplatform.container.xml.InitParams;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OtpServiceTest {
  
  private OtpService otpService;
  
  @Before
  public void setUp() {
    InitParams initParams = new InitParams();
    this.otpService=new OtpService(initParams);
  }
  
  @Test
  public void testValidateToken() {
    assertTrue(otpService.validateToken("john","123456"));
    assertTrue(otpService.validateToken("mary","123456"));
    assertFalse(otpService.validateToken("john","654321"));
    assertFalse(otpService.validateToken("mary","654321"));
  }
}
