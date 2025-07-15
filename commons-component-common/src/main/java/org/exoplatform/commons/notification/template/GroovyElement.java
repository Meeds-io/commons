/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.commons.notification.template;

import java.io.Writer;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.notification.NotificationUtils;

import groovy.lang.Writable;
import groovy.text.GStringTemplateEngine;
import groovy.text.Template;

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
    return StringEscapeUtils.escapeHtml4(str);
  }

  public final void include(String templatePath, TemplateContext ctx) throws Exception {
    String template = TemplateUtils.loadGroovyTemplate(templatePath);
    Template engine = new GStringTemplateEngine().createTemplate(template);
    Writer writer = (Writer) ctx.get("_writer");

    Writable writable = engine.make(ctx);
    writable.writeTo(writer);
  }

}