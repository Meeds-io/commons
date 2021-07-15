package org.exoplatform.mfa.api.storage;

import org.exoplatform.commons.api.settings.ExoFeatureService;
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
import org.exoplatform.services.resources.ResourceBundleService;
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


  @Mock
  ExoFeatureService featureService;

  @Mock
  ResourceBundleService resourceBundleService;

  @Before
  public void setup() {

    RootContainer rootContainer = RootContainer.getInstance();
    InitialContextInitializer initializer = rootContainer.getComponentInstanceOfType(InitialContextInitializer.class);
    initializer.recall();

    brandingService = mock(BrandingServiceImpl.class);
    if (rootContainer.getComponentInstanceOfType(BrandingService.class) == null) {
      rootContainer.registerComponentInstance(BrandingService.class.getName(), brandingService);
    }

    featureService = mock(ExoFeatureService.class);
    if (rootContainer.getComponentInstanceOfType(ExoFeatureService.class) == null) {
      rootContainer.registerComponentInstance(ExoFeatureService.class.getName(), featureService);
    }

    resourceBundleService = mock(ResourceBundleService.class);
    if (rootContainer.getComponentInstanceOfType(ResourceBundleService.class) == null) {
      rootContainer.registerComponentInstance(ResourceBundleService.class.getName(), resourceBundleService);
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

    mfaStorage.deleteRevocationRequest(user,type);
    assertEquals(0L,mfaStorage.countByUsernameAndType(user,type));

  }

  @Test
  public void testDeleteById() {
    MfaStorage mfaStorage = ExoContainerContext.getService(MfaStorage.class);

    String user ="john";
    String user2 ="mary";
    String type="otp";
    assertEquals(0,mfaStorage.findAll().size());

    RevocationRequest revocationRequest = new RevocationRequest(null,user, type);
    revocationRequest=mfaStorage.createRevocationRequest(revocationRequest);
    assertEquals(1,mfaStorage.findAll().size());
    RevocationRequest revocationRequest2 = new RevocationRequest(null,user2, type);
    revocationRequest2=mfaStorage.createRevocationRequest(revocationRequest2);
    assertEquals(2,mfaStorage.findAll().size());

    mfaStorage.deleteById(revocationRequest.getId());
    assertEquals(1,mfaStorage.findAll().size());
    assertEquals(revocationRequest2.getId(),mfaStorage.findAll().get(0).getId());


  }

  @Test
  public void testFindAll() {
    MfaStorage mfaStorage = ExoContainerContext.getService(MfaStorage.class);

    String user1 = "john";
    String user2 = "mary";
    String user3 = "franck";
    String type = "otp";
    assertEquals(0, mfaStorage.findAll().size());

    RevocationRequest revocationRequest = new RevocationRequest(null, user1, type);
    mfaStorage.createRevocationRequest(revocationRequest);
    assertEquals(1, mfaStorage.findAll().size());

    revocationRequest = new RevocationRequest(null, user2, type);
    mfaStorage.createRevocationRequest(revocationRequest);
    assertEquals(2, mfaStorage.findAll().size());

    revocationRequest = new RevocationRequest(null, user3, type);
    mfaStorage.createRevocationRequest(revocationRequest);
    assertEquals(3, mfaStorage.findAll().size());

  }


  @Test
  public void testFindBy() {
    MfaStorage mfaStorage = ExoContainerContext.getService(MfaStorage.class);

    String user1 = "john";
    String type = "otp";
    assertEquals(0, mfaStorage.findAll().size());

    RevocationRequest revocationRequest = new RevocationRequest(null, user1, type);
    revocationRequest = mfaStorage.createRevocationRequest(revocationRequest);
    assertEquals(1, mfaStorage.findAll().size());

    assertEquals(revocationRequest.getId(), mfaStorage.findById(revocationRequest.getId()).getId());


  }
}
