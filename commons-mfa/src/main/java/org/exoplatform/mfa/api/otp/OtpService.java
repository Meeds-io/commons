package org.exoplatform.mfa.api.otp;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.mfa.api.MfaService;
import org.exoplatform.mfa.api.MfaSystemService;
import org.exoplatform.services.resources.ResourceBundleService;


import java.time.Clock;
import java.util.HashMap;
import java.util.Locale;

public class OtpService implements MfaSystemService {

  private static final String TYPE = "OTP";

  private HashMap<String, OtpConnector> otpConnectors;
  private String                        activeConnector;


  private MfaService            mfaService;
  private ResourceBundleService resourceBundleService;
  
  public OtpService(InitParams initParams, MfaService mfaService, ResourceBundleService resourceBundleService) {
    otpConnectors =new HashMap<>();
    ValueParam activeConnectorParam = initParams.getValueParam("activeConnector");
    if (activeConnectorParam!=null) {
      activeConnector=activeConnectorParam.getValue();
    }
    this.mfaService=mfaService;
    this.resourceBundleService=resourceBundleService;
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

  @Override
  public void removeSecret(String userId) {
    getActiveConnector().removeSecret(userId);
  }

  public String generateUrlFromSecret(String user,String secret) {
    return getActiveConnector().generateUrlFromSecret(user,secret);
  }

  public String getType() {
    return TYPE;
  }

  @Override
  public String getHelpTitle(Locale locale) {
    return resourceBundleService.getResourceBundle("locale.portlet.mfaAccess.mfaAccess",locale).getString("mfa.otp.help.title");
  }

  @Override
  public String getHelpContent(Locale locale) {
    return resourceBundleService.getResourceBundle("locale.portlet.mfaAccess.mfaAccess",locale).getString("mfa.otp.help.content");
  }

}
