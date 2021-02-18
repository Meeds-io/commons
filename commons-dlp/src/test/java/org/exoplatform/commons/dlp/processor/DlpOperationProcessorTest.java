package org.exoplatform.commons.dlp.processor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
    when(dlpFileServiceConnector.getDisplayName()).thenReturn("file");
    when(dlpFileServiceConnector.isEnable()).thenReturn(true);
    
    when(dlpActivityServiceConnector.getType()).thenReturn("activity");
    when(dlpActivityServiceConnector.getDisplayName()).thenReturn("activity");
    when(dlpActivityServiceConnector.isEnable()).thenReturn(true);
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
    when(dlpOperationDAO.findAllFirst(anyInt())).thenReturn(getDlpOperations());
    when(dlpFileServiceConnector.processItem(anyString())).thenReturn(true);
    when(dlpActivityServiceConnector.processItem(anyString())).thenReturn(true);
    
    //When
    dlpOperationProcessor.process();
    
    //Then
    verify(dlpOperationDAO, times(3)).delete(any());
  }

  @Test
  public void testGetKeywords() {
    //Given
    dlpOperationProcessor.start();
    dlpOperationProcessor.addConnector(dlpFileServiceConnector);
    dlpOperationProcessor.setKeywords("fruit,legume,tomate");

    //When
    String keywords = dlpOperationProcessor.getKeywords();

    //Then
    assertEquals(keywords, "fruit,legume,tomate");
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
  
}
