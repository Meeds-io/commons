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
 * Checks what Liquibase really created for {@link DigestItemEntity}. The digest
 * repository is a Spring Data one, bootstrapped by the webapp and not by this
 * container, so the table is exercised here through plain SQL: it is the only
 * place where an id generated the way the entity asks for it is proven to work.
 */
public class DigestItemSchemaTest extends BaseTest {

  private static final String DATASOURCE_NAME = "java:/comp/env/exo-jpa_portal";

  private static final String TABLE_NAME      = "NTF_DIGEST_ITEMS";

  private static final String SEQUENCE_NAME   = "SEQ_NTF_DIGEST_ITEM_ID";

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
      assertEquals("NTF_DIGEST_ITEMS must hold the 6 columns of a waiting item", 6, columnCount);
    }
  }

  public void testIdSequenceIsCreated() throws Exception {
    try (Connection connection = dataSource.getConnection();
         Statement statement = connection.createStatement();
         ResultSet resultSet = statement.executeQuery("VALUES(NEXT VALUE FOR " + SEQUENCE_NAME + ")")) {
      assertTrue("The sequence generating the NTF_DIGEST_ITEMS ids must exist", resultSet.next());
      assertTrue(resultSet.getLong(1) > 0);
    }
  }

  public void testCaptureRoundTrip() throws Exception {
    Instant itemDate = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    try (Connection connection = dataSource.getConnection();
         PreparedStatement insert = connection.prepareStatement("INSERT INTO " + TABLE_NAME
             + " (ID, USER_ID, PLUGIN_ID, CATEGORY, ITEM_DATE, PARAMS)"
             + " VALUES (NEXT VALUE FOR " + SEQUENCE_NAME + ", ?, ?, ?, ?, ?)")) {
      insert.setString(1, "mary");
      insert.setString(2, "SpaceInvitationPlugin");
      insert.setString(3, "spaces");
      insert.setTimestamp(4, Timestamp.from(itemDate));
      insert.setString(5, "{\"spaceId\":\"42\"}");
      assertEquals(1, insert.executeUpdate());
    }

    try (Connection connection = dataSource.getConnection();
         Statement statement = connection.createStatement();
         ResultSet resultSet = statement.executeQuery("SELECT USER_ID, PLUGIN_ID, CATEGORY, ITEM_DATE, PARAMS FROM "
             + TABLE_NAME)) {
      assertTrue(resultSet.next());
      assertEquals("mary", resultSet.getString("USER_ID"));
      assertEquals("SpaceInvitationPlugin", resultSet.getString("PLUGIN_ID"));
      assertEquals("spaces", resultSet.getString("CATEGORY"));
      assertEquals(Timestamp.from(itemDate), resultSet.getTimestamp("ITEM_DATE"));
      assertEquals("{\"spaceId\":\"42\"}", resultSet.getString("PARAMS"));
      assertFalse(resultSet.next());
    }
  }

  private void execute(String sql) throws Exception {
    try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
      statement.execute(sql);
    }
  }

}
