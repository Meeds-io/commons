package org.exoplatform.mfa.api.fido;

import org.exoplatform.container.component.ComponentPlugin;
import org.json.JSONObject;

public class FidoConnector implements ComponentPlugin {
  
  String name;
  
  public FidoConnector() {
  
  }
  
  public FidoConnector(String name) {
    this.name=name;
  }
  
  public JSONObject startRegistration(String user, String rpHostName) {
    return null;
  }
  public JSONObject finishRegistration(String user, JSONObject data) {
    return null;
  }
  public JSONObject startAuthentication(String userId, String rpHostName) {return null;}
  public JSONObject finishAuthentication(String user, JSONObject data) {return null;}
  
  
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
