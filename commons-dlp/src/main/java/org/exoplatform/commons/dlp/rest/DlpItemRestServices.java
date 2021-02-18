package org.exoplatform.commons.dlp.rest;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.dlp.connector.DlpServiceConnector;
import org.exoplatform.commons.dlp.dto.DlpPermissionItem;
import org.exoplatform.commons.dlp.dto.DlpPositiveItem;
import org.exoplatform.commons.dlp.processor.DlpOperationProcessor;
import org.exoplatform.commons.dlp.service.DlpPermissionsService;
import org.exoplatform.commons.dlp.service.DlpPositiveItemService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.rest.CollectionEntity;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.IdentityRegistry;

import io.swagger.annotations.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Path("/dlp/items")
@Api(value = "/dlp/items", description = "Manages Dlp positive items") // NOSONAR
public class DlpItemRestServices implements ResourceContainer {

  private static final Log LOG = ExoLogger.getLogger(DlpItemRestServices.class);

    private DlpPositiveItemService dlpPositiveItemService;

  private DlpOperationProcessor dlpOperationProcessor;

    public static final String TYPE = "file";

  public DlpItemRestServices(DlpPositiveItemService dlpPositiveItemService,
                             DlpOperationProcessor dlpOperationProcessor) {
    this.dlpPositiveItemService = dlpPositiveItemService;
    this.dlpOperationProcessor = dlpOperationProcessor;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Retrieves the list of dlp positive items", httpMethod = "GET", response = Response.class, produces = "application/json",
      notes = "Return list of dlp positive items in json format")
  @ApiResponses(
      value = { @ApiResponse(code = HTTPStatus.OK, message = "Request fulfilled"),
          @ApiResponse(code = HTTPStatus.UNAUTHORIZED, message = "Unauthorized operation"),
          @ApiResponse(code = HTTPStatus.INTERNAL_ERROR, message = "Internal server error"), }
  )
  public Response getDlpPositiveItems(@ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
                                      @ApiParam(value = "Limit", required = false, defaultValue = "20") @QueryParam("limit") int limit) {
    if (!isUserMemberOfDlpPermissions() && !isMemberOfAdministratorGroup()) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    try {
      List<DlpPositiveItem> dlpPositiveItems = dlpPositiveItemService.getDlpPositivesItems(offset, limit);
      Long size = dlpPositiveItemService.getDlpPositiveItemsCount();
      CollectionEntity<DlpPositiveItem> collectionDlpPositiveItem = new CollectionEntity<>(dlpPositiveItems, offset, limit, size.intValue());
      return Response.ok(collectionDlpPositiveItem).build();
    } catch (Exception e) {
      LOG.error("Unknown error occurred while getting dlp positive items", e);
      return Response.serverError().build();
    }
  }

  @DELETE
  @Path("/item/{id}")
  @RolesAllowed("users")

