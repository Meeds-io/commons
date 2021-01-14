package org.exoplatform.commons.dlp.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.exoplatform.commons.dlp.domain.DlpOperation;
import org.exoplatform.container.PortalContainer;

public class DlpOperationDAOTest extends AbstractDAOTest {

  private DlpOperationDAO dlpOperationDAO;

  @Before
  public void setUp() {
    PortalContainer container = PortalContainer.getInstance();
    dlpOperationDAO = container.getComponentInstanceOfType(DlpOperationDAO.class);
  }

  @After
  public void tearDown() {
    dlpOperationDAO.deleteAll();
  }

  @Test
  public void testDlpQueueCreation() throws IllegalAccessException, NoSuchFieldException {

    //Given
    List<DlpOperation> dlpOperations = dlpOperationDAO.findAll();
    assertEquals(dlpOperations.size(), 0);

    //When
    createDlpOperations();

    //Then
    assertEquals(dlpOperationDAO.findAll().size(), 3);
    dlpOperations = dlpOperationDAO.findAll();
    Field privateField = DlpOperation.class.getDeclaredField("timestamp");
    privateField.setAccessible(true);
    Date timestamp = (Date) privateField.get(dlpOperations.get(0));
    assertNull(timestamp);
  }
  
  @Test
  public void testFindByEntityIdAndType() {

    //Given
    assertEquals(dlpOperationDAO.findAll().size(), 0);
    assertEquals(dlpOperationDAO.findByEntityIdAndType("1", "file").size(), 0);
    assertEquals(dlpOperationDAO.findByEntityIdAndType("100", "file").size(), 0);

    //When
    createDlpOperations();

    //Then
    assertEquals(dlpOperationDAO.findAll().size(), 3);
    assertEquals(dlpOperationDAO.findByEntityIdAndType("1", "file").size(), 1);
    assertEquals(dlpOperationDAO.findByEntityIdAndType("100", "file").size(), 0);
  }
  
  @Test
  public void testFindAllFirst() {

    //Given
    assertEquals(dlpOperationDAO.findAll().size(), 0);
    assertEquals(dlpOperationDAO.findAllFirst(2).size(), 0);

    //When
    createDlpOperations();

    //Then
    assertEquals(dlpOperationDAO.findAll().size(), 3);
    assertEquals(dlpOperationDAO.findAllFirst(2).size(), 2);
  }
  
  @Test
  public void testDeleteByEntityId() {
    
    //Given
    assertEquals(dlpOperationDAO.findAll().size(), 0);
    assertEquals(dlpOperationDAO.findAllFirst(2).size(), 0);
    
    //When
    createDlpOperations();
    createDlpOperations();
    
    //Then
    assertEquals(dlpOperationDAO.findAll().size(),6);
    assertEquals(dlpOperationDAO.findByEntityIdAndType("22","file").size(), 2);
    dlpOperationDAO.deleteByEntityId("22");
    assertEquals(dlpOperationDAO.findByEntityIdAndType("22","file").size(), 0);
    assertEquals(dlpOperationDAO.findAll().size(),4);
  
  }
  
  private void createDlpOperations () {
    DlpOperation dlpOperation = new DlpOperation();
    dlpOperation.setEntityType("file");
    dlpOperation.setEntityId("1");
    dlpOperationDAO.create(dlpOperation);
    dlpOperation = new DlpOperation();
    dlpOperation.setEntityType("file");
    dlpOperation.setEntityId("22");
    dlpOperationDAO.create(dlpOperation);
    dlpOperation = new DlpOperation();
    dlpOperation.setEntityType("activity");
    dlpOperation.setEntityId("100");
    dlpOperationDAO.create(dlpOperation);
  }
  
  
}

