package org.exoplatform.commons.dlp.service;

import org.exoplatform.commons.dlp.connector.DlpServiceConnector;
import org.exoplatform.commons.dlp.dao.AbstractDAOTest;
import org.exoplatform.commons.dlp.dao.DlpOperationDAO;
import org.exoplatform.commons.dlp.dao.DlpPositiveItemDAO;
import org.exoplatform.commons.dlp.domain.DlpPositiveItemEntity;
import org.exoplatform.commons.dlp.dto.DlpPositiveItem;
import org.exoplatform.commons.dlp.processor.impl.DlpOperationProcessorImpl;
import org.exoplatform.commons.dlp.service.impl.DlpPositiveItemServiceImpl;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.idm.UserImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DlpPositiveItemServiceTest extends AbstractDAOTest {

  @Mock
  private DlpOperationDAO dlpOperationDAO;

  private DlpOperationProcessorImpl dlpOperationProcessor;

  private DlpPositiveItemDAO dlpPositiveItemDAO;

  private DlpPositiveItemServiceImpl dlpPositiveItemService;

  private DlpServiceConnector dlpServiceConnector;

  @Mock
  private DlpServiceConnector dlpServiceConnector1;

  @Mock
  private DlpServiceConnector dlpServiceConnector2;

  private ListenerService listenerService;

  private OrganizationService organizationService;

  private UserHandler userHandler;

  @After
  public void clean() {
    dlpOperationProcessor.getConnectors().clear();
  }

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    organizationService = mock(OrganizationService.class);
    dlpServiceConnector = mock(DlpServiceConnector.class);
    userHandler = mock(UserHandler.class);
    when(organizationService.getUserHandler()).thenReturn(userHandler);
    UserImpl user = new UserImpl();
    user.setUserName("root");
    user.setFullName("root root");
    Mockito.when(userHandler.findUserByName(Mockito.eq("root"))).thenReturn(user);
    PortalContainer container = PortalContainer.getInstance();
    dlpPositiveItemDAO = container.getComponentInstanceOfType(DlpPositiveItemDAO.class);
    initDlpServiceConnector();
    dlpOperationProcessor = new DlpOperationProcessorImpl(dlpOperationDAO);
    dlpPositiveItemService =
        new DlpPositiveItemServiceImpl(dlpPositiveItemDAO, organizationService, listenerService, dlpOperationProcessor);
  }

  private void initDlpServiceConnector() {
    when(dlpServiceConnector.getType()).thenReturn("file");
    when(dlpServiceConnector.getDisplayName()).thenReturn("file");
    when(dlpServiceConnector.isEnable()).thenReturn(true);
    when(dlpServiceConnector.getItemUrl("reference1234")).thenReturn("/Security/file");
    when(dlpServiceConnector.getType()).thenReturn("file");
    when(dlpServiceConnector.getDisplayName()).thenReturn("file");
    when(dlpServiceConnector.isEnable()).thenReturn(true);
    when(dlpServiceConnector.getItemUrl("ref")).thenReturn("/Security/file12");
    when(dlpServiceConnector1.getType()).thenReturn("file1");
    when(dlpServiceConnector1.getDisplayName()).thenReturn("file1");
    when(dlpServiceConnector1.isEnable()).thenReturn(true);
    when(dlpServiceConnector1.getItemUrl("ref1")).thenReturn("/Security/file123");
    when(dlpServiceConnector2.getType()).thenReturn("file2");
    when(dlpServiceConnector2.getDisplayName()).thenReturn("file2");
    when(dlpServiceConnector2.isEnable()).thenReturn(true);
    when(dlpServiceConnector2.getItemUrl("ref2")).thenReturn("/Security/file1234");
  }

  @Test
  public void testGetDlpPositiveItemByReference() throws Exception {
    //Given
    DlpPositiveItemEntity dlpPositiveItemEntity = new DlpPositiveItemEntity();
    dlpPositiveItemEntity.setType("file");
    dlpPositiveItemEntity.setTitle("file");
    dlpPositiveItemEntity.setDetectionDate(Calendar.getInstance());
    dlpPositiveItemEntity.setAuthor("root");
    dlpPositiveItemEntity.setKeywords("test1");
    dlpPositiveItemEntity.setReference("reference1234");
    //When
    dlpPositiveItemService.addDlpPositiveItem(dlpPositiveItemEntity);
    dlpOperationProcessor.addConnector(dlpServiceConnector);
    DlpPositiveItem dlpPositiveItem = dlpPositiveItemService.getDlpPositiveItemByReference("reference1234");

    //Then
    assertNotNull(dlpPositiveItem);
  }

  @Test
  public void testGetDlpPositivesItems() throws Exception {
    //Given
    DlpPositiveItemEntity dlpPositiveItemEntity = new DlpPositiveItemEntity();
    dlpPositiveItemEntity.setType("file");
    dlpPositiveItemEntity.setTitle("file");
    dlpPositiveItemEntity.setReference("ref");
    dlpPositiveItemEntity.setDetectionDate(Calendar.getInstance());
    dlpPositiveItemEntity.setAuthor("root");
    dlpPositiveItemEntity.setKeywords("Keywords");

    DlpPositiveItemEntity dlpPositiveItemEntity1 = new DlpPositiveItemEntity();
    dlpPositiveItemEntity1.setType("file1");
    dlpPositiveItemEntity1.setTitle("file1");
    dlpPositiveItemEntity1.setReference("ref1");
    dlpPositiveItemEntity1.setDetectionDate(Calendar.getInstance());
    dlpPositiveItemEntity1.setAuthor("root");
    dlpPositiveItemEntity.setKeywords("Keywords1");

    DlpPositiveItemEntity dlpPositiveItemEntity2 = new DlpPositiveItemEntity();
    dlpPositiveItemEntity2.setType("file2");
    dlpPositiveItemEntity2.setTitle("file2");
    dlpPositiveItemEntity2.setReference("ref2");
    dlpPositiveItemEntity2.setDetectionDate(Calendar.getInstance());
    dlpPositiveItemEntity2.setAuthor("root");
    dlpPositiveItemEntity.setKeywords("Keywords2");

    //When
    dlpPositiveItemService.addDlpPositiveItem(dlpPositiveItemEntity);
    dlpOperationProcessor.addConnector(dlpServiceConnector);
    dlpPositiveItemService.addDlpPositiveItem(dlpPositiveItemEntity1);
    dlpOperationProcessor.addConnector(dlpServiceConnector1);
    dlpPositiveItemService.addDlpPositiveItem(dlpPositiveItemEntity2);
    dlpOperationProcessor.addConnector(dlpServiceConnector2);

    List<DlpPositiveItem> dlpPositiveItems = dlpPositiveItemService.getDlpPositivesItems(0, 20);
    Long size = dlpPositiveItemService.getDlpPositiveItemsCount();

    //Then
    assertNotNull(dlpPositiveItems);
    assertEquals(size.intValue(), 3);

    // when
    dlpPositiveItemService.deleteDlpPositiveItem(dlpPositiveItems.get(0).getId());
    size = dlpPositiveItemService.getDlpPositiveItemsCount();

    //then
    assertEquals(size.intValue(), 2);
  }
}
