package org.exoplatform.mfa.api;

import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.mfa.storage.MfaStorage;
import org.exoplatform.mfa.storage.dto.RevocationRequest;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MfaServiceTest {
  
  private MfaService mfaService;

  @Mock
  MfaStorage mfaStorage;

  @Before
  public void setUp() {
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
    this.mfaService=new MfaService(initParams,mfaStorage);
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
}
