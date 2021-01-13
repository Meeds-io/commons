package org.exoplatform.commons.dlp.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.exoplatform.commons.dlp.dao.DlpPositiveItemDAO;
import org.exoplatform.commons.dlp.domain.DlpPositiveItemEntity;
import org.exoplatform.commons.dlp.service.impl.DlpPositiveItemServiceImpl;
import org.exoplatform.services.organization.OrganizationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DlpPositiveItemServiceTest {

    private DlpPositiveItemServiceImpl dlpPositiveItemService;

    @Mock
    private DlpPositiveItemDAO dlpPositiveItemDAO;

    @Mock
    private OrganizationService organizationService;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        dlpPositiveItemService = new DlpPositiveItemServiceImpl(dlpPositiveItemDAO, organizationService);
    }

    @Test
    public void testAddDlpPositiveItem() {
        //Given
        DlpPositiveItemEntity dlpPositiveItemEntity = new DlpPositiveItemEntity();
        dlpPositiveItemEntity.setType("file");
        dlpPositiveItemEntity.setTitle("file");
        //When
        dlpPositiveItemService.addDlpPositiveItem(dlpPositiveItemEntity);
        //Then
        verify(dlpPositiveItemDAO, times(1)).create(dlpPositiveItemEntity);
    }
}