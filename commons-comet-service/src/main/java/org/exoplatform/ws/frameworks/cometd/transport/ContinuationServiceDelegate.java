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
package org.exoplatform.ws.frameworks.cometd.transport;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public interface ContinuationServiceDelegate
{

   /**
    * Send individual message to client by channel.
    * 
    * @param channel the channel that listen client.
    * @param exoID the client id.
    * @param message the message (JSON format).
    * @param msgId the id of message.
    */
   void sendMessage(String exoID, String channel, String message, String msgId);

   /**
    * @param exoID the client id.
    * @param channel the channel id
    * @return true if client subscribed on channel.
    */
   Boolean isSubscribed(String exoID, String channel);

   /**
    * Send public message to all that subscribed on channel.
    * 
    * @param channel the channel id.
    * @param message the message (JSON format).
    * @param msgId the id of message.
    */
   void sendBroadcastMessage(String channel, String message, String msgId);
   
  
}
