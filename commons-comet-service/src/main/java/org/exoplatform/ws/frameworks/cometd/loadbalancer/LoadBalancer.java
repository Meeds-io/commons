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

import java.util.List;

/**
 * Created by The eXo Platform SAS.
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public interface LoadBalancer
{

   /**
    * @param exoId the id of client.
    * @return URL of cometd node. 
    */
   String connection(String exoId);

   /**
    * Release connection on the node there client connected.
    * @param exoId client id.
    * @return true if release successful
    */
   boolean release(String exoId);

   /**
    * @return URLs of all cometd nodes.
    */
   List<String> getAliveNodesURL();

   /**
    * Add new node for cometd connection.
    * @param node the new node in cluster.
    */
   void addNode(Node node);

   /**
    * Remove cometd node.
    * @param id the ID of cometd node.
    */
   void removeNode(String id);

   //  List<Node> getNodes();

   //  List<String> getClientsNode(String url);

}
