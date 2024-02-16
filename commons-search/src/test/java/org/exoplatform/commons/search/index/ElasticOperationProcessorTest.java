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
package org.exoplatform.commons.search.index;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.commons.search.dao.IndexingOperationDAO;
import org.exoplatform.commons.search.domain.*;
import org.exoplatform.commons.search.es.client.*;
import org.exoplatform.commons.search.index.impl.ElasticIndexingOperationProcessor;
import org.exoplatform.commons.search.index.impl.ElasticIndexingServiceConnector;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 9/1/15
 */
@RunWith(MockitoJUnitRunner.class)
public class ElasticOperationProcessorTest {

  //Naming Convention Used: methodUnderTest_conditionEncounter_resultExpected

  private ElasticIndexingOperationProcessor elasticIndexingOperationProcessor;

  @Mock
  private IndexingOperationDAO indexingOperationDAO;
  @Mock
  private ElasticIndexingAuditTrail auditTrail;
  @Mock
  private ElasticIndexingClient elasticIndexingClient;

  @Mock
  private ElasticIndexingServiceConnector elasticIndexingServiceConnector;

  @Mock
  private ElasticContentRequestBuilder elasticContentRequestBuilder;

  private EntityManagerService entityManagerService;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);

    // Make sure a portal container is started
    PortalContainer.getInstance();

    entityManagerService = new EntityManagerService();
    entityManagerService.startRequest(null);
    InitParams initParams = new InitParams();
    ValueParam param = new ValueParam();
    param.setName("es.version");
    param.setValue("8.6");
    initParams.addParameter(param);
    elasticIndexingOperationProcessor = new ElasticIndexingOperationProcessor(indexingOperationDAO, elasticIndexingClient, elasticContentRequestBuilder, auditTrail, entityManagerService, initParams);
    initElasticServiceConnector();
    initElasticContentRequestBuilder();
    elasticIndexingOperationProcessor.setInitialized(true);
  }

  private void initElasticServiceConnector() {
    lenient().when(elasticIndexingServiceConnector.getReplicas()).thenReturn(1);
    lenient().when(elasticIndexingServiceConnector.getShards()).thenReturn(5);
    lenient().when(elasticIndexingServiceConnector.getConnectorName()).thenReturn("post");
    when(elasticIndexingServiceConnector.getMapping()).thenReturn("anyMapping");
  }

  private void initElasticContentRequestBuilder() {
    when(elasticContentRequestBuilder.getCreateIndexRequestContent(eq(elasticIndexingServiceConnector))).thenReturn("SomeIndexSettings");
    when(elasticContentRequestBuilder.getCreateDocumentRequestContent(eq(elasticIndexingServiceConnector), anyString())).thenReturn("{ \"workspace\": \"collaboration\", \"title\": \"doc1.pdf\", \"file\": \"xxxx\"}");
    when(elasticContentRequestBuilder.getUpdateDocumentRequestContent(eq(elasticIndexingServiceConnector), anyString())).thenReturn("{ \"workspace\": \"collaboration\", \"title\": \"doc1.pdf\", \"file\": \"xxxx\"}");
    when(elasticContentRequestBuilder.getDeleteDocumentRequestContent(eq(elasticIndexingServiceConnector), anyString())).thenReturn("{\"delete\":{\"_index\":\"file_alias\",\"_type\":\"file\",\"_id\":\"xxxx\"}}\n");
  }

  @After
  public void clean() {
    elasticIndexingOperationProcessor.getConnectors().clear();
    entityManagerService.endRequest(null);
  }

  @Test
  public void testStart() {
    try {
      when(elasticIndexingClient.sendGetESVersion()).thenReturn("8.6.0");
      elasticIndexingOperationProcessor.addConnector(elasticIndexingServiceConnector);
      when(elasticIndexingServiceConnector.getIndexAlias()).thenReturn("post");

      elasticIndexingOperationProcessor.start();
      assertTrue(elasticIndexingOperationProcessor.getConnectors().containsKey("post"));

      when(elasticIndexingServiceConnector.getCurrentIndex()).thenReturn("post_v2");
      when(elasticIndexingServiceConnector.getPreviousIndex()).thenReturn("post_v1");
      when(elasticIndexingServiceConnector.isReindexOnUpgrade()).thenReturn(true);
      when(elasticIndexingClient.sendIsIndexExistsRequest("post_v1")).thenReturn(true);
      when(elasticIndexingClient.sendIsIndexExistsRequest("post_v2")).thenReturn(false);
      elasticIndexingOperationProcessor.start();
      assertTrue(elasticIndexingOperationProcessor.getConnectors().containsKey("post"));
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void addConnector_ifNewConnector_connectorAdded() {
    //Given
    assertEquals(0, elasticIndexingOperationProcessor.getConnectors().size());
    //When
    elasticIndexingOperationProcessor.addConnector(elasticIndexingServiceConnector);
    //Then
    assertEquals(1, elasticIndexingOperationProcessor.getConnectors().size());
  }

  @Test
  public void addConnector_ifConnectorAlreadyExist_connectorNotAdded() {
    //Given
    elasticIndexingOperationProcessor.addConnector(elasticIndexingServiceConnector);
    assertEquals(1, elasticIndexingOperationProcessor.getConnectors().size());
    //When
    elasticIndexingOperationProcessor.addConnector(elasticIndexingServiceConnector);
    //Then
    assertEquals(1, elasticIndexingOperationProcessor.getConnectors().size());
  }

  @Test
  public void addConnector_ifConnectorAlreadyExist_initIndexingQueueNotCreated() {
    //Given
    elasticIndexingOperationProcessor.addConnector(elasticIndexingServiceConnector);
    IndexingOperation indexingOperation = new IndexingOperation(null,"post",OperationType.INIT);
    //When
    elasticIndexingOperationProcessor.addConnector(elasticIndexingServiceConnector);
    //Then
    verify(indexingOperationDAO, times(0)).create(indexingOperation);
  }

  @Test
  public void process_ifAllOperationsInQueue_requestShouldBeSentInAnExpectedOrder() throws ParseException {
    //Given
    elasticIndexingOperationProcessor.addConnector(elasticIndexingServiceConnector);
    IndexingOperation create = new IndexingOperation("1","post",OperationType.CREATE);
    create.setId(4L);
    IndexingOperation delete = new IndexingOperation("1","post",OperationType.DELETE);
    delete.setId(5L);
    IndexingOperation update = new IndexingOperation("1","post",OperationType.UPDATE);
    update.setId(2L);
    IndexingOperation init = new IndexingOperation(null,"post",OperationType.INIT);
    init.setId(3L);
    List<IndexingOperation> indexingOperations = new ArrayList<>();
    indexingOperations.add(create);
    indexingOperations.add(delete);
    indexingOperations.add(update);
    indexingOperations.add(init);
    Document document = new Document("1", new Date());
    when(indexingOperationDAO.findAllFirst(anyInt())).thenReturn(indexingOperations);
    lenient().when(elasticIndexingServiceConnector.create("1")).thenReturn(document);
    lenient().when(elasticIndexingServiceConnector.update("1")).thenReturn(document);

    //When
    elasticIndexingOperationProcessor.process();

    //Then

    //Check Client invocation
    InOrder orderClient = inOrder(elasticIndexingClient);
    //Operation I
    String createIndexRequestContent = elasticContentRequestBuilder.getCreateIndexRequestContent(elasticIndexingServiceConnector);

    orderClient.verify(elasticIndexingClient)
               .sendCreateIndexRequest(elasticIndexingServiceConnector.getIndexAlias(),
                                       createIndexRequestContent,
                                       elasticIndexingServiceConnector.getMapping());
    //Then Operation D, C and U
    orderClient.verify(elasticIndexingClient).sendCUDRequest(anyString());
    //Then no more interaction with client
    verifyNoMoreInteractions(elasticIndexingClient);
  }

  @Test
  public void process_ifAllOperationsInQueue_requestShouldBeCreatedInAnExpectedOrder() throws ParseException {
    //Given
    elasticIndexingOperationProcessor.addConnector(elasticIndexingServiceConnector);
    elasticIndexingOperationProcessor.getConnectors().put("post1", elasticIndexingServiceConnector);
    elasticIndexingOperationProcessor.getConnectors().put("post2", elasticIndexingServiceConnector);
    elasticIndexingOperationProcessor.getConnectors().put("post3", elasticIndexingServiceConnector);
    IndexingOperation init = new IndexingOperation(null,"post",OperationType.INIT);
    init.setId(4L);
    IndexingOperation delete = new IndexingOperation("1","post1",OperationType.DELETE);
    delete.setId(2L);
    IndexingOperation create = new IndexingOperation("2","post2",OperationType.CREATE);
    create.setId(1L);
    IndexingOperation update = new IndexingOperation("3","post3",OperationType.UPDATE);
    update.setId(3L);
    List<IndexingOperation> indexingOperations = new ArrayList<>();
    indexingOperations.add(create);
    indexingOperations.add(delete);
    indexingOperations.add(update);
    indexingOperations.add(init);
    Document document = new Document("1", new Date());
    when(indexingOperationDAO.findAllFirst(anyInt())).thenReturn(indexingOperations);
    lenient().when(elasticIndexingServiceConnector.create("2")).thenReturn(document);
    lenient().when(elasticIndexingServiceConnector.update("3")).thenReturn(document);

    //When
    elasticIndexingOperationProcessor.process();

    //Then

    //Check Request Builder invocation
    InOrder orderRequestBuilder = inOrder(elasticContentRequestBuilder);
    //Operation I
    orderRequestBuilder.verify(elasticContentRequestBuilder).getCreateIndexRequestContent(elasticIndexingServiceConnector);
    //Then Operation D, C and U
    orderRequestBuilder.verify(elasticContentRequestBuilder).getDeleteDocumentRequestContent(any(ElasticIndexingServiceConnector.class), anyString());
    orderRequestBuilder.verify(elasticContentRequestBuilder).getCreateDocumentRequestContent(elasticIndexingServiceConnector, "2");
    orderRequestBuilder.verify(elasticContentRequestBuilder).getUpdateDocumentRequestContent(elasticIndexingServiceConnector, "3");
    //Then no more interaction with builder
    verifyNoMoreInteractions(elasticContentRequestBuilder);
  }

  @Test
  public void process_ifAllOperationsInQueue_requestShouldContinueOnException() throws ParseException {
    //Given
    elasticIndexingOperationProcessor.getConnectors().put("post1", elasticIndexingServiceConnector);
    elasticIndexingOperationProcessor.getConnectors().put("post2", elasticIndexingServiceConnector);
    elasticIndexingOperationProcessor.getConnectors().put("post3", elasticIndexingServiceConnector);

    List<IndexingOperation> indexingOperations = new ArrayList<>();

    IndexingOperation delete = new IndexingOperation("1","post1",OperationType.DELETE);
    delete.setId(2L);
    indexingOperations.add(delete);

    IndexingOperation create = new IndexingOperation("2","post2",OperationType.CREATE);
    create.setId(1L);
    indexingOperations.add(create);

    IndexingOperation update = new IndexingOperation("3","post3",OperationType.UPDATE);
    update.setId(3L);
    indexingOperations.add(update);

    //When an exception is thrown during process
    doThrow(new RuntimeException("Fake error")).when(elasticContentRequestBuilder).getDeleteDocumentRequestContent(elasticIndexingServiceConnector, "1");
    when(indexingOperationDAO.findAllFirst(anyInt())).thenReturn(indexingOperations);
    elasticIndexingOperationProcessor.process();

    //Then Operation C and U was called
    verify(elasticContentRequestBuilder).getDeleteDocumentRequestContent(any(ElasticIndexingServiceConnector.class), eq("1"));
    verify(elasticContentRequestBuilder).getCreateDocumentRequestContent(any(ElasticIndexingServiceConnector.class), eq("2"));
    verify(elasticContentRequestBuilder).getUpdateDocumentRequestContent(any(ElasticIndexingServiceConnector.class), eq("3"));
  }

  @Test
  public void process_ifDeleteOperation_allOldestCreateOperationsWithSameEntityIdStillInQueueShouldBeCanceled() throws ParseException {
    //Given
    elasticIndexingOperationProcessor.addConnector(elasticIndexingServiceConnector);
    //Delete operation are older than create and update
    IndexingOperation delete = new IndexingOperation("1","post",OperationType.DELETE);
    delete.setId(2L);
    IndexingOperation create = new IndexingOperation("1","post",OperationType.CREATE);
    create.setId(1L);
    IndexingOperation update = new IndexingOperation("1","post",OperationType.UPDATE);
    update.setId(3L);
    List<IndexingOperation> indexingOperations = new ArrayList<>();
    indexingOperations.add(create);
    indexingOperations.add(delete);
    indexingOperations.add(update);
    Document document = new Document("1", new Date());
    when(indexingOperationDAO.findAllFirst(anyInt())).thenReturn(indexingOperations);
    lenient().when(elasticIndexingServiceConnector.create("1")).thenReturn(document);
    lenient().when(elasticIndexingServiceConnector.update("1")).thenReturn(document);

    //When
    elasticIndexingOperationProcessor.process();

    //Then
    InOrder order = inOrder(elasticIndexingClient);
    //Only one delete request should be build
    verify(elasticContentRequestBuilder, times(1)).getDeleteDocumentRequestContent(elasticIndexingServiceConnector, "1");
    verifyNoMoreInteractions(elasticContentRequestBuilder);
    //Only one CUD request should be send
    order.verify(elasticIndexingClient, times(1)).sendCUDRequest(anyString());
    verifyNoMoreInteractions(elasticIndexingClient);
  }

  @Test
  public void process_ifDeleteAllOperation_allOldestCreateUpdateDeleteOperationsWithSameTypeStillInQueueShouldBeCanceled() throws ParseException {
    //Given
    when(elasticIndexingServiceConnector.getCurrentIndex()).thenReturn("post");
    elasticIndexingOperationProcessor.getConnectors().put("post", elasticIndexingServiceConnector);
    IndexingOperation deleteAll = new IndexingOperation(null,"post",OperationType.DELETE_ALL);
    deleteAll.setId(5L);
    //CUD operation are older than delete all
    IndexingOperation create = new IndexingOperation("1","post",OperationType.CREATE);
    create.setId(1L);
    IndexingOperation delete = new IndexingOperation("1","post",OperationType.DELETE);
    delete.setId(2L);
    IndexingOperation update = new IndexingOperation("1","post",OperationType.UPDATE);
    update.setId(3L);
    List<IndexingOperation> indexingOperations = new ArrayList<>();
    indexingOperations.add(create);
    indexingOperations.add(delete);
    indexingOperations.add(update);
    indexingOperations.add(deleteAll);
    when(indexingOperationDAO.findAllFirst(anyInt())).thenReturn(indexingOperations);

    //When
    elasticIndexingOperationProcessor.process();

    //Then
    InOrder orderClient = inOrder(elasticIndexingClient);
    //Remove and recreate type request
    orderClient.verify(elasticIndexingClient).sendDeleteAllDocsRequest("post");
    //No CUD request
    verifyNoMoreInteractions(elasticIndexingClient);
  }



  @Test
  public void process_ifCreateOperation_allOldestAndNewestUpdateOperationsWithSameEntityIdStillInQueueShouldBeCanceled() throws ParseException {
    //Given
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    elasticIndexingOperationProcessor.addConnector(elasticIndexingServiceConnector);
    IndexingOperation create = new IndexingOperation("1","post",OperationType.CREATE);
    create.setId(1L);
    IndexingOperation oldUpdate = new IndexingOperation("1","post",OperationType.UPDATE);
    oldUpdate.setId(2L);
    IndexingOperation newUpdate = new IndexingOperation("1","post",OperationType.UPDATE);
    newUpdate.setId(3L);
    List<IndexingOperation> indexingOperations = new ArrayList<>();
    indexingOperations.add(create);
    indexingOperations.add(oldUpdate);
    indexingOperations.add(newUpdate);
    Document document = new Document("1", sdf.parse("19/01/1989"));
    when(indexingOperationDAO.findAllFirst(anyInt())).thenReturn(indexingOperations);
    lenient().when(elasticIndexingServiceConnector.create("1")).thenReturn(document);
    lenient().when(elasticIndexingServiceConnector.update("1")).thenReturn(document);

    //When
    elasticIndexingOperationProcessor.process();

    //Then
    InOrder order = inOrder(elasticIndexingClient);
    //Only one create request should be build (update operations are canceled)
    verify(elasticContentRequestBuilder, times(1)).getCreateDocumentRequestContent(elasticIndexingServiceConnector, "1");
    verifyNoMoreInteractions(elasticContentRequestBuilder);
    //Only one CUD request should be send
    order.verify(elasticIndexingClient, times(1)).sendCUDRequest(anyString());
    verifyNoMoreInteractions(elasticIndexingClient);
  }

  @Test
  public void process_ifDeleteAllOperation_allNewestCreateDeleteOperationsStillInQueueShouldBeProcessed() throws ParseException {
    //Given
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    elasticIndexingOperationProcessor.addConnector(elasticIndexingServiceConnector);
    IndexingOperation create = new IndexingOperation("1","post",OperationType.CREATE);
    create.setId(3L);
    IndexingOperation delete = new IndexingOperation("1","post",OperationType.DELETE);
    delete.setId(2L);
    List<IndexingOperation> indexingOperations = new ArrayList<>();
    indexingOperations.add(create);
    indexingOperations.add(delete);
    Document document = new Document("1", sdf.parse("19/01/1989"));
    when(indexingOperationDAO.findAllFirst(anyInt())).thenReturn(indexingOperations);
    lenient().when(elasticIndexingServiceConnector.create("1")).thenReturn(document);

    //When
    elasticIndexingOperationProcessor.process();

    //Then
    InOrder orderClient = inOrder(elasticIndexingClient);
    //Create and Delete requests should be build
    verify(elasticContentRequestBuilder, times(1)).getCreateDocumentRequestContent(elasticIndexingServiceConnector, "1");
    verify(elasticContentRequestBuilder, times(1)).getDeleteDocumentRequestContent(elasticIndexingServiceConnector, "1");
    //Only one CUD request should be send
    orderClient.verify(elasticIndexingClient, times(1)).sendCUDRequest(anyString());
    verifyNoMoreInteractions(elasticIndexingClient);
  }

  @Test
  public void process_ifDeleteOperation_allNewestCreateOperationsStillInQueueShouldBeProcessed() throws ParseException {
    //Given
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    elasticIndexingOperationProcessor.addConnector(elasticIndexingServiceConnector);
    //Delete operation are older than create
    IndexingOperation delete = new IndexingOperation("1","post",OperationType.DELETE);
    delete.setId(1L);
    IndexingOperation create = new IndexingOperation("1","post",OperationType.CREATE);
    create.setId(2L);
    List<IndexingOperation> indexingOperations = new ArrayList<>();
    indexingOperations.add(create);
    indexingOperations.add(delete);
    Document document = new Document("1", sdf.parse("19/01/1989"));
    when(indexingOperationDAO.findAllFirst(anyInt())).thenReturn(indexingOperations);
    lenient().when(elasticIndexingServiceConnector.create("1")).thenReturn(document);

    //When
    elasticIndexingOperationProcessor.process();

    //Then
    InOrder order = inOrder(elasticContentRequestBuilder);
    //Only one delete and one create request should be build
    order.verify(elasticContentRequestBuilder, times(1)).getDeleteDocumentRequestContent(elasticIndexingServiceConnector, "1");
    order.verify(elasticContentRequestBuilder, times(1)).getCreateDocumentRequestContent(elasticIndexingServiceConnector, "1");
    verifyNoMoreInteractions(elasticContentRequestBuilder);
    //Only one CUD request should be send
    verify(elasticIndexingClient, times(1)).sendCUDRequest(anyString());
    verifyNoMoreInteractions(elasticIndexingClient);
  }

  public void process_ifDeleteOperation_allNewestUpdateOperationsWithSameEntityIdStillInQueueShouldBeCanceled() throws ParseException {
    //Given
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    elasticIndexingOperationProcessor.addConnector(elasticIndexingServiceConnector);
    //Delete operation are older than update
    IndexingOperation delete = new IndexingOperation("1","post",OperationType.DELETE);
    delete.setId(1L);
    IndexingOperation update = new IndexingOperation("1","post",OperationType.UPDATE);
    update.setId(2L);
    List<IndexingOperation> indexingOperations = new ArrayList<>();
    indexingOperations.add(update);
    indexingOperations.add(delete);
    Document document = new Document("1", sdf.parse("19/01/1989"));
    when(indexingOperationDAO.findAllFirst(anyInt())).thenReturn(indexingOperations);
    lenient().when(elasticIndexingServiceConnector.update("1")).thenReturn(document);

    //When
    elasticIndexingOperationProcessor.process();

    //Then
    //Only one delete request should be build
    verify(elasticContentRequestBuilder, times(1)).getDeleteDocumentRequestContent(elasticIndexingServiceConnector, "1");
    verifyNoMoreInteractions(elasticContentRequestBuilder);
    //Only one CUD request should be send
    verify(elasticIndexingClient, times(1)).sendCUDRequest(anyString());
    verifyNoMoreInteractions(elasticIndexingClient);
  }

  @Test
  public void process_ifBulkRequestReachedSizeLimit_requestIsSent() throws ParseException {
    //Given
    elasticIndexingOperationProcessor.setRequestSizeLimit(1);
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    elasticIndexingOperationProcessor.addConnector(elasticIndexingServiceConnector);
    IndexingOperation create1 = new IndexingOperation("1","post",OperationType.CREATE);
    create1.setId(1L);
    IndexingOperation create2 = new IndexingOperation("2","post",OperationType.CREATE);
    create2.setId(2L);
    List<IndexingOperation> indexingOperations = new ArrayList<>();
    indexingOperations.add(create1);
    indexingOperations.add(create2);
    Document document1 = new Document("1", sdf.parse("19/01/1989"));
    Document document2 = new Document("2", sdf.parse("19/01/1989"));
    when(indexingOperationDAO.findAllFirst(anyInt())).thenReturn(indexingOperations);
    lenient().when(elasticIndexingServiceConnector.create("1")).thenReturn(document1);
    lenient().when(elasticIndexingServiceConnector.create("2")).thenReturn(document2);

    //When
    elasticIndexingOperationProcessor.process();

    //Then
    //Two CUD request should be send because the first create request will reached the limit size (= 1 byte)
    verify(elasticIndexingClient, times(2)).sendCUDRequest(anyString());
    verifyNoMoreInteractions(elasticIndexingClient);
  }

  @Test
  public void process_ifAllOperationsInQueue_requestShouldNotBeSentIfDocumentsAreNull() throws ParseException {
    //Given
    elasticIndexingOperationProcessor.addConnector(elasticIndexingServiceConnector);
    IndexingOperation create = new IndexingOperation("1","post",OperationType.CREATE);
    create.setId(4L);
    IndexingOperation delete = new IndexingOperation("1","post",OperationType.DELETE);
    delete.setId(5L);
    IndexingOperation update = new IndexingOperation("1","post",OperationType.UPDATE);
    update.setId(2L);
    IndexingOperation init = new IndexingOperation(null,"post",OperationType.INIT);
    init.setId(3L);
    List<IndexingOperation> indexingOperations = new ArrayList<>();
    indexingOperations.add(create);
    indexingOperations.add(delete);
    indexingOperations.add(update);
    indexingOperations.add(init);
    Document document = new Document("1", new Date());
    when(indexingOperationDAO.findAllFirst(anyInt())).thenReturn(indexingOperations);
    lenient().when(elasticIndexingServiceConnector.create("1")).thenReturn(document);
    lenient().when(elasticIndexingServiceConnector.update("1")).thenReturn(document);
    lenient().when(elasticContentRequestBuilder.getCreateDocumentRequestContent(eq(elasticIndexingServiceConnector), anyString())).thenReturn(null);
    lenient().when(elasticContentRequestBuilder.getUpdateDocumentRequestContent(eq(elasticIndexingServiceConnector), anyString())).thenReturn(null);
    when(elasticContentRequestBuilder.getDeleteDocumentRequestContent(eq(elasticIndexingServiceConnector), anyString())).thenReturn(null);


    //When
    elasticIndexingOperationProcessor.process();

    //Then

    //Check Client invocation
    verify(elasticIndexingClient, never()).sendCUDRequest(anyString());
  }

}

