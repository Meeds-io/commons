/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.commons.digest.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.exoplatform.jpa.BaseTest;

/**
 * Checks what Liquibase really created for {@link DigestUserEntity}. The digest
 * repository is a Spring Data one, bootstrapped by the webapp and not by this
 * container, so the table is exercised here through plain SQL: it is the only
 * place where an id generated the way the entity asks for it is proven to work.
 */
public class DigestUserSchemaTest extends BaseTest {

  private static final String DATASOURCE_NAME = "java:/comp/env/exo-jpa_portal";

  private static final String TABLE_NAME      = "NTF_DIGEST_USERS";

  private static final String SEQUENCE_NAME   = "SEQ_NTF_DIGEST_USER_ID";

  private DataSource          dataSource;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    dataSource = (DataSource) new InitialContext().lookup(DATASOURCE_NAME);
    execute("DELETE FROM " + TABLE_NAME);
  }

  @Override
  protected void tearDown() throws Exception {
    execute("DELETE FROM " + TABLE_NAME);
    super.tearDown();
  }

  public void testTableIsCreated() throws Exception {
    try (Connection connection = dataSource.getConnection();
         ResultSet columns = connection.getMetaData().getColumns(null, null, TABLE_NAME, null)) {
      int columnCount = 0;
      while (columns.next()) {
        columnCount++;
      }
      assertEquals("NTF_DIGEST_USERS must hold the 7 columns the digest sender job reads", 7, columnCount);
    }
  }

  /**
   * Since Hibernate 5.6 the id of an entity using GenerationType.AUTO is drawn
   * from a sequence on hsqldb too, and not only on oracle and postgresql. A
   * missing sequence makes every enrollment fail at runtime while the table
   * itself looks perfectly fine.
   */
  public void testIdSequenceIsCreated() throws Exception {
    try (Connection connection = dataSource.getConnection();
         Statement statement = connection.createStatement();
         ResultSet resultSet = statement.executeQuery("VALUES(NEXT VALUE FOR " + SEQUENCE_NAME + ")")) {
      assertTrue("The sequence generating the NTF_DIGEST_USERS ids must exist", resultSet.next());
      assertTrue(resultSet.getLong(1) > 0);
    }
  }

  public void testEnrollmentRoundTrip() throws Exception {
    // The column keeps the milliseconds, which is far more than what an hourly
    // job needs to know when the last digest went out
    Instant dailyLastSent = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    try (Connection connection = dataSource.getConnection();
         PreparedStatement insert =
                                  connection.prepareStatement("INSERT INTO " + TABLE_NAME
                                      + " (ID, USER_ID, DAILY, WEEKLY, TIMEZONE, DAILY_LAST_SENT, WEEKLY_LAST_SENT)"
                                      + " VALUES (NEXT VALUE FOR " + SEQUENCE_NAME + ", ?, ?, ?, ?, ?, ?)")) {
      insert.setString(1, "digestUser");
      insert.setBoolean(2, true);
      insert.setBoolean(3, false);
      insert.setString(4, "Europe/Paris");
      insert.setTimestamp(5, Timestamp.from(dailyLastSent));
      insert.setTimestamp(6, null);
      assertEquals(1, insert.executeUpdate());
    }

    try (Connection connection = dataSource.getConnection();
         Statement statement = connection.createStatement();
         ResultSet resultSet = statement.executeQuery("SELECT USER_ID, DAILY, WEEKLY, TIMEZONE, DAILY_LAST_SENT,"
             + " WEEKLY_LAST_SENT FROM " + TABLE_NAME)) {
      assertTrue(resultSet.next());
      assertEquals("digestUser", resultSet.getString("USER_ID"));
      assertTrue(resultSet.getBoolean("DAILY"));
      assertFalse(resultSet.getBoolean("WEEKLY"));
      assertEquals("Europe/Paris", resultSet.getString("TIMEZONE"));
      assertEquals(Timestamp.from(dailyLastSent), resultSet.getTimestamp("DAILY_LAST_SENT"));
      assertNull(resultSet.getTimestamp("WEEKLY_LAST_SENT"));
      assertFalse("The job work list must hold one row per user", resultSet.next());
    }
  }

  /**
   * The unique constraint is what keeps two concurrent saves of the same user
   * from enrolling him twice, the service relies on it to enroll again instead.
   */
  public void testUserIsEnrolledOnlyOnce() throws Exception {
    insertUser("digestUser");
    try {
      insertUser("digestUser");
      fail("Enrolling the same user twice must be refused by the database");
    } catch (Exception e) {
      // Expected, USER_ID is unique
    }
  }

  private void insertUser(String username) throws Exception {
    try (Connection connection = dataSource.getConnection();
         PreparedStatement insert = connection.prepareStatement("INSERT INTO " + TABLE_NAME
             + " (ID, USER_ID, DAILY, WEEKLY) VALUES (NEXT VALUE FOR " + SEQUENCE_NAME + ", ?, ?, ?)")) {
      insert.setString(1, username);
      insert.setBoolean(2, true);
      insert.setBoolean(3, false);
      insert.executeUpdate();
    }
  }

  private void execute(String sql) throws Exception {
    try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
      statement.execute(sql);
    }
  }

}
