package org.exoplatform.commons.api.notification.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class WebNotificationFilter implements Serializable {

  private static final long    serialVersionUID = 4731392723219635179L;

  @Getter
  @Setter
  private List<PluginKey>      pluginKeys;

  @Getter
  private String               userId;

  @Getter
  @Setter
  private int                  limitDay;

  @Getter
  @Setter
  private boolean              onPopover;

  @Getter
  @Setter
  private Boolean              isRead;

  @Getter
  private Pair<String, String> parameter;

  public WebNotificationFilter(String userId) {
    this.userId = userId;
  }

  public WebNotificationFilter(String userId, boolean onPopover) {
    this.userId = userId;
    this.onPopover = onPopover;
  }

  public WebNotificationFilter(String userId, List<PluginKey> pluginKeys, boolean onPopover) {
    this.userId = userId;
    this.onPopover = onPopover;
    this.pluginKeys = pluginKeys;
  }

  public WebNotificationFilter setParameter(String paramName, String paramValue) {
    this.parameter = new ImmutablePair<>(paramName, paramValue);
    return this;
  }

  public void setPluginKey(PluginKey pluginKey) {
    this.pluginKeys = pluginKey == null ? Collections.emptyList() : Collections.singletonList(pluginKey);
  }

}
