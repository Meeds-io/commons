package org.exoplatform.commons.api.notification.rest.model;

import java.util.List;
import java.util.Map;

import org.exoplatform.commons.api.notification.model.GroupProvider;
import org.exoplatform.commons.api.notification.model.UserSetting;

public class UserNotificationSettings {
  private List<GroupProvider>           groups;

  private Map<String, String>           groupsLabels;

  private Map<String, String>           pluginLabels;

  private Map<String, String>           channelLabels;

  private Map<String, String>           channelDescriptions;

  private Map<String, String>           digestLabels;

  private Map<String, String>           digestDescriptions;

  private boolean                       hasActivePlugin     = false;

  private List<EmailDigestChoice>       emailDigestChoices  = null;

  private List<ChannelActivationChoice> channelCheckBoxList = null;

  private Map<String, Boolean>          channelStatus;

  private List<String>                  channels;

  private String                        emailChannel        = UserSetting.EMAIL_CHANNEL;

  public UserNotificationSettings(List<GroupProvider> groups,
                                  Map<String, String> groupsLabels,
                                  Map<String, String> pluginLabels,
                                  Map<String, String> channelLabels,
                                  Map<String, String> channelDescriptions,
                                  Map<String, String> digestLabels,
                                  Map<String, String> digestDescriptions,
                                  boolean hasActivePlugin,
                                  List<EmailDigestChoice> emailDigestChoices,
                                  List<ChannelActivationChoice> channelCheckBoxList,
                                  Map<String, Boolean> channelStatus,
                                  List<String> channels) {
    this.groups = groups;
    this.groupsLabels = groupsLabels;
    this.pluginLabels = pluginLabels;
    this.channelLabels = channelLabels;
    this.channelDescriptions = channelDescriptions;
    this.digestLabels = digestLabels;
    this.digestDescriptions = digestDescriptions;
    this.hasActivePlugin = hasActivePlugin;
    this.emailDigestChoices = emailDigestChoices;
    this.channelCheckBoxList = channelCheckBoxList;
    this.channelStatus = channelStatus;
    this.channels = channels;
  }

  public List<GroupProvider> getGroups() {
    return groups;
  }

  public void setGroups(List<GroupProvider> groups) {
    this.groups = groups;
  }

  public Map<String, String> getChannelLabels() {
    return channelLabels;
  }

  public void setChannelLabels(Map<String, String> channelLabels) {
    this.channelLabels = channelLabels;
  }

  public Map<String, String> getChannelDescriptions() {
    return channelDescriptions;
  }

  public void setChannelDescriptions(Map<String, String> channelDescriptions) {
    this.channelDescriptions = channelDescriptions;
  }

  public Map<String, String> getDigestLabels() {
    return digestLabels;
  }

  public void setDigestLabels(Map<String, String> digestLabels) {
    this.digestLabels = digestLabels;
  }

  public Map<String, String> getDigestDescriptions() {
    return digestDescriptions;
  }

  public void setDigestDescriptions(Map<String, String> digestDescriptions) {
    this.digestDescriptions = digestDescriptions;
  }

  public boolean isHasActivePlugin() {
    return hasActivePlugin;
  }

  public void setHasActivePlugin(boolean hasActivePlugin) {
    this.hasActivePlugin = hasActivePlugin;
  }

  public List<EmailDigestChoice> getEmailDigestChoices() {
    return emailDigestChoices;
  }

  public void setEmailDigestChoices(List<EmailDigestChoice> emailDigestChoices) {
    this.emailDigestChoices = emailDigestChoices;
  }

  public Map<String, String> getGroupsLabels() {
    return groupsLabels;
  }

  public void setGroupsLabels(Map<String, String> groupsLabels) {
    this.groupsLabels = groupsLabels;
  }

  public Map<String, String> getPluginLabels() {
    return pluginLabels;
  }

  public void setPluginLabels(Map<String, String> pluginLabels) {
    this.pluginLabels = pluginLabels;
  }

  public List<ChannelActivationChoice> getChannelCheckBoxList() {
    return channelCheckBoxList;
  }

  public void setChannelCheckBoxList(List<ChannelActivationChoice> channelCheckBoxList) {
    this.channelCheckBoxList = channelCheckBoxList;
  }

  public Map<String, Boolean> getChannelStatus() {
    return channelStatus;
  }

  public void setChannelStatus(Map<String, Boolean> channelStatus) {
    this.channelStatus = channelStatus;
  }

  public List<String> getChannels() {
    return channels;
  }

  public void setChannels(List<String> channels) {
    this.channels = channels;
  }

  public String getEmailChannel() {
    return emailChannel;
  }

  public void setEmailChannel(String emailChannel) {
    this.emailChannel = emailChannel;
  }

}
