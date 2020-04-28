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
package org.exoplatform.commons.api.notification.rest;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.jaxrs.PATCH;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import org.exoplatform.commons.api.notification.NotificationMessageUtils;
import org.exoplatform.commons.api.notification.model.WebNotificationFilter;
import org.exoplatform.commons.api.notification.service.WebNotificationService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;

/**
 * Provides REST Services in order to perform all read/write operations related
 * to web notifications.
 */

@Path("notifications/webNotifications")
public class WebNotificationRestService implements ResourceContainer {

  private static final Log       LOG         = ExoLogger.getLogger(WebNotificationRestService.class);

  private WebNotificationService webNftService;

  public WebNotificationRestService(WebNotificationService webNftService) {
    this.webNftService = webNftService;
  }

  @GET
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Get notifications list", httpMethod = "GET", response = Response.class, notes = "This gets the list of the notifications.")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Notifications list returned"),
      @ApiResponse(code = 404, message = "Notifications list not found"),
      @ApiResponse(code = 500, message = "Internal server error") })
  public Response getNotifications() throws Exception {
    int maxItemsInPopover = NotificationMessageUtils.getMaxItemsInPopover();
    JSONArray notificationsJsonArray = new JSONArray();
    JSONObject response = new JSONObject();
    String currentUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (currentUser == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    List<String> notifications = webNftService.get(new WebNotificationFilter(currentUser, true), 0, maxItemsInPopover);
    for (String notification : notifications) {
      JSONObject notificationJsonObject = new JSONObject();
      notificationJsonObject.put("notification", notification);
      notificationsJsonArray.put(notificationJsonObject);
    }
    int badge = webNftService == null ? 0 : webNftService.getNumberOnBadge(currentUser);
    response.put("notifications", notificationsJsonArray);
    response.put("badge", badge);
    return Response.ok(response.toString(), MediaType.APPLICATION_JSON).build();
  }

  @PATCH
  @Path("{id}")
  @Consumes(MediaType.TEXT_PLAIN)
  @RolesAllowed("users")
  @ApiOperation(value = "Update notification", httpMethod = "PATCH", response = Response.class, notes = "Perform some patch operations on notifications")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Notification updated"),
      @ApiResponse(code = 400, message = "Invalid query input"), @ApiResponse(code = 500, message = "Internal server error") })
  public Response updateNotifications(@ApiParam(value = "operation", required = true) String operation,
                                     @ApiParam(value = "id", required = true) @PathParam("id") String notificationId) {

    String currentUser = ConversationState.getCurrent().getIdentity().getUserId();
    try {
      if (operation == null) {
        LOG.warn("Notification operation should be not null");
        return Response.status(Response.Status.BAD_REQUEST).build();
      }

      if (currentUser == null) {
        return Response.status(Response.Status.UNAUTHORIZED).build();
      }

      if (operation.equals("markAsRead")) {
        if (notificationId == null) {
          return Response.status(Response.Status.BAD_REQUEST).build();
        } else {
          webNftService.markRead(notificationId);
        }
      }

      if (operation.equals("hide")) {
        if (notificationId == null) {
          return Response.status(Response.Status.BAD_REQUEST).build();
        } else {
          webNftService.hidePopover(notificationId);
        }
      }

      if (operation.equals("resetNew")) {
        webNftService.resetNumberOnBadge(currentUser);
      }

      if (operation.equals("markAllAsRead")) {
        webNftService.markAllRead(currentUser);
        webNftService.resetNumberOnBadge(currentUser);
      }
      return Response.noContent().build();
    } catch (Exception e) {
      LOG.error("Error when trying to patch operation {}  on notification {} for user {}", operation, notificationId, currentUser, e);
      return Response.serverError().build();
    }
  }
}
