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

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;

import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.notification.impl.jpa.email.entity.MailQueueEntity;
import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.jpa.BaseTest;
import org.exoplatform.services.listener.ListenerService;

import io.meeds.commons.digest.entity.DigestItemEntity;
import io.meeds.commons.digest.entity.DigestUserEntity;
import io.meeds.commons.digest.service.DigestMailQueueStorage;

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

    // The narrowing of the capture: who among these recipients has a digest
    assertEquals(List.of("ayoub", "mary"),
                 userDAO.findByUserIdIn(List.of("ayoub", "mary", "nobody"))
                        .stream()
                        .map(DigestUserEntity::getUserId)
                        .sorted()
                        .toList());
    assertTrue(userDAO.findByUserIdIn(List.of("nobody")).isEmpty());

    // The candidates of the sender: frequency on, watermark before the cutoff,
    // read by keyset on the id, one batch at a time
    List<DigestUserEntity> daily = userDAO.findByDailyTrueAndDailyLastSentBeforeAndIdGreaterThanOrderByIdAsc(NOW, 0, Limit.of(1));
    assertEquals(1, daily.size());
    assertEquals("ayoub", daily.get(0).getUserId());
    assertTrue(userDAO.findByDailyTrueAndDailyLastSentBeforeAndIdGreaterThanOrderByIdAsc(NOW, daily.get(0).getId(), Limit.of(1)).isEmpty());
    List<DigestUserEntity> weekly = userDAO.findByWeeklyTrueAndWeeklyLastSentBeforeAndIdGreaterThanOrderByIdAsc(NOW, 0, Limit.of(1));
    assertEquals(1, weekly.size());
    assertEquals("ayoub", weekly.get(0).getUserId());
    List<DigestUserEntity> nextWeekly = userDAO.findByWeeklyTrueAndWeeklyLastSentBeforeAndIdGreaterThanOrderByIdAsc(NOW, weekly.get(0).getId(), Limit.of(1));
    assertEquals("john", nextWeekly.get(0).getUserId());

    // The claim through the proxy
    assertEquals(1, userDAO.updateDailyWatermark(ayoub.getId(), BEFORE, NOW));
    assertEquals(0, userDAO.updateDailyWatermark(ayoub.getId(), BEFORE, NOW));
    assertEquals(1, userDAO.updateWeeklyWatermark(ayoub.getId(), BEFORE, NOW));
  }

  public void testASettingsSaveDoesNotUndoAWatermarkClaimedMeanwhile() {
    DigestUserEntity ayoub = userDAO.save(new DigestUserEntity(null, "ayoub", true, false, "Europe/Paris", BEFORE, null));
    entityManager.flush();
    entityManager.clear();

    // The settings save reads the row first: from here on it holds the
    // watermark as it was before the claim
    DigestUserEntity readBySettings = userDAO.findByUserId("ayoub");
    assertEquals(BEFORE, readBySettings.getDailyLastSent());

    // The sender job claims the occurrence meanwhile, with its guarded update
    assertEquals(1, userDAO.updateDailyWatermark(ayoub.getId(), BEFORE, NOW));

    // The settings save then writes its own field. Without @DynamicUpdate on
    // the entity the UPDATE carries every column, watermark included, and puts
    // it back to BEFORE: the occurrence would be served a second time
    readBySettings.setTimeZone("Asia/Tokyo");
    userDAO.saveAndFlush(readBySettings);
    entityManager.clear();

    DigestUserEntity fresh = userDAO.findByUserId("ayoub");
    assertEquals("Asia/Tokyo", fresh.getTimeZone());
    assertEquals(NOW, fresh.getDailyLastSent());
  }

  public void testMailQueueRowIsWrittenTheWayTheSenderJobReadsIt() {
    // Through the digest writer itself, on the same proxy the Spring context
    // builds; read back through the named query the legacy queue reader uses
    DigestMailQueueStorage storage = new DigestMailQueueStorage(mailQueueDAO, getService(ListenerService.class));
    entityManager.createQuery("DELETE FROM NotificationsMailQueueEntity").executeUpdate();
    MessageInfo message = new MessageInfo().pluginId("digest")
                                           .from("Platform<noreply@example.com>")
                                           .to("Ayoub Z<ayoub@example.com>")
                                           .subject("Your daily recap")
                                           .body("<p>hi</p>")
                                           .footer("footer")
                                           .end();

    storage.enqueue(message);
    entityManager.flush();
    entityManager.clear();

    List<MailQueueEntity> queued = entityManager.createNamedQuery("NotificationsMailQueueEntity.getMessagesInQueue", MailQueueEntity.class)
                                                .getResultList();
    assertEquals(1, queued.size());
    MailQueueEntity read = queued.get(0);
    assertTrue(read.getId() > 0);
    assertEquals("digest", read.getType());
    assertEquals("Platform<noreply@example.com>", read.getFrom());
    assertEquals("Ayoub Z<ayoub@example.com>", read.getTo());
    assertEquals("Your daily recap", read.getSubject());
    assertEquals("<p>hi</p>", read.getBody());
    assertEquals("footer", read.getFooter());
    assertNotNull(read.getCreationDate());
    mailQueueDAO.deleteById(read.getId());
  }

}
