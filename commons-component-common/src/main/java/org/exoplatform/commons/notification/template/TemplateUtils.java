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
package org.exoplatform.commons.notification.template;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;

import org.exoplatform.commons.api.notification.plugin.config.PluginConfig;
import org.exoplatform.commons.api.notification.plugin.config.TemplateConfig;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.api.notification.template.Element;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.resources.ResourceBundleService;

import lombok.SneakyThrows;

public class TemplateUtils {

  private static final Log LOG                = ExoLogger.getLogger(TemplateUtils.class);

  private static final int MAX_SUBJECT_LENGTH = 50;

  /**
   * Process the Groovy template associate with Template context to generate It
   * will be use for digest mail
   * 
   * @param ctx
   * @return
   */
  public static String processGroovy(TemplateContext ctx) {
    return ExoContainerContext.getService(TemplateContentTransformerService.class).processGroovy(ctx);
  }

  /**
   * Render for Subject template
   * 
   * @param ctx
   * @return
   */
  public static String processSubject(TemplateContext ctx) {
    return ExoContainerContext.getService(TemplateContentTransformerService.class).processSubject(ctx);
  }

  /**
   * Render for digest template
   * 
   * @param ctx
   * @return
   */
  public static String processDigest(TemplateContext ctx) {
    return ExoContainerContext.getService(TemplateContentTransformerService.class).processDigest(ctx);
  }

  /**
   * Loads the Groovy template file
   * 
   * @param templatePath
   * @return Groovy Template File content
   */
  public static String loadGroovyTemplate(String templatePath) {
    try (InputStream inputStream = getTemplateInputStream(templatePath)) {
      return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    } catch (Exception e) {
      if (StringUtils.startsWith(templatePath, TemplateConfig.DEFAULT_SRC_RESOURCE_TEMPLATE_KEY)) {
        LOG.info("Failed to read default template file: {}. An empty message will be used", templatePath);
      } else {
        LOG.warn("Failed to read template file: {}. An empty message will be used", templatePath, e);
      }
      return "";
    }
  }

  /**
   * Load the Groovy template element.
   * 
   * @param pluginId The plugin's id
   * @param language The language's id.
   * @return The Groovy element
   */
  public static Element loadGroovyElement(String pluginId, String language) {
    PluginConfig templateConfig = getPluginConfig(pluginId);
    return new GroovyElement().language(language).config(templateConfig);
  }

  /**
   * Get the excerpt subject of notification mail from origin string - Just
   * contains plain text - Limit number of characters
   * 
   * @param subject the origin string
   * @return the excerpt of subject
   * @since 4.1.x
   */
  public static String getExcerptSubject(String subject) {
    String newSubject = StringEscapeUtils.unescapeHtml4(cleanHtmlTags(subject));
    if (newSubject != null && newSubject.length() > MAX_SUBJECT_LENGTH) {
      newSubject = newSubject.substring(0, MAX_SUBJECT_LENGTH);
      int lastSpace = newSubject.lastIndexOf(" ");
      return ((lastSpace > 0) ? newSubject.substring(0, lastSpace) : newSubject) + "...";
    }

    return newSubject;
  }

  /**
   * Clean all HTML tags on string
   * 
   * @param str the origin string
   * @return The string has not contain HTML tags.
   * @since 4.1.x
   */
  public static String cleanHtmlTags(String str) {
    //
    if (StringUtils.isBlank(str)) {
      return "";
    } else {
      return Jsoup.parse(str).text();
    }
  }

  /**
   * Gets Plugin configuration for specified PluginId
   * 
   * @param pluginId
   * @return
   */
  public static PluginConfig getPluginConfig(String pluginId) {
    PluginConfig pluginConfig = NotificationContextImpl.cloneInstance().getPluginSettingService().getPluginConfig(pluginId);

    if (pluginConfig == null) {
      throw new IllegalStateException("PluginConfig is NULL with plugId = " + pluginId);
    }

    return pluginConfig;
  }

  /**
   * Gets Resource Bundle value
   * 
   * @param key
   * @param locale
   * @param resourcePath
   * @return
   */
  public static String getResourceBundle(String key, Locale locale, String resourcePath) {
    if (key == null || key.trim().length() == 0 || resourcePath == null || resourcePath.isEmpty()) {
      return "";
    }
    if (locale == null || locale.getLanguage().isEmpty()) {
      locale = ResourceBundleService.DEFAULT_CROWDIN_LOCALE;
    }
    ResourceBundleService bundleService = CommonsUtils.getService(ResourceBundleService.class);
    ResourceBundle res = bundleService.getResourceBundle(resourcePath, locale);
    if (res != null && res.containsKey(key)) {
      return res.getString(key);
    } else {
      LOG.warn("Resource Bundle key not found. " + key + " in source path: " + resourcePath);
      return key;
    }
  }

  /**
   * Gets InputStream for groovy template
   * 
   * @param templatePath
   * @return {@link InputStream} corresponding to relative or absolute template path
   */
  @SneakyThrows
  public static InputStream getTemplateInputStream(String templatePath) {
    ConfigurationManager configurationManager = CommonsUtils.getService(ConfigurationManager.class);
    String uri = templatePath;
    if (!templatePath.startsWith("war:")
        && !templatePath.startsWith("jar:")
        && !templatePath.startsWith("classpath:")) {
      if (templatePath.startsWith("/")) {
        templatePath = templatePath.substring(1);
      }
      uri = "war:/" + templatePath;
      if (configurationManager.getURL(uri) == null) {
        uri = "jar:/" + templatePath;
        if (configurationManager.getURL(uri) == null) {
          uri = "classpath:/" + templatePath;
        }
      }
    }
    return configurationManager.getInputStream(uri);
  }

}
