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
package org.exoplatform.settings.rest;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.api.settings.ExoFeatureService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;


@Path("/v1/features")
@Tag(name = "/v1/features", description = "Manages product experimental features")
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
  @Operation(
      summary = "Check if a feature is enabled for a user",
      description = "Check if a feature is enabled for a user",
      method = "GET"
  )
  @ApiResponses(
      value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "400", description = "Invalid query input"),
          @ApiResponse(responseCode = "401", description = "Unauthorized operation"),
          @ApiResponse(responseCode = "500", description = "Internal server error"), }
  )
  public Response isFeatureActiveForUser(
                                         @Parameter(description = "Feature name identifier", required = true) @PathParam(
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
