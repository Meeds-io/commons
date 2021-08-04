package org.exoplatform.mfa.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import org.exoplatform.commons.api.settings.ExoFeatureService;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.mfa.api.otp.OtpService;
import org.exoplatform.mfa.storage.MfaStorage;
import org.exoplatform.mfa.storage.dto.RevocationRequest;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;

public class MfaServiceTest {

  private MfaService mfaService;

  @Mock
  MfaStorage         mfaStorage;

  @Mock
  ExoFeatureService  featureService;

  @Mock
  SettingService     settingService;

  @Mock
  ListenerService    listenerService;

  @Mock
  OtpService         otpService;

  @Before
  public void setUp() throws Exception {
    featureService = mock(ExoFeatureService.class);
    settingService = mock(SettingService.class);
    listenerService = mock(ListenerService.class);
    doNothing().when(listenerService).broadcast(any());
    when(settingService.get(Context.GLOBAL, Scope.GLOBAL, "mfaSystem")).thenReturn(null);
    when(settingService.get(Context.GLOBAL, Scope.GLOBAL, "protectedGroups")).thenReturn(null);
    InitParams initParams = new InitParams();
    ValueParam protectedGroupNavigations = new ValueParam();
    protectedGroupNavigations.setName("protectedGroupNavigations");
    protectedGroupNavigations.setValue("/portal/g/:platform:administrators");
    initParams.addParam(protectedGroupNavigations);

    ValueParam protectedGroups = new ValueParam();
    protectedGroups.setName("protectedGroups");
    protectedGroups.setValue("/platform/administrators");
    initParams.addParam(protectedGroups);

    ValueParam mfaSystem = new ValueParam();
    mfaSystem.setName("mfaSystem");
    mfaSystem.setValue("OTP");
    initParams.addParam(mfaSystem);

    mfaStorage = mock(MfaStorage.class);
    this.mfaService = new MfaService(initParams, mfaStorage, featureService, settingService, listenerService);
  }

  @Test
  public void testOtpService() {
    otpService = mock(OtpService.class);
    when(otpService.getType()).thenReturn("otp");
    MfaSystemComponentPlugin componentPlugin = mock(MfaSystemComponentPlugin.class);
    when(componentPlugin.getMfaSystemService()).thenReturn(otpService);
    this.mfaService.addConnector(componentPlugin);
    assertEquals("otp", mfaService.getMfaSystemService("otp").getType());
  }

  @Test
  public void testIsProtectedUri() {
    assertTrue(mfaService.isProtectedUri("/portal/g/:platform:administrators/page1"));
    assertTrue(mfaService.isProtectedUri("/portal/g/:platform:administrators/page2"));
    assertFalse(mfaService.isProtectedUri("/portal/g/:platform:web-contributors/page1"));
    assertFalse(mfaService.isProtectedUri("/portal/g/:platform:web-contributors/page2"));
    assertFalse(mfaService.isProtectedUri("/portal/dw"));
  }

  @Test
  public void testCurrentUserIsInProtectedGroup() {
    List<MembershipEntry> groups = new ArrayList<>();
    MembershipEntry mb = new MembershipEntry("/platform/administrators", "member");
    groups.add(mb);

    Identity rootIdentity = new Identity("root", groups);
    ConversationState.setCurrent(new ConversationState(rootIdentity));

    assertTrue(mfaService.currentUserIsInProtectedGroup(rootIdentity));

  }

  @Test
  public void testAddRevocationRequestWhichNotExists() {
    String type = "otp";
    mfaService.addRevocationRequest("john", type);

    ArgumentCaptor<RevocationRequest> revocationRequestArgumentCaptor = ArgumentCaptor.forClass(RevocationRequest.class);

    verify(mfaStorage, times(1)).createRevocationRequest(revocationRequestArgumentCaptor.capture());

    assertEquals(type, revocationRequestArgumentCaptor.getValue().getType());

  }

