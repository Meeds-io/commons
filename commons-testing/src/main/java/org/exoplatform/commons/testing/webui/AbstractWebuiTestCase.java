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
package org.exoplatform.commons.testing.webui;


import java.util.HashMap;

import junit.framework.TestCase;

import org.exoplatform.commons.testing.mock.MockParentRequestContext;
import org.exoplatform.commons.testing.mock.MockResourceBundle;
import org.exoplatform.commons.testing.mock.MockWebUIRequestContext;
import org.exoplatform.commons.testing.mock.MockWebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;

/**
 * Abstract TestCase to test a Webui UIComponent
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public abstract class AbstractWebuiTestCase extends TestCase {


  protected MockWebuiApplication webuiApplication;

  public AbstractWebuiTestCase() throws Exception {
    webuiApplication = new MockWebuiApplication();
    webuiApplication.setResourceBundle(new MockResourceBundle(new HashMap<String, Object>()));
  }

  public final void setUp() throws Exception {

    initRequest();
    
    doSetUp();
  }



  private void initRequest() {
    MockWebUIRequestContext context = new MockWebUIRequestContext(new MockParentRequestContext(null, null), webuiApplication);
    WebuiRequestContext.setCurrentInstance(context);
  }
  
  protected void doSetUp() {
    // to be overriden
  }
  


  /**
   * Convenience method to set an entry in the application resource bundle
   * @param key
   * @param value
   */
  protected void setResourceBundleEntry(String key, String value) {
    getAppRes().put(key, value);
  }

  
  /**
   * Convenience method to access the app resource bundle mock
   * @return
   */
  private MockResourceBundle getAppRes() {
    try {
      return (MockResourceBundle) webuiApplication.getResourceBundle(null);
    } catch (Exception e) {  //we want to catch all exceptions here to know a testcase is false or not.
      fail(e.getMessage());
    }
    return null;
  }
  
  

  
}
