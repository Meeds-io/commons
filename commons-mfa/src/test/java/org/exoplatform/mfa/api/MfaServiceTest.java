package org.exoplatform.mfa.api;

import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class MfaServiceTest {
  
  private MfaService mfaService;
  
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
    this.mfaService=new MfaService(initParams);
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
    ConversationState.setCurrent(new ConversationState(new Identity("root", groups)));
    
    assertTrue(mfaService.currentUserIsInProtectedGroup());
    
  
  
  
  }
}
