/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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
package io.meeds.commons.digest.service;

import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import org.exoplatform.commons.api.notification.plugin.config.PluginConfig;
import org.exoplatform.commons.api.notification.service.setting.PluginSettingService;
import org.exoplatform.services.resources.ResourceBundleService;

import io.meeds.commons.digest.plugin.DigestCategoryProvider;

/**
 * Finds the wording of a digest label in the language of the recipient. The
 * category labels and the line wordings belong to the addons, so they are
 * looked up in the notification bundle the addon declared for its plugin; the
 * words of the email itself live in the commons bundle.
 */
@Component
public class DigestLabelResolver {

  public static final String          COMMONS_BUNDLE = "locale.notification.template.CommonsNotification";

  private final PluginSettingService  pluginSettingService;

  private final ResourceBundleService resourceBundleService;

  public DigestLabelResolver(PluginSettingService pluginSettingService, ResourceBundleService resourceBundleService) {
    this.pluginSettingService = pluginSettingService;
    this.resourceBundleService = resourceBundleService;
  }

  /**
   * @param labelKey the key of the wording
   * @param pluginId the notification type whose bundle holds it
   * @param locale the recipient language
   * @return the wording, the commons one when the addon bundle doesn't have
   *         it, the key itself when nobody has it
   */
  public String resolve(String labelKey, String pluginId, Locale locale) {
    Locale userLocale = locale == null ? ResourceBundleService.DEFAULT_CROWDIN_LOCALE : locale;
    String label = lookup(bundlePathOf(pluginId), labelKey, userLocale);
    if (label == null) {
      label = lookup(COMMONS_BUNDLE, labelKey, userLocale);
    }
    return label == null ? labelKey : label;
  }

  /**
   * @param key a key of the commons notification bundle
   * @param locale the recipient language
   * @return the wording, the key itself when missing
   */
  public String commons(String key, Locale locale) {
    String label = lookup(COMMONS_BUNDLE, key, locale == null ? ResourceBundleService.DEFAULT_CROWDIN_LOCALE : locale);
    return label == null ? key : label;
  }

  public String categoryLabel(DigestCategoryProvider category, Locale locale) {
    Locale userLocale = locale == null ? ResourceBundleService.DEFAULT_CROWDIN_LOCALE : locale;
    for (String pluginId : category.getPluginIds()) {
      String label = lookup(bundlePathOf(pluginId), category.getLabelKey(), userLocale);
      if (label != null) {
        return label;
      }
    }
    return category.getId();
  }

  private String bundlePathOf(String pluginId) {
    PluginConfig pluginConfig = pluginId == null ? null : pluginSettingService.getPluginConfig(pluginId);
    return pluginConfig == null ? null : pluginConfig.getBundlePath();
  }

  private String lookup(String bundlePath, String key, Locale locale) {
    if (StringUtils.isBlank(bundlePath) || StringUtils.isBlank(key)) {
      return null;
    }
    ResourceBundle bundle = resourceBundleService.getResourceBundle(bundlePath, locale);
    return bundle != null && bundle.containsKey(key) ? bundle.getString(key) : null;
  }

}
