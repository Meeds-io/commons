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
package org.exoplatform.ws.frameworks.cometd.transport;

import javax.annotation.security.DenyAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;

@Path("/continuation/")
public class RESTContinuationService
   implements ResourceContainer
{
   /**
    * Class logger.
    */
   private static final Log LOG = ExoLogger.getLogger(RESTContinuationService.class);

   private final ContinuationService continuation;

   public RESTContinuationService(ContinuationService continuationService)
   {
      this.continuation = continuationService;
   }

   /**
    * @param exoID the id of client, which must be the signed-in user's own id.
    * @return userToken for user, not storable by caches; 403 when exoID is
    *         another user's id
    */
   @GET
   @Path("/gettoken/{exoID}/")
   @Produces(MediaType.TEXT_PLAIN)
   @RolesAllowed("users")
   public Response getToken(@PathParam("exoID") String exoID)
   {
      if (!isSignedInUser(exoID))
         return Response.status(Response.Status.FORBIDDEN).build();
      String token = continuation.getUserToken(exoID);
      if (LOG.isDebugEnabled())
         LOG.debug("Client with exoId " + exoID + " got its token");
      return Response.ok(token, MediaType.TEXT_PLAIN).header("Content-Length", Integer.toString(token.length()))
               .cacheControl(noStore()).build();
   }

   /**
    * @param exoID the id of client, which must be the signed-in user's own id.
    * @param channel the id of channel
    * @return true if client subscribed on channel else false, not storable by
    *         caches; 403 when exoID is another user's id
    */
   @GET
   @Path("/issubscribed/{exoID}/")
   @Produces(MediaType.TEXT_PLAIN)
   @RolesAllowed("users")
   public Response isSubscribed(@PathParam("exoID") String exoID, @QueryParam("channel") String channel)
   {
      if (!isSignedInUser(exoID))
         return Response.status(Response.Status.FORBIDDEN).build();
      Boolean b = continuation.isSubscribe(exoID, channel);
      if (LOG.isDebugEnabled())
         LOG.debug("Is subcribed client " + exoID + " on channel " + channel + " " + b);
      return Response.ok(b.toString(), MediaType.TEXT_PLAIN).cacheControl(noStore()).build();
   }

   /**
    * @param channel the id of channel
    * @return true if channel exist else false, not storable by caches
    */
   @GET
   @Path("/haschannel/")
   @Produces(MediaType.TEXT_PLAIN)
   @DenyAll
   public Response hasChannel(@QueryParam("channel") String channel)
   {
      Boolean b = continuation.hasChannel(channel);
      if (LOG.isDebugEnabled())
         LOG.debug("Has channel " + channel + " " + b);
      return Response.ok(b.toString(), MediaType.TEXT_PLAIN).header("Content-Length",
               Integer.toString(b.toString().length())).cacheControl(noStore()).build();
   }

   /**
    * @param data content message, clientId, channel. 
    * @return Response with status 
    */
   @POST
   @Path("/sendprivatemessage/")
   @Consumes(MediaType.APPLICATION_JSON)
   @DenyAll
   public Response sendMessage(DelegateMessage data)
   {
      continuation.sendMessage(data.getExoId(), data.getChannel(), data.getMessage(), data.getId());
      if (LOG.isDebugEnabled())
         LOG.debug("Send private message " + data.getMessage() + " on channel " + data.getChannel() + " to client "
                  + data.getExoId());
      return Response.ok().build();
   }

   /**
    * @param data content message, clientId, channel.
    * @return Response with status
    */
   @POST
   @Path("/sendbroadcastmessage/")
   @Consumes(MediaType.APPLICATION_JSON)
   @DenyAll
   public Response sendBroadcastMessage(DelegateMessage data)
   {
      continuation.sendBroadcastMessage(data.getChannel(), data.getMessage(), data.getId());
      if (LOG.isDebugEnabled())
         LOG.debug("Send broadcast message " + data.getMessage() + " on channel " + data.getChannel());
      return Response.ok().build();
   }

   private static boolean isSignedInUser(String exoID)
   {
      ConversationState state = ConversationState.getCurrent();
      return exoID != null && state != null && state.getIdentity() != null
               && exoID.equals(state.getIdentity().getUserId());
   }

   private static CacheControl noStore()
   {
      CacheControl cacheControl = new CacheControl();
      cacheControl.setPrivate(true);
      cacheControl.setNoStore(true);
      return cacheControl;
   }

}
