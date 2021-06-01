package org.exoplatform.mfa.api.fido;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.json.JSONObject;

import java.util.HashMap;

public class FidoService {
  private HashMap<String, FidoConnector> fidoConnectors;
  private String activeConnector;
  
  public static String FEATURE_NAME="fido-service";
  
  private static final Log LOG = ExoLogger.getLogger(FidoService.class);
  
  
  public FidoService(InitParams initParams) {
    fidoConnectors =new HashMap<>();
    ValueParam activeConnectorParam = initParams.getValueParam("activeConnector");
    if (activeConnectorParam!=null) {
      activeConnector=activeConnectorParam.getValue();
    }
  }
  
  public void addConnector (FidoConnector fidoConnector) {
    fidoConnectors.put(fidoConnector.getName(),fidoConnector);
  }
  
  public JSONObject startRegistration(String userId, String rpHostName) {
    if (getActiveConnector()!=null) {
      return getActiveConnector().startRegistration(userId, rpHostName);
    }
    LOG.warn("No Fido active connector registred");
    return null;
    
  }
  
  public JSONObject finishRegistration(String userId, JSONObject data) {
    if (getActiveConnector()!=null) {
      return getActiveConnector().finishRegistration(userId, data);
    }
    LOG.warn("No Fido active connector registred");
    return null;
    
  }
  
  public JSONObject startAuthentication(String userId, String rpHostName) {
    if (getActiveConnector()!=null) {
      return getActiveConnector().startAuthentication(userId,rpHostName);
    }
    LOG.warn("No Fido active connector registred");
    return null;
  
  }
  
  public JSONObject finishAuthentication(String userId, JSONObject data) {
    if (getActiveConnector()!=null) {
      return getActiveConnector().finishAuthentication(userId, data);
    }
    LOG.warn("No Fido active connector registred");
    return null;
    
  }
  
  private FidoConnector getActiveConnector() {
    return fidoConnectors.get(activeConnector);
  }
}
