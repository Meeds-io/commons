package org.exoplatform.mfa.api.rest;

import org.exoplatform.commons.api.settings.ExoFeatureService;
import org.exoplatform.mfa.api.MfaService;
import org.exoplatform.mfa.api.MfaSystemComponentPlugin;
import org.exoplatform.mfa.api.otp.OtpService;
import org.exoplatform.mfa.rest.mfa.MfaRestService;
import org.exoplatform.mfa.rest.otp.OtpRestService;
import org.exoplatform.mfa.storage.dto.RevocationRequest;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.idm.UserDAOImpl;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;


import static org.mockito.Mockito.doNothing;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class MfaRestServiceTest {

  MfaRestService mfaRestService;


  @Mock
  MfaService mfaService;

  @Before
  public void setUp() {
    mfaService=mock(MfaService.class);
    mfaRestService = new MfaRestService(mfaService);
  }
  private void startSessionAs(String username) {
    Identity identity = new Identity(username);
    ConversationState state = new ConversationState(identity);
    ConversationState.setCurrent(state);
  }

  @Test
  public void testAddNonExistingRevocationRequest() {
    String user = "john";
    String mfaSystem = "otp";
    startSessionAs(user);
    when(mfaService.addRevocationRequest(user, mfaSystem)).thenReturn(true);
    Response response = mfaRestService.addRevocationRequest(mfaSystem);
    assertEquals(200,response.getStatus());
    assertEquals("{\"result\":\"true\"}",response.getEntity().toString());
    verify(mfaService,times(1)).addRevocationRequest(user, mfaSystem);
  }

  @Test
  public void testAddExistingRevocationRequest() {
    String user = "john";
    String mfaSystem = "otp";
    startSessionAs(user);
    when(mfaService.addRevocationRequest(user, mfaSystem)).thenReturn(false);
    Response response = mfaRestService.addRevocationRequest(mfaSystem);
    assertEquals(200,response.getStatus());
    assertEquals("{\"result\":\"false\"}",response.getEntity().toString());

    verify(mfaService,times(1)).addRevocationRequest(user, mfaSystem);
  }

  @Test
  public void testGetRevocationRequest() {
    String user = "root";
    startSessionAs(user);

    List<RevocationRequest> revocationRequests = new ArrayList<>();
    revocationRequests.add(new RevocationRequest(0L,"john","otp"));
    revocationRequests.add(new RevocationRequest(1L,"mary","otp"));
    when(mfaService.getAllRevocationRequests()).thenReturn(revocationRequests);

    Response response = mfaRestService.getRevocationRequests();
    assertEquals(200,response.getStatus());
    assertEquals(
        "{\"requests\":["
            + "{\"id\":0,\"type\":\"otp\",\"username\":\"john\"},"
            + "{\"id\":1,\"type\":\"otp\",\"username\":\"mary\"}"
            + "]}",response.getEntity().toString());

  }


  @Test
  public void testCancelRevocationRequest() {
    String user = "root";
    startSessionAs(user);
    RevocationRequest revocationRequest =new RevocationRequest(0L,"john","otp");
    when(mfaService.getRevocationRequestById(0l)).thenReturn(revocationRequest);
    doNothing().when(mfaService).cancelRevocationRequest(0L);
    Response response = mfaRestService.updateRevocationRequests("0","cancel");
    assertEquals(200,response.getStatus());
    verify(mfaService,times(1)).cancelRevocationRequest(0L);
  }

  @Test
  public void testConfirmRevocationRequest() {
    String user = "root";
    startSessionAs(user);
    RevocationRequest revocationRequest =new RevocationRequest(0L,"john","otp");
    when(mfaService.getRevocationRequestById(0l)).thenReturn(revocationRequest);
    doNothing().when(mfaService).confirmRevocationRequest(0L);
    Response response = mfaRestService.updateRevocationRequests("0","confirm");
    assertEquals(200,response.getStatus());
    verify(mfaService,times(1)).confirmRevocationRequest(0L);
  }

  @Test
  public void testUpdateNonExistingRevocationRequest() {
    String user = "root";
    startSessionAs(user);
    when(mfaService.getRevocationRequestById(0l)).thenReturn(null);
    doNothing().when(mfaService).confirmRevocationRequest(0L);
    Response response = mfaRestService.updateRevocationRequests("0","confirm");
    assertEquals(400,response.getStatus());
    verify(mfaService,times(0)).confirmRevocationRequest(0L);
    verify(mfaService,times(0)).cancelRevocationRequest(0L);
  }

  @Test
  public void testUpdateNonValidStatusRevocationRequest() {
    String user = "root";
    startSessionAs(user);
    RevocationRequest revocationRequest =new RevocationRequest(0L,"john","otp");
    when(mfaService.getRevocationRequestById(0l)).thenReturn(revocationRequest);
    doNothing().when(mfaService).confirmRevocationRequest(0L);
    Response response = mfaRestService.updateRevocationRequests("0","nonValidStatus");
    assertEquals(400,response.getStatus());
    verify(mfaService,times(0)).confirmRevocationRequest(0L);
    verify(mfaService,times(0)).cancelRevocationRequest(0L);
  }
  
  @Test
  public void testChangeMfaFeatureActivation() {
    Response response = mfaRestService.changeMfaFeatureActivation("true");

    assertEquals(200 ,response.getStatus());
    verify(mfaService,times(1)).saveActiveFeature("true");
  }

  @Test
  public void testChangeMfaSystemWithExistingOne() {
    when(mfaService.setMfaSystem("oidc")).thenReturn(true);
    Response response = mfaRestService.changeMfaSystem("oidc");
    assertEquals(200 ,response.getStatus());
    verify(mfaService,times(1)).setMfaSystem("oidc");
  }
  @Test
  public void testChangeMfaSystemWithNonExistingOne() {
    when(mfaService.setMfaSystem("oidc")).thenReturn(false);
    Response response = mfaRestService.changeMfaSystem("oidc");
    assertEquals(400 ,response.getStatus());
    verify(mfaService,times(1)).setMfaSystem("oidc");
  }

  @Test
  public void testGetProtectedGroups() {
    Response response = mfaRestService.getProtectedGroups();

    assertEquals(200 ,response.getStatus());
    verify(mfaService,times(1)).getProtectedGroups();
  }

  @Test
  public void testSaveProtectedGroups() {
    Response response = mfaRestService.saveProtectedGroups("/platform/users, /platform/rewarding");

    assertEquals(200 ,response.getStatus());
    verify(mfaService,times(1)).saveProtectedGroups("/platform/users, /platform/rewarding");
  }

  @Test
  public void testGetMfaSystems() {
    Response response = mfaRestService.getAvalailableMfaSystems();

    assertEquals(200 ,response.getStatus());
    verify(mfaService,times(1)).getAvailableMfaSystems();
  }

  @Test
  public void testGetProtectedNavigations() {
    Response response = mfaRestService.getProtectedNavigations();

    assertEquals(200 ,response.getStatus());
    verify(mfaService,times(1)).getProtectedNavigations();
  }

  @Test
  public void testDeleteNavigation() {
    Response response = mfaRestService.deleteNavigation("UsersManegement");

    assertEquals(200 ,response.getStatus());
    verify(mfaService,times(1)).deleteProtectedNavigations("UsersManegement");
  }

  @Test
  public void testSaveProtectedNavigations() {
    Response response = mfaRestService.saveProtectedNavigations("UsersManegement, GroupsManegement");

    assertEquals(200 ,response.getStatus());
    verify(mfaService,times(1)).saveProtectedNavigations("UsersManegement, GroupsManegement");
  }
}