  @Test
  public void testAddRevocationRequestWhichExists() {

    String type = "otp";
    String user = "john";
    when(mfaStorage.countByUsernameAndType(user, type)).thenReturn(0L, 1L);
    mfaService.addRevocationRequest(user, type);
    mfaService.addRevocationRequest(user, type);

    verify(mfaStorage, times(1)).createRevocationRequest(any());
  }

  @Test
  public void testDeleteRevocationRequestByUserAndType() {

    String type = "otp";
    String user = "john";
    mfaService.deleteRevocationRequest(user, type);

    verify(mfaStorage, times(1)).deleteRevocationRequest(user, type);
  }

  @Test
  public void testCancelRevocationRequest() {
    long id = 0L;
    mfaService.cancelRevocationRequest(id);
    ArgumentCaptor<RevocationRequest> revocationRequestArgumentCaptor = ArgumentCaptor.forClass(RevocationRequest.class);
    verify(mfaStorage, times(1)).deleteById(id);
  }

  // @Test
  // public void testConfirmRevocationRequest(){
  // To activate when OtpService is loaded as connector in mfa service
  // long id = 0L;
  // RevocationRequest returnedRevocationRequest = new RevocationRequest();
  // returnedRevocationRequest.setId(id);
  // returnedRevocationRequest.setUser("user");
  // returnedRevocationRequest.setType("otp");
  //
  // when(mfaStorage.findById(id)).thenReturn(returnedRevocationRequest);
  //
  // mfaService.confirmRevocationRequest(id);
  // verify(mfaStorage,times(1)).deleteById(id);
  // }

  @Test
  public void testFindAll() {

    List<RevocationRequest> revocationRequests = new ArrayList<>();
    revocationRequests.add(new RevocationRequest(0L, "john", "otp"));
    revocationRequests.add(new RevocationRequest(1L, "mary", "otp"));

    when(mfaStorage.findAll()).thenReturn(revocationRequests);

    List<RevocationRequest> returnedRevocationRequests = mfaService.getAllRevocationRequests();
    assertEquals(revocationRequests.size(), returnedRevocationRequests.size());
    returnedRevocationRequests.stream().forEach(revocationRequest -> {
      RevocationRequest rq =
                           revocationRequests.stream()
                                             .filter(revocationRequest1 -> revocationRequest1.getId() == revocationRequest.getId())
                                             .findFirst()
                                             .get();
      assertEquals(revocationRequest.getUser(), rq.getUser());
      assertEquals(revocationRequest.getType(), rq.getType());

    });
  }

  @Test
  public void testFindById() {

    RevocationRequest revocationRequest = new RevocationRequest(0L, "john", "otp");
    when(mfaStorage.findById(0L)).thenReturn(revocationRequest);

    RevocationRequest result = mfaService.getRevocationRequestById(revocationRequest.getId());

    assertEquals(revocationRequest.getId(), result.getId());
    assertEquals(revocationRequest.getUser(), result.getUser());
    assertEquals(revocationRequest.getType(), result.getType());

  }

  @Test
  public void testIsMfaFeatureActivated() {
    when(featureService.isActiveFeature(any())).thenReturn(true);
    boolean isMfaActivated = mfaService.isMfaFeatureActivated();
    assertTrue(isMfaActivated);
  }

  @Test
  public void testGetMfaSystem() {
    String mfaSystem = mfaService.getMfaSystem();
    assertEquals("OTP", mfaSystem);
  }

  @Test
  public void testSaveActiveFeature() {
    mfaService.saveActiveFeature("otp");
    String mfaSystem = mfaService.getMfaSystem();
    assertEquals("OTP", mfaSystem);
  }

  @Test
  public void testSaveProtectedGroups() {
    List<String> groups = mfaService.getProtectedGroups();
    assertEquals("/platform/administrators", groups.get(0));
    mfaService.saveProtectedGroups("/platform/rewarding,/platform/users");
    groups = mfaService.getProtectedGroups();
    assertEquals("/platform/rewarding", groups.get(0));
    assertEquals("/platform/users", groups.get(1));
  }

