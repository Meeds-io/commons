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
package org.exoplatform.commons.search.es.client;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com
 * 9/14/15
 */
public class ElasticSearchingClient extends ElasticClient {
  private static final Log LOG = ExoLogger.getLogger(ElasticSearchingClient.class);

  private static final String ES_SEARCH_CLIENT_PROPERTY_NAME = "exo.es.search.server.url";
  private static final String ES_SEARCH_CLIENT_PROPERTY_USERNAME = "exo.es.search.server.username";
  private static final String ES_SEARCH_CLIENT_PROPERTY_PASSWORD = "exo.es.search.server.password";
  private static final String ES_SEARCH_CLIENT_PROPERTY_MAX_CONNECTIONS = "exo.es.search.http.connections.max";


  public ElasticSearchingClient(ElasticIndexingAuditTrail auditTrail) {
    super(auditTrail);
    //Get url client from exo global properties
    if (StringUtils.isNotBlank(PropertyManager.getProperty(ES_SEARCH_CLIENT_PROPERTY_NAME))) {
      this.urlClient = PropertyManager.getProperty(ES_SEARCH_CLIENT_PROPERTY_NAME);
      LOG.info("Using {} as Searching URL", this.urlClient);
    } else {
      LOG.info("Using default as Searching URL");
    }
  }

  public String sendRequest(String esQuery, String index, String type) {
    long startTime = System.currentTimeMillis();
    StringBuilder url = new StringBuilder();
    url.append(urlClient);
    if (StringUtils.isNotBlank(index)) {
      url.append("/" + index);
      if (StringUtils.isNotBlank(type)) url.append("/" + type);
    }
    url.append("/_search");
    ElasticResponse elasticResponse = sendHttpPostRequest(url.toString(), esQuery);
    String response = elasticResponse.getMessage();
    int statusCode = elasticResponse.getStatusCode();
    if (ElasticIndexingAuditTrail.isError(statusCode) || StringUtils.isBlank(response)) {
      if(StringUtils.isBlank(response)) {
        response = "Empty response was sent by ES";
      }
      auditTrail.logRejectedSearchOperation(ElasticIndexingAuditTrail.SEARCH_TYPE, index, type, statusCode, response, (System.currentTimeMillis() - startTime));
    } else {
      JSONParser parser = new JSONParser();
      Map json = null;
      try {
        json = (Map)parser.parse(response);
      } catch (ParseException e) {
        auditTrail.logRejectedSearchOperation(ElasticIndexingAuditTrail.SEARCH_TYPE,
                                              index,
                                              type,
                                              statusCode,
                                              "Error parsing response to JSON, content = " + response,
                                              (System.currentTimeMillis() - startTime));
        throw new IllegalStateException("Error occured while requesting ES HTTP code: '" + statusCode
            + "', Error parsing response to JSON format, content = '" + response + "'", e );
      }
      Long status = json.get("status") == null ? null : (Long) json.get("status");
      String error = json.get("error") == null ? null : (String) ((JSONObject) json.get("error")).get("reason");
      Integer httpStatusCode = status == null ? null : status.intValue();
      if (ElasticIndexingAuditTrail.isError(httpStatusCode)) {
        auditTrail.logRejectedSearchOperation(ElasticIndexingAuditTrail.SEARCH_TYPE, index, type, httpStatusCode, error, (System.currentTimeMillis() - startTime));
        throw new IllegalStateException("Error occured while requesting ES HTTP error code: '" + statusCode + "', HTTP response: '"
            + response + "'");
      }
      if (auditTrail.isFullLogEnabled()) {
        auditTrail.logAcceptedSearchOperation(ElasticIndexingAuditTrail.SEARCH_TYPE, index, type, statusCode, response, (System.currentTimeMillis() - startTime));
      }
    }
    return response;
  }

  @Override
  protected String getEsUsernameProperty() {
    return PropertyManager.getProperty(ES_SEARCH_CLIENT_PROPERTY_USERNAME);
  }

  @Override
  protected String getEsPasswordProperty() {
    return PropertyManager.getProperty(ES_SEARCH_CLIENT_PROPERTY_PASSWORD);
  }

  @Override
  protected HttpClientConnectionManager getClientConnectionManager() {
    PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
    String maxConnectionsProperty = PropertyManager.getProperty(ES_SEARCH_CLIENT_PROPERTY_MAX_CONNECTIONS);
    if(StringUtils.isNotBlank(maxConnectionsProperty)) {
      maxConnectionsProperty = maxConnectionsProperty.trim();
      if(StringUtils.isNumeric(maxConnectionsProperty)) {
        int maxConnections = Integer.parseInt(maxConnectionsProperty);
        if(maxConnections > 0) {
          connectionManager.setDefaultMaxPerRoute(maxConnections);
        } else {
          LOG.warn(ES_SEARCH_CLIENT_PROPERTY_MAX_CONNECTIONS + " value is not a positive number : " + maxConnectionsProperty +
                  ". Using default HTTP max connections (" + connectionManager.getDefaultMaxPerRoute() + ").");
        }
      } else {
        LOG.warn(ES_SEARCH_CLIENT_PROPERTY_MAX_CONNECTIONS + " value is not a valid number : " + maxConnectionsProperty +
                ". Using default HTTP max connections (" + connectionManager.getDefaultMaxPerRoute() + ").");
      }
    }
    return connectionManager;
  }

}
