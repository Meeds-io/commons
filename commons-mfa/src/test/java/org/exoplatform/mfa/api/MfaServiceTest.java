package org.exoplatform.mfa.api;

import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.container.xml.InitParams;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class MfaServiceTest {
  
  private MfaService mfaService;
  
  @Before
  public void setUp() {
    InitParams initParams = new InitParams();
    ValueParam protectedGroupNavigations = new ValueParam();
    protectedGroupNavigations.setName("protectedGroupNavigations");
    protectedGroupNavigations.setValue("/platform/administrators");
    initParams.addParam(protectedGroupNavigations);
    this.mfaService=new MfaService(initParams);
  }
  
  @Test
  public void testIsProtectedUri() {
    assertTrue(mfaService.isProtectedUri("/portal/g/:platform:administrators/page1"));
    assertTrue(mfaService.isProtectedUri("/portal/g/:platform:administrators/page2"));
    assertFalse(mfaService.isProtectedUri("/portal/g/:platform:web-contributors/page1"));
    assertFalse(mfaService.isProtectedUri("/portal/g/:platform:web-contributors/page1"));
    assertFalse(mfaService.isProtectedUri("/portal/dw"));
  }
  
}
