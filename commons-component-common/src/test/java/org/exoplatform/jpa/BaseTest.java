package org.exoplatform.jpa;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import org.exoplatform.commons.testing.BaseExoTestCase;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com
 */
@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.ROOT, path = "conf/configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/configuration.xml"),
})
public abstract class BaseTest extends BaseExoTestCase {
  protected void setUp() throws Exception {
    begin();
  }

  protected void tearDown() throws Exception {
    end();
  }

  @BeforeClass
  @Override
  protected void beforeRunBare() {
    if(System.getProperty("gatein.test.output.path") == null) {
      System.setProperty("gatein.test.output.path", System.getProperty("java.io.tmpdir"));
    }
    super.beforeRunBare();
  }

  @AfterClass
  @Override
  protected void afterRunBare() {
    super.afterRunBare();
  }

  public <T> T getService(Class<T> clazz) {
    return (T) getContainer().getComponentInstanceOfType(clazz);
  }
}
