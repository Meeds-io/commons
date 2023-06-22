package org.exoplatform.commons.resource;

import java.util.*;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;

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

  private static final Log          LOG                    = ExoLogger.getLogger(ResourceBundleREST.class);

  private static final Locale       DEFAULT_I18N_LOCALE    = Locale.ENGLISH;

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
  @SuppressWarnings("unchecked")
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
      ResourceBundle resourceBundle = resourceBundleService.getResourceBundle(resourceBundleName, localeConfig.getLocale());
      ResourceBundle defaultResourceBundle = resourceBundleService.getResourceBundle(resourceBundleName, DEFAULT_I18N_LOCALE);
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
        String key = keys.nextElement();
        resultJSON.put(key, resourceBundle.getString(key));
      }
      keys = defaultResourceBundle.getKeys();
      while (keys.hasMoreElements()) {
        String key = keys.nextElement();
        resultJSON.putIfAbsent(key, defaultResourceBundle.getString(key));
      }
      builder = Response.ok(resultJSON.toJSONString(), MediaType.APPLICATION_JSON);
      builder.cacheControl(CACHE_CONTROL);
      builder.expires(new Date(System.currentTimeMillis() + CACHE_IN_MILLI_SECONDS));
      builder.lastModified(DEFAULT_LAST_MODIFED);
    }
    return builder.build();
  }

}
