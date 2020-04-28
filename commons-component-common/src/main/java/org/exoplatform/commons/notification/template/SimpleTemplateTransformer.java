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

import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.api.notification.template.TemplateTransformer;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.resources.ResourceBundleService;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Aug 1, 2013  
 */
public class SimpleTemplateTransformer implements TemplateTransformer {
  
  private String template;

  @Override
  public TemplateTransformer from(String template) {
    this.template = template;
    return this;
  }

  @Override
  public String transform(TemplateContext context) {
    String got = template;
    String newKey = "";
    Locale locale = new Locale(context.getLanguage());
    ResourceBundleService resourceBundleService = CommonsUtils.getService(ResourceBundleService.class);
    ResourceBundle resourceBundle = resourceBundleService.getResourceBundle("locale.extension.SocialIntegration",
        locale);
    //
    for (String key : context.keySet()) {
      newKey = key.indexOf("$") == 0 ? key : "$" + key;
      if (context.get(key).toString().startsWith("$UIShareDocuments")) {
        String localized = context.get(key).toString().replace("$", "");
        got = got.replace(newKey, resourceBundle.getString(localized));
      } else {
        got = got.replace(newKey, (String) context.get(key));
      }
    }
    return got;
  }

}
