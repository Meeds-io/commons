package org.exoplatform.mfa.api;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.mfa.api.otp.OtpConnector;
import org.exoplatform.portal.config.UserACL;

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

  public boolean canAccess() {
    UserACL userACL = CommonsUtils.getService(UserACL.class);
    if (System.getProperty("exo.mfa.protectedGroupNavigations") != null) {
      String[] protectedGroup = System.getProperty("exo.mfa.protectedGroupNavigations").split(",") ;
      for (String group : protectedGroup) {
        if (userACL.isUserInGroup(group)) return true;
      }

    }
    return false;
  }
}
