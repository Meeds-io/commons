package org.exoplatform.commons.dlp.connector;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;

/**
 * Is extended by all DlpService connectors, and allows to build configuration needed by a list of connectors that is used for Dlp.
 * 
 */
public abstract class DlpServiceConnector extends BaseComponentPlugin {
  private String type; //type name
  private String displayName; //for use when rendering
  private boolean enable = true;
  protected static final String DLP_POSITIVE_DETECTION = "dlpPositiveDetection";
  
  /**
   * Initializes a dlp service connector. The constructor is default that connectors must implement.
   * @param initParams The parameters which are used for initializing the dlp service connector from configuration.
   * @LevelAPI Experimental
   */
  public DlpServiceConnector(InitParams initParams) {
    PropertiesParam param = initParams.getPropertiesParam("constructor.params");
    this.type = param.getProperty("type");
    this.displayName = param.getProperty("displayName");
    if (StringUtils.isNotBlank(param.getProperty("enable"))) this.setEnable(Boolean.parseBoolean(param.getProperty("enable")));
  }
  
  /**
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * @param type the type to set
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * @return the displayName
   */
  public String getDisplayName() {
    return displayName;
  }

  
  /**
   * @param displayName the displayName to set
   */
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   * @return the enable
   */
  public boolean isEnable() {
    return enable;
  }

  /**
   * @param enable the enable to set
   */
  public void setEnable(boolean enable) {
    this.enable = enable;
  }

  public abstract boolean processItem(String entityId);
}
