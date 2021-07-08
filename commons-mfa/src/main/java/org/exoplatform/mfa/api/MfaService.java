package org.exoplatform.mfa.api;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.mfa.storage.MfaStorage;
import org.exoplatform.mfa.storage.dto.RevocationRequest;
import org.exoplatform.services.security.Identity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MfaService {
  private static final String DEFAULT_MFA_SYSTEM = "otp";

  private String              mfaSystem;

  private List<String>        protectedNavigations=new ArrayList<>();
  private List<String>        protectedGroups=new ArrayList<>();


  private MfaStorage      mfaStorage;

  public MfaService(InitParams initParams, MfaStorage mfaStorage) {

    ValueParam protectedGroupNavigations = initParams.getValueParam("protectedGroupNavigations");
    if (protectedGroupNavigations!=null) {
      this.protectedNavigations = Arrays.stream(protectedGroupNavigations.getValue().split(","))
                                        .filter(s -> !s.isEmpty())
                                        .map(s -> "/portal/g/" + s.replace("/", ":"))
                                        .collect(Collectors.toList());
    }
  
    ValueParam protectedGroupsValueParam = initParams.getValueParam("protectedGroups");
    if (protectedGroupsValueParam!=null) {
      this.protectedGroups = Arrays.stream(protectedGroupsValueParam.getValue().split(","))
                                   .filter(s -> !s.isEmpty())
                                   .collect(Collectors.toList());
    }

    ValueParam mfaSystemParam = initParams.getValueParam("mfaSystem");
    if (mfaSystemParam != null) {
      mfaSystem = mfaSystemParam.getValue();
    } else {
      mfaSystem = DEFAULT_MFA_SYSTEM;
    }

    this.mfaStorage=mfaStorage;
  }

  public boolean isProtectedUri(String requestUri) {
    return protectedNavigations.stream().anyMatch(requestUri::contains);

  }
  
  public boolean currentUserIsInProtectedGroup(Identity identity) {
    return protectedGroups.stream().anyMatch(identity::isMemberOf);
  }

  public String getMfaSystem() {
    return mfaSystem;
  }

  public boolean addRevocationRequest(String username, String mfaType) {
    if (!hasRevocationRequest(username,mfaType)) {
      RevocationRequest revocationRequest = new RevocationRequest();
      revocationRequest.setUser(username);
      revocationRequest.setType(mfaType);
      mfaStorage.createRevocationRequest(revocationRequest);
      return true;
    } else {
      return false;
    }
  }

  public boolean hasRevocationRequest(String username, String mfaType) {
    return mfaStorage.countByUsernameAndType(username,mfaType) > 0;
  }

  public void deleteRevocationRequest(String username, String type) {
    mfaStorage.deleteRevocationRequest(username,type);
  }
}
