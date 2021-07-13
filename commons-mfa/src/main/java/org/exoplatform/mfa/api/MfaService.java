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

import java.util.*;
import java.util.stream.Collectors;

public class MfaService {

  public static final String MFA_FEATURE = "mfa";

  private static final String MFA_SYSTEM_SETTING = "mfaSystem";

  private static final String MFA_PROTECTED_GROUPS = "protectedGroups";

  private String              mfaSystem;

  private ExoFeatureService featureService;
  private SettingService    settingService;

  private List<String>        protectedNavigations=new ArrayList<>();

  private List<String>        protectedGroups;

  private MfaStorage      mfaStorage;
  
  private Map<String, MfaSystemComponentPlugin> mfaSystemServices;
  
  public MfaService(InitParams initParams, MfaStorage mfaStorage, ExoFeatureService featureService, SettingService settingService) {
    this.featureService = featureService;
    this.settingService = settingService;
    ValueParam protectedGroupNavigations = initParams.getValueParam("protectedGroupNavigations");
    if (protectedGroupNavigations!=null) {
      this.protectedNavigations = Arrays.stream(protectedGroupNavigations.getValue().split(","))
                                        .filter(s -> !s.isEmpty())
                                        .map(s -> "/portal/g/" + s.replace("/", ":"))
                                        .collect(Collectors.toList());
    }

    String protectedGroupsValue="";
    if (settingService.get(Context.GLOBAL, Scope.GLOBAL,MFA_PROTECTED_GROUPS)!=null &&
        !settingService.get(Context.GLOBAL,Scope.GLOBAL,MFA_PROTECTED_GROUPS).getValue().toString().isEmpty()) {
      protectedGroupsValue=settingService.get(Context.GLOBAL, Scope.GLOBAL,MFA_PROTECTED_GROUPS).getValue().toString();
    } else  {
      protectedGroupsValue=initParams.getValueParam(MFA_PROTECTED_GROUPS).getValue();
    }
    this.protectedGroups = Arrays.stream(protectedGroupsValue.split(","))
                                 .filter(s -> !s.isEmpty())
                                 .collect(Collectors.toList());

    mfaSystemServices=new HashMap<>();
    if (settingService.get(Context.GLOBAL, Scope.GLOBAL,MFA_SYSTEM_SETTING)!=null &&
        !settingService.get(Context.GLOBAL,Scope.GLOBAL,MFA_SYSTEM_SETTING).getValue().toString().isEmpty()) {
      this.mfaSystem=settingService.get(Context.GLOBAL, Scope.GLOBAL,MFA_SYSTEM_SETTING).getValue().toString();
    } else {
      this.mfaSystem = initParams.getValueParam(MFA_SYSTEM_SETTING).getValue();
    }
    this.mfaStorage=mfaStorage;
    mfaSystemServices=new HashMap<>();
  }

  public MfaSystemService getMfaSystemService(String type) {
    return mfaSystemServices.get(type).getMfaSystemService();
  }

  public void addConnector(MfaSystemComponentPlugin mfaSystemComponentPlugin) {
    this.mfaSystemServices.put(mfaSystemComponentPlugin.getMfaSystemService().getType(), mfaSystemComponentPlugin);
  }

  public boolean isProtectedUri(String requestUri) {
    return protectedNavigations.stream().anyMatch(requestUri::contains);

  }
  
  public boolean currentUserIsInProtectedGroup(Identity identity) {
    return protectedGroups.stream().anyMatch(identity::isMemberOf);
  }

  public String getMfaSystem() {
    return this.mfaSystem;
  }

  public MfaSystemService getMfaSystemService() {
    return this.mfaSystemServices.get(mfaSystem).getMfaSystemService();
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

  public List<String> getAvailableMfaSystems() {
    return new ArrayList<>(this.mfaSystemServices.keySet());
  }

  public void saveActiveFeature(String status) {
    featureService.saveActiveFeature(MFA_FEATURE, Boolean.parseBoolean(status));
  }

  public boolean setMfaSystem(String mfaSystem) {
    if (mfaSystemServices.containsKey(mfaSystem)) {
      settingService.set(Context.GLOBAL, Scope.GLOBAL, MFA_SYSTEM_SETTING, new SettingValue<>(mfaSystem));
      this.mfaSystem=mfaSystem;
      return true;
    } else {
      return false;
    }
  }

  public boolean isMfaFeatureActivated() {

    return featureService.isActiveFeature(MFA_FEATURE);
  }

  public void saveProtectedGroups(String groups) {
    settingService.set(Context.GLOBAL, Scope.GLOBAL, MFA_PROTECTED_GROUPS, new SettingValue<>(groups));
    this.protectedGroups = Arrays.asList(groups.split(","));
  }

  public List<String> getProtectedGroups() {
    return this.protectedGroups;
  }
}
