package org.exoplatform.mfa.rest.otp;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.mfa.api.otp.OtpService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/otp")
@Api(value = "/otp")
public class OtpRestService implements ResourceContainer {
  
  private static final Log LOG = ExoLogger.getLogger(OtpRestService.class);

  private OtpService otpService;

  public OtpRestService(OtpService otpService) {
    this.otpService=otpService;
  }


  @Path("/checkRegistration")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(
      value = "Check if user have activated his OTP",
      httpMethod = "GET", response = Response.class, produces = MediaType.APPLICATION_JSON
  )
  @ApiResponses(
      value = { @ApiResponse(code = HTTPStatus.OK, message = "Request fulfilled"),
          @ApiResponse(code = HTTPStatus.BAD_REQUEST, message = "Invalid query input"),
          @ApiResponse(code = HTTPStatus.UNAUTHORIZED, message = "Unauthorized operation"),
          @ApiResponse(code = HTTPStatus.INTERNAL_ERROR, message = "Internal server error"), }
  )
  public Response checkRegistration(@Context HttpServletRequest request) {

    String userId = ConversationState.getCurrent().getIdentity().getUserId();
    return Response.ok().entity("{\"result\":\"" + otpService.isMfaInitializedForUser(userId) + "\"}").build();

  }

  @Path("/generateSecret")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(
      value = "Generate New secret OTP for user",
      httpMethod = "GET", response = Response.class, produces = MediaType.APPLICATION_JSON
  )
  @ApiResponses(
      value = { @ApiResponse(code = HTTPStatus.OK, message = "Request fulfilled"),
          @ApiResponse(code = HTTPStatus.BAD_REQUEST, message = "Invalid query input"),
          @ApiResponse(code = HTTPStatus.UNAUTHORIZED, message = "Unauthorized operation"),
          @ApiResponse(code = HTTPStatus.INTERNAL_ERROR, message = "Internal server error"), }
  )
  public Response generateSecret(@Context HttpServletRequest request) {

    String userId = ConversationState.getCurrent().getIdentity().getUserId();
    if (!otpService.isMfaInitializedForUser(userId)) {
      String secret=otpService.generateSecret(userId);
      String urlFromSecret= otpService.generateUrlFromSecret(userId,secret);
      return Response.ok().entity("{\"secret\":\"" + secret + "\",\"url\":\""+urlFromSecret+"\"}").build();
    } else {
      return Response.ok().build();
    }

  }

  
  @Path("/verify")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(
      value = "Verify OTP token",
      httpMethod = "GET", response = Response.class, produces = MediaType.APPLICATION_JSON
  )
  @ApiResponses(
      value = { @ApiResponse(code = HTTPStatus.OK, message = "Request fulfilled"),
          @ApiResponse(code = HTTPStatus.BAD_REQUEST, message = "Invalid query input"),
          @ApiResponse(code = HTTPStatus.UNAUTHORIZED, message = "Unauthorized operation"),
          @ApiResponse(code = HTTPStatus.INTERNAL_ERROR, message = "Internal server error"), }
  )
  public Response verifyToken(@Context HttpServletRequest request,
                              @ApiParam(value = "Token to verify", required = true) @QueryParam("token") String token) {
  
    String userId=null;
    try {

      userId = ConversationState.getCurrent().getIdentity().getUserId();

      boolean otpResult = otpService.validateToken(userId,token);
      request.getSession().setAttribute("mfaValidated",otpResult);
      return Response.ok().entity("{\"result\":\"" + otpResult + "\"}").build();
    } catch (Exception e) {
      LOG.warn("Error when checking OTP token for user='{}', token='{}'", userId, token, e);
      return Response.serverError().entity(e.getMessage()).build();
    }
  }
}
