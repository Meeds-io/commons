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
package org.exoplatform.ws.frameworks.cometd.loadbalancer;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
@Path("/balancer/")
public class RESTLoadBalancerService
   implements ResourceContainer
{
   /**
    * Class logger.
    */
   private static final Log LOG = ExoLogger.getLogger(RESTLoadBalancerService.class);

   /**
    * 
    */
   private LoadBalancer balancer;

   /**
    * @param balancer the strategy for load balancing of cometd cluster.
    */
   public RESTLoadBalancerService(LoadBalancer balancer)
   {
      this.balancer = balancer;
   }

   /**
    * @param exoid the client id.
    * @return base URL of cometd server in cluster for user.
    */
   @GET
   @Path("/cometdurl/{exoid}/")
   @Produces(MediaType.TEXT_PLAIN)
   public Response getCometdURL(@PathParam("exoid") String exoid)
   {
      String url = balancer.connection(exoid);
      if (!(url.equals("") || url.length() == 0))
      {
         if (LOG.isDebugEnabled())
            LOG.debug("Client with exoid " + exoid + " get URL " + url + " for cometd connection");
         return Response.ok(url).build();
      }
      else
      {
         if (LOG.isDebugEnabled())
            LOG.debug("All nodes are owerflow client with exoid " + exoid + " can't connect to cometd!");
         return Response.status(Status.FORBIDDEN).entity("Owerflow!").type(MediaType.TEXT_PLAIN).build();
      }
   }

   /**
    * @param exoid the client id.
    * @return OK if release successful.
    */
   @GET
   @Path("/releasecometd/{exoid}/")
   public Response release(@PathParam("exoid") String exoid)
   {
      if (balancer.release(exoid))
         return Response.ok().build();
      else
         return Response.serverError().build();
   }
   
    
}
