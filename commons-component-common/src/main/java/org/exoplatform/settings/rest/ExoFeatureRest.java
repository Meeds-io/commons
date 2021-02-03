package org.exoplatform.settings.rest;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.commons.api.settings.ExoFeatureService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;

import io.swagger.annotations.*;

@Path("/v1/features")
@Api(value = "/v1/features", description = "Manages product experimental features") // NOSONAR
public class ExoFeatureRest implements ResourceContainer {

  private static final Log  LOG = ExoLogger.getLogger(ExoFeatureRest.class);

  private ExoFeatureService featureService;

  public ExoFeatureRest(ExoFeatureService featureService) {
    this.featureService = featureService;
  }

  @Path("{featureName}")
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  @RolesAllowed("users")
  @ApiOperation(
      value = "Check if a feature is enabled for a user.",
      httpMethod = "GET", response = Response.class, produces = "text/plain"
  )
  @ApiResponses(
      value = { @ApiResponse(code = HTTPStatus.OK, message = "Request fulfilled"),
          @ApiResponse(code = HTTPStatus.BAD_REQUEST, message = "Invalid query input"),
          @ApiResponse(code = HTTPStatus.UNAUTHORIZED, message = "Unauthorized operation"),
          @ApiResponse(code = HTTPStatus.INTERNAL_ERROR, message = "Internal server error"), }
  )
  public Response isFeatureActiveForUser(
                                         @ApiParam(value = "Feature name identifier", required = true) @PathParam(
                                           "featureName"
                                         ) String featureName) {
    if (StringUtils.isBlank(featureName)) {
      return Response.status(Response.Status.BAD_REQUEST).entity("Feature name must not be null or blank").build();
    }
    try {
      String username = ConversationState.getCurrent().getIdentity().getUserId();
      boolean isFeatureActive = featureService.isFeatureActiveForUser(featureName, username);
      return Response.ok().entity(String.valueOf(isFeatureActive)).type(MediaType.TEXT_PLAIN).build();
    } catch (Exception e) {
      LOG.warn("Error retrieving feature status with name '{}'", featureName, e);
      return Response.serverError().entity(e.getMessage()).build();
    }
  }
}
