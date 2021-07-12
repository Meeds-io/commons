package org.exoplatform.mfa.api.otp;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.mfa.api.MfaService;

import java.time.Clock;
import java.util.HashMap;

public class OtpService {

  private static final String TYPE = "otp";

  private HashMap<String, OtpConnector> otpConnectors;
  private String                        activeConnector;

  private MfaService mfaService;
  
  public OtpService(InitParams initParams, MfaService mfaService) {
    otpConnectors =new HashMap<>();
    ValueParam activeConnectorParam = initParams.getValueParam("activeConnector");
    if (activeConnectorParam!=null) {
      activeConnector=activeConnectorParam.getValue();
    }
    this.mfaService=mfaService;
  }
  
  public boolean validateToken(String user, String token) {
    boolean isValidToken = getActiveConnector().validateToken(user, token, Clock.systemDefaultZone());
    if (isValidToken) {
      mfaService.deleteRevocationRequest(user,TYPE);
    }
    return isValidToken;
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
  public void removeSecret(String userId) {
    getActiveConnector().removeSecret(userId);
  }
  public String generateUrlFromSecret(String user,String secret) {
    return getActiveConnector().generateUrlFromSecret(user,secret);
  }

  public String getType() {
    return TYPE;
  }
}
