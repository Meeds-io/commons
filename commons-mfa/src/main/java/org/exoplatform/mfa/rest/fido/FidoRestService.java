package org.exoplatform.mfa.rest.fido;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.mfa.api.fido.FidoService;
import org.exoplatform.mfa.api.otp.OtpService;
import org.exoplatform.mfa.rest.otp.OtpRestService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.json.JSONObject;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/fido")
@Api(value = "/fido", description = "Manages FIDO features")
public class FidoRestService implements ResourceContainer {
  private static final Log LOG = ExoLogger.getLogger(FidoRestService.class);
  
  @Path("/startRegistration")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(
      value = "Start FIDO2 Registration STEP 1",
      httpMethod = "GET", response = Response.class, produces = MediaType.APPLICATION_JSON
  )
  @ApiResponses(
      value = { @ApiResponse(code = HTTPStatus.OK, message = "Request fulfilled"),
          @ApiResponse(code = HTTPStatus.BAD_REQUEST, message = "Invalid query input"),
          @ApiResponse(code = HTTPStatus.UNAUTHORIZED, message = "Unauthorized operation"),
          @ApiResponse(code = HTTPStatus.INTERNAL_ERROR, message = "Internal server error"), }
  )
  public Response startRegistration(@Context UriInfo uriInfo) {
    
    String userId=null;
    try {
      
      FidoService fidoService = CommonsUtils.getService(FidoService.class);
      try {
        userId = ConversationState.getCurrent().getIdentity().getUserId();
      } catch (Exception e) {
        return Response.status(HTTPStatus.UNAUTHORIZED).build();
      }
      String host = uriInfo.getRequestUri().getScheme()+"://"+uriInfo.getRequestUri().getHost();
      JSONObject result = fidoService.startRegistration(userId,host);
      if (result!=null) {
        return Response.ok().entity(result.toString()).build();
      } else {
        return Response.serverError().build();
      }
      
    } catch (Exception e) {
      LOG.warn("Error when starting FIDO Registration Step 1 user='{}'", userId, e);
      return Response.serverError().entity(e.getMessage()).build();
    }
  }
  
  @Path("/finishRegistration")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(
      value = "Start FIDO2 Registration STEP 2",
      httpMethod = "POST", response = Response.class, produces = MediaType.APPLICATION_JSON
  )
  @ApiResponses(
      value = { @ApiResponse(code = HTTPStatus.OK, message = "Request fulfilled"),
          @ApiResponse(code = HTTPStatus.BAD_REQUEST, message = "Invalid query input"),
          @ApiResponse(code = HTTPStatus.UNAUTHORIZED, message = "Unauthorized operation"),
          @ApiResponse(code = HTTPStatus.INTERNAL_ERROR, message = "Internal server error"), }
  )
  public Response finishRegistration(@FormParam("data") String data,
                                     @Context UriInfo uriInfo, @Context HttpServletRequest request) {
    
    String userId=null;
    try {
      
      FidoService fidoService = CommonsUtils.getService(FidoService.class);
      try {
        userId = ConversationState.getCurrent().getIdentity().getUserId();
      } catch (Exception e) {
        return Response.status(HTTPStatus.UNAUTHORIZED).build();
      }
      JSONObject result = fidoService.finishRegistration(userId,new JSONObject(data));
      LOG.info("Finish Registration, data={}",data);
      if (result!=null) {
        request.getSession().setAttribute("mfaValidated",true);
        return Response.ok().entity("{\"ok\":\"true\"}").build();
      } else {
        return Response.serverError().build();
      }
    
    } catch (Exception e) {
      LOG.warn("Error when finishing FIDO Registration user='{}'", userId, e);
      return Response.serverError().entity(e.getMessage()).build();
    }
  }
  
  @Path("/startAuthentication")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(
      value = "Start FIDO2 Authentication STEP 1",
      httpMethod = "GET", response = Response.class, produces = MediaType.APPLICATION_JSON
  )
  @ApiResponses(
      value = { @ApiResponse(code = HTTPStatus.OK, message = "Request fulfilled"),
          @ApiResponse(code = HTTPStatus.BAD_REQUEST, message = "Invalid query input"),
          @ApiResponse(code = HTTPStatus.UNAUTHORIZED, message = "Unauthorized operation"),
          @ApiResponse(code = HTTPStatus.INTERNAL_ERROR, message = "Internal server error"), }
  )
  public Response startAuthentication(@Context UriInfo uriInfo) {
    
    String userId=null;
    try {
      
      FidoService fidoService = CommonsUtils.getService(FidoService.class);
      try {
        userId = ConversationState.getCurrent().getIdentity().getUserId();
      } catch (Exception e) {
        return Response.status(HTTPStatus.UNAUTHORIZED).build();
      }
      String host = uriInfo.getRequestUri().getScheme()+"://"+uriInfo.getRequestUri().getHost();
      JSONObject result = fidoService.startAuthentication(userId,host);
      if (result!=null) {
        return Response.ok().entity(result.toString()).build();
      } else {
        return Response.serverError().build();
      }
      
    } catch (Exception e) {
      LOG.warn("Error when starting FIDO Registration Step 1 user='{}'", userId, e);
      return Response.serverError().entity(e.getMessage()).build();
    }
  }
  
  @Path("/finishAuthentication")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(
      value = "Start FIDO2 Authentication STEP 2",
      httpMethod = "POST", response = Response.class, produces = MediaType.APPLICATION_JSON
  )
  @ApiResponses(
      value = { @ApiResponse(code = HTTPStatus.OK, message = "Request fulfilled"),
          @ApiResponse(code = HTTPStatus.BAD_REQUEST, message = "Invalid query input"),
          @ApiResponse(code = HTTPStatus.UNAUTHORIZED, message = "Unauthorized operation"),
          @ApiResponse(code = HTTPStatus.INTERNAL_ERROR, message = "Internal server error"), }
  )
  public Response finishAuthentication(@FormParam("data") String data,
                                     @Context UriInfo uriInfo, @Context HttpServletRequest request) {
    
    String userId=null;
    try {
      
      FidoService fidoService = CommonsUtils.getService(FidoService.class);
      try {
        userId = ConversationState.getCurrent().getIdentity().getUserId();
      } catch (Exception e) {
        return Response.status(HTTPStatus.UNAUTHORIZED).build();
      }
      JSONObject result = fidoService.finishAuthentication(userId,new JSONObject(data));
      LOG.info("Finish Authentication, data={}",data);
      if (result!=null) {
        request.getSession().setAttribute("mfaValidated",true);
        return Response.ok().entity("{\"ok\":\"true\"}").build();
      } else {
        return Response.serverError().build();
      }
      
    } catch (Exception e) {
      LOG.warn("Error when finishing FIDO Authentication user='{}'", userId, e);
      return Response.serverError().entity(e.getMessage()).build();
    }
  }
  
}
