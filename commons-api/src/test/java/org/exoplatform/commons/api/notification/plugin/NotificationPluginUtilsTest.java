package org.exoplatform.commons.api.notification.plugin;

import static org.exoplatform.commons.api.notification.plugin.NotificationPluginUtils.BRANDING_COMPANY_NAME_SETTING_KEY;

import org.mockito.Mockito;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.component.test.*;
import org.exoplatform.services.organization.*;
import org.exoplatform.services.organization.impl.UserProfileImpl;

/**
 * Created by eXo Platform SAS.
 *
 * @author Ali Hamdi <ahamdi@exoplatform.com>
 * @since 15/02/18 10:23
 */

@ConfiguredBy(
  {
      @ConfigurationUnit(scope = ContainerScope.ROOT, path = "conf/configuration.xml"),
      @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/configuration.xml"),
  }
)
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
    assertEquals(langFR_FR, language);
  }

  public void testGetBrandingPortalName() throws Exception {
    SettingService settingService = Mockito.mock(SettingService.class);
    getContainer().unregisterComponent(SettingService.class);
    getContainer().registerComponentInstance(SettingService.class, settingService);

    String companyName = "ACME";
    SettingValue value = new SettingValue(companyName);
    Mockito.when(settingService.get(Context.GLOBAL, Scope.GLOBAL, BRANDING_COMPANY_NAME_SETTING_KEY)).thenReturn(value);

    // Make sure that method getBrandingPortalName returns the changed company
    // name.
    assertEquals(companyName, NotificationPluginUtils.getBrandingPortalName());
  }
}
