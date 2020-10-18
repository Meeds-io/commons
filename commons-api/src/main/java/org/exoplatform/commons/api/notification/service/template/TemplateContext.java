/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.commons.api.notification.service.template;

import java.util.HashMap;
import java.util.Locale;

import org.exoplatform.commons.api.notification.model.ChannelKey;
import org.exoplatform.commons.api.notification.model.UserSetting;

public class TemplateContext extends HashMap<String, Object> {
  
  private static final long serialVersionUID = 1L;

  private String            pluginId;

  private ChannelKey        channelKey;

  private String            language;

  private int               digestSize       = 0;
  
  private Throwable         error = null;

  public TemplateContext() {
    
  }

  public TemplateContext(String pluginId, String language) {
    this.channelKey = ChannelKey.key(UserSetting.EMAIL_CHANNEL);
    this.pluginId = pluginId;
    this.language = language;
  }

  public static TemplateContext newChannelInstance(ChannelKey channelKey, String pluginId, String language) {
    TemplateContext context = new TemplateContext(pluginId, language);
    context.channelKey = channelKey;
    return context;
  }
  
  /**
   * Holds the exception if have any on transform processing.
   * @param exception
   */
  public void setException(Throwable exception) {
    this.error = exception;
  }
  
  public Throwable getException() {
    return this.error;
  }

  public TemplateContext digestType(int digestSize) {
    this.digestSize = digestSize;
    return this;
  }

  public TemplateContext channelKey(ChannelKey channelKey) {
    this.channelKey = channelKey;
    return this;
  }

  public TemplateContext pluginId(String pluginId) {
    this.pluginId = pluginId;
    return this;
  }

  public TemplateContext language(String language) {
    this.language = language;
    return this;
  }

  /**
   * @return the TemplateContext
   */
  public TemplateContext end() {
    return this;
  }

  /**
   * @return the pluginId
   */
  public String getPluginId() {
    return pluginId;
  }

  /**
   * @return the channelId
   */
  public ChannelKey getChannelKey() {
    return channelKey;
  }

  /**
   * @return the language
   */
  public String getLanguage() {
    if (this.language == null) {
      return Locale.ENGLISH.getLanguage();
    }
    
    return language;
  }

  /**
   * @return the digestSize
   */
  public int getDigestSize() {
    return this.digestSize;
  }
    
}