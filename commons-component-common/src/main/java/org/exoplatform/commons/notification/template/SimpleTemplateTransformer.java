/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
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
    ResourceBundle resourceBundle = null;
    //
    for (String key : context.keySet()) {
      Object value = context.get(key);
      if (value == null) {
        value = "";
      } else if (!(value instanceof String) && !value.getClass().isPrimitive()) {
        continue;
      }
      newKey = key.indexOf("$") == 0 ? key : "$" + key;
      if (value.toString().startsWith("$UIShareDocuments")) {
        String localized = value.toString().replace("$", "");
        if (resourceBundle == null) {
          resourceBundle = resourceBundleService.getResourceBundle("locale.extension.SocialIntegration", locale);
        }
        got = got.replace(newKey, resourceBundle.getString(localized));
      } else {
        got = got.replace(newKey, value.toString());
      }
    }
    return got;
  }

}
