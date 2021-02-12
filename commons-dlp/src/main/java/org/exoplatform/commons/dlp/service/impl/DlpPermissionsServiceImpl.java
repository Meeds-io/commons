package org.exoplatform.commons.dlp.service.impl;

import org.exoplatform.commons.dlp.service.DlpPermissionsService;
import org.exoplatform.portal.config.DataStorage;
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

import java.util.ArrayList;
import java.util.Arrays;
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
}
