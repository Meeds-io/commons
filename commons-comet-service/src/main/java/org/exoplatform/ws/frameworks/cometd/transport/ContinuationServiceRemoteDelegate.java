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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.common.http.client.HTTPConnection;
import org.exoplatform.common.http.client.HTTPResponse;
import org.exoplatform.common.http.client.ModuleException;
import org.exoplatform.common.http.client.NVPair;
import org.exoplatform.common.http.client.ProtocolNotSuppException;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.ws.frameworks.cometd.loadbalancer.LoadBalancer;
import org.exoplatform.ws.frameworks.json.impl.JsonException;
import org.exoplatform.ws.frameworks.json.impl.JsonGeneratorImpl;
import org.exoplatform.ws.frameworks.json.value.JsonValue;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */

public class ContinuationServiceRemoteDelegate implements ContinuationServiceDelegate
{
   /**
    * Class logger.
    */
   private static final Log LOG = ExoLogger.getLogger(ContinuationServiceRemoteDelegate.class);

   private final LoadBalancer loadBalancer;

   /** 
    * The name of the rest context
    */
   private final String restContextName;

   public ContinuationServiceRemoteDelegate(LoadBalancer loadBalancer, ExoContainerContext context)
   {
      this.loadBalancer = loadBalancer;
      this.restContextName = context.getRestContextName();
   }

   /**
    * @param exoID the id of client.
    * @return base URL of cometd server for user with exoID.
    */
   private String getBaseCometdURL(String exoID)
   {
      return loadBalancer.connection(exoID);
   }

   /**
    * {@inheritDoc}
    */
   public Boolean isSubscribed(String exoID, String channel)
   {
      try
      {
         String baseURICometdServer = getBaseCometdURL(exoID);
         URL url =
            new URL(baseURICometdServer + "/" + restContextName + "/issubscribed/" + exoID + "/" + channel + "/");
         HTTPConnection connection = new HTTPConnection(url);
         HTTPResponse response = connection.Get(url.getFile());
         String bol = new String(response.getData());
         if (LOG.isDebugEnabled())
            LOG.debug("Check user " + exoID + " subscription to channel " + channel);
         return new Boolean(bol);
      } 
      catch (MalformedURLException e) 
      {
         LOG.error("Malformed URL: ", e);
      } 
      catch (ProtocolNotSuppException e) 
      {
         LOG.error("Protocol is not supported: ", e);
      } 
      catch (IOException e) 
      {
         LOG.error("IO exception: ", e);
      } 
      catch (ModuleException e) 
      {
         LOG.error("error when getting response: ", e);
      }
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public void sendMessage(String exoID, String channel, String message, String msgId)
   {
      try
      {
         String baseURICometdServer = getBaseCometdURL(exoID);
         URL url = new URL(baseURICometdServer + "/" + restContextName + "/continuation/sendprivatemessage/");
         HTTPConnection connection = new HTTPConnection(url);
         DelegateMessage transportData = new DelegateMessage(channel, exoID, message, msgId);
         JsonGeneratorImpl generatorImpl = new JsonGeneratorImpl();
         JsonValue json = generatorImpl.createJsonObject(transportData);
         NVPair[] pairs = new NVPair[1];
         pairs[0] = new NVPair("Content-Type", MediaType.APPLICATION_JSON);
         HTTPResponse response = connection.Post(url.getFile(), json.toString(), pairs);
         if (response.getStatusCode() == HTTPStatus.OK)
         {
            if (LOG.isDebugEnabled())
               LOG.debug("Send private message : " + message + " to client " + exoID + " by cahnnel " + channel
                  + " success");
         }
         else
         {
            if (LOG.isDebugEnabled())
               LOG.debug("Send private message : " + message + " to client " + exoID + " by cahnnel " + channel
                  + " fail!");
         }
      } 
      catch (MalformedURLException e) 
      {
         LOG.error("Malformed URL: ", e);
      } 
      catch (ProtocolNotSuppException e) 
      {
         LOG.error("Protocol is not supported: ", e);
      } 
      catch (JsonException e) 
      {
         LOG.error("error when create JSON object: ", e);
      } 
      catch (IOException e) 
      {
         LOG.error("IO exception: ", e);
      } 
      catch (ModuleException e) 
      {
         LOG.error("error when getting response: ", e);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void sendBroadcastMessage(String channel, String message, String msgId)
   {
      try
      {
         List<String> us = getCometdURLsByChannel(channel);
         if (us != null)
         {
            for (String u : us)
            {
               URL url = new URL(u + "/" + restContextName + "/continuation/sendbroadcastmessage/");
               DelegateMessage transportData = new DelegateMessage(channel, message, msgId);
               JsonGeneratorImpl generatorImpl = new JsonGeneratorImpl();
               JsonValue json = generatorImpl.createJsonObject(transportData);
               HTTPConnection connection = new HTTPConnection(url);
               NVPair[] pairs = new NVPair[1];
               pairs[0] = new NVPair("Content-Type", MediaType.APPLICATION_JSON);
               HTTPResponse response = connection.Post(url.getFile(), json.toString(), pairs);
               if (response.getStatusCode() == HTTPStatus.OK)
               {
                  if (LOG.isDebugEnabled())
                     LOG.debug("Send public message : " + message + " to channel " + channel + " success");
               }
               else
               {
                  if (LOG.isDebugEnabled())
                     LOG.debug("Send public message : " + message + " to channel " + channel + " fail!");
               }
            }
         }
      } 
      catch (MalformedURLException e) 
      {
         LOG.error("Malformed URL: ", e);
      } 
      catch (ProtocolNotSuppException e) 
      {
         LOG.error("Protocol is not supported: ", e);
      } 
      catch (JsonException e) 
      {
         LOG.error("error when create JSON object: ", e);
      } 
      catch (IOException e) 
      {
         LOG.error("IO exception: ", e);
      } 
      catch (ModuleException e) 
      {
         LOG.error("error when getting response: ", e);
      }
   }

   /**
    * @param channel id of channel.
    * @return Array of URL of cometd server there exist users subscribed on channel
    */
   private List<String> getCometdURLsByChannel(String channel)
   {
      try
      {
         Collection<String> curls = loadBalancer.getAliveNodesURL();
         List<String> urls = new ArrayList<String>();
         for (String curl : curls)
         {
            String u = new String(curl + "/" + restContextName + "/continuation/haschannel?channel=" + channel);
            URL url = new URL(u);
            HTTPConnection connection = new HTTPConnection(url);
            HTTPResponse response = connection.Get(url.getFile());
            boolean b = Boolean.parseBoolean(new String(response.getData()));
            if (b)
               urls.add(curl);
         }
         return urls;
      }
      catch (MalformedURLException e) 
      {
         LOG.error("Malformed URL: ", e);
      } 
      catch (ProtocolNotSuppException e) 
      {
         LOG.error("Protocol is not supported: ", e);
      } 
      catch (IOException e) 
      {
         LOG.error("IO exception: ", e);
      } 
      catch (ModuleException e) 
      {
         LOG.error("error when getting response: ", e);
      }
      return null;
   }

 
}