  @ApiOperation(value = "Delete a document by id",
      httpMethod = "DELETE",
      response = Response.class,
      notes = "This delete the document if the authenticated user is a super manager")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 500, message = "Internal server error"),
      @ApiResponse(code = 400, message = "Invalid query input") })
  public Response deleteDlpDocumentById(@ApiParam(value = "Document id", required = true) @PathParam("id") Long id) {
    if (!isUserMemberOfDlpPermissions() && !isMemberOfAdministratorGroup()) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    dlpPositiveItemService.deleteDlpPositiveItem(id);
    return Response.ok().build();
  }

  @GET
  @Path("/keywords")
  @RolesAllowed("users")
  @ApiOperation(value = "Retrieves the list of dlp keywords", httpMethod = "GET", response = Response.class, produces = "application/json",
      notes = "Return list of dlp keywords in json format")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 500, message = "Internal server error"),
      @ApiResponse(code = 400, message = "Invalid query input") })
  public Response getDlpKeywords() {
    if (!isUserMemberOfDlpPermissions() && !isMemberOfAdministratorGroup()) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    try {
      String keywords = dlpOperationProcessor.getKeywords();
      return Response.ok(keywords).build();
    } catch (Exception e) {
      LOG.error("Unknown error occurred while getting dlp keywords", e);
      return Response.serverError().build();
    }
  }

  @POST
  @Path("/keywords")
  @RolesAllowed("users")
  @ApiOperation(value = "set dlp keywords", httpMethod = "POST", response = Response.class, produces = "application/json",
      notes = "set the dlp keywords")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 500, message = "Internal server error") })
  public Response setDlpKeywords(@ApiParam(value = "keywords", required = true) String keywords) {

    if (!isUserMemberOfDlpPermissions() && !isMemberOfAdministratorGroup()) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    try {
      dlpOperationProcessor.setKeywords(keywords);
      return Response.ok().entity("{\"result\":\"" + keywords + "\"}").build();
    } catch (Exception e) {
      LOG.error("Unknown error occurred while setting dlp keywords", e);
      return Response.serverError().build();
    }
  }

  @PUT
  @Path("item/restore/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Restore the dlp positive items", httpMethod = "PUT", response = Response.class, produces = "application/json",
      notes = "Return the restored positive item")
  @ApiResponses(
      value = { @ApiResponse(code = HTTPStatus.OK, message = "Request fulfilled"),
          @ApiResponse(code = HTTPStatus.UNAUTHORIZED, message = "Unauthorized operation"),
          @ApiResponse(code = HTTPStatus.INTERNAL_ERROR, message = "Internal server error"), }
  )
  public Response restoreDlpPositiveItems(@ApiParam(value = "Document id", required = true) @PathParam("id") Long id) {

    if (!isUserMemberOfDlpPermissions() && !isMemberOfAdministratorGroup()) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    try {
      dlpPositiveItemService.restoreDlpPositiveItem(id);
      return Response.ok().build();
    } catch (Exception e) {
      LOG.error("Unknown error occurred while restoring dlp positive items", e);
      return Response.serverError().build();
    }
  }

  @POST
  @Path("/save/permissions")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Save the dlp group permissions", httpMethod = "POST", response = Response.class, produces = "application/json",
      notes = "Return the saved permissions")
  @ApiResponses(
      value = { @ApiResponse(code = HTTPStatus.OK, message = "Request fulfilled"),
          @ApiResponse(code = HTTPStatus.UNAUTHORIZED, message = "Unauthorized operation"),
          @ApiResponse(code = HTTPStatus.INTERNAL_ERROR, message = "Internal server error"), }
  )
  public Response saveDlpPermissions(@ApiParam(value = "The new permissions", required = true) String permissions) {
    if (!isUserMemberOfDlpPermissions() && !isMemberOfAdministratorGroup()) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    try {
      DlpServiceConnector dlpServiceConnector = (DlpServiceConnector) dlpOperationProcessor.getConnectors().get(TYPE);
      String oldPermissions = dlpOperationProcessor.getOldPermissions();
      dlpOperationProcessor.savePermissions(permissions);
      DlpPermissionsService dlpPermissionsService = CommonsUtils.getService(DlpPermissionsService.class);
      dlpPermissionsService.removeDlpPermissionsPagesAndNavigations(oldPermissions);
      dlpServiceConnector.addDriveAndFolderSecurityPermissions(permissions);
      dlpPermissionsService.addDlpPermissionsPagesAndNavigations(permissions);
      return Response.ok().build();
    } catch (Exception e) {
      LOG.error("Unknown error occurred while saving dlp permissions", e);
      return Response.serverError().build();
    }
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/permissions/isAdministrator")
  @RolesAllowed("users")
  @ApiOperation(value = "Check if Current user is member of administrator group", httpMethod = "GET", response = Response.class, produces = "application/json",
      notes = "Check if Current user is member of administrator group in json format")
  @ApiResponses(
      value = { @ApiResponse(code = HTTPStatus.OK, message = "Request fulfilled"),
          @ApiResponse(code = HTTPStatus.UNAUTHORIZED, message = "Unauthorized operation"),
          @ApiResponse(code = HTTPStatus.INTERNAL_ERROR, message = "Internal server error"), }
  )
  public Response checkIsMemberOfAdministratorGroup() {
    if (!isUserMemberOfDlpPermissions() && !isMemberOfAdministratorGroup()) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    try {
      return Response.ok().entity("{\"isAdmin\":\"" + isMemberOfAdministratorGroup() + "\"}").build();
    } catch (Exception e) {
      LOG.error("Unknown error occurred while checking is admin member ", e);
      return Response.serverError().build();
    }
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/permissions")
  @RolesAllowed("users")
  @ApiOperation(value = "Retrieves the list of dlp permissions", httpMethod = "GET", response = Response.class, produces = "application/json",
      notes = "Return list of dlp permissions in json format")
  @ApiResponses(
      value = { @ApiResponse(code = HTTPStatus.OK, message = "Request fulfilled"),
          @ApiResponse(code = HTTPStatus.UNAUTHORIZED, message = "Unauthorized operation"),
          @ApiResponse(code = HTTPStatus.INTERNAL_ERROR, message = "Internal server error"), }
  )
  public Response getDlpPermissions(@ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
                                    @ApiParam(value = "Limit", required = false, defaultValue = "20") @QueryParam("limit") int limit) {

    if (!isUserMemberOfDlpPermissions() && !isMemberOfAdministratorGroup()) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    try {
      List<DlpPermissionItem> dlpPermissionItemList = dlpOperationProcessor.getPermissions();
      CollectionEntity<DlpPermissionItem>
          collectionDlpPermissions =
          new CollectionEntity<>(dlpPermissionItemList, offset, limit, dlpPermissionItemList.size());
      return Response.ok(collectionDlpPermissions).build();
    } catch (Exception e) {
      LOG.error("Unknown error occurred while getting list of dlp permissions", e);
      return Response.serverError().build();
    }
  }

  private final boolean isUserMemberOfDlpPermissions() {
    IdentityRegistry identityRegistry = CommonsUtils.getService(IdentityRegistry.class);
    OrganizationService organizationService = CommonsUtils.getService(OrganizationService.class);
    SettingService settingService = CommonsUtils.getService(SettingService.class);
    String username = ConversationState.getCurrent().getIdentity().getUserId();
    if (StringUtils.isBlank(username)) {
      return false;
    }
    SettingValue<?>
        settingValue =
        settingService.get(org.exoplatform.commons.api.settings.data.Context.GLOBAL,
                           Scope.APPLICATION.id("DlpPermissions"),
                           "exo:dlpPermissions");
    if (settingValue == null || settingValue.getValue().toString().isEmpty())
      return false;
    List<String> permissionsList = Arrays.asList(settingValue.getValue().toString().split(","));
    for (String permissionExpression : permissionsList) {
      org.exoplatform.services.security.Identity identity = identityRegistry.getIdentity(username);
      if (identity != null && identity.isMemberOf(permissionExpression)) {
        return true;
      }
      try {
        Collection<Membership> memberships = organizationService.getMembershipHandler()
                                                                .findMembershipsByUserAndGroup(username, permissionExpression);
        if (memberships != null && !memberships.isEmpty()) {
          return true;
        }
      } catch (Exception e) {
        throw new IllegalStateException("Error getting memberships of user " + username, e);
      }
    }
    return false;
  }

  private boolean isMemberOfAdministratorGroup() {
    UserACL userACL = CommonsUtils.getService(UserACL.class);
    return userACL.isSuperUser() || userACL.isUserInGroup(userACL.getAdminGroups());
  }

}
