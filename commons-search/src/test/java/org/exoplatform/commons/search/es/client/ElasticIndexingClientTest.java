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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;

import org.exoplatform.commons.utils.PropertyManager;
import org.mockito.stubbing.Answer;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 9/1/15
 */
@RunWith(MockitoJUnitRunner.class)
public class ElasticIndexingClientTest {
  private ElasticIndexingClient elasticIndexingClient;
  @Mock
  private HttpClient httpClient;
  @Mock
  private ElasticIndexingAuditTrail auditTrail;
  @Captor
  private ArgumentCaptor<HttpPut> httpPutRequestCaptor;
  @Captor
  private ArgumentCaptor<HttpPost> httpPostRequestCaptor;
  @Captor
  private ArgumentCaptor<HttpDelete> httpDeleteRequestCaptor;
  @Captor
  private ArgumentCaptor<HttpGet> httpGetRequestCaptor;


  @Before
  public void initMock() throws IOException {
    MockitoAnnotations.initMocks(this);
    PropertyManager.setProperty("exo.es.index.server.url", "http://127.0.0.1:9200");
    elasticIndexingClient = new ElasticIndexingClient(auditTrail);
    elasticIndexingClient.client = httpClient;
    elasticIndexingClient.resetMaxConnections();
  }

  @Test
  public void sendCreateIndexRequest_IfCreateNewIndex_requestShouldBeSentToElastic() throws IOException {
    //Given
    initClientMock(404, "Not Found", 200, "Success");
    //When
    elasticIndexingClient.sendCreateIndexRequest("index", "fakeSettings", "fakeMappings");
    //Then
    verify(httpClient, times(2)).execute(any(HttpRequestBase.class));
  }

  @Test
  public void sendCUDRequest_IfCUDOperation_bulkRequestShouldBeSentToElastic() throws IOException {
    //Given
    String response = "{\"took\":15," +
        "\"errors\":true," +
        "\"items\":[" +
        "{\"index\":{\"_index\":\"test\",\"_type\":\"type1\",\"_id\":\"1\",\"_version\":3,\"status\":200}}" +
        "]}";
    initClientMock(999, "", 200, response);
    //When
    elasticIndexingClient.sendCUDRequest("FakeBulkRequest");
    //Then
    verify(httpClient).execute(httpPostRequestCaptor.capture());
    assertEquals("http://127.0.0.1:9200/_bulk", httpPostRequestCaptor.getValue().getURI().toString());
    assertEquals("FakeBulkRequest", IOUtils.toString(httpPostRequestCaptor.getValue().getEntity().getContent()));

  }

  @Test
  public void sendBulkRequest_forEveryDocument_callAuditTrail() throws IOException {
    //Given
    String response = "{\"took\":15," +
        "\"errors\":true," +
        "\"items\":[" +
        "{\"index\":{\"_index\":\"test\",\"_type\":\"type1\",\"_id\":\"1\",\"_version\":3,\"status\":200}}," +
        "{\"delete\":{\"_index\":\"test\",\"_type\":\"type1\",\"_id\":\"2\",\"_version\":1,\"status\":404,\"found\":false}}," +
        "{\"create\":{\"_index\":\"test\",\"_type\":\"type1\",\"_id\":\"3\",\"status\":409,\"error\":{\"reason\":\"DocumentAlreadyExistsException[[test][4] [type1][3]: document already exists]\"}}}," +
        "{\"update\":{\"_index\":\"index1\",\"_type\":\"type1\",\"_id\":\"1\",\"status\":404,\"error\":{\"reason\":\"DocumentMissingException[[index1][-1] [type1][1]: document missing]\"}}}" +
        "]}";
    initClientMock(999, "", 200, response);
    when(auditTrail.isFullLogEnabled()).thenReturn(true);
    //When
    elasticIndexingClient.sendCUDRequest("myBulk");
    //Then
    verify(auditTrail).isFullLogEnabled();
    verify(auditTrail).logAcceptedBulkOperation(eq("index"), eq("1"), eq("test"), eq(HttpStatus.SC_OK), isNull(String.class), anyLong());
    verify(auditTrail).logRejectedDocumentBulkOperation(eq("delete"), eq("2"), eq("test"), eq(HttpStatus.SC_NOT_FOUND), isNull(String.class), anyLong());
    verify(auditTrail).logRejectedDocumentBulkOperation(eq("create"), eq("3"), eq("test"), eq(HttpStatus.SC_CONFLICT), eq("DocumentAlreadyExistsException[[test][4] [type1][3]: document already exists]"), anyLong());
    verify(auditTrail).logRejectedDocumentBulkOperation(eq("update"), eq("1"), eq("index1"), eq(HttpStatus.SC_NOT_FOUND), eq("DocumentMissingException[[index1][-1] [type1][1]: document missing]"), anyLong());
    verifyNoMoreInteractions(auditTrail);
  }

