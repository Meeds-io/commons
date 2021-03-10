package org.exoplatform.mfa.api;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;

public class MfaService {
  
  private static final String FEATURE_NAME = "mfa";
  
  private HashMap<String, MfaConnector> mfaConnectors;
  private String         activeConnector;
  
  private List<String> protectedNavigations;
  
  public MfaService(InitParams initParams) {
    mfaConnectors=new HashMap<>();
    ValueParam activeConnectorParam = initParams.getValueParam("activeConnector");
    MfaConnector defaultMfaConnector=new MfaConnector("defaultConnector");
    mfaConnectors.put(defaultMfaConnector.getName(), defaultMfaConnector);
    if (activeConnectorParam!=null) {
      activeConnector=activeConnectorParam.getValue();
    } else {
      activeConnector = defaultMfaConnector.getName();
    }
    
    ValueParam protectedGroupNavigations = initParams.getValueParam("protectedGroupNavigations");
    protectedNavigations = Arrays.stream(protectedGroupNavigations.getValue().split(","))
                                 .filter(s -> !s.isEmpty())
                                 .map(s -> "/portal/g/"+s.replace("/",":"))
                                 .collect(Collectors.toList());
  }
  
  public boolean validateToken(String user, String token) {
    return getActiveConnector().validateToken(user,token);
  }
  
  public void addConnector (MfaConnector mfaConnector) {
    mfaConnectors.put(mfaConnector.getName(),mfaConnector);
  }
  
  private MfaConnector getActiveConnector() {
    return mfaConnectors.get(activeConnector);
  }
  
  public boolean isProtectedUri(String requestUri) {
    return protectedNavigations.stream()
                               .filter(s -> requestUri.contains(s))
                               .count() > 0;
    
  }
}
