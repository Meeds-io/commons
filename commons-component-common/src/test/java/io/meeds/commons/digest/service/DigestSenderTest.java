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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.service.QueueMessage;

import io.meeds.commons.digest.entity.DigestItemEntity;
import io.meeds.commons.digest.entity.DigestUserEntity;
import io.meeds.commons.digest.model.DigestFrequency;
import io.meeds.commons.digest.model.DigestUserSettings;

/**
 * One run of the sender, around the claim: who is served, what the
 * administrator switch gates, what happens on a failure, and what is deleted
 * afterwards.
 */
@RunWith(MockitoJUnitRunner.class)
public class DigestSenderTest {

  private static final String   USERNAME = "ayoub";

  private static final Instant  PREVIOUS = Instant.parse("2026-09-02T16:05:00Z");

  @Mock
  private DigestSettingStorage  settingStorage;

  @Mock
  private DigestScheduleStorage scheduleStorage;

  @Mock
  private DigestDueCalculator   dueCalculator;

  @Mock
  private DigestMailBuilder     mailBuilder;

  @Mock
  private QueueMessage          queueMessage;

  private DigestSender          sender;

  private DigestUserEntity      user;

  private DigestUserSettings    settings;

  private final AtomicInteger   containerRuns = new AtomicInteger();

  @Before
  public void setUp() throws Exception {
    // One thread and no container: the tasks run inline, the assertions are
    // deterministic; the container runs are counted, one per task
    sender = new DigestSender(settingStorage, scheduleStorage, dueCalculator, mailBuilder, queueMessage, 1, 8) {
      @Override
      protected void runInContainer(Runnable task) {
        containerRuns.incrementAndGet();
        task.run();
      }
    };
    user = new DigestUserEntity(7L, USERNAME, true, false, "Europe/Paris", PREVIOUS, null);
    settings = new DigestUserSettings(true, List.of("spaces"), false, List.of());
    lenient().when(scheduleStorage.findCandidates(eq(DigestFrequency.DAILY), any())).thenReturn(List.of(user));
    lenient().when(scheduleStorage.findCandidates(eq(DigestFrequency.WEEKLY), any())).thenReturn(List.of());
    lenient().when(dueCalculator.isDue(eq(user), eq(DigestFrequency.DAILY), any())).thenReturn(true);
    lenient().when(scheduleStorage.claim(eq(7L), eq(DigestFrequency.DAILY), eq(PREVIOUS), any())).thenReturn(true);
    lenient().when(scheduleStorage.find(7L)).thenReturn(user);
    lenient().when(settingStorage.isDigestAllowed()).thenReturn(true);
    lenient().when(settingStorage.getUserSettings(USERNAME)).thenReturn(settings);
    lenient().when(scheduleStorage.findItems(eq(USERNAME), eq(PREVIOUS), any())).thenReturn(List.of(new DigestItemEntity()));
    lenient().when(mailBuilder.build(eq(user), eq(DigestFrequency.DAILY), eq(settings), any(), eq(PREVIOUS), any()))
             .thenReturn(new MessageInfo());
    lenient().when(queueMessage.put(any())).thenReturn(true);
    lenient().when(scheduleStorage.release(anyLong(), any(), any(), any())).thenReturn(true);
  }

  @Test
  public void testBothFrequenciesOfOneUserAreServedOneAfterTheOther() throws Exception {
    // Friday: the daily and the weekly of the same user are due. The weekly
    // must read its items before the daily deletes what both have covered
    Instant weeklyPrevious = PREVIOUS.minusSeconds(3600 * 24 * 6);
    DigestUserEntity both = new DigestUserEntity(7L, USERNAME, true, true, "Europe/Paris", PREVIOUS, weeklyPrevious);
    DigestUserSettings bothSettings = new DigestUserSettings(true, List.of("spaces"), true, List.of("spaces"));
    when(scheduleStorage.findCandidates(eq(DigestFrequency.DAILY), any())).thenReturn(List.of(both));
    when(scheduleStorage.findCandidates(eq(DigestFrequency.WEEKLY), any())).thenReturn(List.of(both));
    when(dueCalculator.isDue(eq(both), any(), any())).thenReturn(true);
    when(scheduleStorage.claim(eq(7L), any(), any(), any())).thenReturn(true);
    when(scheduleStorage.find(7L)).thenReturn(both);
    when(settingStorage.getUserSettings(USERNAME)).thenReturn(bothSettings);
    when(scheduleStorage.findItems(eq(USERNAME), any(), any())).thenReturn(List.of(new DigestItemEntity()));
    when(mailBuilder.build(eq(both), any(), eq(bothSettings), any(), any(), any())).thenReturn(new MessageInfo());

    sender.processDueDigests();

    // cleanup, candidates, and ONE task for the user holding both frequencies:
    // two tasks would run in parallel with more threads and race each other
    assertEquals(3, containerRuns.get());
    InOrder order = inOrder(scheduleStorage, queueMessage);
    order.verify(scheduleStorage).claim(eq(7L), eq(DigestFrequency.DAILY), eq(PREVIOUS), any());
    order.verify(queueMessage).put(any());
    order.verify(scheduleStorage).deleteCoveredItems(eq(USERNAME), any());
    order.verify(scheduleStorage).claim(eq(7L), eq(DigestFrequency.WEEKLY), eq(weeklyPrevious), any());
    order.verify(scheduleStorage).findItems(eq(USERNAME), eq(weeklyPrevious), any());
    order.verify(queueMessage).put(any());
    order.verify(scheduleStorage).deleteCoveredItems(eq(USERNAME), any());
  }

