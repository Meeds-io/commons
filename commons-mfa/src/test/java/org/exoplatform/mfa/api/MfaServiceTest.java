package org.exoplatform.mfa.api;

import org.exoplatform.commons.api.settings.ExoFeatureService;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.mfa.storage.MfaStorage;
import org.exoplatform.mfa.storage.dto.RevocationRequest;
import org.exoplatform.portal.branding.BrandingService;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;
import org.junit.Before;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

public class MfaServiceTest {
  
  private MfaService mfaService;

  @Mock
  MfaStorage mfaStorage;

  @Mock
  ExoFeatureService featureService;

  @Before
  public void setUp() {
    featureService=mock(ExoFeatureService.class);
    InitParams initParams = new InitParams();
    ValueParam protectedGroupNavigations = new ValueParam();
    protectedGroupNavigations.setName("protectedGroupNavigations");
    protectedGroupNavigations.setValue("/platform/administrators");
    initParams.addParam(protectedGroupNavigations);
    
    ValueParam protectedGroups = new ValueParam();
    protectedGroups.setName("protectedGroups");
    protectedGroups.setValue("/platform/administrators");
    initParams.addParam(protectedGroups);

    mfaStorage=mock(MfaStorage.class);
    this.mfaService=new MfaService(initParams,mfaStorage,featureService);
  }
  
  @Test
  public void testIsProtectedUri() {
    assertTrue(mfaService.isProtectedUri("/portal/g/:platform:administrators/page1"));
    assertTrue(mfaService.isProtectedUri("/portal/g/:platform:administrators/page2"));
    assertFalse(mfaService.isProtectedUri("/portal/g/:platform:web-contributors/page1"));
    assertFalse(mfaService.isProtectedUri("/portal/g/:platform:web-contributors/page1"));
    assertFalse(mfaService.isProtectedUri("/portal/dw"));
  }
  
  @Test
  public void testCurrentUserIsInProtectedGroup() {
    List<MembershipEntry> groups = new ArrayList<>();
    MembershipEntry mb = new MembershipEntry("/platform/administrators", "member");
    groups.add(mb);
   
    Identity rootIdentity=new Identity("root", groups);
    ConversationState.setCurrent(new ConversationState(rootIdentity));
    
    assertTrue(mfaService.currentUserIsInProtectedGroup(rootIdentity));

  }

  @Test
  public void testAddRevocationRequestWhichNotExists(){
    String type = "otp";
    mfaService.addRevocationRequest("john", type);

    ArgumentCaptor<RevocationRequest> revocationRequestArgumentCaptor=ArgumentCaptor.forClass(RevocationRequest.class);

    verify(mfaStorage,times(1)).createRevocationRequest(revocationRequestArgumentCaptor.capture());

    assertEquals(type, revocationRequestArgumentCaptor.getValue().getType());

  }

  @Test
  public void testAddRevocationRequestWhichExists(){

    String type = "otp";
    String user = "john";
    when(mfaStorage.countByUsernameAndType(user,type)).thenReturn(0L,1L);
    mfaService.addRevocationRequest(user, type);
    mfaService.addRevocationRequest(user, type);


    verify(mfaStorage,times(1)).createRevocationRequest(any());
  }

  @Test
  public void testDeleteRevocationRequestByUserAndType(){

    String type = "otp";
    String user = "john";
    mfaService.deleteRevocationRequest(user, type);

    verify(mfaStorage,times(1)).deleteRevocationRequest(user,type);
  }

  @Test
  public void testCancelRevocationRequest(){
    long id = 0L;
    mfaService.cancelRevocationRequest(id);
    ArgumentCaptor<RevocationRequest> revocationRequestArgumentCaptor=ArgumentCaptor.forClass(RevocationRequest.class);
    verify(mfaStorage,times(1)).deleteById(id);
  }

//  @Test
//  public void testConfirmRevocationRequest(){
    //To activate when OtpService is loaded as connector in mfa service
//    long id = 0L;
//    RevocationRequest returnedRevocationRequest = new RevocationRequest();
//    returnedRevocationRequest.setId(id);
//    returnedRevocationRequest.setUser("user");
//    returnedRevocationRequest.setType("otp");
//
//    when(mfaStorage.findById(id)).thenReturn(returnedRevocationRequest);
//
//    mfaService.confirmRevocationRequest(id);
//    verify(mfaStorage,times(1)).deleteById(id);
//  }

  @Test
  public void testFindAll() {

    List<RevocationRequest> revocationRequests = new ArrayList<>();
    revocationRequests.add(new RevocationRequest(0L,"john","otp"));
    revocationRequests.add(new RevocationRequest(1L,"mary","otp"));

    when(mfaStorage.findAll()).thenReturn(revocationRequests);

    List<RevocationRequest> returnedRevocationRequests = mfaService.getAllRevocationRequests();
    assertEquals(revocationRequests.size(),returnedRevocationRequests.size());
    returnedRevocationRequests.stream().forEach(revocationRequest -> {
      RevocationRequest rq = revocationRequests.stream()
                                               .filter(revocationRequest1 -> revocationRequest1.getId()==revocationRequest.getId())
                                               .findFirst().get();
      assertEquals(revocationRequest.getUser(),rq.getUser());
      assertEquals(revocationRequest.getType(),rq.getType());

    });
  }

  @Test
  public void testFindById() {

    RevocationRequest revocationRequest = new RevocationRequest(0L,"john","otp");
    when(mfaStorage.findById(0L)).thenReturn(revocationRequest);

    RevocationRequest result = mfaService.getRevocationRequestById(revocationRequest.getId());

    assertEquals(revocationRequest.getId(),result.getId());
    assertEquals(revocationRequest.getUser(),result.getUser());
    assertEquals(revocationRequest.getType(),result.getType());

  }

  @Test
  public void testIsMfaFeatureActivated() {
    boolean isMfaActivated = mfaService.isMfaFeatureActivated();
    assertFalse(isMfaActivated);
  }

  @Test
  public void testGetMfaSystem() {
    String mfaSystem = mfaService.getMfaSystem();
    assertEquals("otp", mfaSystem);
  }


  @Test
  public void testswitchMfaSystem() {
    String mfaSystem = mfaService.getMfaSystem();
    assertEquals("otp", mfaSystem);

    mfaService.switchMfaSystem("Fido 2");
    assertEquals("fido", mfaService.getMfaSystem());

    mfaService.switchMfaSystem("SuperGluu");
    assertEquals("oidc", mfaService.getMfaSystem());
  }
}
