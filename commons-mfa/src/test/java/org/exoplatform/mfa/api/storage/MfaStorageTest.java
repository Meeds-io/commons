package org.exoplatform.mfa.api.storage;

import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.mfa.storage.MfaStorage;
import org.exoplatform.mfa.storage.dao.RevocationRequestDAO;
import org.exoplatform.mfa.storage.dto.RevocationRequest;
import org.exoplatform.portal.branding.BrandingService;
import org.exoplatform.portal.branding.BrandingServiceImpl;
import org.exoplatform.services.naming.InitialContextInitializer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.jgroups.util.Util.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class MfaStorageTest {
  private PortalContainer container;

  @Mock
  BrandingService brandingService;

  @Before
  public void setup() {

    RootContainer rootContainer = RootContainer.getInstance();
    InitialContextInitializer initializer = rootContainer.getComponentInstanceOfType(InitialContextInitializer.class);
    initializer.recall();

    brandingService = mock(BrandingServiceImpl.class);
    if (rootContainer.getComponentInstanceOfType(BrandingService.class) == null) {
      rootContainer.registerComponentInstance(BrandingService.class.getName(), brandingService);
    }

    container = PortalContainer.getInstance();
    assertNotNull(container);

    ExoContainerContext.setCurrentContainer(container);


    RequestLifeCycle.begin(container);
  }

  @After
  public void teardown() {
    RevocationRequestDAO revocationRequestDAO = ExoContainerContext.getService(RevocationRequestDAO.class);
    revocationRequestDAO.deleteAll();


    RequestLifeCycle.end();
    container.stop();
    container = null;
    ExoContainerContext.setCurrentContainer(null);
  }

  @Test
  public void testCreateRevocationRequest() {
    MfaStorage mfaStorage = ExoContainerContext.getService(MfaStorage.class);
    assertNotNull(mfaStorage);

    try {
      mfaStorage.createRevocationRequest(null);
      fail("Shouldn't allow to add null application");
    } catch (IllegalArgumentException e) {
      // Expected
    }

    RevocationRequest revocationRequest = new RevocationRequest(null,"john", "type");

    RevocationRequest storedRequest = mfaStorage.createRevocationRequest(revocationRequest);
    assertNotNull(storedRequest);
    assertNotNull(storedRequest.getId());
    assertEquals(revocationRequest.getUser(), storedRequest.getUser());
    assertEquals(revocationRequest.getType(), storedRequest.getType());
  }

  @Test
  public void testCountByUsernameAndType() {
    MfaStorage mfaStorage = ExoContainerContext.getService(MfaStorage.class);

    String user ="john";
    String type="otp";
    assertEquals(0L,mfaStorage.countByUsernameAndType(user,type));

    RevocationRequest revocationRequest = new RevocationRequest(null,user, type);
    mfaStorage.createRevocationRequest(revocationRequest);
    assertEquals(1L,mfaStorage.countByUsernameAndType(user,type));

    mfaStorage.createRevocationRequest(revocationRequest);
    assertEquals(2L,mfaStorage.countByUsernameAndType(user,type));

  }

  @Test
  public void testDeleteByUsernameAndType() {
    MfaStorage mfaStorage = ExoContainerContext.getService(MfaStorage.class);

    String user ="john";
    String type="otp";
    assertEquals(0L,mfaStorage.countByUsernameAndType(user,type));

    RevocationRequest revocationRequest = new RevocationRequest(null,user, type);
    mfaStorage.createRevocationRequest(revocationRequest);
    assertEquals(1L,mfaStorage.countByUsernameAndType(user,type));

    mfaStorage.createRevocationRequest(revocationRequest);
    assertEquals(2L,mfaStorage.countByUsernameAndType(user,type));

//    entityMgrService.getEntityManager().getTransaction().commit();
//    entityMgrService.getEntityManager().getTransaction().begin();
    mfaStorage.deleteRevocationRequest(user,type);
//    mfaStorage.delete(revocationRequest);
//    entityMgrService.getEntityManager().getTransaction().commit();
//    entityMgrService.getEntityManager().getTransaction().begin();
    assertEquals(0L,mfaStorage.countByUsernameAndType(user,type));

  }



}
