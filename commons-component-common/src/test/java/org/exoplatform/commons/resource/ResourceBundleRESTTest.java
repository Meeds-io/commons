package org.exoplatform.commons.resource;

import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import org.exoplatform.commons.testing.BaseResourceTestCase;
import org.exoplatform.component.test.*;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.wadl.research.HTTPMethods;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;

@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.ROOT, path = "conf/configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.commons.component.core-dependencies-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.commons.component.core-configuration.xml"),
})
public class ResourceBundleRESTTest extends BaseResourceTestCase {

  public void setUp() throws Exception {
    super.setUp();

    ResourceBundleREST resourceBundleREST = getService(ResourceBundleREST.class);
    this.resourceBinder.addResource(resourceBundleREST, null);
    ConversationState c = new ConversationState(new Identity("root"));
    ConversationState.setCurrent(c);
  }

  public void testGetBundleContent() throws Exception {
    String restPath = "/i18n/bundle/locale.test-fr.json";
    ContainerResponse response = service(HTTPMethods.GET.toString(), restPath, StringUtils.EMPTY, null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    String data = (String) response.getEntity();
    JSONObject jsonObject = new JSONObject(data);
    assertEquals(2, jsonObject.length());
    assertEquals("testvalue_fr", jsonObject.getString("testkey"));
    assertEquals("testvalue2", jsonObject.getString("testkey2"));
  }

  public void testGetBundleContentUnknownLanguage() throws Exception {
    String restPath = "/i18n/bundle/locale.test-ca.json";
    ContainerResponse response = service(HTTPMethods.GET.toString(), restPath, StringUtils.EMPTY, null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    String data = (String) response.getEntity();
    JSONObject jsonObject = new JSONObject(data);
    assertEquals(2, jsonObject.length());
    assertEquals("testvalue", jsonObject.getString("testkey"));
    assertEquals("testvalue2", jsonObject.getString("testkey2"));
  }

}
