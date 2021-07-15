package org.exoplatform.mfa.rest.mfa;


import io.swagger.annotations.*;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.mfa.api.MfaService;

import javax.annotation.security.RolesAllowed;

import org.exoplatform.mfa.storage.dto.RevocationRequest;
import org.exoplatform.mfa.rest.entities.RevocationRequestEntity;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.stream.Collectors;

import java.util.Locale;


@Path("/mfa")
@Api(value = "/mfa")
public class MfaRestService implements ResourceContainer {

  private MfaService mfaService;

  private static final Log LOG = ExoLogger.getLogger(MfaRestService.class);


  public MfaRestService(MfaService mfaService) {
    this.mfaService=mfaService;
  }

  @Path("/settings")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Get Activated MFA System", httpMethod = "GET", response = Response.class, produces = MediaType.APPLICATION_JSON)
  @ApiResponses(value = { @ApiResponse(code = HTTPStatus.OK, message = "Request fulfilled"),
      @ApiResponse(code = HTTPStatus.UNAUTHORIZED, message = "Unauthorized operation"),
      @ApiResponse(code = HTTPStatus.INTERNAL_ERROR, message = "Internal server error"), })
  public Response getMfaSystem(@Context HttpServletRequest request) {
    JSONObject result = new JSONObject();
    Locale locale = request == null ? Locale.ENGLISH : request.getLocale();
    try {
      result.put("mfaSystem", mfaService.getMfaSystemService().getType());
      result.put("helpTitle", mfaService.getMfaSystemService().getHelpTitle(locale));
      result.put("helpContent", mfaService.getMfaSystemService().getHelpContent(locale));
      return Response.ok().entity(result.toString()).build();
    } catch (JSONException e) {
      return Response.serverError().build();

    }
  }

  @Path("/available")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Get Activated MFA System", httpMethod = "GET", response = Response.class, produces = MediaType.APPLICATION_JSON)
  @ApiResponses(value = { @ApiResponse(code = HTTPStatus.OK, message = "Request fulfilled"),
      @ApiResponse(code = HTTPStatus.UNAUTHORIZED, message = "Unauthorized operation"),
      @ApiResponse(code = HTTPStatus.INTERNAL_ERROR, message = "Internal server error"), })
  public Response getAvalailableMfaSystems() {
    JSONObject result = new JSONObject();
    try {
      result.put("available", mfaService.getAvailableMfaSystems());
      return Response.ok().entity(result.toString()).build();
    } catch (JSONException e) {
      return Response.serverError().build();

    }
  }

  @Path("/changeMfaFeatureActivation/{status}")
  @PUT
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("administrators")
  @ApiOperation(value = "Switch the Activated MFA System", httpMethod = "PUT", response = Response.class, produces = MediaType.APPLICATION_JSON)
  @ApiResponses(value = { @ApiResponse(code = HTTPStatus.OK, message = "Request fulfilled"),
      @ApiResponse(code = HTTPStatus.UNAUTHORIZED, message = "Unauthorized operation"),
      @ApiResponse(code = HTTPStatus.INTERNAL_ERROR, message = "Internal server error"), })
  public Response changeMfaFeatureActivation(@ApiParam(value = "Switch the Activated MFA System to avtivated or deactivated", required = true) String status) {
    mfaService.saveActiveFeature(status);
    return Response.ok().type(MediaType.TEXT_PLAIN).build();
  }

  @Path("/revocations")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("administrators")
  @ApiOperation(value = "Get Revocation Request list", httpMethod = "GET", response = Response.class, produces =
      MediaType.APPLICATION_JSON)
  @ApiResponses(value = { @ApiResponse(code = HTTPStatus.OK, message = "Request fulfilled"),
      @ApiResponse(code = HTTPStatus.UNAUTHORIZED, message = "Unauthorized operation"),
      @ApiResponse(code = HTTPStatus.INTERNAL_ERROR, message = "Internal server error") })
  public Response getRevocationRequests() {

    List<RevocationRequest> revocationRequests = mfaService.getAllRevocationRequests();

    try {
      JSONObject result = new JSONObject();
      result.put("requests", revocationRequests.stream()
                                               .map(this::buildRevocationRequest)
                                               .map(revocationRequestEntity -> new JSONObject(revocationRequestEntity.asMap()))
                                               .collect(Collectors.toList()));

      return Response.ok().entity(result.toString()).build();
    } catch (JSONException e) {
      LOG.error("Unable to build get revocation request answer",e);
      return Response.serverError().build();
    }
  }

