/*
 * Copyright (C) 2015 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.commons.utils;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.webui.util.Util;

/**
 * Test class for {@link CommonsUtils}
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class CommonsUtilsMockingTest {

  private MockedStatic<CommonsUtils> COMMONS_UTILS;
  private MockedStatic<Util> UTIL;

  @Before
  public void beforeTest() {
    COMMONS_UTILS = mockStatic(CommonsUtils.class);
    UTIL = mockStatic(Util.class);
  }

  @After
  public void afterTest() {
    COMMONS_UTILS.close();
    UTIL.close();
  }

  @Test
  public void testShouldReturnDefaultPortalSite() {
    UserPortalConfigService userPortalConfigService = mock(UserPortalConfigService.class);
    when(userPortalConfigService.getDefaultPortal()).thenReturn("intranet");

    COMMONS_UTILS.when(() -> CommonsUtils.getService(UserPortalConfigService.class)).thenReturn(userPortalConfigService);
    COMMONS_UTILS.when(() -> CommonsUtils.getCurrentSite()).thenCallRealMethod();

    SiteKey site = CommonsUtils.getCurrentSite();
    assertEquals("intranet", site.getName());
    assertEquals(SiteType.PORTAL, site.getType());
  }

  @Test
  public void testShouldReturnCurrentSite() {
    UserPortalConfigService userPortalConfig = mock(UserPortalConfigService.class);
    when(userPortalConfig.getDefaultPortal()).thenReturn("intranet");

    PortalRequestContext requestContext = mock(PortalRequestContext.class);

    UTIL.when(() -> Util.getPortalRequestContext()).thenReturn(requestContext);

    COMMONS_UTILS.when(() -> CommonsUtils.getService(UserPortalConfigService.class)).thenReturn(userPortalConfig);
    COMMONS_UTILS.when(() -> CommonsUtils.getCurrentSite()).thenCallRealMethod();

    when(requestContext.getSiteKey()).thenReturn(SiteKey.portal("test_site"));
    SiteKey site = CommonsUtils.getCurrentSite();
    assertEquals("test_site", site.getName());
    assertEquals(SiteType.PORTAL, site.getType());

    when(requestContext.getSiteKey()).thenReturn(SiteKey.group("group_site"));
    site = CommonsUtils.getCurrentSite();
    assertEquals("group_site", site.getName());
    assertEquals(SiteType.GROUP, site.getType());

    when(requestContext.getSiteKey()).thenReturn(SiteKey.user("user_site"));
    site = CommonsUtils.getCurrentSite();
    assertEquals("user_site", site.getName());
    assertEquals(SiteType.USER, site.getType());
  }

  public void testShouldReturnDefaultPortalOwner() {
    UserPortalConfigService userPortalConfig = mock(UserPortalConfigService.class);
    when(userPortalConfig.getDefaultPortal()).thenReturn("intranet");

    COMMONS_UTILS.when(() -> CommonsUtils.getService(UserPortalConfigService.class)).thenReturn(userPortalConfig);
    COMMONS_UTILS.when(() -> CommonsUtils.getCurrentSite()).thenCallRealMethod();

    String portalOwner = CommonsUtils.getCurrentPortalOwner();
    assertEquals("intranet", portalOwner);
  }

  public void testShouldReturnCurrentPortalOwner() {
    UserPortalConfigService userPortalConfig = mock(UserPortalConfigService.class);
    when(userPortalConfig.getDefaultPortal()).thenReturn("intranet");

    PortalRequestContext requestContext = mock(PortalRequestContext.class);

    UTIL.when(() -> Util.getPortalRequestContext()).thenReturn(requestContext);

    COMMONS_UTILS.when(() -> CommonsUtils.getService(UserPortalConfigService.class)).thenReturn(userPortalConfig);
    COMMONS_UTILS.when(() -> CommonsUtils.getCurrentSite()).thenCallRealMethod();

    when(requestContext.getPortalOwner()).thenReturn("current_portal_owner");
    String portalOwner = CommonsUtils.getCurrentPortalOwner();
    assertEquals("current_portal_owner", portalOwner);
  }
}
