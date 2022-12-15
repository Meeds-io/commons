package org.exoplatform.commons.api.notification.rest;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;

import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.channel.ChannelManager;
import org.exoplatform.commons.api.notification.model.*;
import org.exoplatform.commons.api.notification.model.UserSetting.FREQUENCY;
import org.exoplatform.commons.api.notification.plugin.config.PluginConfig;
import org.exoplatform.commons.api.notification.rest.model.*;
import org.exoplatform.commons.api.notification.service.setting.PluginSettingService;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.portal.application.localization.LocalizationFilter;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.resources.ResourceBundleService;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.exoplatform.services.rest.http.PATCH;

@Path("notifications/settings")
@Tag(name = "notifications/settings", description = "Managing users notifications settings")
public class NotificationSettingsRestService implements ResourceContainer {

  private static final String NOTIFICATION_LABEL_CHANNEL_DEFAULT = "UINotification.label.channel.default";

  private static final String   MAIN_RESOURCE_BUNDLE_NAME = "locale.portlet.notification.UserNotificationPortlet";

  private static final Log      LOG                       = ExoLogger.getLogger(NotificationSettingsRestService.class);

  private static final String   DAILY                     = "Daily";

  private static final String   WEEKLY                    = "Weekly";

  private static final String   NEVER                     = "Never";

  private ResourceBundleService resourceBundleService;

  private PluginSettingService  pluginSettingService;

  private ChannelManager        channelManager;

  private UserSettingService    userSettingService;

  private UserACL               userACL;

  public NotificationSettingsRestService(ResourceBundleService resourceBundleService,
                                         PluginSettingService pluginSettingService,
                                         ChannelManager channelManager,
                                         UserSettingService userSettingService,
                                         UserACL userACL) {
    this.resourceBundleService = resourceBundleService;
    this.pluginSettingService = pluginSettingService;
    this.channelManager = channelManager;
    this.userSettingService = userSettingService;
    this.userACL = userACL;
  }

