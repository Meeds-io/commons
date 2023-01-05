package org.exoplatform.commons.info.test;

import static org.junit.Assert.assertNotEquals;

import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.commons.info.PlatformInformationRESTService;
import org.exoplatform.commons.info.PlatformInformationRESTService.JsonPlatformInfo;
import org.exoplatform.commons.info.ProductInformations;
import org.exoplatform.commons.testing.BaseResourceTestCase;
import org.exoplatform.component.test.*;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.wadl.research.HTTPMethods;
import org.exoplatform.services.security.*;

@ConfiguredBy(
  {
      @ConfigurationUnit(scope = ContainerScope.ROOT, path = "conf/configuration.xml"),
      @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/configuration.xml"),
      @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/commons-component-product-configuration.xml"),
  }
)
public class PlatformInformationRESTServiceTEST extends BaseResourceTestCase {

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
    endSession();
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    assertNotNull(getContainer().getComponentInstanceOfType(PlatformInformationRESTService.class));
    registry(new PlatformInformationRESTService(getService(ProductInformations.class), getService(UserACL.class)));
  }

  public void testGetPlatformInformationAnonymous() throws Exception { // NOSONAR
    ContainerResponse response = service(HTTPMethods.GET.toString(), "/platform/info", StringUtils.EMPTY, null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    JsonPlatformInfo jsonPlatformInfo = (JsonPlatformInfo) response.getEntity();
    assertNotNull(jsonPlatformInfo);
    assertNull(jsonPlatformInfo.getBuildNumber());
    assertNull(jsonPlatformInfo.getDateOfKeyGeneration());
    assertNull(jsonPlatformInfo.getDuration());
    assertNull(jsonPlatformInfo.getIsMobileCompliant());
    assertNull(jsonPlatformInfo.getNbUsers());
    assertNull(jsonPlatformInfo.getPlatformBuildNumber());
    assertNull(jsonPlatformInfo.getPlatformEdition());
    assertNull(jsonPlatformInfo.getPlatformRevision());
    assertNull(jsonPlatformInfo.getProductCode());
    assertNull(jsonPlatformInfo.getRunningProfile());
    assertNull(jsonPlatformInfo.getUnlockKey());
    assertEquals(PlatformInformationRESTService.MIN_MOBILE_SUPPORTED_VERSION, jsonPlatformInfo.getPlatformVersion());
  }

  public void testGetPlatformInformationAuthenticatedUser() throws Exception { // NOSONAR
    startSessionAs("mary");
    ContainerResponse response = service(HTTPMethods.GET.toString(), "/platform/info", StringUtils.EMPTY, null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    JsonPlatformInfo jsonPlatformInfo = (JsonPlatformInfo) response.getEntity();
    assertNotNull(jsonPlatformInfo);
    assertNull(jsonPlatformInfo.getBuildNumber());
    assertNull(jsonPlatformInfo.getDateOfKeyGeneration());
    assertNull(jsonPlatformInfo.getDuration());
    assertNull(jsonPlatformInfo.getIsMobileCompliant());
    assertNull(jsonPlatformInfo.getNbUsers());
    assertNull(jsonPlatformInfo.getPlatformBuildNumber());
    assertNull(jsonPlatformInfo.getPlatformEdition());
    assertNull(jsonPlatformInfo.getPlatformRevision());
    assertNull(jsonPlatformInfo.getProductCode());
    assertNull(jsonPlatformInfo.getRunningProfile());
    assertNull(jsonPlatformInfo.getUnlockKey());
    assertEquals(PlatformInformationRESTService.MIN_MOBILE_SUPPORTED_VERSION, jsonPlatformInfo.getPlatformVersion());
  }

  public void testGetPlatformInformationAuthenticatedAdminUser() throws Exception { // NOSONAR
    startSessionAs("root");
    ContainerResponse response = service(HTTPMethods.GET.toString(), "/platform/info", StringUtils.EMPTY, null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    JsonPlatformInfo jsonPlatformInfo = (JsonPlatformInfo) response.getEntity();
    assertNotNull(jsonPlatformInfo);
    assertNotNull(jsonPlatformInfo.getPlatformBuildNumber());
    assertNotNull(jsonPlatformInfo.getPlatformEdition());
    assertNotNull(jsonPlatformInfo.getPlatformRevision());
    assertNotNull(jsonPlatformInfo.getPlatformVersion());
    assertNotEquals(PlatformInformationRESTService.MIN_MOBILE_SUPPORTED_VERSION, jsonPlatformInfo.getPlatformVersion());
  }

  private void startSessionAs(String user) {
    try {
      Authenticator authenticator = getContainer().getComponentInstanceOfType(Authenticator.class);
      Identity userIdentity = authenticator.createIdentity(user);
      ConversationState.setCurrent(new ConversationState(userIdentity));
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private void endSession() {
    ConversationState.setCurrent(null);
  }

  private void registry(Object resource) {
    resourceBinder.addResource(resource, null);
  }

}
