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
package org.exoplatform.commons.testing.mock;

import java.util.Locale;

import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.services.resources.Orientation;
import org.exoplatform.web.application.Application;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.web.application.URLBuilder;
import org.exoplatform.web.url.PortalURL;
import org.exoplatform.web.url.ResourceType;
import org.exoplatform.web.url.URLFactory;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class MockParentRequestContext extends RequestContext {

  public MockParentRequestContext(RequestContext parentAppRequestContext, Application app) {
    super(parentAppRequestContext, app);
    
  }

  @Override
  public Orientation getOrientation() {

    return null;
  }

  @Override
  public String getRequestParameter(String name) {

    return null;
  }

  @Override
  public String[] getRequestParameterValues(String name) {
    return new String[0];
  }

  @Override
  public URLBuilder getURLBuilder() {

    return null;
  }
  
  public Locale getLocale()
  {
     return null;
  }

  @Override
  public boolean useAjax() {

    return false;
  }

  @Override
  public URLFactory getURLFactory() {
    return null;
  }

  @Override
  public <R, U extends PortalURL<R, U>> U newURL(ResourceType<R, U> resourceType, URLFactory urlFactory) {
    return null;
  }

  @Override
  public UserPortal getUserPortal() {
    return null;
  }

}
