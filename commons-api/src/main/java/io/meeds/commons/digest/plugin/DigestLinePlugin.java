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

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValuesParam;

/**
 * The base of the digest line providers the addons declare in their own kernel
 * configuration, next to their category:
 *
 * <pre>
 * &lt;external-component-plugins&gt;
 *   &lt;target-component&gt;io.meeds.commons.digest.DigestCategoryRegistry&lt;/target-component&gt;
 *   &lt;component-plugin&gt;
 *     &lt;name&gt;news.lines&lt;/name&gt;
 *     &lt;set-method&gt;addLineProvider&lt;/set-method&gt;
 *     &lt;type&gt;io.meeds.content.news.digest.NewsDigestLinePlugin&lt;/type&gt;
 *     &lt;init-params&gt;
 *       &lt;values-param&gt;
 *         &lt;name&gt;pluginIds&lt;/name&gt;
 *         &lt;value&gt;PostNewsNotificationPlugin&lt;/value&gt;
 *       &lt;/values-param&gt;
 *     &lt;/init-params&gt;
 *   &lt;/component-plugin&gt;
 * &lt;/external-component-plugins&gt;
 * </pre>
 *
 * A kernel plugin on purpose, like the category: the kernel container is
 * shared by every webapp, so the line provider reaches the digest whether or
 * not the addon uses Spring. The subclass looks its services up in the
 * container when it needs them.
 */
public abstract class DigestLinePlugin extends BaseComponentPlugin implements DigestLineProvider {

  private static final String PLUGIN_IDS_PARAM = "pluginIds";

  private final List<String>  pluginIds;

  protected DigestLinePlugin(InitParams params) {
    ValuesParam valuesParam = params == null ? null : params.getValuesParam(PLUGIN_IDS_PARAM);
    this.pluginIds = valuesParam == null
        || valuesParam.getValues() == null ? Collections.emptyList() : List.copyOf(valuesParam.getValues());
    if (pluginIds.isEmpty()) {
      throw new IllegalArgumentException("A digest line provider must declare the notification plugins it covers");
    }
  }

  @Override
  public List<String> getPluginIds() {
    return pluginIds;
  }

}