  @Test
  public void testRunCleansThenServesTheDueUsers() throws Exception {
    sender.processDueDigests();

    verify(scheduleStorage).cleanup(any());
    verify(scheduleStorage).claim(eq(7L), eq(DigestFrequency.DAILY), eq(PREVIOUS), any());
    verify(queueMessage).put(any());
    verify(scheduleStorage).deleteCoveredItems(eq(USERNAME), any());
    verify(scheduleStorage, never()).release(anyLong(), any(), any(), any());
  }

  @Test
  public void testUserWhoseSettingsAreGoneIsForgotten() throws Exception {
    // The account was deleted, its settings purged: both frequencies read off
    when(settingStorage.getUserSettings(USERNAME)).thenReturn(new DigestUserSettings(false, List.of(), false, List.of()));

    sender.processDueDigests();

    verify(scheduleStorage).claim(eq(7L), eq(DigestFrequency.DAILY), eq(PREVIOUS), any());
    verify(scheduleStorage).forget(7L, USERNAME);
    verify(mailBuilder, never()).build(any(), any(), any(), any(), any(), any());
    verify(queueMessage, never()).put(any());
    verify(scheduleStorage, never()).deleteCoveredItems(anyString(), any());
    verify(scheduleStorage, never()).release(anyLong(), any(), any(), any());
  }

  @Test
  public void testUserNotDueIsLeftAlone() throws Exception {
    when(dueCalculator.isDue(eq(user), eq(DigestFrequency.DAILY), any())).thenReturn(false);

    sender.processDueDigests();

    verify(scheduleStorage, never()).claim(anyLong(), any(), any(), any());
    verify(queueMessage, never()).put(any());
  }

  @Test
  public void testLostClaimMeansAnotherWorkerServesTheUser() throws Exception {
    when(scheduleStorage.claim(eq(7L), eq(DigestFrequency.DAILY), eq(PREVIOUS), any())).thenReturn(false);

    sender.processDueDigests();

    verify(mailBuilder, never()).build(any(), any(), any(), any(), any(), any());
    verify(queueMessage, never()).put(any());
    verify(scheduleStorage, never()).deleteCoveredItems(anyString(), any());
  }

  @Test
  public void testAdminSwitchOffClaimsAndCleansButSendsNothing() throws Exception {
    when(settingStorage.isDigestAllowed()).thenReturn(false);

    sender.processDueDigests();

    verify(scheduleStorage).claim(eq(7L), eq(DigestFrequency.DAILY), eq(PREVIOUS), any());
    verify(mailBuilder, never()).build(any(), any(), any(), any(), any(), any());
    verify(queueMessage, never()).put(any());
    verify(scheduleStorage).deleteCoveredItems(eq(USERNAME), any());
    verify(scheduleStorage, never()).release(anyLong(), any(), any(), any());
  }

  @Test
  public void testNothingToSayMovesTheWatermarkAndDeletesTheCoveredItems() throws Exception {
    when(mailBuilder.build(any(), any(), any(), any(), any(), any())).thenReturn(null);

    sender.processDueDigests();

    verify(queueMessage, never()).put(any());
    verify(scheduleStorage).deleteCoveredItems(eq(USERNAME), any());
  }

  @Test
  public void testFailureGivesTheOccurrenceBackAndKeepsTheItems() throws Exception {
    doThrow(new IllegalStateException("smtp down")).when(queueMessage).put(any());

    sender.processDueDigests();

    verify(scheduleStorage).release(eq(7L), eq(DigestFrequency.DAILY), any(), eq(PREVIOUS));
    verify(scheduleStorage, never()).deleteCoveredItems(anyString(), any());
  }

  @Test
  public void testCoveredItemsStopAtTheOldestEnabledWatermark() throws Exception {
    Instant weeklyWatermark = PREVIOUS.minusSeconds(3600 * 24 * 3);
    DigestUserEntity fresh = new DigestUserEntity(7L, USERNAME, true, true, "Europe/Paris", Instant.now(), weeklyWatermark);
    when(scheduleStorage.find(7L)).thenReturn(fresh);

    sender.processDueDigests();

    verify(scheduleStorage).deleteCoveredItems(USERNAME, weeklyWatermark);
  }

  @Test
  public void testCleanupFailureDoesNotStopTheRun() throws Exception {
    when(scheduleStorage.cleanup(any())).thenThrow(new IllegalStateException("db hiccup"));

    sender.processDueDigests();

    verify(queueMessage).put(any());
  }

}
