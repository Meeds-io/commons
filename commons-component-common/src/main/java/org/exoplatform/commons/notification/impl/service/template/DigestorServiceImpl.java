/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Affero General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.notification.impl.service.template;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.model.ChannelKey;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.plugin.NotificationPluginUtils;
import org.exoplatform.commons.api.notification.service.template.DigestorService;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.notification.NotificationContextFactory;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.notification.channel.MailChannel;
import org.exoplatform.commons.notification.impl.DigestDailyPlugin;
import org.exoplatform.commons.notification.impl.DigestWeeklyPlugin;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.notification.job.NotificationJob;
import org.exoplatform.commons.notification.template.TemplateUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.utils.TimeConvertUtils;

public class DigestorServiceImpl implements DigestorService {
  private static final Log LOG = ExoLogger.getLogger(DigestorServiceImpl.class);
  private static final Pattern LI_PATTERN = Pattern.compile("<li([^>]+)>(.+?)</li>");

  public DigestorServiceImpl() {}
  
  
  public MessageInfo buildMessage(NotificationContext jobContext, Map<PluginKey, List<NotificationInfo>> notificationData, UserSetting userSetting) {
    MessageInfo messageInfo = null;

    if (notificationData == null || notificationData.size() == 0) {
      return messageInfo;
    }

    long startTime = System.currentTimeMillis();
    try {
      messageInfo = new MessageInfo();
      List<String> activePlugins = jobContext.getPluginSettingService().getActivePluginIds(MailChannel.ID);
      NotificationContext nCtx = NotificationContextImpl.cloneInstance();

      Writer writer = new StringWriter();
      AbstractChannel channel = nCtx.getChannelManager().getChannel(ChannelKey.key(MailChannel.ID));
      
      for (String pluginId : activePlugins) {
        List<NotificationInfo> messages = notificationData.get(PluginKey.key(pluginId));
        if (messages == null || messages.size() == 0){
          continue;
        }
        nCtx.setNotificationInfos(messages);
        channel.getTemplateBuilder(PluginKey.key(pluginId)).buildDigest(nCtx, writer);
      }

      StringBuffer sb = ((StringWriter) writer).getBuffer();
      if (sb.length() == 0) {
        return null;
      }

      String digestMessageList = sb.toString();
      int totalDigestMsg = 0;
      Matcher matcher = LI_PATTERN.matcher(digestMessageList);
      String li_attribute = null;
      while (matcher.find()) {
        totalDigestMsg += 1;
        li_attribute = matcher.group(1);
      }

      if (totalDigestMsg == 1) {
        int beginIndex = li_attribute.indexOf("margin");
        int endIndex = li_attribute.indexOf(";", beginIndex) + 1;
        String replacedStr = li_attribute.substring(beginIndex, endIndex);
        digestMessageList = digestMessageList
            .replace(replacedStr, "margin: 0; padding: 15px 20px;");
      }

      DigestInfo digestInfo = new DigestInfo(jobContext, userSetting);

      TemplateContext ctx = TemplateContext.newChannelInstance(channel.getKey(), digestInfo.getPluginId(), digestInfo.getLocale().getLanguage());
      ctx.put("FIRSTNAME", digestInfo.getFirstName());
      ctx.put("PORTAL_NAME", digestInfo.getPortalName());
      ctx.put("PORTAL_HOME", digestInfo.getPortalHome());
      ctx.put("PERIOD", digestInfo.getPeriodType());
      ctx.put("FROM_TO", digestInfo.getFromTo());
      String subject = TemplateUtils.processSubject(ctx);
      
      ctx.put("FOOTER_LINK", digestInfo.getFooterLink());
      ctx.put("DIGEST_MESSAGES_LIST", digestMessageList);
      ctx.put("HAS_ONE_MESSAGE", (totalDigestMsg == 1));

      String body = TemplateUtils.processGroovy(ctx);

      messageInfo.from(NotificationPluginUtils.getFrom(null)).subject(subject)
                 .body(body).to(digestInfo.getSendTo());
    } catch (Exception e) {
      LOG.error("Can not build template of DigestorProviderImpl ", e);
      return null;
    }
    
    LOG.debug("End build template of DigestorProviderImpl ... " + (System.currentTimeMillis() - startTime) + " ms");
    
    final boolean stats = NotificationContextFactory.getInstance().getStatistics().isStatisticsEnabled();
    if (stats) {
      NotificationContextFactory.getInstance().getStatisticsCollector().createDigestCount(messageInfo.getPluginId());
    }
    
    return messageInfo;
  }
  
  private class DigestInfo {
    private String  firstName;

    private String  portalName;
    
    private String  portalHome;

    private String  sendTo;

    private String  footerLink;

    private String  fromTo     = "Today";

    private String  periodType = fromTo;

    private String  pluginId   = DigestDailyPlugin.ID;

    private Locale  locale;

    private Boolean isWeekly;

    public DigestInfo(NotificationContext context, UserSetting userSetting) {
      firstName = NotificationPluginUtils.getFirstName(userSetting.getUserId());
      sendTo = NotificationPluginUtils.getTo(userSetting.getUserId());
      portalName = NotificationPluginUtils.getBrandingPortalName();
      String language = NotificationPluginUtils.getLanguage(userSetting.getUserId());
      portalHome = NotificationUtils.getPortalHome(portalName);
      footerLink = NotificationUtils.getProfileUrl(userSetting.getUserId());
      locale = NotificationUtils.getLocale(language);
      
      this.isWeekly = context.value(NotificationJob.JOB_WEEKLY);
      //
      if(isWeekly && userSetting.getWeeklyPlugins().size() > 0) {
        pluginId = DigestWeeklyPlugin.ID;
        periodType = "Weekly";
        //
        Calendar periodFrom = userSetting.getLastUpdateTime();
        long t = System.currentTimeMillis() - 604800000;
        if(t > periodFrom.getTimeInMillis()) {
          periodFrom.setTimeInMillis(t);
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append(TimeConvertUtils.getFormatDate(periodFrom.getTime(), "mmmm dd", locale))
              .append(" - ")
              .append(TimeConvertUtils.getFormatDate(Calendar.getInstance().getTime(), "mmmm dd, yyyy", locale));
        fromTo = buffer.toString();
      }
    }

    public String getFromTo() {
      return fromTo;
    }

    public String getPeriodType() {
      return periodType;
    }

    public String getPluginId() {
      return pluginId;
    }

    public Locale getLocale() {
      return locale;
    }

    public String getFirstName() {
      return firstName;
    }

    public String getPortalName() {
      return portalName;
    }

    public String getPortalHome() {
      return portalHome;
    }

    public String getSendTo() {
      return sendTo;
    }

    public String getFooterLink() {
      return footerLink;
    }
    
    public Boolean isWeekly() {
      return this.isWeekly;
    }
  }
  
}
