package org.exoplatform.mfa.api.otp;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;

import java.time.Clock;
import java.util.HashMap;

public class OtpService {
  
  private HashMap<String, OtpConnector> otpConnectors;
  private String                        activeConnector;
  
  public OtpService(InitParams initParams) {
    otpConnectors =new HashMap<>();
    ValueParam activeConnectorParam = initParams.getValueParam("activeConnector");
    if (activeConnectorParam!=null) {
      activeConnector=activeConnectorParam.getValue();
    }
  }
  
  public boolean validateToken(String user, String token) {
    return getActiveConnector().validateToken(user, token, Clock.systemDefaultZone());
  }
  
  public void addConnector (OtpConnector mfaConnector) {
    otpConnectors.put(mfaConnector.getName(),mfaConnector);
  }
  
  private OtpConnector getActiveConnector() {
    return otpConnectors.get(activeConnector);
  }

  public boolean isMfaInitializedForUser(String userId) {
    return getActiveConnector().isMfaInitializedForUser(userId);
  }

  public String generateSecret(String userId) {
    return getActiveConnector().generateSecret(userId);
  }
  public String generateUrlFromSecret(String user,String secret) {
    return getActiveConnector().generateUrlFromSecret(user,secret);
  }


}