  @Test
  public void createIndex_callAuditTrail() throws IOException {
    //Given
    String response = "{\"error\":\"IndexAlreadyExistsException[[profile] already exists]\",\"status\":400}";
    initClientMock(404, "Not Found", 400, response);
    //When
    elasticIndexingClient.sendCreateIndexRequest("profile", "mySettings", "someMappings");
    //Then
    verify(auditTrail).audit(eq("create_index"), isNull(String.class), eq("profile"), eq(HttpStatus.SC_BAD_REQUEST), eq("{\"error\":\"IndexAlreadyExistsException[[profile] already exists]\",\"status\":400}"), anyLong());
    verifyNoMoreInteractions(auditTrail);
  }

  @Test
  public void sendBulkRequest_fullLogNotEnabled_auditTrailNotCalled() throws IOException {
    //Given
    String response = "{\"took\":15," +
        "\"errors\":false," +
        "\"items\":[" +
        "{\"index\":{\"_index\":\"test\",\"_type\":\"type1\",\"_id\":\"1\",\"_version\":3,\"status\":200}}" +
        "]}";
    initClientMock(999, "", 200, response);
    when(auditTrail.isFullLogEnabled()).thenReturn(false);
    //When
    elasticIndexingClient.sendCUDRequest("myBulk");
    //Then
    verify(auditTrail).isFullLogEnabled();
    verifyNoMoreInteractions(auditTrail);
  }

  @Test(expected = ElasticClientAuthenticationException.class)
  public void createType_notAuthenticated_throwsException() throws IOException {
    //Given
    initClientMock(999, "", 401, "Authentication Required");
    //When
    elasticIndexingClient.sendCUDRequest("myBulk");
    //Then
    fail("ElasticClientAuthenticationException expected");
  }

  @Test
  public void createIndex_indexAlreadyExists_noRequestIsSentToES() throws IOException {
    //Given
    initClientMock(200, "{my existing Mapping}", 999, "");
    //When
    elasticIndexingClient.sendCreateIndexRequest("myIndex", "mySettings", "someMappings");
    //Then
    verify(httpClient, times(1)).execute(httpGetRequestCaptor.capture());
    assertEquals("http://127.0.0.1:9200/myIndex", httpGetRequestCaptor.getValue().getURI().toString());
    verifyNoMoreInteractions(httpClient);
  }

  @Test
  public void createIndex_indexNotExists_requestIsSentToES() throws IOException {
    //Given
    initClientMock(404, ElasticIndexingClient.EMPTY_JSON, 200, "Success");
    //When
    elasticIndexingClient.sendCreateIndexRequest("myIndex", "mySettings", "someMappings");
    //Then
    verify(httpClient, times(2)).execute(httpPutRequestCaptor.capture());
    assertEquals("http://127.0.0.1:9200/myIndex", httpPutRequestCaptor.getValue().getURI().toString());
    assertEquals("{\"settings\": mySettings, \"mappings\": someMappings}", IOUtils.toString(httpPutRequestCaptor.getValue().getEntity().getContent()));
    verifyNoMoreInteractions(httpClient);
  }

