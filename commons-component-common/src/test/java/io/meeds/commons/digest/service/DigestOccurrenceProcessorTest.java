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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.transaction.annotation.Transactional;

import org.exoplatform.commons.api.notification.model.MessageInfo;

import io.meeds.commons.digest.entity.DigestItemEntity;
import io.meeds.commons.digest.entity.DigestUserEntity;
import io.meeds.commons.digest.model.DigestFrequency;
import io.meeds.commons.digest.model.DigestUserSettings;

/**
 * One occurrence, around the claim: who gets it, what the administrator switch
 * gates, what a failure does, and what is deleted afterwards. The atomicity
 * itself is Spring's: the test pins that the method is transactional and rolls
 * back on every exception, and that nothing compensates by hand any more.
 */
@RunWith(MockitoJUnitRunner.class)
public class DigestOccurrenceProcessorTest {

  private static final String       USERNAME = "ayoub";

  private static final Instant      PREVIOUS = Instant.parse("2026-09-02T16:05:00Z");

  private static final Instant      NOW      = Instant.parse("2026-09-03T16:00:00Z");

  @Mock
  private DigestSettingStorage      settingStorage;

  @Mock
  private DigestScheduleStorage     scheduleStorage;

  @Mock
  private DigestMailBuilder         mailBuilder;

  @Mock
  private DigestMailQueueStorage    mailQueueStorage;

  private DigestOccurrenceProcessor processor;

  private DigestUserEntity          user;

  private DigestUserSettings        settings;

  @Before
  public void setUp() {
    processor = new DigestOccurrenceProcessor(settingStorage, scheduleStorage, mailBuilder, mailQueueStorage);
    user = new DigestUserEntity(7L, USERNAME, true, false, "Europe/Paris", PREVIOUS, null);
    settings = new DigestUserSettings(true, List.of("spaces"), false, List.of());
    lenient().when(scheduleStorage.claim(7L, DigestFrequency.DAILY, PREVIOUS, NOW)).thenReturn(true);
    lenient().when(scheduleStorage.find(7L)).thenReturn(user);
    lenient().when(settingStorage.isDigestAllowed()).thenReturn(true);
    lenient().when(settingStorage.getUserSettings(USERNAME)).thenReturn(settings);
    lenient().when(scheduleStorage.findItems(USERNAME, PREVIOUS, NOW)).thenReturn(List.of(new DigestItemEntity()));
    lenient().when(mailBuilder.build(eq(user), eq(DigestFrequency.DAILY), eq(settings), any(), eq(PREVIOUS), eq(NOW)))
             .thenReturn(new MessageInfo());
  }

  @Test
  public void testServeIsOneTransactionThatRollsBackOnAnyException() throws Exception {
    // The claim, the queue row and the deletion commit together or not at all:
    // a crash in between leaves the occurrence to the next run. Checked
    // exceptions must roll back too, Spring's default only covers runtime ones
    Method serve = DigestOccurrenceProcessor.class.getMethod("serve", DigestUserEntity.class, DigestFrequency.class, Instant.class);
    Transactional transactional = serve.getAnnotation(Transactional.class);
    assertTrue(transactional != null);
    assertTrue(List.of(transactional.rollbackFor()).contains(Exception.class));
  }

  @Test
  public void testOccurrenceClaimsBuildsQueuesThenDeletesTheCoveredItems() {
    assertTrue(processor.serve(user, DigestFrequency.DAILY, NOW));

    InOrder order = inOrder(scheduleStorage, mailBuilder, mailQueueStorage);
    order.verify(scheduleStorage).claim(7L, DigestFrequency.DAILY, PREVIOUS, NOW);
    order.verify(scheduleStorage).findItems(USERNAME, PREVIOUS, NOW);
    order.verify(mailBuilder).build(eq(user), eq(DigestFrequency.DAILY), eq(settings), any(), eq(PREVIOUS), eq(NOW));
    order.verify(mailQueueStorage).enqueue(any());
    order.verify(scheduleStorage).deleteCoveredItems(eq(USERNAME), any());
  }

  @Test
  public void testLostClaimMeansAnotherWorkerServesTheUser() {
    when(scheduleStorage.claim(7L, DigestFrequency.DAILY, PREVIOUS, NOW)).thenReturn(false);

    assertFalse(processor.serve(user, DigestFrequency.DAILY, NOW));

    verify(mailBuilder, never()).build(any(), any(), any(), any(), any(), any());
    verify(mailQueueStorage, never()).enqueue(any());
    verify(scheduleStorage, never()).deleteCoveredItems(anyString(), any());
  }

