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
import java.util.ResourceBundle;

import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIComponent;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class MockWebuiApplication extends WebuiApplication {

  @Override
  public String getApplicationInitParam(String name) {

    return null;
  }

  @Override
  public String getApplicationGroup() {

    return null;
  }

  @Override
  public String getApplicationId() {

    return null;
  }

  @Override
  public String getApplicationName() {

    return null;
  }

  @Override
  public String getApplicationType() {

    return null;
  }

  @Override
  public ResourceBundle getOwnerResourceBundle(String username, Locale locale) {

    return null;
  }

  @Override
  public ResourceBundle getResourceBundle(Locale locale) {

    return rb;
  }
  
  ResourceBundle rb;
  
  public void setResourceBundle(ResourceBundle rb) {
    this.rb = rb;
  }
  
  public <T extends UIComponent> T createUIComponent(Class<T> type, String configId, String id,
                                                     WebuiRequestContext context) throws Exception {
    return type.getConstructor().newInstance();
    
  }

}
