package org.exoplatform.mfa.api;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;


public class MfaSystemComponentPlugin implements ComponentPlugin {

  private String name;
  private String description;

  private MfaSystemService mfaSystemService;

  private static final Log LOG = ExoLogger.getLogger(MfaSystemComponentPlugin.class);


  public MfaSystemComponentPlugin(InitParams initParams) {
    ValueParam serviceValueParam = initParams.getValueParam("service");
    if (serviceValueParam!=null) {
      String service = serviceValueParam.getValue();
      try {
        Object o = CommonsUtils.getService(Class.forName(service));
        if (o instanceof MfaSystemService) {
          this.mfaSystemService=(MfaSystemService) o;
        } else {
          LOG.error("Class {} is not instance of MfaSystemService");
        }
      } catch (ClassNotFoundException e) {
        LOG.error("Unable to find class {}",service,e);
      }
    }

  }

  public MfaSystemService getMfaSystemService() {
    return this.mfaSystemService;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name=name;
  }

  @Override
  public String getDescription() {
    return this.description;
  }

  @Override
  public void setDescription(String description) {
    this.description=description;
  }
}
