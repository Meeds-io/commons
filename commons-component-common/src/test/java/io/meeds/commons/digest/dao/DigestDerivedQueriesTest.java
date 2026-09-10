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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;

import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.notification.impl.jpa.email.entity.MailQueueEntity;
import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.jpa.BaseTest;

import io.meeds.commons.digest.entity.DigestItemEntity;
import io.meeds.commons.digest.entity.DigestUserEntity;

import jakarta.persistence.EntityManager;

/**
 * Runs every derived query of the digest repositories through the real Spring
 * Data proxy, on the kernel persistence unit and HSQLDB: a method name Spring
 * Data can't derive, or that Hibernate refuses, fails here and not when the
 * social webapp builds its Spring context at startup. The hand-written JPQL
 * constants are covered by {@link DigestItemDAOTest} and
 * {@link DigestUserDAOTest}; this test also runs them through the proxy once.
 */
public class DigestDerivedQueriesTest extends BaseTest {

  private static final Instant NOW    = Instant.now().truncatedTo(ChronoUnit.SECONDS);

  private static final Instant BEFORE = NOW.minus(1, ChronoUnit.HOURS);

  private EntityManager        entityManager;

  private DigestItemDAO        itemDAO;

  private DigestUserDAO        userDAO;

  private DigestMailQueueDAO   mailQueueDAO;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    entityManager = getService(EntityManagerService.class).getEntityManager();
    // The very proxies the Spring context builds, on the very EntityManager
    JpaRepositoryFactory factory = new JpaRepositoryFactory(entityManager);
    itemDAO = factory.getRepository(DigestItemDAO.class);
    userDAO = factory.getRepository(DigestUserDAO.class);
    mailQueueDAO = factory.getRepository(DigestMailQueueDAO.class);
    entityManager.getTransaction().begin();
    entityManager.createQuery("DELETE FROM DigestItem").executeUpdate();
    entityManager.createQuery("DELETE FROM DigestUser").executeUpdate();
  }

  @Override
  protected void tearDown() throws Exception {
    entityManager.createQuery("DELETE FROM DigestItem").executeUpdate();
    entityManager.createQuery("DELETE FROM DigestUser").executeUpdate();
    if (entityManager.getTransaction().isActive()) {
      entityManager.getTransaction().rollback();
    }
    super.tearDown();
  }

  public void testItemQueriesDeriveAndRun() {
    DigestItemEntity waiting = itemDAO.save(new DigestItemEntity(null, "ayoub", "SpaceInvitationPlugin", "spaces", BEFORE, "{\"spaceId\":\"42\"}"));
    itemDAO.save(new DigestItemEntity(null, "ayoub", "SpaceInvitationPlugin", "spaces", NOW, "{\"spaceId\":\"43\"}"));
    itemDAO.save(new DigestItemEntity(null, "mary", "SpaceInvitationPlugin", "spaces", NOW, "{\"spaceId\":\"42\"}"));
    entityManager.flush();

    // The dedup of the capture: who already waits for this very notification
    List<DigestItemEntity> same = itemDAO.findByUserIdInAndPluginIdAndParams(List.of("ayoub", "mary", "john"),
                                                                             "SpaceInvitationPlugin",
                                                                             "{\"spaceId\":\"42\"}");
    assertEquals(2, same.size());
    assertEquals(List.of(), itemDAO.findByUserIdInAndPluginIdAndParams(List.of("john"), "SpaceInvitationPlugin", "{\"spaceId\":\"42\"}"));

    // The discard: the items about one object, matched on a JSON fragment
    // ("_" and "%" of the fragment are literal, not LIKE wildcards)
    List<DigestItemEntity> about42 = itemDAO.findByUserIdAndPluginIdAndParamsContaining("ayoub", "SpaceInvitationPlugin", "\"spaceId\":\"42\"");
    assertEquals(1, about42.size());
    assertEquals(waiting.getId(), about42.get(0).getId());
    assertEquals(0, itemDAO.findByUserIdAndPluginIdAndParamsContaining("ayoub", "SpaceInvitationPlugin", "\"spaceId\":\"4_\"").size());

    // The assembly window: after the previous watermark, up to the new one
    // included, most recent first
    List<DigestItemEntity> window = itemDAO.findByUserIdAndItemDateGreaterThanAndItemDateLessThanEqualOrderByItemDateDesc("ayoub",
                                                                                                                          BEFORE.minusSeconds(1),
                                                                                                                          NOW);
    assertEquals(2, window.size());
    assertEquals(NOW, window.get(0).getItemDate());
    assertEquals(BEFORE, window.get(1).getItemDate());

    // The @Query statements through the proxy as well
    assertEquals(1, itemDAO.deleteCovered("ayoub", BEFORE));
    assertEquals(0, itemDAO.deleteOlderThan(BEFORE.minusSeconds(1)));
    assertEquals(2, itemDAO.deleteOrphans());
    assertEquals(0, itemDAO.deleteByUser("ayoub"));
  }

  public void testUserQueriesDeriveAndRun() {
    DigestUserEntity ayoub = userDAO.save(new DigestUserEntity(null, "ayoub", true, true, "Europe/Paris", BEFORE, BEFORE));
    userDAO.save(new DigestUserEntity(null, "mary", true, false, "Europe/Paris", NOW, null));
    userDAO.save(new DigestUserEntity(null, "john", false, true, null, null, BEFORE));
    entityManager.flush();

    assertEquals(ayoub.getId(), userDAO.findByUserId("ayoub").getId());
    assertNull(userDAO.findByUserId("nobody"));

    // The candidates of the sender: frequency on, watermark before the cutoff,
    // one page at a time, sorted by id so that the pages never overlap
    Page<DigestUserEntity> daily = userDAO.findByDailyTrueAndDailyLastSentBefore(NOW, PageRequest.of(0, 1, Sort.by("id")));
    assertEquals(1, daily.getTotalElements());
    assertEquals("ayoub", daily.getContent().get(0).getUserId());
    assertFalse(daily.hasNext());
    Page<DigestUserEntity> weekly = userDAO.findByWeeklyTrueAndWeeklyLastSentBefore(NOW, PageRequest.of(0, 1, Sort.by("id")));
    assertEquals(2, weekly.getTotalElements());
    assertTrue(weekly.hasNext());
    assertEquals("john", userDAO.findByWeeklyTrueAndWeeklyLastSentBefore(NOW, weekly.nextPageable()).getContent().get(0).getUserId());

    // The claim through the proxy
    assertEquals(1, userDAO.updateDailyWatermark(ayoub.getId(), BEFORE, NOW));
    assertEquals(0, userDAO.updateDailyWatermark(ayoub.getId(), BEFORE, NOW));
    assertEquals(1, userDAO.updateWeeklyWatermark(ayoub.getId(), BEFORE, NOW));
  }

  public void testMailQueueRowIsWrittenTheWayTheSenderJobReadsIt() {
    MessageInfo message = new MessageInfo().pluginId("digest")
                                           .from("noreply@example.com")
                                           .to("ayoub@example.com")
                                           .subject("Your daily recap")
                                           .body("<p>hi</p>")
                                           .end();
    MailQueueEntity row = new MailQueueEntity();
    row.setType(message.getPluginId());
    row.setFrom(message.getFrom());
    row.setTo(message.getTo());
    row.setSubject(message.getSubject());
    row.setBody(message.getBody());
    row.setCreationDate(java.util.Calendar.getInstance());
    long id = mailQueueDAO.save(row).getId();
    entityManager.flush();
    entityManager.clear();

    MailQueueEntity read = entityManager.find(MailQueueEntity.class, id);
    assertNotNull(read);
    assertEquals("digest", read.getType());
    assertEquals("ayoub@example.com", read.getTo());
    mailQueueDAO.deleteById(id);
  }

}
