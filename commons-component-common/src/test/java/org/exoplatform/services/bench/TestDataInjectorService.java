/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.services.bench;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.RuntimeDelegate;

import junit.framework.TestCase;

import org.exoplatform.services.rest.impl.MultivaluedMapImpl;
import org.exoplatform.services.rest.impl.RuntimeDelegateImpl;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Aug 2, 2011  
 */
public class TestDataInjectorService extends TestCase {
  
  DataInjectorService service;
  
  FakeDataInjector injector;
  
  UriInfo uriInfo;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
    service = new DataInjectorService();
    injector = new FakeDataInjector();
    service.addInjector(injector);
    uriInfo = new UriInfo() {
      
      @Override
      public UriBuilder getRequestUriBuilder() {
        return null;
      }
      
      @Override
      public URI getRequestUri() {
        return null;
      }
      
      @Override
      public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
        return new MultivaluedMapImpl();
      }
      
      @Override
      public MultivaluedMap<String, String> getQueryParameters() {
        return new MultivaluedMapImpl();
      }
      
      @Override
      public List<PathSegment> getPathSegments(boolean decode) {
        return null;
      }
      
      @Override
      public List<PathSegment> getPathSegments() {
        return null;
      }
      
      @Override
      public MultivaluedMap<String, String> getPathParameters(boolean decode) {
        return null;
      }
      
      @Override
      public MultivaluedMap<String, String> getPathParameters() {
        return null;
      }
      
      @Override
      public String getPath(boolean decode) {
        return null;
      }
      
      @Override
      public String getPath() {
        return null;
      }
      
      @Override
      public List<String> getMatchedURIs(boolean decode) {
        return null;
      }
      
      @Override
      public List<String> getMatchedURIs() {
        return null;
      }
      
      @Override
      public List<Object> getMatchedResources() {
        return null;
      }
      
      @Override
      public UriBuilder getBaseUriBuilder() {
        return null;
      }
      
      @Override
      public URI getBaseUri() {
        return null;
      }
      
      @Override
      public UriBuilder getAbsolutePathBuilder() {
        return null;
      }
      
      @Override
      public URI getAbsolutePath() {
        return null;
      }
    };
  }
  
  public void testInject() {
    service.inject(injector.getName(), uriInfo);
    assertTrue(injector.isInitialized());
  }
  
  public void testReject() {
    service.reject(injector.getName(), uriInfo);
    assertFalse(injector.isInitialized());
  }
  
  public void testExecute() {
    Object obj = service.execute(injector.getName(), uriInfo);
    assertNotNull(obj);
  }
  
}
