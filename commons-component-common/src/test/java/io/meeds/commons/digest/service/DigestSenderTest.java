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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Limit;

import io.meeds.commons.digest.entity.DigestUserEntity;
import io.meeds.commons.digest.model.DigestFrequency;

/**
 * One run of the sender: the cleanup first, the candidates read batch by batch
 * and checked in Java, one task per due user serving his frequencies in order,
 * and a failing occurrence that never stops the run. What happens inside an
 * occurrence is {@link DigestOccurrenceProcessorTest}.
 */
@RunWith(MockitoJUnitRunner.class)
public class DigestSenderTest {

  private static final String       USERNAME = "ayoub";

  private static final Instant      PREVIOUS = Instant.parse("2026-09-02T16:05:00Z");

  @Mock
  private DigestScheduleStorage     scheduleStorage;

  @Mock
  private DigestDueCalculator       dueCalculator;

  @Mock
  private DigestOccurrenceProcessor occurrenceProcessor;

  private DigestSender              sender;

  private DigestUserEntity          user;

  private final AtomicInteger       containerRuns = new AtomicInteger();

  private int                       batchSize     = DigestSender.CANDIDATE_BATCH_SIZE;

  @Before
  public void setUp() throws Exception {
    // One thread and no container: the tasks run inline, the assertions are
    // deterministic; the container runs are counted, one per task
    sender = new DigestSender(scheduleStorage, dueCalculator, occurrenceProcessor, 1, 8) {
      @Override
      protected void runInContainer(Runnable task) {
        containerRuns.incrementAndGet();
        task.run();
      }

      @Override
      protected int candidateBatchSize() {
        return batchSize;
      }
    };
    user = new DigestUserEntity(7L, USERNAME, true, false, "Europe/Paris", PREVIOUS, null);
    lenient().when(scheduleStorage.findCandidates(eq(DigestFrequency.DAILY), any(), anyLong(), any())).thenReturn(List.of(user));
    lenient().when(scheduleStorage.findCandidates(eq(DigestFrequency.WEEKLY), any(), anyLong(), any())).thenReturn(List.of());
    lenient().when(dueCalculator.isDue(eq(user), eq(DigestFrequency.DAILY), any())).thenReturn(true);
    lenient().when(occurrenceProcessor.serve(any(), any(), any())).thenReturn(true);
  }

  @Test
  public void testRunCleansThenServesTheDueUsers() {
    sender.processDueDigests();

    InOrder order = inOrder(scheduleStorage, occurrenceProcessor);
    order.verify(scheduleStorage).cleanup(any());
    order.verify(occurrenceProcessor).serve(eq(user), eq(DigestFrequency.DAILY), any());
    // cleanup, candidates, one task for the user
    assertEquals(3, containerRuns.get());
  }

  @Test
  public void testBothFrequenciesOfOneUserAreServedOneAfterTheOtherByTheSameTask() {
    // Friday: the daily and the weekly of the same user are due. The weekly
    // must read its items before the daily deletes what both have covered, so
    // the same task serves both, in this order
    DigestUserEntity both = new DigestUserEntity(7L, USERNAME, true, true, "Europe/Paris", PREVIOUS, PREVIOUS.minusSeconds(3600 * 24 * 6));
    when(scheduleStorage.findCandidates(eq(DigestFrequency.DAILY), any(), anyLong(), any())).thenReturn(List.of(both));
    when(scheduleStorage.findCandidates(eq(DigestFrequency.WEEKLY), any(), anyLong(), any())).thenReturn(List.of(both));
    when(dueCalculator.isDue(eq(both), any(), any())).thenReturn(true);

    sender.processDueDigests();

    // cleanup, candidates, and ONE task for the user holding both frequencies:
    // two tasks would run in parallel with more threads and race each other
    assertEquals(3, containerRuns.get());
    InOrder order = inOrder(occurrenceProcessor);
    order.verify(occurrenceProcessor).serve(eq(both), eq(DigestFrequency.DAILY), any());
    order.verify(occurrenceProcessor).serve(eq(both), eq(DigestFrequency.WEEKLY), any());
  }

  @Test
  public void testUserNotDueIsLeftAlone() {
    when(dueCalculator.isDue(eq(user), eq(DigestFrequency.DAILY), any())).thenReturn(false);

    sender.processDueDigests();

    verify(occurrenceProcessor, never()).serve(any(), any(), any());
  }

  @Test
  public void testCandidatesAreReadBatchByBatchOnTheirId() {
    // Batches of one: the scan asks for the ids after the last one seen until a
    // batch comes back short, and the same user is never served twice
    batchSize = 1;
    DigestUserEntity second = new DigestUserEntity(8L, "mary", true, false, "Europe/Paris", PREVIOUS, null);
    when(scheduleStorage.findCandidates(eq(DigestFrequency.DAILY), any(), anyLong(), any())).thenReturn(List.of(user),
                                                                                                       List.of(second),
                                                                                                       List.of());
    when(dueCalculator.isDue(any(), eq(DigestFrequency.DAILY), any())).thenReturn(true);

    sender.processDueDigests();

    ArgumentCaptor<Long> afterIds = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<Limit> limits = ArgumentCaptor.forClass(Limit.class);
    verify(scheduleStorage, times(3)).findCandidates(eq(DigestFrequency.DAILY), any(), afterIds.capture(), limits.capture());
    assertEquals(List.of(0L, 7L, 8L), afterIds.getAllValues());
    assertEquals(1, limits.getAllValues().get(0).max());
    verify(occurrenceProcessor).serve(eq(user), eq(DigestFrequency.DAILY), any());
    verify(occurrenceProcessor).serve(eq(second), eq(DigestFrequency.DAILY), any());
  }

  @Test
  public void testFailingOccurrenceIsLoggedAndTheNextFrequencyIsStillServed() {
    // The transaction of the failing occurrence rolled back on its own: the
    // sender has nothing to give back, it only goes on with the next one
    DigestUserEntity both = new DigestUserEntity(7L, USERNAME, true, true, "Europe/Paris", PREVIOUS, PREVIOUS);
    when(scheduleStorage.findCandidates(eq(DigestFrequency.DAILY), any(), anyLong(), any())).thenReturn(List.of(both));
    when(scheduleStorage.findCandidates(eq(DigestFrequency.WEEKLY), any(), anyLong(), any())).thenReturn(List.of(both));
    when(dueCalculator.isDue(eq(both), any(), any())).thenReturn(true);
    doThrow(new IllegalStateException("smtp down")).when(occurrenceProcessor).serve(eq(both), eq(DigestFrequency.DAILY), any());

    sender.processDueDigests();

    verify(occurrenceProcessor).serve(eq(both), eq(DigestFrequency.WEEKLY), any());
  }

  @Test
  public void testCleanupFailureDoesNotStopTheRun() {
    when(scheduleStorage.cleanup(any())).thenThrow(new IllegalStateException("db hiccup"));

    sender.processDueDigests();

    verify(occurrenceProcessor).serve(eq(user), eq(DigestFrequency.DAILY), any());
  }

}
