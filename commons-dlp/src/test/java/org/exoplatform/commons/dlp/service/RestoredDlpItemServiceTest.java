package org.exoplatform.commons.dlp.service;

import org.exoplatform.commons.dlp.dao.AbstractDAOTest;
import org.exoplatform.commons.dlp.dao.RestoredDlpItemDAO;
import org.exoplatform.commons.dlp.domain.RestoredDlpItemEntity;
import org.exoplatform.commons.dlp.dto.RestoredDlpItem;
import org.exoplatform.commons.dlp.service.impl.RestoredDlpItemServiceImpl;
import org.exoplatform.container.PortalContainer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class RestoredDlpItemServiceTest extends AbstractDAOTest {

  private RestoredDlpItemDAO restoredDlpItemDAO;

  private RestoredDlpItemServiceImpl restoredDlpItemService;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    PortalContainer container = PortalContainer.getInstance();
    restoredDlpItemDAO = container.getComponentInstanceOfType(RestoredDlpItemDAO.class);
    restoredDlpItemService = new RestoredDlpItemServiceImpl(restoredDlpItemDAO);
  }

  @Test
  public void testGetRestoredDlpItemByReference() {
    //Given
    RestoredDlpItemEntity restoredDlpItemEntity = new RestoredDlpItemEntity();
    restoredDlpItemEntity.setReference("nodeUID");
    restoredDlpItemEntity.setDetectionDate(Calendar.getInstance());
    //When
    restoredDlpItemService.addRestoredDlpItem(restoredDlpItemEntity);
    RestoredDlpItem restoredDlpItem = restoredDlpItemService.getRestoredDlpItemByReference("nodeUID");

    //Then
    assertNotNull(restoredDlpItem);
  }

}
