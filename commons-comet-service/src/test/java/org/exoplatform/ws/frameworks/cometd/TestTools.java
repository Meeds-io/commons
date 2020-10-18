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
package org.exoplatform.ws.frameworks.cometd;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;

import javax.ws.rs.core.MediaType;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.common.http.client.HTTPConnection;
import org.exoplatform.common.http.client.HTTPResponse;
import org.exoplatform.common.http.client.NVPair;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.ws.frameworks.cometd.transport.DelegateMessage;
import org.exoplatform.ws.frameworks.json.JsonHandler;
import org.exoplatform.ws.frameworks.json.JsonParser;
import org.exoplatform.ws.frameworks.json.impl.BeanBuilder;
import org.exoplatform.ws.frameworks.json.impl.JsonDefaultHandler;
import org.exoplatform.ws.frameworks.json.impl.JsonGeneratorImpl;
import org.exoplatform.ws.frameworks.json.impl.JsonParserImpl;
import org.exoplatform.ws.frameworks.json.value.JsonValue;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public class TestTools
{
  /**
   * Logger.
   */
  private static final Log LOG = ExoLogger.getLogger(TestTools.class);

   public static String getBaseURLCometdServer(String u)
   {
      try
      {
         URL url = new URL(u);
         HTTPConnection connection = new HTTPConnection(url);
         HTTPResponse response = connection.Get(url.getFile());
         if (response.getStatusCode() == HTTPStatus.OK)
         {
            String baseCometdURL = new String(response.getData());
            return baseCometdURL;
         }
      }
      catch (Exception e)
      {
        LOG.error(e);
      }
      return null;
   }

   public static String getUserToken(String u)
   {
      try
      {
        LOG.info("TestTools.getUserToken()" + u);
         URL url = new URL(u);// + "/rest/ext/gettoken/" + id + "/");
         HTTPConnection connection = new HTTPConnection(url);
         HTTPResponse response = connection.Get(url.getFile());
         String token = new String(response.getData());
         return token;
      }
      catch (Exception e)
      {
        LOG.error(e);
      }
      return null;
   }

   public static void sendMessage(String exoid, String channel, String message, String msgId, String baseURI)
   {
      try
      {
         DelegateMessage data = new DelegateMessage(channel, exoid, message, msgId);
         URL url = new URL(baseURI);// + "ext/sendprivatemessage/");
         JsonGeneratorImpl generatorImpl = new JsonGeneratorImpl();
         JsonValue json = generatorImpl.createJsonObject(data);
         HTTPConnection connection = new HTTPConnection(url);
         NVPair[] pairs = new NVPair[1];
         pairs[0] = new NVPair("Content-Type", MediaType.APPLICATION_JSON);
         connection.Post(url.getFile(), json.toString().getBytes(), pairs);
      }
      catch (Exception e)
      {
        LOG.error(e);
      }
   }

   public static void sendBroadcastMessage(String channel, String message, String msgId, String baseURI)
   {
      try
      {
         DelegateMessage data = new DelegateMessage(channel, message, msgId);
         JsonGeneratorImpl generatorImpl = new JsonGeneratorImpl();
         JsonValue json = generatorImpl.createJsonObject(data);
         URL url = new URL(baseURI);// + "ext/sendbroadcastmessage/");
         NVPair[] pairs = new NVPair[1];
         pairs[0] = new NVPair("Content-Type", MediaType.APPLICATION_JSON);
         HTTPConnection connection = new HTTPConnection(url);
         connection.Post(url.getFile(), json.toString().getBytes(), pairs);
      }
      catch (Exception e)
      {
        LOG.error(e);
      }
   }

   public static CMessage stringToCMessage(String incomString)
   {
      try
      {
         String tmpJ = incomString.trim();
         String jsonString = tmpJ.substring(1, tmpJ.length() - 1);
         JsonHandler jsonHandler = new JsonDefaultHandler();
         JsonParser jsonParser = new JsonParserImpl();
         InputStream inputStream = new ByteArrayInputStream(jsonString.getBytes());
         jsonParser.parse(inputStream, jsonHandler);
         JsonValue jsonValue = jsonHandler.getJsonObject();
         return (CMessage) new BeanBuilder().createObject(CMessage.class, jsonValue);
      }
      catch (Exception e)
      {
        LOG.error(e);
      }
      return null;
   }

   public static CMessages stringToCMessages(String incomString)
   {
      try
      {
         String tmpJ = incomString.trim();
         String jsonString = "{\"cometdMessages\":" + tmpJ + "}";//.substring(1,tmpJ
         // .length()-1);
         JsonHandler jsonHandler = new JsonDefaultHandler();
         JsonParser jsonParser = new JsonParserImpl();
         InputStream inputStream = new ByteArrayInputStream(jsonString.getBytes());
         jsonParser.parse(inputStream, jsonHandler);
         JsonValue jsonValue = jsonHandler.getJsonObject();
         return (CMessages) new BeanBuilder().createObject(CMessages.class, jsonValue);
      }
      catch (Exception e)
      {
        LOG.error(e);
      }
      return null;
   }

   public static Boolean release(String u)
   {
      try
      {
         URL url = new URL(u);
         HTTPConnection connection = new HTTPConnection(url);
         HTTPResponse response = connection.Get(url.getFile());
         if (response.getStatusCode() == HTTPStatus.OK)
         {
            return true;
         }
      }
      catch (Exception e)
      {
        LOG.error(e);
      }
      return false;
   }

}
