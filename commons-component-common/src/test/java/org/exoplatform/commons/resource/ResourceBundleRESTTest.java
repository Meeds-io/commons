/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.commons.resource;

import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
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
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.commons.component.core-local-configuration.xml"),
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
