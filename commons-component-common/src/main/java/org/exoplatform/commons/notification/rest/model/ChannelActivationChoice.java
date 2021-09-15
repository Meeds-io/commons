package org.exoplatform.commons.notification.rest.model;

public class ChannelActivationChoice {

  private String  channelId;

  private String  pluginId;

  private boolean active;

  private boolean channelActive;

  public ChannelActivationChoice(String channelId, String pluginId, boolean active, boolean channelActive) {
    this.channelId = channelId;
    this.pluginId = pluginId;
    this.active = active;
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

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public boolean isChannelActive() {
    return channelActive;
  }

  public void setChannelActive(boolean channelActive) {
    this.channelActive = channelActive;
  }

}
