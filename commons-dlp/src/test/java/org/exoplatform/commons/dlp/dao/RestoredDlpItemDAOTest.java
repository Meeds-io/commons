package org.exoplatform.commons.dlp.dao;

import org.exoplatform.commons.dlp.domain.RestoredDlpItemEntity;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RestoredDlpItemDAOTest {

  private RestoredDlpItemDAO restoredDlpItemDAO;

  @Before
  public void setUp() {
    PortalContainer container = PortalContainer.getInstance();
    restoredDlpItemDAO = container.getComponentInstanceOfType(RestoredDlpItemDAO.class);
    ExoContainerContext.setCurrentContainer(container);
    RequestLifeCycle.begin(container);
    restoredDlpItemDAO.deleteAll();
  }

  @After
  public void tearDown() {
    restoredDlpItemDAO.deleteAll();
    RequestLifeCycle.end();
  }

  @Test
  public void testDeleteDlpPositiveItemByReference() {

    //When
    RestoredDlpItemEntity restoredDlpItemEntity = new RestoredDlpItemEntity();
    restoredDlpItemEntity.setReference("nodeUID1");
    restoredDlpItemEntity.setDetectionDate(Calendar.getInstance());
    restoredDlpItemDAO.create(restoredDlpItemEntity);

    //Then
    assertEquals(1, restoredDlpItemDAO.count().intValue());

    //when
    restoredDlpItemDAO.delete(restoredDlpItemEntity);

    //Then
    assertEquals(0, restoredDlpItemDAO.count().intValue());
  }

  @Test
  public void testFindRestoredDlpItemByReference() {

    //Given
    assertEquals(restoredDlpItemDAO.findAll().size(), 0);
    assertEquals(null, restoredDlpItemDAO.findRestoredDlpItemByReference("001002003"));

    //When
    RestoredDlpItemEntity restoredDlpItemEntity = new RestoredDlpItemEntity();
    restoredDlpItemEntity.setReference("001002003");
    restoredDlpItemEntity.setDetectionDate(Calendar.getInstance());
    restoredDlpItemDAO.create(restoredDlpItemEntity);

    //Then
    assertNotNull(restoredDlpItemDAO.findRestoredDlpItemByReference("001002003"));
  }
}

