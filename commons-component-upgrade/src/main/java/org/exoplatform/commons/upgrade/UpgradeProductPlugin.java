package org.exoplatform.commons.upgrade;

import javax.jcr.AccessDeniedException;
import javax.jcr.InvalidItemStateException;
import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.RepositoryException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.info.ProductInformations;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public abstract class UpgradeProductPlugin extends BaseComponentPlugin {

  public static final String  UPGRADE_COMPLETED_STATUS       = "Completed";

  private static final Log LOG = ExoLogger.getLogger(UpgradeProductPlugin.class);
  private static final String PRODUCT_GROUP_ID = "product.group.id";
  private static final String OLD_PRODUCT_GROUP_ID = "old.product.group.id";
  private static final String UPGRADE_PLUGIN_EXECUTION_ORDER = "plugin.execution.order";
  private static final String UPGRADE_PLUGIN_ENABLE = "commons.upgrade.{$0}.enable";

  private SettingService      settingService;

  private int pluginExecutionOrder;

  /**
   * The plugin's product maven group identifier, by example: org.exoplatform.portal for gatein.
   */
  protected String productGroupId;
  
  protected String oldProductGroupId;


  public UpgradeProductPlugin(SettingService settingService, InitParams initParams) {
    this(initParams);
    this.settingService = settingService;
  }

  public UpgradeProductPlugin(InitParams initParams) {
    if (!initParams.containsKey(PRODUCT_GROUP_ID)) {
      if(LOG.isErrorEnabled()){
        LOG.error("Couldn't find the init value param: " + PRODUCT_GROUP_ID);
      }
      return;
    }
    
    //
    productGroupId = initParams.getValueParam(PRODUCT_GROUP_ID).getValue();
    
    //
    ValueParam vp = initParams.getValueParam(OLD_PRODUCT_GROUP_ID);
    oldProductGroupId = vp != null ? vp.getValue() : productGroupId;
    
    if (!initParams.containsKey(UPGRADE_PLUGIN_EXECUTION_ORDER)) {
      pluginExecutionOrder = 0;
    }else{
      pluginExecutionOrder = Integer.parseInt(initParams.getValueParam(UPGRADE_PLUGIN_EXECUTION_ORDER).getValue());
    }
  }

  public String getProductGroupId() {
    return productGroupId;
  }
  

  /**
   * Gets old version what needs to upgrade
   * @return
   */
  public String getOldProductGroupId() {
    return oldProductGroupId;
  }

  /**
   * Determines if the plugin is enabled, this method will be called when adding the plugin to the upgradePlugins list.
   * See {@link UpgradeProductService#addUpgradePlugin(UpgradeProductPlugin)}
   * 
   * @return
   *          true: if the plugin is enabled: should be added to the upgradePlugins list
   *          false: if the plugin is disabled: should not be added to the upgradePlugins list
   */
  public boolean isEnabled(){
    String isEnabledProperty = PropertyManager.getProperty(UPGRADE_PLUGIN_ENABLE.replace("{$0}", getName()));
    if(isEnabledProperty == null || isEnabledProperty.equals("true")){
      return true;
    }else {
      return false;
    }
  }

  /**
   * 
   * Add node's version with a label. The node's session have to still open, and this method don't handle
   * the node's session closure.
   * 
   * @param nodeToAddVersion
   *          the node that will be versioned
   * @param versionLabel
   *          the label to apply to the new created version
   * 
   * @throws NoSuchNodeTypeException
   * @throws VersionException
   * @throws ConstraintViolationException
   * @throws LockException
   * @throws AccessDeniedException
   * @throws ItemExistsException
   * @throws InvalidItemStateException
   * @throws ReferentialIntegrityException
   * @throws RepositoryException
   */
  public void addNodeVersion(Node nodeToAddVersion, String versionLabel) throws NoSuchNodeTypeException, VersionException,
      ConstraintViolationException, LockException, AccessDeniedException, ItemExistsException, InvalidItemStateException,
      ReferentialIntegrityException, RepositoryException {
    if (!nodeToAddVersion.isNodeType(ProductInformations.MIX_VERSIONABLE)) {
      nodeToAddVersion.addMixin(ProductInformations.MIX_VERSIONABLE);
      nodeToAddVersion.save();
      nodeToAddVersion.getSession().save();
      nodeToAddVersion.getSession().refresh(true);
    }
    if (nodeToAddVersion.isCheckedOut()) {
      Version version = nodeToAddVersion.checkin();
      nodeToAddVersion.getVersionHistory().addVersionLabel(version.getName(), versionLabel, false);
    }
    nodeToAddVersion.checkout();
  }

  /**
   * Proceed to the transparent upgrade, this method will be called if the
   * Product version has changed, which means, it will be called once the
   * Product Version stored in the JCR and the one declared in ProductInfo are
   * different
   * 
   * @param oldVersion the old version that is stored in the JCR
   * @param newVersion the new version read from ProductInfo Service
   */
  public abstract void processUpgrade(String oldVersion, String newVersion);

  /**
   * This method is called when a new version has been detected to decide whether proceed to upgrade or not.
   * It should take care that some versions could be skipped while upgrading, i.e: the upgrade could happen 
   * when the product is switched from version 1.0 to 1.3.
   * 
   * @param previousVersion
   *          The previous version of plugin's product
   * @param newVersion
   *          The previous version of plugin's product
   * @return
   *          true: if the plugin should be executed when switching product from previousVersion to newVersion
   *          true: if the upgrade isn't necessary
   */
  public abstract boolean shouldProceedToUpgrade(String newVersion, String previousVersion);

  /**
   * {@inheritDoc}
   */
  public final boolean equals(Object obj) {
    if (obj != null && obj instanceof UpgradeProductPlugin) {
      return this.getName().equals(((UpgradeProductPlugin) obj).getName());
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public final int hashCode() {
    return this.getName().hashCode();
  }

  public int getPluginExecutionOrder() {
    return pluginExecutionOrder;
  }

  public String getValue(String paramName) {
    if (settingService == null) {
      throw new IllegalStateException("SettingService Service is not set");
    }
    try {
      Scope appId = Scope.APPLICATION.id(getName());
      SettingValue<?> paramValue = settingService.get(Context.GLOBAL, appId, paramName);
      if (paramValue != null && paramValue.getValue() != null) {
        return paramValue.getValue().toString();
      }
      return null;
    } finally {
      Scope.APPLICATION.id(null);
    }
  }

  public void storeValueForPlugin(String paramName, String paramValue) {
    if (settingService == null) {
      throw new IllegalStateException("SettingService Service is not set");
    }
    try {
      settingService.set(Context.GLOBAL, Scope.APPLICATION.id(getName()), paramName, SettingValue.create(paramValue));
    } finally {
      Scope.APPLICATION.id(null);
    }
  }

}
