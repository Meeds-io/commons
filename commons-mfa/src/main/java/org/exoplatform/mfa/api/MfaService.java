package org.exoplatform.mfa.api;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MfaService {
  private static final String DEFAULT_MFA_SYSTEM = "fido2";

  private String              mfaSystem;

  private List<String>        protectedNavigations=new ArrayList<>();
  private List<String>        protectedGroups=new ArrayList<>();

  public MfaService(InitParams initParams) {

    ValueParam protectedGroupNavigations = initParams.getValueParam("protectedGroupNavigations");
    if (protectedGroupNavigations!=null) {
      this.protectedNavigations = Arrays.stream(protectedGroupNavigations.getValue().split(","))
                                        .filter(s -> !s.isEmpty())
                                        .map(s -> "/portal/g/" + s.replace("/", ":"))
                                        .collect(Collectors.toList());
    }
  
    ValueParam protectedGroups = initParams.getValueParam("protectedGroups");
    if (protectedGroups!=null) {
      this.protectedGroups = Arrays.stream(protectedGroups.getValue().split(","))
                                   .filter(s -> !s.isEmpty())
                                   .collect(Collectors.toList());
    }

    ValueParam mfaSystemParam = initParams.getValueParam("mfaSystem");
    if (mfaSystemParam != null) {
      mfaSystem = mfaSystemParam.getValue();
    } else {
      mfaSystem = DEFAULT_MFA_SYSTEM;
    }
  }

  public boolean isProtectedUri(String requestUri) {
    return protectedNavigations.stream().filter(s -> requestUri.contains(s)).count() > 0;

  }
  
  public boolean currentUserIsInProtectedGroup() {
    Identity identity = ConversationState.getCurrent().getIdentity();
    return protectedGroups.stream().anyMatch(group -> identity.isMemberOf(group));
  }

  public String getMfaSystem() {
    return mfaSystem;
  }
}
