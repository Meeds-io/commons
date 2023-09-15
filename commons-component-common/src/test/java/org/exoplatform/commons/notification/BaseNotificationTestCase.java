package org.exoplatform.commons.notification;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.service.storage.WebNotificationStorage;
import org.exoplatform.commons.notification.plugin.PluginTest;
import org.exoplatform.commons.testing.BaseCommonsTestCase;
import org.exoplatform.component.test.*;
import org.exoplatform.container.ExoContainerContext;

@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.ROOT, path = "conf/configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.commons.component.core-local-configuration.xml"),
})
public abstract class BaseNotificationTestCase extends BaseCommonsTestCase {

  protected static final String    NOTIFICATIONS = "notifications";

  protected List<String>           userIds       = new ArrayList<>();

  protected WebNotificationStorage storage;

  @Override
  protected void beforeClass() {
    super.beforeClass();
    ExoContainerContext.setCurrentContainer(getContainer());
    storage = getContainer().getComponentInstanceOfType(WebNotificationStorage.class);
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    userIds = new ArrayList<String>();
  }

  protected NotificationInfo makeWebNotificationInfo(String userId) {
    NotificationInfo info = NotificationInfo.instance();
    info.key(new PluginKey(PluginTest.ID));
    info.setTitle("The title");
    info.setFrom("mary");
    info.setTo(userId);
    info.setRead(false);
    info.setOnPopOver(true);
    info.with("activityId", "TheActivityId")
        .with("accessLink", "http://fsdfsdf.com/fsdfsf");
    return info;
  }

}
