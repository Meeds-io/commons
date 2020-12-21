package org.exoplatform.commons.dlp.dao;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.FileSystemResourceAccessor;

import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.component.test.*;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import javax.persistence.EntityManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.ROOT, path = "conf/portal/test-configuration.xml"),
})
public abstract class AbstractDAOTest {
  private static Connection conn;
  private static Liquibase liquibase;

  EntityManagerService entityMgrService;

  @BeforeClass
  public static void startDB () throws ClassNotFoundException, SQLException, LiquibaseException {

    Class.forName("org.hsqldb.jdbcDriver");
    conn = DriverManager.getConnection("jdbc:hsqldb:file:target/hsql-db", "sa", "");

    Database database = DatabaseFactory.getInstance()
        .findCorrectDatabaseImplementation(new JdbcConnection(conn));

    //Create Table
    liquibase = new Liquibase("./src/main/resources/db/changelog/exo-dlp.db.changelog-1.0.0.xml",
        new FileSystemResourceAccessor(), database);
    liquibase.update((String) null);

  }

  @AfterClass
  public static void stopDB () throws LiquibaseException, SQLException {

    liquibase.rollback(1000, null);
    conn.close();

  }

  @Before
  public void initializeContainerAndStartRequestLifecycle() {
    PortalContainer container = PortalContainer.getInstance();

    RequestLifeCycle.begin(container);

    entityMgrService = container.getComponentInstanceOfType(EntityManagerService.class);
    entityMgrService.getEntityManager().getTransaction().begin();
  }

  @After
  public void endRequestLifecycle() {

    entityMgrService.getEntityManager().getTransaction().commit();

    RequestLifeCycle.end();
  }

  protected EntityManager getEntityManager() {
    return entityMgrService.getEntityManager();
  }

}

