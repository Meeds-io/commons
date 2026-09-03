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
package io.meeds.commons.digest.job;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import io.meeds.commons.digest.DigestService;

/**
 * The job glue: one run at a time on its own thread, an occurrence skipped
 * while a run is going, and a failed run never blocks the next one.
 */
@RunWith(MockitoJUnitRunner.class)
public class DigestSenderJobTest {

  @Mock
  private DigestService   digestService;

  private DigestSenderJob job;

  @Before
  public void setUp() {
    job = new DigestSenderJob(digestService);
  }

  @After
  public void tearDown() {
    job.stop();
  }

  @Test
  public void testOccurrenceIsSkippedWhileTheRunIsGoingThenAcceptedAgain() throws Exception {
    CountDownLatch started = new CountDownLatch(1);
    CountDownLatch release = new CountDownLatch(1);
    doAnswer(call -> {
      started.countDown();
      release.await(5, TimeUnit.SECONDS);
      return null;
    }).when(digestService).processDueDigests();

    job.run();
    started.await(5, TimeUnit.SECONDS);
    job.run(); // the previous run is still going: skipped
    verify(digestService, times(1)).processDueDigests();

    release.countDown();
    verify(digestService, timeout(5000).times(1)).processDueDigests();
    waitUntilIdle();
    job.run();
    verify(digestService, timeout(5000).times(2)).processDueDigests();
  }

  @Test
  public void testFailedRunDoesNotBlockTheNextOne() throws Exception {
    doThrow(new IllegalStateException("db down")).doNothing().when(digestService).processDueDigests();

    job.run();
    verify(digestService, timeout(5000).times(1)).processDueDigests();
    waitUntilIdle();
    job.run();
    verify(digestService, timeout(5000).times(2)).processDueDigests();
  }

  private void waitUntilIdle() throws InterruptedException {
    long deadline = System.currentTimeMillis() + 5000;
    while (job.isRunning() && System.currentTimeMillis() < deadline) {
      Thread.sleep(10);
    }
  }

}
