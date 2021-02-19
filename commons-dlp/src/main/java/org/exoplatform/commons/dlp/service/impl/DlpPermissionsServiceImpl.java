package org.exoplatform.commons.dlp.service.impl;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.dlp.service.DlpPermissionsService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.*;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.navigation.*;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.page.PageService;
import org.exoplatform.portal.mop.page.PageState;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.IdentityRegistry;

import javax.xml.ws.handler.MessageContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class DlpPermissionsServiceImpl implements DlpPermissionsService {

  private static final String DLP_QUARANTINE = "dlp-quarantine";

  private static final Log LOG = ExoLogger.getLogger(DlpPermissionsServiceImpl.class);

  private DataStorage dataStorage;

  private NavigationService navigationService;

  private PageService pageService;

  public DlpPermissionsServiceImpl(PageService pageService,
                                   NavigationService navigationService,
                                   DataStorage dataStorage) {
    this.pageService = pageService;
    this.navigationService = navigationService;
    this.dataStorage = dataStorage;
  }

  @Override
  public void addDlpPermissionsPagesAndNavigations(String permissions) throws Exception {
    //Load quarantine page and Application portlet
    List<String> permissionsList = Arrays.asList(permissions.split(","));
    for (String permission : permissionsList) {
      if(permission.equals("/platform/administrators")) continue;
      Page dlpPage = new Page();
      dlpPage.setOwnerType(PortalConfig.GROUP_TYPE);
      dlpPage.setOwnerId(permission);
      dlpPage.setName(DLP_QUARANTINE);
      String administratorsDlpQuarantinePageKey = PortalConfig.GROUP_TYPE + "::" + "/platform/administrators::" + DLP_QUARANTINE;
      PageContext administratorsDlpQuarantinePageContext = pageService.loadPage(PageKey.parse(administratorsDlpQuarantinePageKey));
      PageState administratorsDlpQuarantinePageState = administratorsDlpQuarantinePageContext.getState();
      List<String> accessPermissions = new ArrayList<>();
      accessPermissions.add("member:" + permission);
      PageState
          pageState =
          new PageState(administratorsDlpQuarantinePageState.getDisplayName(),
                        administratorsDlpQuarantinePageState.getDescription(),
                        administratorsDlpQuarantinePageState.getShowMaxWindow(),
                        administratorsDlpQuarantinePageState.getFactoryId(),
                        accessPermissions,
                        administratorsDlpQuarantinePageState.getEditPermission(),
                        administratorsDlpQuarantinePageState.getMoveAppsPermissions(),
                        administratorsDlpQuarantinePageState.getMoveContainersPermissions());
      PageContext dlpPageContext = new PageContext(dlpPage.getPageKey(), pageState);
      Application quarantineApp = new Application(ApplicationType.PORTLET);
      quarantineApp.setState(new TransientApplicationState("social-portlet/dlpQuarantine"));
      String[] dlpPagePermission = { "member:" + permission };
      quarantineApp.setAccessPermissions(dlpPagePermission);
      ArrayList<ModelObject> dlpPageChildren = new ArrayList<ModelObject>();
      dlpPageChildren.add(quarantineApp);
      dlpPage.setChildren(dlpPageChildren);
      pageService.savePage(dlpPageContext);
      dataStorage.save(dlpPage);

      //Load quarantine navigation and set permissions
      NavigationContext nav = navigationService.loadNavigation(SiteKey.group(permission));
      if (nav == null) {
        navigationService.saveNavigation(new NavigationContext(new SiteKey(SiteType.GROUP, permission), new NavigationState(1)));
        nav = navigationService.loadNavigation(SiteKey.group(permission));
        NodeContext rootContext = navigationService.loadNode(NodeModel.SELF_MODEL, nav, Scope.ALL, null);
        navigationService.saveNode(rootContext, null);
      }
      NodeContext node = navigationService.loadNode(NodeModel.SELF_MODEL, nav, Scope.ALL, null);
      NavigationState navigationState = new NavigationState(nav.getState().getPriority() + 1);
      NavigationContext navigationContext = new NavigationContext(SiteKey.group(permission), navigationState);
      NodeContext dlpNodeContext = node.getNode(DLP_QUARANTINE) != null ? node.get(DLP_QUARANTINE) : node.add(node.getIndex(), DLP_QUARANTINE) ;
      dlpNodeContext.setState(dlpNodeContext.getState().builder().pageRef(dlpPage.getPageKey()).build());
      navigationService.saveNavigation(navigationContext);
      navigationService.saveNode(dlpNodeContext, null);
    }
  }

  @Override
  public void removeDlpPermissionsPagesAndNavigations(String oldPermissions) {
    List<String> permissionsList = Arrays.asList(oldPermissions.split(","));
    for (String permission : permissionsList){
      if(permission.equals("/platform/administrators")) continue;
      NavigationContext nav = navigationService.loadNavigation(SiteKey.group(permission));
      if (nav != null) {
        NodeContext dlpPermissionNodeContext = navigationService.loadNode(NodeModel.SELF_MODEL, nav, Scope.ALL, null);
        if(dlpPermissionNodeContext.get(DLP_QUARANTINE) != null){
          if(dlpPermissionNodeContext.getNodeSize() == 1) {
            navigationService.destroyNavigation(nav);
          } else {
            dlpPermissionNodeContext.removeNode(DLP_QUARANTINE);
          }
          String dlpPageKey = PortalConfig.GROUP_TYPE + "::" + permission + "::" + DLP_QUARANTINE;
          pageService.destroyPage(PageKey.parse(dlpPageKey));
        }
      }
    }
  }

  @Override
  public String getDlpQuarantinePageUrl(String username) {
    IdentityRegistry identityRegistry = CommonsUtils.getService(IdentityRegistry.class);
    OrganizationService organizationService = CommonsUtils.getService(OrganizationService.class);
    SettingService settingService = CommonsUtils.getService(SettingService.class);
    UserACL userACL = CommonsUtils.getService(UserACL.class);
    if (StringUtils.isBlank(username)) {
      return new String();
    }
    if(userACL.isSuperUser() || userACL.isUserInGroup(userACL.getAdminGroups())){
      return "/g/:platform:administrators/" + DLP_QUARANTINE;
    }
    SettingValue<?> settingValue = settingService.get(org.exoplatform.commons.api.settings.data.Context.GLOBAL,
                           org.exoplatform.commons.api.settings.data.Scope.APPLICATION.id("DlpPermissions"),
                           "exo:dlpPermissions");
    if (settingValue == null || settingValue.getValue().toString().isEmpty()) return new String();
    List<String> permissionsList = Arrays.asList(settingValue.getValue().toString().split(","));
    for (String permissionExpression : permissionsList) {
      org.exoplatform.services.security.Identity identity = identityRegistry.getIdentity(username);
      if (identity != null && identity.isMemberOf(permissionExpression)) {
        return "/g/" + permissionExpression.replaceAll("/", ":") + "/" + DLP_QUARANTINE;
      }
      try {
        Collection<Membership> memberships = organizationService.getMembershipHandler()
                                                                .findMembershipsByUserAndGroup(username, permissionExpression);
        if (memberships != null && !memberships.isEmpty()) {
          return "/g/" + permissionExpression.replaceAll("/", ":") + "/" + DLP_QUARANTINE;
        }
      } catch (Exception e) {
        throw new IllegalStateException("Error getting memberships of user " + username, e);
      }
    }
    return new String();
  }
}
