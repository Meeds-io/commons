package org.exoplatform.mfa.impl;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.mfa.api.MfaConnector;

public class ActivIdMfaConnector extends MfaConnector {
  
  private String name;
  
  
  public ActivIdMfaConnector() {
  }
  public ActivIdMfaConnector(InitParams initParams) {
  }
  
  public ActivIdMfaConnector(String name) {
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
