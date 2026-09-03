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
package io.meeds.commons.digest.dao;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import jakarta.persistence.EntityManager;

import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.jpa.BaseTest;

import io.meeds.commons.digest.entity.DigestItemEntity;
import io.meeds.commons.digest.entity.DigestUserEntity;

/**
 * Runs the hand-written JPQL statements of the digest DAOs through the real
 * engine, on the kernel persistence unit that knows the two entities: a
 * statement Hibernate refuses, or that behaves differently from what the
 * sender expects, fails here and not at 18:00 in production. The Spring Data
 * repositories themselves are not available in this container, the statements
 * are shared with them through the DAO constants.
 */
public class DigestQueriesEngineTest extends BaseTest {

  private static final Instant NOW    = Instant.now().truncatedTo(ChronoUnit.SECONDS);

  private static final Instant BEFORE = NOW.minus(1, ChronoUnit.HOURS);

  private EntityManager        entityManager;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    entityManager = getService(EntityManagerService.class).getEntityManager();
    entityManager.getTransaction().begin();
    entityManager.createQuery("DELETE FROM DigestItem").executeUpdate();
    entityManager.createQuery("DELETE FROM DigestUser").executeUpdate();
  }

  @Override
  protected void tearDown() throws Exception {
    entityManager.createQuery("DELETE FROM DigestItem").executeUpdate();
    entityManager.createQuery("DELETE FROM DigestUser").executeUpdate();
    super.tearDown();
  }

  public void testClaimMovesTheWatermarkOnlyWhenItStillHoldsTheReadValue() {
    DigestUserEntity user = persistUser("ayoub", BEFORE, null);

    int firstClaim = entityManager.createQuery(DigestUserDAO.UPDATE_DAILY_WATERMARK_QUERY)
                                  .setParameter("id", user.getId())
                                  .setParameter("expected", BEFORE)
                                  .setParameter("value", NOW)
                                  .executeUpdate();
    int secondClaim = entityManager.createQuery(DigestUserDAO.UPDATE_DAILY_WATERMARK_QUERY)
                                   .setParameter("id", user.getId())
                                   .setParameter("expected", BEFORE)
                                   .setParameter("value", NOW)
                                   .executeUpdate();
    assertEquals("The first worker gets the occurrence", 1, firstClaim);
    assertEquals("The second worker read a stale watermark and gets nothing", 0, secondClaim);

    entityManager.clear();
    assertEquals(NOW, entityManager.find(DigestUserEntity.class, user.getId()).getDailyLastSent());

    // Giving the occurrence back is the same statement the other way round
    int release = entityManager.createQuery(DigestUserDAO.UPDATE_DAILY_WATERMARK_QUERY)
                               .setParameter("id", user.getId())
                               .setParameter("expected", NOW)
                               .setParameter("value", BEFORE)
                               .executeUpdate();
    assertEquals(1, release);
  }

  public void testWeeklyClaimIsIndependentFromTheDailyOne() {
    DigestUserEntity user = persistUser("ayoub", BEFORE, BEFORE);

    int weeklyClaim = entityManager.createQuery(DigestUserDAO.UPDATE_WEEKLY_WATERMARK_QUERY)
                                   .setParameter("id", user.getId())
                                   .setParameter("expected", BEFORE)
                                   .setParameter("value", NOW)
                                   .executeUpdate();
    assertEquals(1, weeklyClaim);

    entityManager.clear();
    DigestUserEntity fresh = entityManager.find(DigestUserEntity.class, user.getId());
    assertEquals(NOW, fresh.getWeeklyLastSent());
    assertEquals(BEFORE, fresh.getDailyLastSent());
  }

  public void testCoveredItemsAreDeletedUpToTheWatermarkIncluded() {
    persistUser("ayoub", NOW, null);
    persistItem("ayoub", BEFORE);
    persistItem("ayoub", NOW);
    persistItem("ayoub", NOW.plusSeconds(1));
    persistItem("mary", BEFORE);

    int deleted = entityManager.createQuery(DigestItemDAO.DELETE_COVERED_QUERY)
                               .setParameter("userId", "ayoub")
                               .setParameter("until", NOW)
                               .executeUpdate();
    assertEquals("The items at or before the watermark of this user only", 2, deleted);
    assertEquals(List.of(NOW.plusSeconds(1)), itemDatesOf("ayoub"));
    assertEquals(1, itemDatesOf("mary").size());
  }

  public void testRetentionDeletesTheOldItemsOfEverybody() {
    persistItem("ayoub", BEFORE);
    persistItem("mary", BEFORE);
    persistItem("mary", NOW);

    int deleted = entityManager.createQuery(DigestItemDAO.DELETE_OLDER_THAN_QUERY)
                               .setParameter("before", NOW)
                               .executeUpdate();
    assertEquals(2, deleted);
    assertEquals(List.of(NOW), itemDatesOf("mary"));
  }

  public void testOrphanItemsOfUsersWithoutDigestAreDeleted() {
    persistUser("ayoub", NOW, null);
    persistItem("ayoub", NOW);
    persistItem("gone", NOW);
    persistItem("gone", BEFORE);

    int deleted = entityManager.createQuery(DigestItemDAO.DELETE_ORPHANS_QUERY).executeUpdate();
    assertEquals("The items of the user who has no digest enabled any more", 2, deleted);
    assertEquals(1, itemDatesOf("ayoub").size());
    assertEquals(0, itemDatesOf("gone").size());
  }

  private DigestUserEntity persistUser(String username, Instant dailyLastSent, Instant weeklyLastSent) {
    DigestUserEntity user = new DigestUserEntity(null,
                                                 username,
                                                 dailyLastSent != null,
                                                 weeklyLastSent != null,
                                                 "Europe/Paris",
                                                 dailyLastSent,
                                                 weeklyLastSent);
    entityManager.persist(user);
    entityManager.flush();
    return user;
  }

  private void persistItem(String username, Instant itemDate) {
    entityManager.persist(new DigestItemEntity(null, username, "SpaceInvitationPlugin", "spaces", itemDate, "{\"spaceId\":\"1\"}"));
    entityManager.flush();
  }

  private List<Instant> itemDatesOf(String username) {
    entityManager.clear();
    return entityManager.createQuery("SELECT i.itemDate FROM DigestItem i WHERE i.userId = :userId ORDER BY i.itemDate",
                                     Instant.class)
                        .setParameter("userId", username)
                        .getResultList();
  }

}
