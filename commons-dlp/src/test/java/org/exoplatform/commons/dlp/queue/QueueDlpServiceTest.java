package org.exoplatform.commons.dlp.queue;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

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
  public void init_ifInitOperation_initIndexingQueueCreated() {
    //Given
    DlpOperation dlpOperation = new DlpOperation("100", "file");
    //When
    queueDlpService.addToQueue("file", dlpOperation.getEntityId());
    //Then
    verify(dlpOperationDAO, times(1)).create(dlpOperation);
  }
}