  @GET
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(
      summary = "Gets all notification settings of a user",
      description = "Gets all notification settings of a user",
      method = "GET")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "500", description = "Internal server error")
      }
  )
  public Response getSettings(
                              @Parameter(
                                  description = "User name that will be used to retrieve its settings. "
                                      + "If current user is and administrator, it will be able to retrieve settings of all users",
                                  required = true
                              ) @PathParam("id") String username) {
    boolean isAdmin = userACL.isSuperUser() || userACL.isUserInGroup(userACL.getAdminGroups());
    boolean isSameUser = ConversationState.getCurrent().getIdentity().getUserId().equals(username);
    if (!isAdmin && !isSameUser) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    Locale userLocale = LocalizationFilter.getCurrentLocale();
    if (userLocale == null) {
      userLocale = Locale.ENGLISH;
    }

    try {
      String[] sharedResourceBundles = resourceBundleService.getSharedResourceBundleNames();
      String[] resourceBundles = ArrayUtils.insert(0, sharedResourceBundles, MAIN_RESOURCE_BUNDLE_NAME);
      ResourceBundle resourceBundle = resourceBundleService.getResourceBundle(resourceBundles,
                                                                              userLocale);
      Context context = new Context(resourceBundle, userLocale);

      //
      UserSetting setting = userSettingService.get(username);

      List<String> channels = getChannels();
      Map<String, Boolean> channelStatus = computeChannelStatuses(setting, channels);
      List<GroupProvider> groups = pluginSettingService.getGroupPlugins();
      Map<String, String> groupsLabels = groups.stream()
                                               .collect(Collectors.toMap(GroupProvider::getGroupId,
                                                                         group -> context.pluginRes(group.getResourceBundleKey(),
                                                                                                    group.getGroupId())));
      Map<String, String> pluginLabels = groups.stream()
                                               .flatMap(group -> group.getPluginInfos().stream())
                                               .collect(Collectors.toMap(PluginInfo::getType,
                                                                         plugin -> context.pluginRes("UINotification.title."
                                                                             + plugin.getType(), plugin.getType())));
      Map<String, String> channelLabels = channels.stream()
                                                  .collect(Collectors.toMap(Function.identity(),
                                                                            channel -> {
                                                                              String channelKey = context.getChannelKey(channel);
                                                                              String key = "UINotification.label.channel-"
                                                                                  + channelKey;
                                                                              if (resourceBundle != null
                                                                                  && resourceBundle.containsKey(key)) {
                                                                                return resourceBundle.getString(key);
                                                                              } else if (resourceBundle != null
                                                                                  && resourceBundle.containsKey(NOTIFICATION_LABEL_CHANNEL_DEFAULT)) {
                                                                                return resourceBundle.getString(NOTIFICATION_LABEL_CHANNEL_DEFAULT)
                                                                                                     .replace("{0}", channelKey);
                                                                              }
                                                                              return channelKey;
                                                                            }));
      Map<String, String> channelDescriptions = channels.stream()
                                                        .collect(Collectors.toMap(Function.identity(),
                                                                                  channel -> {
                                                                                    String channelKey =
                                                                                                      context.getChannelKey(channel);
                                                                                    String key =
                                                                                               "UINotification.description.channel-"
                                                                                                   + channelKey;
                                                                                    if (resourceBundle != null
                                                                                        && resourceBundle.containsKey(key)) {
                                                                                      return resourceBundle.getString(key);
                                                                                    } else if (resourceBundle != null
                                                                                        && resourceBundle.containsKey("UINotification.description.channel.default")) {
                                                                                      return resourceBundle.getString("UINotification.description.channel.default")
                                                                                                           .replace("{0}",
                                                                                                                    channelKey);
                                                                                    }
                                                                                    return StringUtils.EMPTY;
                                                                                  }));

      Map<String, String> digestLabels = buildDigestLabels(context);
      Map<String, String> digestDescriptions = buildDigestDescriptions(context);
      List<EmailDigestChoice> emailDigestChoices = new ArrayList<>();
      List<ChannelActivationChoice> channelCheckBoxList = new ArrayList<>();
      boolean hasActivePlugin = computeChoices(setting,
                                               channels,
                                               groups,
                                               channelStatus,
                                               emailDigestChoices,
                                               channelCheckBoxList);

      UserNotificationSettings notificationSettings = new UserNotificationSettings(groups,
                                                                                   groupsLabels,
                                                                                   pluginLabels,
                                                                                   channelLabels,
                                                                                   channelDescriptions,
                                                                                   digestLabels,
                                                                                   digestDescriptions,
                                                                                   hasActivePlugin,
                                                                                   emailDigestChoices,
                                                                                   channelCheckBoxList,
                                                                                   channelStatus,
                                                                                   channels);

      return Response.ok(notificationSettings).build();
    } catch (Exception e) {
      LOG.warn("Error while retrieving notification settings", e);
      return Response.serverError().build();
    }
  }

  @PATCH
  @Path("{id}/plugin/{pluginId}")
  @RolesAllowed("users")
  @Operation(
      summary = "Change enablement status of Channel for a user",
      description = "Change enablement status of Channel for a user",
      method = "PATCH")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "500", description = "Internal server error")
      }
  )
  public Response saveSetting(
                              @Parameter(
                                  description = "User name that will be used to save its settings.",
                                  required = true
                              ) @PathParam("id") String username,
                              @Parameter(
                                  description = "Notification plugin Id",
                                  required = true
                              ) @PathParam("pluginId") String pluginId,
                              @Parameter(
                                  description = "Notification digest to use for corresponding plugin Id",
                                  required = true
                              ) @FormParam("channels") String channels,
                              @Parameter(
                                  description = "Notification digest to use for corresponding plugin Id",
                                  required = true
                              ) @FormParam("digest") String digest) {

    boolean isAdmin = userACL.isSuperUser() || userACL.isUserInGroup(userACL.getAdminGroups());
    boolean isSameUser = ConversationState.getCurrent().getIdentity().getUserId().equals(username);
    if (!isAdmin && !isSameUser) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    try {
      UserSetting setting = userSettingService.get(username);

      // digest
      if (WEEKLY.equals(digest)) {
        setting.addPlugin(pluginId, FREQUENCY.WEEKLY);
      } else if (DAILY.equals(digest)) {
        setting.addPlugin(pluginId, FREQUENCY.DAILY);
      } else {
        setting.removePlugin(pluginId, FREQUENCY.WEEKLY);
        setting.removePlugin(pluginId, FREQUENCY.DAILY);
      }

      // channels
      String[] channelsArray = StringUtils.split(channels, ',');
      Map<String, String> channelsStatus = Arrays.stream(channelsArray)
                                                 .collect(Collectors.toMap(channelStatus -> StringUtils.split(channelStatus,
                                                                                                              "=")[0],
                                                                           channelStatus -> StringUtils.split(channelStatus,
                                                                                                              "=")[1]));
      for (Map.Entry<String, String> channelByStatus : channelsStatus.entrySet()) {
        if (Boolean.parseBoolean(channelByStatus.getValue())) {
          setting.addChannelPlugin(channelByStatus.getKey(), pluginId);
        } else {
          setting.removeChannelPlugin(channelByStatus.getKey(), pluginId);
        }
      }
      userSettingService.save(setting);
    } catch (Exception e) {
      return Response.serverError().entity("Exception in switching state of plugin " + pluginId + ". " + e.toString()).build();
    }
    return Response.noContent().build();
  }

  @PATCH
  @Path("{id}/channel/{channelId}")
  @RolesAllowed("users")
  @Operation(
      summary = "Change enablement status of Channel for a user",
      description = "Change enablement status of Channel for a user",
      method = "PATCH")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "500", description = "Internal server error")
      }
  )
  public Response saveActiveStatus(
                                   @Parameter(
                                       description = "User name that will be used to save its settings.",
                                       required = true
                                   ) @PathParam("id") String username,
                                   @Parameter(
                                       description = "Channel Id like MAIL_CHANNEL, WEB_CHANNEL...",
                                       required = true
                                   ) @PathParam("channelId") String channelId,
                                   @Parameter(
                                       description = "Enable/disable a channel",
                                       required = true
                                   ) @FormParam("enable") boolean enable) {

    boolean isAdmin = userACL.isSuperUser() || userACL.isUserInGroup(userACL.getAdminGroups());
    boolean isSameUser = ConversationState.getCurrent().getIdentity().getUserId().equals(username);
    if (!isAdmin && !isSameUser) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    try {
      UserSetting setting = userSettingService.get(username);
      if (enable) {
        setting.setChannelActive(channelId);
      } else {
        setting.removeChannelActive(channelId);
      }
      userSettingService.save(setting);
    } catch (Exception e) {
      return Response.serverError().entity("Exception in switching state of provider " + channelId + ". " + e.toString()).build();
    }
    return Response.noContent().build();
  }

  private Map<String, String> buildDigestDescriptions(Context context) {
    Map<String, String> options = new HashMap<>();
    options.put(DAILY, context.appRes("UINotification.description.Daily"));
    options.put(WEEKLY, context.appRes("UINotification.description.Weekly"));
    return options;
  }

  private Map<String, String> buildDigestLabels(Context context) {
    Map<String, String> options = new HashMap<>();
    options.put(DAILY, context.appRes("UINotification.label.Daily"));
    options.put(WEEKLY, context.appRes("UINotification.label.Weekly"));
    options.put(NEVER, context.appRes("UINotification.label.Never"));
    return options;
  }

  private List<String> getChannels() {
    List<String> channels = new ArrayList<>();
    for (AbstractChannel channel : channelManager.getChannels()) {
      channels.add(channel.getId());
    }
    return channels;
  }

  private Map<String, Boolean> computeChannelStatuses(UserSetting setting, List<String> channels) {
    Map<String, Boolean> channelStatus = new HashMap<>();
    for (String channelId : channels) {
      channelStatus.put(channelId, setting != null && setting.isChannelGloballyActive(channelId));
    }
    return channelStatus;
  }

  private boolean computeChoices(UserSetting userSetting, // NOSONAR
                                 List<String> channels,
                                 List<GroupProvider> groups,
                                 Map<String, Boolean> channelStatus,
                                 List<EmailDigestChoice> emailDigestChoices,
                                 List<ChannelActivationChoice> channelCheckBoxList) {
    boolean hasActivePlugin = false;
    for (GroupProvider groupProvider : groups) {
      for (PluginInfo pluginInfo : groupProvider.getPluginInfos()) {
        String pluginId = pluginInfo.getType();
        for (String channelId : channels) {
          hasActivePlugin = true;
          boolean isChannelActive = channelStatus.get(channelId)
              && pluginSettingService.isActive(channelId, pluginId)
              && userSetting.isChannelGloballyActive(channelId);
          channelCheckBoxList.add(new ChannelActivationChoice(channelId,
                                                              pluginId,
                                                              pluginSettingService.isAllowed(channelId, pluginId),
                                                              isChannelActive && userSetting.isActive(channelId, pluginId),
                                                              isChannelActive));
          if (UserSetting.EMAIL_CHANNEL.equals(channelId)) {
            emailDigestChoices.add(new EmailDigestChoice(channelId,
                                                         pluginId,
                                                         getValue(userSetting, pluginId),
                                                         isChannelActive));
          }
        }
      }
    }
    return hasActivePlugin;
  }

  private String getValue(UserSetting setting, String pluginId) {
    if (setting != null && setting.isInWeekly(pluginId)) {
      return WEEKLY;
    } else if (setting != null && setting.isInDaily(pluginId)) {
      return DAILY;
    } else {
      return NEVER;
    }
  }

  public class Context {
    ResourceBundle resourceBundle;

    Locale         userLocale;

    public Context(ResourceBundle resourceBundle, Locale userLocale) {
      this.resourceBundle = resourceBundle;
      this.userLocale = userLocale;
    }

    public String appRes(String key) {
      try {
        return resourceBundle.getString(key).replace("'", "&#39;").replace("\"", "&#34;");
      } catch (java.util.MissingResourceException e) {
        if (key.indexOf("checkbox-") > -1) {
          return appRes("UINotification.label.checkbox.default").replace("{0}", capitalizeFirstLetter(key.split("-")[1]));
        }
        if (key.indexOf("channel-") > -1) {
          return appRes(NOTIFICATION_LABEL_CHANNEL_DEFAULT).replace("{0}", capitalizeFirstLetter(key.split("-")[1]));
        }
      } catch (Exception e) {
        LOG.debug("Error when get resource bundle key " + key, e);
      }
      return capitalizeFirstLetter(key.substring(key.lastIndexOf('.') + 1));
    }

    private String getBundlePath(String id) {
      PluginConfig pluginConfig = pluginSettingService.getPluginConfig(id);
      if (pluginConfig != null) {
        return pluginConfig.getBundlePath();
      }
      //
      if (GroupProvider.defaultGroupIds.contains(id)) {
        return pluginSettingService.getPluginConfig("DigestDailyPlugin").getBundlePath();
      }
      //
      List<GroupProvider> groups = pluginSettingService.getGroupPlugins();
      for (GroupProvider groupProvider : groups) {
        if (groupProvider.getGroupId().equals(id) && groupProvider.getPluginInfos() != null
            && !groupProvider.getPluginInfos().isEmpty()) {
          return groupProvider.getPluginInfos().get(0).getBundlePath();
        }
      }

      return "";
    }

    public String pluginRes(String key, String id) {
      String path = getBundlePath(id);
      ResourceBundle pluginResourceBundle = StringUtils.isBlank(path) ? null
                                                                      : resourceBundleService.getResourceBundle(path, userLocale);
      return pluginResourceBundle != null && pluginResourceBundle.containsKey(key) ? pluginResourceBundle.getString(key) : id;
    }

    public String getChannelKey(String channelId) {
      return channelId.replace("_CHANNEL", "").toLowerCase();
    }

    public String capitalizeFirstLetter(String original) {
      return original.length() <= 1 ? original : original.substring(0, 1).toUpperCase() + original.substring(1);
    }
  }

}
