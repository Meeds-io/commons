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
package io.meeds.commons.digest.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.liquibase.autoconfigure.LiquibaseAutoConfiguration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;

import org.exoplatform.commons.api.notification.model.MessageInfo;

import io.meeds.commons.digest.dao.DigestItemDAO;
import io.meeds.commons.digest.dao.DigestUserDAO;
import io.meeds.commons.digest.entity.DigestItemEntity;
import io.meeds.commons.digest.entity.DigestUserEntity;
import io.meeds.commons.digest.model.DigestFrequency;
import io.meeds.commons.digest.model.DigestUserSettings;
import io.meeds.kernel.test.AbstractSpringTest;
import io.meeds.spring.AvailableIntegration;

/**
 * The atomicity of one digest occurrence, through the real Spring transaction
 * manager over the kernel persistence unit (HSQLDB), the way the social webapp
 * hosts these beans: when the mail queue refuses the email after the claim,
 * the watermark and the waiting items are exactly as before; when it accepts,
 * the watermark has moved and the covered items are gone. The settings, the
 * builder and the queue writer are mocked: the transaction is what is under
 * test, not what they do.
 */
@SpringBootApplication(scanBasePackages = {
  "io.meeds.commons.digest",
  AvailableIntegration.KERNEL_TEST_MODULE,
  AvailableIntegration.JPA_MODULE,
}, exclude = {
  LiquibaseAutoConfiguration.class
})
// The repositories the social webapp declares for these beans (SocialApplication)
@EnableJpaRepositories(basePackages = "io.meeds.commons.digest")
@PropertySource("classpath:application.properties")
@PropertySource("classpath:application-common.properties")
// The scanned package holds the hourly job: its scheduler must never fire under
// these assertions ("-" is Scheduled.CRON_DISABLED)
@TestPropertySource(properties = "exo.notification.digest.job.expression=-")
@RunWith(SpringRunner.class)
public class DigestOccurrenceTransactionTest extends AbstractSpringTest {

  private static final String       USERNAME = "digest-tx-user";

  private static final Instant      NOW      = Instant.now().truncatedTo(ChronoUnit.SECONDS);

  private static final Instant      PREVIOUS = NOW.minus(1, ChronoUnit.DAYS);

  @MockitoBean
  private DigestSettingStorage      settingStorage;

  @MockitoBean
  private DigestMailBuilder         mailBuilder;

  @MockitoBean
  private DigestMailQueueStorage    mailQueueStorage;

  @Autowired
  private DigestOccurrenceProcessor processor;

  @Autowired
  private DigestUserDAO             userDAO;

  @Autowired
  private DigestItemDAO             itemDAO;

  private DigestUserEntity          user;

  public DigestOccurrenceTransactionTest() {
    AbstractSpringTest.setTestClass(DigestOccurrenceTransactionTest.class);
  }

  @Before
  public void setUp() {
    begin();
    itemDAO.deleteAll();
    userDAO.deleteAll();
    user = userDAO.save(new DigestUserEntity(null, USERNAME, true, false, "Europe/Paris", PREVIOUS, null));
    itemDAO.save(new DigestItemEntity(null, USERNAME, "SpaceInvitationPlugin", "spaces", NOW.minus(1, ChronoUnit.HOURS), "{\"spaceId\":\"42\"}"));
    when(settingStorage.isDigestAllowed()).thenReturn(true);
    when(settingStorage.getUserSettings(anyString())).thenReturn(new DigestUserSettings(true, List.of("spaces"), false, List.of()));
    when(mailBuilder.build(any(), any(), any(), any(), any(), any())).thenReturn(new MessageInfo().to("digest@example.com").end());
  }

  @After
  public void tearDown() {
    itemDAO.deleteAll();
    userDAO.deleteAll();
    end();
  }

  @Test
  public void testQueueFailureAfterTheClaimLeavesTheWatermarkAndTheItemsUntouched() {
    doThrow(new IllegalStateException("queue down")).when(mailQueueStorage).enqueue(any());

    assertThrows(IllegalStateException.class, () -> processor.serve(user, DigestFrequency.DAILY, NOW));

    DigestUserEntity fresh = userDAO.findById(user.getId()).orElseThrow();
    assertEquals("The claim must roll back with the failed queueing", PREVIOUS, fresh.getDailyLastSent());
    assertEquals("The waiting item must still be there for the next run", 1, waitingItems());
  }

  @Test
  public void testAcceptedQueueingCommitsTheClaimAndTheCoveredDeletionTogether() {
    doNothing().when(mailQueueStorage).enqueue(any());

    assertTrue(processor.serve(user, DigestFrequency.DAILY, NOW));

    DigestUserEntity fresh = userDAO.findById(user.getId()).orElseThrow();
    assertEquals(NOW, fresh.getDailyLastSent());
    assertEquals("The covered item is deleted in the same transaction", 0, waitingItems());
  }

  private int waitingItems() {
    return itemDAO.findByUserIdAndItemDateGreaterThanAndItemDateLessThanEqualOrderByItemDateDesc(USERNAME, PREVIOUS, NOW).size();
  }

}