  @Test
  public void testSaveProtectedNavigations() {
    mfaService.saveProtectedNavigations("UsersManegement, GroupsManegement");
    List<MfaNavigations> mfaNavigations = mfaService.getProtectedNavigations();
    assertEquals(2,mfaNavigations.size());
    assertEquals("UsersManegement",mfaNavigations.get(0).getId());
    assertEquals(" GroupsManegement",mfaNavigations.get(1).getId());
  }

  @Test
  public void testDeleteProtectedNavigations() {
    mfaService.saveProtectedNavigations("UsersManegement, GroupsManegement");
    List<MfaNavigations> mfaNavigations = mfaService.getProtectedNavigations();
    assertEquals(2,mfaNavigations.size());
    mfaService.deleteProtectedNavigations("UsersManegement");
    mfaNavigations = mfaService.getProtectedNavigations();
    assertEquals(1,mfaNavigations.size());
  }

  @Test
  public void testswitchMfaSystemToExistingOne() {
    MfaSystemService system1 = mock(MfaSystemService.class);
    when(system1.getType()).thenReturn("type1");
    MfaSystemComponentPlugin componentPlugin = mock(MfaSystemComponentPlugin.class);
    when(componentPlugin.getMfaSystemService()).thenReturn(system1);
    this.mfaService.addConnector(componentPlugin);

    String mfaSystem = mfaService.getMfaSystem();
    assertEquals("OTP", mfaSystem);

    boolean result = mfaService.setMfaSystem("type1");
    assertTrue(result);
    assertEquals("type1", mfaService.getMfaSystem());

  }

  @Test
  public void testswitchMfaSystemToNonExistingOne() {
    MfaSystemService system1 = mock(MfaSystemService.class);
    when(system1.getType()).thenReturn("type1");
    MfaSystemComponentPlugin componentPlugin = mock(MfaSystemComponentPlugin.class);
    when(componentPlugin.getMfaSystemService()).thenReturn(system1);
    this.mfaService.addConnector(componentPlugin);

    String mfaSystem = mfaService.getMfaSystem();
    assertEquals("OTP", mfaSystem);

    boolean result = mfaService.setMfaSystem("type2");
    assertFalse(result);
    assertEquals("OTP", mfaService.getMfaSystem());

  }

  @Test
  public void testGetAvailableMfaSystems() {

    MfaSystemService system1 = mock(MfaSystemService.class);
    when(system1.getType()).thenReturn("type1");
    MfaSystemComponentPlugin componentPlugin = mock(MfaSystemComponentPlugin.class);
    when(componentPlugin.getMfaSystemService()).thenReturn(system1);
    this.mfaService.addConnector(componentPlugin);

    MfaSystemService system2 = mock(MfaSystemService.class);
    when(system2.getType()).thenReturn("type2");
    MfaSystemComponentPlugin componentPlugin2 = mock(MfaSystemComponentPlugin.class);
    when(componentPlugin2.getMfaSystemService()).thenReturn(system2);
    this.mfaService.addConnector(componentPlugin2);

    MfaSystemService system3 = mock(MfaSystemService.class);
    when(system3.getType()).thenReturn("type3");
    MfaSystemComponentPlugin componentPlugin3 = mock(MfaSystemComponentPlugin.class);
    when(componentPlugin3.getMfaSystemService()).thenReturn(system3);
    this.mfaService.addConnector(componentPlugin3);

    assertEquals(3, this.mfaService.getAvailableMfaSystems().size());
    assertTrue(this.mfaService.getAvailableMfaSystems().stream().anyMatch(s -> s.equals("type1")));
    assertTrue(this.mfaService.getAvailableMfaSystems().stream().anyMatch(s -> s.equals("type2")));
    assertTrue(this.mfaService.getAvailableMfaSystems().stream().anyMatch(s -> s.equals("type3")));

  }
}