  @Test
  public void shouldUseDefaultHttpMaxConnectionsPerRouteWhenNoConfiguration() {
    //Given
    PropertyManager.setProperty("exo.es.index.http.connections.max", "");

    //When
    PoolingHttpClientConnectionManager clientConnectionManager = (PoolingHttpClientConnectionManager) elasticIndexingClient.getClientConnectionManager();

    //Then
    assertEquals(ElasticClient.DEFAULT_MAX_HTTP_POOL_CONNECTIONS, clientConnectionManager.getDefaultMaxPerRoute());
  }

  @Test
  public void shouldUseDefaultHttpMaxConnectionsPerRouteWhenConfiguredValueIsNotANumber() {
    //Given
    PropertyManager.setProperty("exo.es.index.http.connections.max", "string");

    //When
    PoolingHttpClientConnectionManager clientConnectionManager = (PoolingHttpClientConnectionManager) elasticIndexingClient.getClientConnectionManager();

    //Then
    assertEquals(ElasticClient.DEFAULT_MAX_HTTP_POOL_CONNECTIONS, clientConnectionManager.getDefaultMaxPerRoute());

    PropertyManager.setProperty("exo.es.index.http.connections.max", "");
  }

  @Test
  public void shouldUseDefaultHttpMaxConnectionsPerRouteWhenConfiguredValueIsNotAPositiveNumber() {
    //Given
    PropertyManager.setProperty("exo.es.index.http.connections.max", "0");

    //When
    PoolingHttpClientConnectionManager clientConnectionManager = (PoolingHttpClientConnectionManager) elasticIndexingClient.getClientConnectionManager();

    //Then
    assertEquals(ElasticClient.DEFAULT_MAX_HTTP_POOL_CONNECTIONS, clientConnectionManager.getDefaultMaxPerRoute());

    PropertyManager.setProperty("exo.es.index.http.connections.max", "");
  }

  @Test
  public void shouldUseConfiguredHttpMaxConnectionsPerRouteWhenConfiguredValueIsAPositiveNumber() {
    //Given
    PropertyManager.setProperty("exo.es.index.http.connections.max", "10");

    //When
    PoolingHttpClientConnectionManager clientConnectionManager = (PoolingHttpClientConnectionManager) elasticIndexingClient.getClientConnectionManager();

    //Then
    assertEquals(10, clientConnectionManager.getDefaultMaxPerRoute());

    PropertyManager.setProperty("exo.es.index.http.connections.max", "");
  }


  private void initClientMock(Integer getOrHeadStatus, String getOrHeadContent, Integer postStatus, String postContent) throws IOException {
    // Get request
    final HttpResponse getOrHeadResponse = mock(HttpResponse.class);
    StatusLine getOrHeadStatusLine = mock(StatusLine.class);
    HttpEntity getOrHeadHttpEntity = mock(HttpEntity.class);
    when(getOrHeadResponse.getStatusLine()).thenReturn(getOrHeadStatusLine);
    when(getOrHeadStatusLine.getStatusCode()).thenReturn(getOrHeadStatus);
    when(getOrHeadResponse.getEntity()).thenReturn(getOrHeadHttpEntity);
    when(getOrHeadHttpEntity.getContent()).thenReturn(IOUtils.toInputStream(getOrHeadContent, "UTF-8"));
    // Post request
    final HttpResponse postResponse = mock(HttpResponse.class);
    StatusLine postStatusLine = mock(StatusLine.class);
    HttpEntity postHttpEntity = mock(HttpEntity.class);
    when(postResponse.getStatusLine()).thenReturn(postStatusLine);
    when(postStatusLine.getStatusCode()).thenReturn(postStatus);
    when(postResponse.getEntity()).thenReturn(postHttpEntity);
    when(postHttpEntity.getContent()).thenReturn(IOUtils.toInputStream(postContent, "UTF-8"));
    // Mock setting
    when(httpClient.execute(any(HttpRequestBase.class))).thenAnswer(new Answer<HttpResponse>() {
      @Override
      public HttpResponse answer(InvocationOnMock invocation) throws Throwable {
        Object[] args = invocation.getArguments();
        if (args[0] instanceof HttpGet || args[0] instanceof HttpHead) {
          return getOrHeadResponse;
        }
        return postResponse;
      }
    });
  }
}