  @Path("/revocations/{id}")
  @PUT
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("administrators")
  @ApiOperation(value = "Update a revocation request", httpMethod = "PUT", response = Response.class, produces =
      MediaType.APPLICATION_JSON)
  @ApiResponses(value = { @ApiResponse(code = HTTPStatus.OK, message = "Request fulfilled"),
      @ApiResponse(code = HTTPStatus.UNAUTHORIZED, message = "Unauthorized operation"),
      @ApiResponse(code = HTTPStatus.INTERNAL_ERROR, message = "Internal server error") })
  public Response updateRevocationRequests(@ApiParam(value = "RevocationRequest id", required = true) @PathParam("id") String id,
                                           @ApiParam(value = "RevocationRequest status confirm/cancel", required = true) @QueryParam("status") String status) {

    RevocationRequest revocationRequest = mfaService.getRevocationRequestById(Long.parseLong(id));
    if (revocationRequest!=null) {
      switch (status) {
      case "confirm":
        mfaService.confirmRevocationRequest(Long.parseLong(id));
        break;
      case "cancel":
        mfaService.cancelRevocationRequest(Long.parseLong(id));
        break;
      default:
        return Response.status(Response.Status.BAD_REQUEST).build();
      }
      return Response.ok().build();
    } else {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
  }

  @Path("/revocations")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Create a Revocation Request", httpMethod = "POST", response = Response.class, produces =
      MediaType.APPLICATION_JSON)
  @ApiResponses(value = { @ApiResponse(code = HTTPStatus.OK, message = "Request fulfilled"),
      @ApiResponse(code = HTTPStatus.UNAUTHORIZED, message = "Unauthorized operation"),
      @ApiResponse(code = HTTPStatus.INTERNAL_ERROR, message = "Internal server error") })
  public Response addRevocationRequest(String type) {
    String userId = ConversationState.getCurrent().getIdentity().getUserId();
    boolean result=mfaService.addRevocationRequest(userId,type);
    return Response.ok().entity("{\"result\":\"" + result + "\"}").build();
  }


  private RevocationRequestEntity buildRevocationRequest(RevocationRequest revocationRequest) {
    return new RevocationRequestEntity(revocationRequest.getId(),
                                       revocationRequest.getUser(),
                                       revocationRequest.getType());
  }

  @Path("/changeMfaSystem/{mfaSystem}")
  @PUT
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("administrators")
  @ApiOperation(value = "Change the MFA System", httpMethod = "PUT", response = Response.class, produces = MediaType.APPLICATION_JSON)
  @ApiResponses(value = { @ApiResponse(code = HTTPStatus.OK, message = "Request fulfilled"),
          @ApiResponse(code = HTTPStatus.UNAUTHORIZED, message = "Unauthorized operation"),
          @ApiResponse(code = HTTPStatus.INTERNAL_ERROR, message = "Internal server error"), })
  public Response changeMfaSystem(@ApiParam(value = "Change the MFA MFA System", required = true) String mfaSystem) {
    if (mfaService.setMfaSystem(mfaSystem)) {
      return Response.ok().type(MediaType.TEXT_PLAIN).build();
    } else {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
  }

  @POST
  @Path("/saveProtectedGroups")
  @RolesAllowed("administrators")
  @ApiOperation(value = "set mfa groups",
          httpMethod = "POST",
          response = Response.class,
          produces = "application/json",
          notes = "set mfa groups")
  @ApiResponses(value = {
          @ApiResponse (code = HTTPStatus.OK, message = "Request fulfilled"),
          @ApiResponse(code = HTTPStatus.UNAUTHORIZED, message = "Unauthorized operation"),
          @ApiResponse(code = HTTPStatus.INTERNAL_ERROR, message = "Internal server error") })
  public Response saveProtectedGroups(@ApiParam(value = "groups", required = true) String groups) {
    mfaService.saveProtectedGroups(groups);
    return Response.ok().entity("{\"groups\":\"" + groups + "\"}").build();
  }

  @Path("/getProtectedGroups")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("administrators")
  @ApiOperation(value = "Get protected groups for MFA System", httpMethod = "GET", response = Response.class, produces = MediaType.APPLICATION_JSON)
  @ApiResponses(value = { @ApiResponse(code = HTTPStatus.OK, message = "Request fulfilled"),
          @ApiResponse(code = HTTPStatus.UNAUTHORIZED, message = "Unauthorized operation"),
          @ApiResponse(code = HTTPStatus.INTERNAL_ERROR, message = "Internal server error"), })
  public Response getProtectedGroups() {

    JSONArray groups = new JSONArray();
    mfaService.getProtectedGroups().stream().forEach(groups::put);
    return Response.ok().entity("{\"protectedGroups\":" + groups + "}").build();
  }
}
