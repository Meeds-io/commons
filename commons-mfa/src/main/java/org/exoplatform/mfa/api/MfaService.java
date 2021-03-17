package org.exoplatform.mfa.api;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.mfa.api.otp.OtpConnector;

public class MfaService {

//  private static final String FEATURE_NAME = "mfa";
  
  
  
  private List<String> protectedNavigations;
  
  public MfaService(InitParams initParams) {
    
    ValueParam protectedGroupNavigations = initParams.getValueParam("protectedGroupNavigations");
    protectedNavigations = Arrays.stream(protectedGroupNavigations.getValue().split(","))
                                 .filter(s -> !s.isEmpty())
                                 .map(s -> "/portal/g/"+s.replace("/",":"))
                                 .collect(Collectors.toList());
  }
  
  
  
  public boolean isProtectedUri(String requestUri) {
    return protectedNavigations.stream()
                               .filter(s -> requestUri.contains(s))
                               .count() > 0;
    
  }
}
