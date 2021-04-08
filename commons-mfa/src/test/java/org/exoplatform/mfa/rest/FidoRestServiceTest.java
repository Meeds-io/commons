package org.exoplatform.mfa.rest;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.mfa.api.otp.OtpService;
import org.exoplatform.mfa.rest.fido.FidoRestService;
import org.junit.Before;
import org.junit.Test;

public class FidoRestServiceTest {
  
  private FidoRestService fidoRestService;
  
  @Before
  public void setUp() {
    this.fidoRestService=new FidoRestService();
  }
  
}
