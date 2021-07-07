package org.exoplatform.mfa.rest.mfa;


import io.swagger.annotations.*;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.commons.api.settings.ExoFeatureService;
import org.exoplatform.mfa.api.MfaService;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.exoplatform.mfa.storage.dto.RevocationRequest;
import org.exoplatform.mfa.rest.entities.RevocationRequestEntity;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.stream.Collectors;

@Path("/mfa")
@Api(value = "/mfa")
public class MfaRestService implements ResourceContainer {

  public static final String MFA_FEATURE = "mfa";

  private MfaService mfaService;

  private ExoFeatureService featureService;

  private static final Log LOG = ExoLogger.getLogger(MfaRestService.class);

  public MfaRestService(MfaService mfaService, ExoFeatureService featureService) {
    this.mfaService=mfaService;
    this.featureService=featureService;
  }

  @Path("/settings")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Get Activated MFA System", httpMethod = "GET", response = Response.class, produces = MediaType.APPLICATION_JSON)
  @ApiResponses(value = { @ApiResponse(code = HTTPStatus.OK, message = "Request fulfilled"),
      @ApiResponse(code = HTTPStatus.UNAUTHORIZED, message = "Unauthorized operation"),
      @ApiResponse(code = HTTPStatus.INTERNAL_ERROR, message = "Internal server error"), })
  public Response getMfaSystem() {
    JSONObject result = new JSONObject();
    try {
      result.put("mfaSystem", mfaService.getMfaSystem());
      return Response.ok().entity(result.toString()).build();
    } catch (JSONException e) {
      return Response.serverError().build();

    }
  }

  @Path("/changeMfaFeatureActivation/{status}")
  @PUT
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Switch the Activated MFA System", httpMethod = "PUT", response = Response.class, produces = MediaType.APPLICATION_JSON)
  @ApiResponses(value = { @ApiResponse(code = HTTPStatus.OK, message = "Request fulfilled"),
      @ApiResponse(code = HTTPStatus.UNAUTHORIZED, message = "Unauthorized operation"),
      @ApiResponse(code = HTTPStatus.INTERNAL_ERROR, message = "Internal server error"), })
  public Response changeMfaFeatureActivation(@ApiParam(value = "Switch the Activated MFA System to avtivated or deactivated", required = true) String status) {
    boolean isActiveBool = Boolean.parseBoolean(status);
    featureService.saveActiveFeature(MFA_FEATURE, isActiveBool);
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

}
