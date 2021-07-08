package org.exoplatform.mfa.api;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.api.settings.ExoFeatureService;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.mfa.api.otp.OtpService;
import org.exoplatform.mfa.storage.MfaStorage;
import org.exoplatform.mfa.storage.dto.RevocationRequest;
import org.exoplatform.services.security.Identity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MfaService {
  private static final String DEFAULT_MFA_SYSTEM = "otp";

  private static final String MFA_OIDC = "oidc";

  private static final String MFA_FIDO = "fido";

  public static final String MFA_FEATURE = "mfa";

  private String              mfaSystem;

  private ExoFeatureService featureService;

  private List<String>        protectedNavigations=new ArrayList<>();
  private List<String>        protectedGroups=new ArrayList<>();

  private MfaStorage      mfaStorage;

  public MfaService(InitParams initParams, MfaStorage mfaStorage) {

  public MfaService(InitParams initParams, MfaStorage mfaStorage, ExoFeatureService featureService) {
    this.featureService = featureService;
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

  public List<RevocationRequest> getAllRevocationRequests() {
    return mfaStorage.findAll();
  }
  public RevocationRequest getRevocationRequestById(Long id) {
    return mfaStorage.findById(id);
  }

  public void confirmRevocationRequest(Long id) {
    RevocationRequest revocationRequest = mfaStorage.findById(id);
    if (revocationRequest!=null && revocationRequest.getType().equals("otp")) {
      //todo : change this when services are added in map by configuration
      OtpService otpService = CommonsUtils.getService(OtpService.class);
      otpService.removeSecret(revocationRequest.getUser());
    }
    mfaStorage.deleteById(id);
  }

  public void cancelRevocationRequest(Long id) {
    mfaStorage.deleteById(id);
  }

  public void disableMfaFeature(String status) {
    SettingService settingService = CommonsUtils.getService(SettingService.class);
    if(protectedNavigations != null&& !this.protectedNavigations.isEmpty()) {
        settingService.set(Context.GLOBAL, Scope.GLOBAL.id(null), "PROTECTED_GROUPS_NAVIGATIONS", SettingValue.create(String.join(",", this.protectedNavigations)));
        this.protectedNavigations = new ArrayList<>();
    }
    if(protectedGroups != null&& !this.protectedGroups.isEmpty()) {
        settingService.set(Context.GLOBAL, Scope.GLOBAL.id(null), "PROTECTED_GROUPS", SettingValue.create(String.join(",", this.protectedGroups)));
        this.protectedGroups = new ArrayList<>();
    }
    settingService.set(Context.GLOBAL, Scope.GLOBAL.id(null), "MFA_STATUS", SettingValue.create(status));
  }

  public void enableMfaFeature(String status) {
    SettingService settingService = CommonsUtils.getService(SettingService.class);
    if(this.protectedNavigations.isEmpty()) {
      SettingValue<?> protectedNavigationsValue = settingService.get(Context.GLOBAL, Scope.GLOBAL.id(null), "PROTECTED_GROUPS_NAVIGATIONS");
      if(protectedNavigationsValue != null) {
        this.protectedNavigations = Arrays.asList(protectedNavigationsValue.getValue().toString().split(","));
      }
    }
    if(this.protectedGroups.isEmpty()) {
      SettingValue<?> protectedGroupsValue = settingService.get(Context.GLOBAL, Scope.GLOBAL.id(null), "PROTECTED_GROUPS");
      if(protectedGroupsValue != null) {
        this.protectedGroups = Arrays.asList(protectedGroupsValue.getValue().toString().split(","));
      }
    }
    settingService.set(Context.GLOBAL, Scope.GLOBAL.id(null), "MFA_STATUS", SettingValue.create(status));
  }

  public boolean getMfaStatus() {
    SettingService settingService = CommonsUtils.getService(SettingService.class);
    SettingValue<?> settingValue = settingService.get(Context.GLOBAL, Scope.GLOBAL.id(null), "MFA_STATUS");
    return Boolean.valueOf(settingValue.getValue().toString());
  }
  
  public void switchMfaSystem(String mfaSystem) {
    switch (mfaSystem) {
      case "OTP" :
        this.mfaSystem = DEFAULT_MFA_SYSTEM;
        break;
      case "Fido 2" :
        this.mfaSystem = MFA_FIDO;
        break;
      case "SuperGluu" :
        this.mfaSystem = MFA_OIDC;
        break;
      default:
        this.mfaSystem =DEFAULT_MFA_SYSTEM;
        break;
    }
  }

  public boolean isMfaFeatureActivated() {
    Boolean isMfaFeatureAcivated = featureService.isActiveFeature(MFA_FEATURE);
    if(isMfaFeatureAcivated == null) {
      return true;
    }
    return isMfaFeatureAcivated;
  }
}
