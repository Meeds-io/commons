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
package org.exoplatform.services.user;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;

@Path("/state/")
public class UserStateServiceREST implements ResourceContainer {
  private final UserStateService userService;

  public UserStateServiceREST(UserStateService userService) {
    this.userService = userService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/status")
  @RolesAllowed("users")
  public Response online() {
    List<UserStateModel> usersOnline = userService.online();
    return Response.ok(usersOnline).build();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/status/{userId}")
  @RolesAllowed("users")
  public Response getStatus(@PathParam("userId")
  String userId) {
    UserStateModel model = userService.getUserState(userId);
    if (model == null)
      return Response.noContent().build();
    //
    return Response.ok(model).build();
  }

  @PUT
  @Path("/status")
  @RolesAllowed("users")
  public Response setStatus(
                            @QueryParam("status")
                            String status) {
    String currentUser = ConversationState.getCurrent().getIdentity().getUserId();
    userService.saveStatus(currentUser, status);
    return Response.noContent().build();
  }

  @PUT
  @Path("/status/{userId}")
  @RolesAllowed("users")
  @Deprecated
  public Response setStatus(
                            @PathParam("userId")
                            String userId,
                            @QueryParam("status")
                            String status) {
    return setStatus(status);
  }
}
