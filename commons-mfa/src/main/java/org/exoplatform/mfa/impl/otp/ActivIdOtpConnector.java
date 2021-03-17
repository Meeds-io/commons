package org.exoplatform.mfa.impl.otp;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.mfa.api.otp.OtpConnector;

public class ActivIdOtpConnector extends OtpConnector {
  
  private String name;
  
  
  public ActivIdOtpConnector() {
  }
  public ActivIdOtpConnector(InitParams initParams) {
  }
  
  public ActivIdOtpConnector(String name) {
    super(name);
  }
  
  @Override
  public boolean isMfaInitializedForUser(String user) {
    return true;
  }
  
  @Override
  public boolean validateToken(String user, String token) {
    return token.equals("123456");
  }
}
