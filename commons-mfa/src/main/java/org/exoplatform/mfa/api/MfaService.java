package org.exoplatform.mfa.api;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.mfa.api.otp.OtpConnector;
import org.exoplatform.portal.config.UserACL;

public class MfaService {
  private static final String DEFAULT_MFA_SYSTEM = "fido2";
  
  private String mfaSystem;
  
  //  private static final String FEATURE_NAME = "mfa";
  
  
  
  private List<String> protectedNavigations;
  
  public MfaService(InitParams initParams) {
    
    ValueParam protectedGroupNavigations = initParams.getValueParam("protectedGroupNavigations");
    protectedNavigations = Arrays.stream(protectedGroupNavigations.getValue().split(","))
                                 .filter(s -> !s.isEmpty())
                                 .map(s -> "/portal/g/"+s.replace("/",":"))
                                 .collect(Collectors.toList());
    
    ValueParam mfaSystemParam = initParams.getValueParam("mfaSystem");
    if (mfaSystemParam!=null) {
      mfaSystem=mfaSystemParam.getValue();
    } else {
      mfaSystem=DEFAULT_MFA_SYSTEM;
    }
  }
  
  
  
  public boolean isProtectedUri(String requestUri) {
    return protectedNavigations.stream()
                               .filter(s -> requestUri.contains(s))
                               .count() > 0;
    
  }

  public boolean canAccess(String requestUri) {
    UserACL userACL = CommonsUtils.getService(UserACL.class);
    if (System.getProperty("exo.mfa.protectedGroupNavigations") != null) {
      String[] protectedGroup = System.getProperty("exo.mfa.protectedGroupNavigations").split(",") ;
      String currentGroup = requestUri.split("g/:")[1].split("/")[0].replace(":", "/");
      if ( Arrays.toString(protectedGroup).contains(currentGroup) && userACL.isUserInGroup("/" + currentGroup) ) return true;
    }
    return false;
  }
  
  public String getMfaSystem() {
    return mfaSystem;
  }
}
