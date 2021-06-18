package org.exoplatform.commons.dlp.processor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import org.exoplatform.commons.dlp.connector.DlpServiceConnector;
import org.exoplatform.commons.dlp.dao.DlpOperationDAO;
import org.exoplatform.commons.dlp.domain.DlpOperation;
import org.exoplatform.commons.dlp.processor.impl.DlpOperationProcessorImpl;
import org.exoplatform.container.PortalContainer;

@RunWith(MockitoJUnitRunner.class)
public class DlpOperationProcessorTest {

  private DlpOperationProcessorImpl dlpOperationProcessor;

  @Mock
  private DlpOperationDAO dlpOperationDAO;
  
  @Mock
  private DlpServiceConnector dlpFileServiceConnector;
  
  @Mock
  private DlpServiceConnector dlpActivityServiceConnector;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);

    // Make sure a portal container is started
    PortalContainer.getInstance();
    initDlpServiceConnector();
    dlpOperationProcessor = new DlpOperationProcessorImpl(dlpOperationDAO);
  }

  private void initDlpServiceConnector() {
    when(dlpFileServiceConnector.getType()).thenReturn("file");
    lenient().when(dlpFileServiceConnector.getDisplayName()).thenReturn("file");
    lenient().when(dlpFileServiceConnector.isEnable()).thenReturn(true);
    
    when(dlpActivityServiceConnector.getType()).thenReturn("activity");
    lenient().when(dlpActivityServiceConnector.getDisplayName()).thenReturn("activity");
    lenient().when(dlpActivityServiceConnector.isEnable()).thenReturn(true);
  }

  @After
  public void clean() {
    dlpOperationProcessor.getConnectors().clear();
  }

  @Test
  public void testAddConnector() {
    //Given
    assertEquals(0, dlpOperationProcessor.getConnectors().size());
    //When
    dlpOperationProcessor.addConnector(dlpFileServiceConnector);
    //Then
    assertEquals(1, dlpOperationProcessor.getConnectors().size());
  }
  
  @Test
  public void testProcess() {
    //Given
    dlpOperationProcessor.start();
    dlpOperationProcessor.addConnector(dlpFileServiceConnector);
    dlpOperationProcessor.addConnector(dlpActivityServiceConnector);
    when(dlpOperationDAO.findAllFirstWithOffset(anyInt(),anyInt())).thenReturn(getDlpOperations());
    when(dlpOperationDAO.count()).thenReturn(Long.valueOf(getDlpOperations().size())).thenReturn(0L);
    when(dlpFileServiceConnector.processItem(anyString())).thenReturn(true);
    when(dlpActivityServiceConnector.processItem(anyString())).thenReturn(true);
    
    //When
    dlpOperationProcessor.process();
    
    //Then
    verify(dlpOperationDAO, times(3)).delete(any());
  }

  private List<DlpOperation> getDlpOperations() {
    List<DlpOperation> dlpOperations = new ArrayList<DlpOperation>();
    DlpOperation dlpOperation = new DlpOperation();
    dlpOperation.setId(1L);
    dlpOperation.setEntityType("file");
    dlpOperation.setEntityId("1");
    dlpOperations.add(dlpOperation);
    dlpOperation = new DlpOperation();
    dlpOperation.setId(2L);
    dlpOperation.setEntityType("file");
    dlpOperation.setEntityId("22");
    dlpOperations.add(dlpOperation);
    dlpOperation = new DlpOperation();
    dlpOperation.setId(3L);
    dlpOperation.setEntityType("activity");
    dlpOperation.setEntityId("100");
    dlpOperations.add(dlpOperation);
    return dlpOperations;
  }
  
  private List<DlpOperation> getDlpOperations(int size) {
  
    List<DlpOperation> dlpOperations = new ArrayList<DlpOperation>();
    for (int i=1; i<=size;i++) {
      DlpOperation dlpOperation = new DlpOperation();
      dlpOperation.setId(new Long(i));
      dlpOperation.setEntityType("file");
      dlpOperation.setEntityId(""+i);
      dlpOperations.add(dlpOperation);
    }
    return dlpOperations;
  }
  
  @Test
  public void testProcessWhenmoreItemsThanBatchStayInQueue() throws Exception{
    //This test verify that all items in queue a read,
    //even if the bactchNumber first items are waiting in queue (not indexed for example)
    
    //Given
    dlpOperationProcessor.start();
    dlpOperationProcessor.addConnector(dlpFileServiceConnector);
    dlpOperationProcessor.setBatchNumber(15);
    List<DlpOperation> dlpOperations = getDlpOperations(dlpOperationProcessor.getBatchNumber()+10);
    when(dlpOperationDAO.findAllFirstWithOffset(anyInt(),anyInt()))
        .thenAnswer(invocation -> {
          int offset=invocation.getArgument(0, Integer.class);
          int limit=invocation.getArgument(1, Integer.class);
          return dlpOperations.stream().skip(offset).limit(limit).collect(Collectors.toList());
        });
    when(dlpOperationDAO.count()).thenReturn(Long.valueOf(dlpOperations.size()));
    when(dlpFileServiceConnector.processItem(anyString())).thenReturn(false);
  
    //When
    dlpOperationProcessor.process();
    
    //Then
    verify(dlpOperationDAO, times(3)).count();
  }
  
}
