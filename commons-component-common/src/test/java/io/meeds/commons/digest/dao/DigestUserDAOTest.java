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
 * Runs the hand-written JPQL of the work list DAO (the claim and its release)
 * through the real engine, on the kernel persistence unit that knows the
 * entities: a statement Hibernate refuses, or that behaves differently from
 * what the sender expects, fails here and not at 18:00 in production. The
 * Spring Data repository itself is not available in this container, the
 * statements are shared with it through the DAO constants.
 */
public class DigestUserDAOTest extends BaseTest {

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

}
