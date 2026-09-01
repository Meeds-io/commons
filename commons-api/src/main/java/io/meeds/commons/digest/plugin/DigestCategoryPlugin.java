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
package io.meeds.commons.digest.plugin;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.container.xml.ValuesParam;

/**
 * The way an addon declares the digest category it owns, in its own kernel
 * configuration and without a single line of Java:
 *
 * <pre>
 * &lt;external-component-plugins&gt;
 *   &lt;target-component&gt;io.meeds.commons.digest.DigestCategoryRegistry&lt;/target-component&gt;
 *   &lt;component-plugin&gt;
 *     &lt;name&gt;news&lt;/name&gt;
 *     &lt;set-method&gt;addCategoryProvider&lt;/set-method&gt;
 *     &lt;type&gt;io.meeds.commons.digest.plugin.DigestCategoryPlugin&lt;/type&gt;
 *     &lt;init-params&gt;
 *       &lt;value-param&gt;&lt;name&gt;id&lt;/name&gt;&lt;value&gt;news&lt;/value&gt;&lt;/value-param&gt;
 *       &lt;value-param&gt;&lt;name&gt;labelKey&lt;/name&gt;&lt;value&gt;digest.category.news&lt;/value&gt;&lt;/value-param&gt;
 *       &lt;value-param&gt;&lt;name&gt;order&lt;/name&gt;&lt;value&gt;30&lt;/value&gt;&lt;/value-param&gt;
 *       &lt;values-param&gt;
 *         &lt;name&gt;pluginIds&lt;/name&gt;
 *         &lt;value&gt;PostNewsNotificationPlugin&lt;/value&gt;
 *       &lt;/values-param&gt;
 *     &lt;/init-params&gt;
 *   &lt;/component-plugin&gt;
 * &lt;/external-component-plugins&gt;
 * </pre>
 *
 * The kernel container is shared by every webapp, so a category declared this
 * way reaches the digest wherever the addon lives, and whether or not that
 * addon uses Spring at all.
 */
public class DigestCategoryPlugin extends BaseComponentPlugin implements DigestCategoryProvider {

  private static final String ID_PARAM         = "id";

  private static final String LABEL_KEY_PARAM  = "labelKey";

  private static final String ORDER_PARAM      = "order";

  private static final String PLUGIN_IDS_PARAM = "pluginIds";

  private final String        id;

  private final String        labelKey;

  private final int           order;

  private final List<String>  pluginIds;

  public DigestCategoryPlugin(InitParams params) {
    this.id = getValue(params, ID_PARAM);
    if (StringUtils.isBlank(id)) {
      throw new IllegalArgumentException("A digest category must declare its id");
    }
    String labelKeyValue = getValue(params, LABEL_KEY_PARAM);
    this.labelKey = StringUtils.isBlank(labelKeyValue) ? "digest.category." + id : labelKeyValue;
    String orderValue = getValue(params, ORDER_PARAM);
    this.order = StringUtils.isBlank(orderValue) ? 0 : Integer.parseInt(orderValue);
    ValuesParam valuesParam = params == null ? null : params.getValuesParam(PLUGIN_IDS_PARAM);
    this.pluginIds = valuesParam == null
        || valuesParam.getValues() == null ? Collections.emptyList() : List.copyOf(valuesParam.getValues());
    if (pluginIds.isEmpty()) {
      throw new IllegalArgumentException("The digest category " + id + " must declare the notification plugins it covers");
    }
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getLabelKey() {
    return labelKey;
  }

  @Override
  public int getOrder() {
    return order;
  }

  @Override
  public List<String> getPluginIds() {
    return pluginIds;
  }

  private String getValue(InitParams params, String name) {
    ValueParam valueParam = params == null ? null : params.getValueParam(name);
    return valueParam == null ? null : valueParam.getValue();
  }

}
