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
package org.exoplatform.commons.api.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;

class NotificationCompletionServiceTest {

  private NotificationCompletionService completionService;

  @AfterEach
  void tearDown() {
    if (completionService != null) {
      completionService.stop();
    }
  }

  /**
   * The configured thread-number is the size of the pool. It is set to one more
   * than the available processors, so that the pool size cannot be mistaken for
   * the processors fallback.
   */
  @Test
  void testTheConfiguredThreadNumberIsThePoolSize() throws Exception {
    int threadNumber = Runtime.getRuntime().availableProcessors() + 1;
    completionService = new NotificationCompletionService(params("true", String.valueOf(threadNumber)));

    assertEquals(threadNumber, maxConcurrentTasks(threadNumber + 2));
  }

  @Test
  void testWithoutThreadNumberThePoolHasOneThreadPerProcessor() throws Exception {
    int processors = Runtime.getRuntime().availableProcessors();
    completionService = new NotificationCompletionService(params("true", null));

    assertEquals(processors, maxConcurrentTasks(processors + 2));
  }

  @Test
  void testANonPositiveThreadNumberGivesOneThreadPerProcessor() throws Exception {
    int processors = Runtime.getRuntime().availableProcessors();
    completionService = new NotificationCompletionService(params("true", "0"));

    assertEquals(processors, maxConcurrentTasks(processors + 2));
  }

  @Test
  void testASynchronousTaskRunsOnTheCallerThreadBeforeReturning() {
    completionService = new NotificationCompletionService(params("false", "5"));
    AtomicReference<Thread> taskThread = new AtomicReference<>();

    completionService.addTask(() -> {
      taskThread.set(Thread.currentThread());
      return true;
    });

    assertSame(Thread.currentThread(), taskThread.get());
  }

  /**
   * Nothing keeps a task, nor its result, once it has run: a notification
   * platform runs one task per notification for months.
   */
  @Test
  void testACompletedTaskIsNotRetained() throws Exception {
    completionService = new NotificationCompletionService(params("false", "5"));
    AtomicReference<WeakReference<Object>> result = new AtomicReference<>();

    completionService.addTask(() -> {
      Object taskResult = new Object();
      result.set(new WeakReference<>(taskResult));
      return taskResult;
    });

    for (int i = 0; i < 50 && result.get().get() != null; i++) {
      System.gc(); // NOSONAR the reference is cleared only by a collection
      Thread.sleep(20); // NOSONAR leaves the collector the time to clear it
    }
    assertNull(result.get().get(), "The result of a completed task is still reachable");
  }

  @Test
  void testATaskOfThePoolRunsOnAPoolThread() throws Exception {
    completionService = new NotificationCompletionService(params("true", "1"));
    CountDownLatch done = new CountDownLatch(1);
    AtomicReference<Boolean> poolThread = new AtomicReference<>();

    completionService.addTask(() -> {
      poolThread.set(completionService.isPoolThread());
      done.countDown();
      return true;
    });

    assertTrue(done.await(10, TimeUnit.SECONDS));
    assertTrue(poolThread.get());
    assertFalse(completionService.isPoolThread());
  }

  @Test
  void testASynchronousTaskDoesNotRunOnAPoolThread() {
    completionService = new NotificationCompletionService(params("false", "5"));
    AtomicReference<Boolean> poolThread = new AtomicReference<>();

    completionService.addTask(() -> {
      poolThread.set(completionService.isPoolThread());
      return true;
    });

    assertFalse(poolThread.get());
  }

  /**
   * Submits blocking tasks and returns how many of them ran at the same time
   * once the pool was saturated.
   */
  private int maxConcurrentTasks(int tasksCount) throws InterruptedException {
    AtomicInteger running = new AtomicInteger();
    AtomicInteger maxRunning = new AtomicInteger();
    CountDownLatch release = new CountDownLatch(1);
    CountDownLatch finished = new CountDownLatch(tasksCount);
    for (int i = 0; i < tasksCount; i++) {
      completionService.addTask(() -> {
        maxRunning.accumulateAndGet(running.incrementAndGet(), Math::max);
        try {
          return release.await(10, TimeUnit.SECONDS);
        } finally {
          running.decrementAndGet();
          finished.countDown();
        }
      });
    }
    // Leaves the pool the time to start every task it can run at once
    long deadline = System.currentTimeMillis() + 5000;
    int previous = -1;
    while (System.currentTimeMillis() < deadline && running.get() != previous) {
      previous = running.get();
      Thread.sleep(300); // NOSONAR nothing signals that the pool started all it can run at once
    }
    release.countDown();
    assertTrue(finished.await(10, TimeUnit.SECONDS), "Tasks did not finish");
    return maxRunning.get();
  }

  private InitParams params(String asyncExecution, String threadNumber) {
    InitParams params = new InitParams();
    params.addParameter(valueParam("async-execution", asyncExecution));
    if (threadNumber != null) {
      params.addParameter(valueParam("thread-number", threadNumber));
    }
    return params;
  }

  private ValueParam valueParam(String name, String value) {
    ValueParam valueParam = new ValueParam();
    valueParam.setName(name);
    valueParam.setValue(value);
    return valueParam;
  }

}
