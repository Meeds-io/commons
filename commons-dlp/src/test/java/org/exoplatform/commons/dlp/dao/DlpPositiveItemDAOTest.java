package org.exoplatform.commons.dlp.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.exoplatform.commons.dlp.domain.DlpPositiveItemEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.exoplatform.container.PortalContainer;

public class DlpPositiveItemDAOTest extends AbstractDAOTest {

    private DlpPositiveItemDAO dlpPositiveItemDAO;

    @Before
    public void setUp() {
        PortalContainer container = PortalContainer.getInstance();
        dlpPositiveItemDAO = container.getComponentInstanceOfType(DlpPositiveItemDAO.class);
        dlpPositiveItemDAO.deleteAll();
    }

    @After
    public void tearDown() {
        dlpPositiveItemDAO.deleteAll();
    }

    @Test
    public void testDlpPositiveItemsCreation() {

        //Given
        List<DlpPositiveItemEntity> dlpPositiveItemEntities = dlpPositiveItemDAO.getDlpPositiveItems(0, 20);
        assertEquals(dlpPositiveItemEntities.size(), 0);

        //When
        DlpPositiveItemEntity dlpPositiveItemEntity = new DlpPositiveItemEntity();
        dlpPositiveItemEntity.setType("file");
        dlpPositiveItemEntity.setTitle("file");
        dlpPositiveItemDAO.create(dlpPositiveItemEntity);

        DlpPositiveItemEntity dlpPositiveItemEntity1 = new DlpPositiveItemEntity();
        dlpPositiveItemEntity.setType("file");
        dlpPositiveItemEntity.setTitle("file1");
        dlpPositiveItemDAO.create(dlpPositiveItemEntity1);

        DlpPositiveItemEntity dlpPositiveItemEntity2 = new DlpPositiveItemEntity();
        dlpPositiveItemEntity.setType("file");
        dlpPositiveItemEntity.setTitle("file2");
        dlpPositiveItemDAO.create(dlpPositiveItemEntity2);

        //Then
        dlpPositiveItemEntities = dlpPositiveItemDAO.getDlpPositiveItems(0, 20);
        assertEquals(dlpPositiveItemEntities.size(), 3);
    }

    @Test
    public void testFindDlpPositiveItemByReference() {

        //Given
        assertEquals(dlpPositiveItemDAO.findAll().size(), 0);
        assertEquals(null, dlpPositiveItemDAO.findDlpPositiveItemByReference( "ref1"));

        //When
        DlpPositiveItemEntity dlpPositiveItemEntity = new DlpPositiveItemEntity();
        dlpPositiveItemEntity.setType("file");
        dlpPositiveItemEntity.setTitle("file");
        dlpPositiveItemEntity.setReference("ref1");
        dlpPositiveItemDAO.create(dlpPositiveItemEntity);

        //Then
        assertNotNull(dlpPositiveItemDAO.findDlpPositiveItemByReference( "ref1"));
    }

    @Test
    public void testDeleteDlpPositiveItemByReference() {

        //When
        DlpPositiveItemEntity dlpPositiveItemEntity = new DlpPositiveItemEntity();
        dlpPositiveItemEntity.setType("file");
        dlpPositiveItemEntity.setTitle("file");
        dlpPositiveItemEntity.setReference("ref1");
        dlpPositiveItemDAO.create(dlpPositiveItemEntity);

        //Then
        assertEquals(1, dlpPositiveItemDAO.count().intValue());
        
        //when
        dlpPositiveItemDAO.delete(dlpPositiveItemEntity);

        //Then
        assertEquals(0, dlpPositiveItemDAO.count().intValue());
    }
    
    
    @Test
    public void testDlpPositiveItemsCreationWithLongKeyword() {

        //Given
        List<DlpPositiveItemEntity> dlpPositiveItemEntities = dlpPositiveItemDAO.getDlpPositiveItems(0, 20);
        assertEquals(dlpPositiveItemEntities.size(), 0);

        //When
        DlpPositiveItemEntity dlpPositiveItemEntity = new DlpPositiveItemEntity();
        dlpPositiveItemEntity.setType("file");
        dlpPositiveItemEntity.setTitle("file");
        dlpPositiveItemEntity.setKeywords("keyword1, "
                                              + "keyword2, "
                                              + "keyword3, "
                                              + "keyword4, "
                                              + "keyword5, "
                                              + "keyword6");
        dlpPositiveItemDAO.create(dlpPositiveItemEntity);


        //Then
        dlpPositiveItemEntities = dlpPositiveItemDAO.getDlpPositiveItems(0, 20);
        assertEquals(dlpPositiveItemEntities.size(), 1);
    }
}

