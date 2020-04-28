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
package org.exoplatform.commons.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;


import org.exoplatform.services.resources.LocaleConfig;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.services.resources.impl.LocaleConfigImpl;


/**
 * Created by eXo Platform SAS.
 *
 * @author Ali Hamdi <ahamdi@exoplatform.com>
 * @since 16/02/18 17:44
 */

public class MockLocaleConfigService implements LocaleConfigService {

  @Override
  public LocaleConfig getDefaultLocaleConfig() {
    return new LocaleConfigImpl();
  }

  @Override
  public LocaleConfig getLocaleConfig(String lang) {
    return new LocaleConfigImpl();
  }

  @Override
  public Collection<LocaleConfig> getLocalConfigs() {
    Collection<LocaleConfig> localConfigs = new ArrayList<>();
    Locale [] locales = {Locale.FRENCH,Locale.GERMAN, Locale.ENGLISH,Locale.FRANCE};
    for(Locale locale : locales) {
      LocaleConfig config = new LocaleConfigImpl();
      config.setLocale(locale);
      localConfigs.add(config);
    }
    return localConfigs;
  }
}
