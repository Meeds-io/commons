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
package org.exoplatform.commons.notification.template;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.notification.NotificationUtils;


/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Aug 8, 2013  
 */
public class GroovyElement extends SimpleElement {

  public String appRes(String key) {
    return TemplateUtils.getResourceBundle(key, NotificationUtils.getLocale(getLanguage()), getPluginConfig().getBundlePath());
  }

  public String appRes(String key, String... strs) {
    
    String value = appRes(key);
    if (strs != null && strs.length > 0) {
      for (int i = 0; i < strs.length; ++i) {
        value = StringUtils.replace(value, "{" + i + "}", strs[i]);
      }
    }
    return value;
  }

  public String escapeHTML(String str) {
    return StringEscapeUtils.escapeHtml(str);
  }
}