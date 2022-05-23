package org.exoplatform.commons.dlp.queue;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.dlp.dao.DlpOperationDAO;
import org.exoplatform.commons.dlp.domain.DlpOperation;
import org.exoplatform.commons.dlp.queue.impl.QueueDlpServiceImpl;

@RunWith(MockitoJUnitRunner.class)
public class QueueDlpServiceTest {
  
  private QueueDlpServiceImpl queueDlpService;
  
  @Mock
  private DlpOperationDAO dlpOperationDAO;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
    queueDlpService = new QueueDlpServiceImpl(dlpOperationDAO);
  }

  @Test
  public void testAddToQueue() {
    //Given
    DlpOperation dlpOperation = new DlpOperation("100", "file");
    //When
    queueDlpService.addToQueue("file", dlpOperation.getEntityId());
    //Then
    verify(dlpOperationDAO, times(1)).create(dlpOperation);
  }
  
  @Test
  public void testRemoveFromQueue() {
    //Given
    DlpOperation dlpOperation = new DlpOperation("100", "file");
    DlpOperation dlpOperation2 = new DlpOperation("101", "file");
    DlpOperation dlpOperation3 = new DlpOperation("100", "file");
    //When
    queueDlpService.addToQueue("file", dlpOperation.getEntityId());
    queueDlpService.addToQueue("file", dlpOperation2.getEntityId());
    queueDlpService.addToQueue("file", dlpOperation3.getEntityId());
    
    queueDlpService.removeAllItemFromQueue("100");
    //Then
    verify(dlpOperationDAO, times(1)).deleteByEntityId("100");
  }
}
