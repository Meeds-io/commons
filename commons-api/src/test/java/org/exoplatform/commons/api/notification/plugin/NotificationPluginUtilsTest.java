package org.exoplatform.commons.api.notification.plugin;

import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.branding.BrandingService;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserProfileHandler;
import org.exoplatform.services.organization.impl.UserProfileImpl;

/**
 * Created by eXo Platform SAS.
 *
 * @author Ali Hamdi <ahamdi@exoplatform.com>
 * @since  15/02/18 10:23
 */

@ConfiguredBy({
    @ConfigurationUnit(scope = ContainerScope.ROOT, path = "conf/configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/configuration.xml"),
})
public class NotificationPluginUtilsTest extends AbstractKernelTest {

  public void testGetLanguage() throws Exception {
    OrganizationService organizationService = getContainer().getComponentInstanceOfType(OrganizationService.class);
    String userName = "john";
    String langFR = "fr";
    String langFR_FR = "fr_FR";
    UserProfileHandler profileHandler = organizationService.getUserProfileHandler();
    UserProfile profile = new UserProfileImpl(userName);
    profile.setAttribute("user.language", langFR);
    begin();
    try {
      profileHandler.saveUserProfile(profile, true);
    } finally {
      end();
    }

    String language = NotificationPluginUtils.getLanguage(userName);
    assertEquals(langFR, language);

    profile.setAttribute("user.language", langFR_FR);
    begin();
    try {
      profileHandler.saveUserProfile(profile, true);
    } finally {
      end();
    }

    language = NotificationPluginUtils.getLanguage(userName);
    assertEquals(langFR, language);
  }

  public void testGetBrandingPortalName() throws Exception {
    assertEquals(ExoContainerContext.getService(BrandingService.class).getCompanyName(),
                 NotificationPluginUtils.getBrandingPortalName());
  }
}
