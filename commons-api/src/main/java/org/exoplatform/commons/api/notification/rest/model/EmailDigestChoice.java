package org.exoplatform.commons.api.notification.rest.model;

public class EmailDigestChoice {
  String  channelId;

  String  pluginId;

  String  value;

  boolean channelActive;

  public EmailDigestChoice(String channelId,
                           String pluginId,
                           String value,
                           boolean channelActive) {
    this.channelId = channelId;
    this.pluginId = pluginId;
    this.value = value;
    this.channelActive = channelActive;
  }

  public String getChannelId() {
    return channelId;
  }

  public void setChannelId(String channelId) {
    this.channelId = channelId;
  }

  public String getPluginId() {
    return pluginId;
  }

  public void setPluginId(String pluginId) {
    this.pluginId = pluginId;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public boolean isChannelActive() {
    return channelActive;
  }

  public void setChannelActive(boolean channelActive) {
    this.channelActive = channelActive;
  }

}
