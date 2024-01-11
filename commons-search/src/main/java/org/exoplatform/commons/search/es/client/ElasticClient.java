/* 
 * Copyright (C) 2003-2015 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/ .
 */
package org.exoplatform.commons.search.es.client;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.impl.DefaultConnectionReuseStrategy;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import org.exoplatform.commons.search.es.ElasticSearchException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS Author : Thibault Clement
 * tclement@exoplatform.com 10/16/15
 */
public abstract class ElasticClient {

  public static final int             DEFAULT_MAX_HTTP_POOL_CONNECTIONS = 100;

  private static final String         ES_INDEX_CLIENT_DEFAULT           = "http://127.0.0.1:9200";

  private static final Log    LOG                     = ExoLogger.getExoLogger(ElasticClient.class);

  protected String            urlClient;
  protected HttpClient        client;
  protected ElasticIndexingAuditTrail auditTrail;

  protected ElasticClient(ElasticIndexingAuditTrail auditTrail) {
    this.urlClient = ES_INDEX_CLIENT_DEFAULT;
    if (auditTrail==null) {
      throw new IllegalArgumentException("AuditTrail is null");
    }
    this.auditTrail = auditTrail;
  }

  protected ElasticResponse sendHttpPostRequest(String url, String content) {
    ElasticResponse response;

    try {
      HttpPost httpTypeRequest = new HttpPost(url);
      if(StringUtils.isNotBlank(content)) {
        httpTypeRequest.setEntity(new StringEntity(content, ContentType.APPLICATION_JSON));
      }
      response = client.execute(httpTypeRequest, this::handleHttpResponse);
      LOG.debug("Sent request to ES:\n Method = POST \nURI =  {} \nContent = {}", url, content);
      logResultDependingOnStatusCode(url, response);
    } catch (IOException e) {
      throw new ElasticClientException(e);
    }
    return response;
  }

  protected ElasticResponse sendHttpPutRequest(String url, String content) {
    ElasticResponse response;
    try {
      HttpPut httpTypeRequest = new HttpPut(url);
      if(StringUtils.isNotBlank(content)) {
        httpTypeRequest.setEntity(new StringEntity(content, ContentType.APPLICATION_JSON));
      }
      response = client.execute(httpTypeRequest, this::handleHttpResponse);
      LOG.debug("Sent request to ES:\n Method = PUT \nURI =  '{}' \nContent = '{}'", url, content);
      logResultDependingOnStatusCode(url, response);
    } catch (IOException e) {
      throw new ElasticClientException(e);
    }
    return response;
  }

  protected ElasticResponse sendHttpDeleteRequest(String url) {
    ElasticResponse response;

    try {
      HttpDelete httpDeleteRequest = new HttpDelete(url);
      response = client.execute(httpDeleteRequest, this::handleHttpResponse);
      LOG.debug("Sent request to ES:\n Method = DELETE \nURI =  {}", url);
      logResultDependingOnStatusCode(url, response);
    } catch (IOException e) {
      throw new ElasticClientException(e);
    }
    return response;
  }

  protected ElasticResponse sendHttpGetRequest(String url) {
    ElasticResponse response;

    try {
      HttpGet httpGetRequest = new HttpGet(url);
      response = client.execute(httpGetRequest, this::handleHttpResponse);
      LOG.debug("Sent request to ES:\n Method = GET \nURI =  {}", url);
    } catch (IOException e) {
      throw new ElasticClientException(e);
    }
    return response;
  }

  
  protected ElasticResponse sendHttpHeadRequest(String url) {
    ElasticResponse response;

    try {
      HttpHead httpHeadRequest = new HttpHead(url);
      response = client.execute(httpHeadRequest, this::handleHttpResponse);
      LOG.debug("Sent request to ES:\n Method = HEAD \nURI =  {}", url);
    } catch (IOException e) {
      throw new ElasticClientException(e);
    }
    return response;
  }

  protected void initHttpClient() {
    this.client = getHttpClient();
  }

  protected HttpClient getHttpClient() {
    // Check if Basic Authentication need to be used
    HttpClientConnectionManager clientConnectionManager = getClientConnectionManager();
    HttpClientBuilder httpClientBuilder = HttpClientBuilder
        .create().disableAutomaticRetries()
        .setConnectionManager(clientConnectionManager)
        .setConnectionReuseStrategy(new DefaultConnectionReuseStrategy());
    if (StringUtils.isNotBlank(getEsUsernameProperty())) {
      BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
      credsProvider.setCredentials(
              new AuthScope(null, -1),
              new UsernamePasswordCredentials(getEsUsernameProperty(),
                                              getEsPasswordProperty().toCharArray()));

      HttpClient httpClient = httpClientBuilder
          .setDefaultCredentialsProvider(credsProvider)
          .build();
      LOG.debug("Basic authentication for ES activated with username = {}",
                getEsUsernameProperty());
      return httpClient;
    } else {
      LOG.debug("Basic authentication for ES not activated");
      return httpClientBuilder.build();
    }
  }

  /**
   * Handle Http response receive from ES Log an INFO if the return status code
   * is 2xx Log an ERROR if the return code is different from 2xx
   *
   * @param httpResponse The Http Response to handle
   */
  private ElasticResponse handleHttpResponse(ClassicHttpResponse httpResponse) throws IOException {
    final HttpEntity entity = httpResponse.getEntity();
    int statusCode = httpResponse.getCode();
    try {
      return new ElasticResponse(EntityUtils.toString(entity), statusCode);
    } catch (ParseException e) {
      throw new ElasticSearchException("Error while parsing http response with code " + statusCode + " and response " + entity,
                                       e);
    }
  }

  private void logResultDependingOnStatusCode(String url, ElasticResponse response) {
    if (ElasticIndexingAuditTrail.isError(response.getStatusCode())) {
      LOG.error("Error when trying to send request to ES. Url: {}, StatusCode: {}, Message: {}",
          url,
          response.getStatusCode(),
          response.getMessage());
    } else {
      LOG.debug("Success request to ES. Url: {}, StatusCode: {}, Message: {}",
          url,
          response.getStatusCode(),
          response.getMessage());
    }
  }

  protected abstract String getEsUsernameProperty();

  protected abstract String getEsPasswordProperty();

  protected abstract HttpClientConnectionManager getClientConnectionManager();

  protected int getMaxConnections() {
    return DEFAULT_MAX_HTTP_POOL_CONNECTIONS;
  }

}
