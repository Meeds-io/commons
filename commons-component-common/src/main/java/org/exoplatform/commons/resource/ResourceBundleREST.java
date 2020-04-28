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
package org.exoplatform.commons.resource;

import java.util.*;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;

import com.google.javascript.rhino.jstype.Property;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.resources.*;
import org.exoplatform.services.rest.resource.ResourceContainer;

/**
 * This REST Service deserves resource bundles in JSON format
 */
@Path("/i18n/bundle")
public class ResourceBundleREST implements ResourceContainer {

  private static final Log          LOG                         = ExoLogger.getLogger(ResourceBundleREST.class);

  private ResourceBundleService     resourceBundleService;

  private LocaleConfigService       localeConfigService;

  private static final CacheControl CACHE_CONTROL               = new CacheControl();

  private static final Date         DEFAULT_LAST_MODIFED        = new Date();

  // half a day
  private static final int          CACHE_IN_SECONDS            = 43200;

  private static final int          CACHE_IN_MILLI_SECONDS      = CACHE_IN_SECONDS * 1000;

  public ResourceBundleREST(ResourceBundleService resourceBundleService, LocaleConfigService localeConfigService) {
    this.resourceBundleService = resourceBundleService;
    this.localeConfigService = localeConfigService;

    CACHE_CONTROL.setMaxAge(CACHE_IN_SECONDS);
  }

  @GET
  @Path("{name}-{lang}.json")
  @Produces(MediaType.APPLICATION_JSON)
  @SuppressWarnings("unchecked")
  public Response getBundleContent(@PathParam("name") String resourceBundleName, @PathParam("lang") String lang) {
    if (StringUtils.isBlank(resourceBundleName) || StringUtils.isBlank(lang)) {
      return Response.status(400).build();
    }
    LocaleConfig localeConfig = localeConfigService.getLocaleConfig(lang);
    if (localeConfig == null) {
      LOG.warn("Locale '{}' is not supported", lang);
      return Response.status(400).build();
    }
    ResourceBundle resourceBundle = resourceBundleService.getResourceBundle(resourceBundleName, localeConfig.getLocale());
    ResourceBundle defaultResourceBundle = resourceBundleService.getResourceBundle(resourceBundleName,
                                                                                   localeConfigService.getDefaultLocaleConfig()
                                                                                                      .getLocale());
    if (resourceBundle == null) {
      if (defaultResourceBundle == null) {
        LOG.warn("resourceBundleName '{}' wasn't found", resourceBundleName);
        return Response.status(404).build();
      } else {
        resourceBundle = defaultResourceBundle;
      }
    }

    JSONObject resultJSON = new JSONObject();

    Enumeration<String> keys = resourceBundle.getKeys();
    while (keys.hasMoreElements()) {
      String key = (String) keys.nextElement();
      resultJSON.put(key, resourceBundle.getString(key));
    }
    keys = defaultResourceBundle.getKeys();
    while (keys.hasMoreElements()) {
      String key = (String) keys.nextElement();
      resultJSON.putIfAbsent(key, resourceBundle.getString(key));
    }

    ResponseBuilder builder = Response.ok(resultJSON.toJSONString(), MediaType.APPLICATION_JSON);
    if (!PropertyManager.isDevelopping()) {
      builder.cacheControl(CACHE_CONTROL)
             .lastModified(DEFAULT_LAST_MODIFED)
             .expires(new Date(System.currentTimeMillis() + CACHE_IN_MILLI_SECONDS));
    }
    return builder.build();
  }

}
