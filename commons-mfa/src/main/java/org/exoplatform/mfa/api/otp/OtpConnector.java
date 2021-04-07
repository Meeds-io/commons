package org.exoplatform.mfa.api.otp;

import org.exoplatform.container.component.ComponentPlugin;

public class OtpConnector implements ComponentPlugin {
  
  String name;
  
  public OtpConnector() {
    
  }
  
  public OtpConnector(String name) {
    this.name=name;
  }
  
  public boolean isMfaInitializedForUser(String user) {
    return true;
  }
  
  public boolean validateToken(String user, String token) {
    return token.equals("123456");
  }
  
  public String getName() {
    return name;
    
  }
  
  @Override
  public void setName(String s) {
    this.name=s;
  }
  
  @Override
  public String getDescription() {
    return null;
  }
  
  @Override
  public void setDescription(String s) {
  
  }
  
}