  @Test
  public void testUserWhoseSettingsAreGoneIsForgotten() {
    // The account was deleted, its settings purged: both frequencies read off
    when(settingStorage.getUserSettings(USERNAME)).thenReturn(new DigestUserSettings(false, List.of(), false, List.of()));

    assertTrue(processor.serve(user, DigestFrequency.DAILY, NOW));

    verify(scheduleStorage).forget(7L, USERNAME);
    verify(mailBuilder, never()).build(any(), any(), any(), any(), any(), any());
    verify(mailQueueStorage, never()).enqueue(any());
    verify(scheduleStorage, never()).deleteCoveredItems(anyString(), any());
  }

  @Test
  public void testAdminSwitchOffClaimsAndCleansButSendsNothing() {
    when(settingStorage.isDigestAllowed()).thenReturn(false);

    assertTrue(processor.serve(user, DigestFrequency.DAILY, NOW));

    verify(scheduleStorage).claim(7L, DigestFrequency.DAILY, PREVIOUS, NOW);
    verify(mailBuilder, never()).build(any(), any(), any(), any(), any(), any());
    verify(mailQueueStorage, never()).enqueue(any());
    verify(scheduleStorage).deleteCoveredItems(eq(USERNAME), any());
  }

  @Test
  public void testFrequencyOffInTheSettingsClaimsAndCleansButSendsNothing() {
    // The row says weekly, the settings say daily only: the settings win
    DigestUserEntity weeklyRow = new DigestUserEntity(7L, USERNAME, true, true, "Europe/Paris", PREVIOUS, PREVIOUS);
    when(scheduleStorage.claim(7L, DigestFrequency.WEEKLY, PREVIOUS, NOW)).thenReturn(true);

    assertTrue(processor.serve(weeklyRow, DigestFrequency.WEEKLY, NOW));

    verify(mailBuilder, never()).build(any(), any(), any(), any(), any(), any());
    verify(scheduleStorage).deleteCoveredItems(eq(USERNAME), any());
  }

  @Test
  public void testNothingToSayQueuesNoEmailButStillDeletesTheCoveredItems() {
    when(mailBuilder.build(any(), any(), any(), any(), any(), any())).thenReturn(null);

    assertTrue(processor.serve(user, DigestFrequency.DAILY, NOW));

    verify(mailQueueStorage, never()).enqueue(any());
    verify(scheduleStorage).deleteCoveredItems(eq(USERNAME), any());
  }

  @Test
  public void testUnusableRecipientIsConsumedLikeAnEmptyOccurrenceNotRetried() {
    // The builder answers null for a recipient with no usable address (a
    // property of the user, not of the run): the occurrence commits, the
    // covered items go, nothing is thrown, nothing is retried every hour
    when(mailBuilder.build(any(), any(), any(), any(), any(), any())).thenReturn(null);

    assertTrue(processor.serve(user, DigestFrequency.DAILY, NOW));

    verify(mailQueueStorage, never()).enqueue(any());
    verify(scheduleStorage).deleteCoveredItems(eq(USERNAME), any());
  }

  @Test
  public void testFailureReachesTheCallerAndNothingIsCompensatedByHand() {
    // No release, no manual rollback: the exception leaves the transactional
    // method and Spring rolls everything back, the claim included
    doThrow(new IllegalStateException("db down")).when(mailQueueStorage).enqueue(any());

    assertThrows(IllegalStateException.class, () -> processor.serve(user, DigestFrequency.DAILY, NOW));

    verify(scheduleStorage, never()).deleteCoveredItems(anyString(), any());
    verify(scheduleStorage, never()).forget(any(Long.class), anyString());
  }

  @Test
  public void testCoveredItemsStopAtTheOldestEnabledWatermark() {
    Instant weeklyWatermark = PREVIOUS.minusSeconds(3600 * 24 * 3);
    DigestUserEntity fresh = new DigestUserEntity(7L, USERNAME, true, true, "Europe/Paris", NOW, weeklyWatermark);
    when(scheduleStorage.find(7L)).thenReturn(fresh);

    processor.serve(user, DigestFrequency.DAILY, NOW);

    verify(scheduleStorage).deleteCoveredItems(USERNAME, weeklyWatermark);
  }

}
