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
package org.exoplatform.commons.resource;

import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.resources.LocaleConfig;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.services.resources.ResourceBundleService;
import org.exoplatform.services.rest.resource.ResourceContainer;

/**
 * This REST Service deserves resource bundles in JSON format
 */
@Path("/i18n/bundle")
public class ResourceBundleREST implements ResourceContainer {

  private static final Log          LOG                    = ExoLogger.getLogger(ResourceBundleREST.class);

  private static final CacheControl CACHE_CONTROL          = new CacheControl();

  private static final Date         DEFAULT_LAST_MODIFED   = new Date();

  // 7 days
  private static final int          CACHE_IN_SECONDS       = 604800;

  private static final int          CACHE_IN_MILLI_SECONDS = CACHE_IN_SECONDS * 1000;

  static {
    CACHE_CONTROL.setMaxAge(CACHE_IN_SECONDS);
  }

  private ResourceBundleService resourceBundleService;

  private LocaleConfigService   localeConfigService;

  public ResourceBundleREST(ResourceBundleService resourceBundleService, LocaleConfigService localeConfigService) {
    this.resourceBundleService = resourceBundleService;
    this.localeConfigService = localeConfigService;
  }

  @GET
  @Path("{name}-{lang}.json")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getBundleContent( // NOSONAR
                                   @Context
                                   Request request,
                                   @PathParam("name") String resourceBundleName,
                                   @PathParam("lang") String lang,
                                   @PathParam("v") String version) {
    if (StringUtils.isBlank(resourceBundleName) || StringUtils.isBlank(lang)) {
      return Response.status(400).build();
    }
    String eTagValue = version == null ? String.valueOf(System.currentTimeMillis()) : version;

    EntityTag eTag = new EntityTag(eTagValue);
    Response.ResponseBuilder builder = request.evaluatePreconditions(eTag);
    if (PropertyManager.isDevelopping() || builder == null) {
      LocaleConfig localeConfig = localeConfigService.getLocaleConfig(lang);
      if (localeConfig == null) {
        LOG.warn("Locale '{}' is not supported", lang);
        return Response.status(400).build();
      }
      String content = resourceBundleService.getResourceBundleContent(resourceBundleName, localeConfig.getLocale());
      if (content == null) {
        return Response.status(404).build();
      } else {
        builder = Response.ok(content, MediaType.APPLICATION_JSON);
        builder.cacheControl(CACHE_CONTROL);
        builder.expires(new Date(System.currentTimeMillis() + CACHE_IN_MILLI_SECONDS));
        builder.lastModified(DEFAULT_LAST_MODIFED);
      }
    }
    return builder.build();
  }

}
