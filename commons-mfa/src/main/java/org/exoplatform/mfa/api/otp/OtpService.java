package org.exoplatform.mfa.api.otp;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

public class OtpService {
  
  private HashMap<String, OtpConnector> otpConnectors;
  private String                        activeConnector;
  
  public OtpService(InitParams initParams) {
    otpConnectors =new HashMap<>();
    ValueParam activeConnectorParam = initParams.getValueParam("activeConnector");
    OtpConnector defaultOtpConnector =new OtpConnector("defaultConnector");
    otpConnectors.put(defaultOtpConnector.getName(), defaultOtpConnector);
    if (activeConnectorParam!=null) {
      activeConnector=activeConnectorParam.getValue();
    } else {
      activeConnector = defaultOtpConnector.getName();
    }
  }
  
  public boolean validateToken(String user, String token) {
    return getActiveConnector().validateToken(user,token);
  }
  
  public void addConnector (OtpConnector mfaConnector) {
    otpConnectors.put(mfaConnector.getName(),mfaConnector);
  }
  
  private OtpConnector getActiveConnector() {
    return otpConnectors.get(activeConnector);
  }
}
