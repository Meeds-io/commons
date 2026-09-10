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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;

import io.meeds.commons.digest.entity.DigestUserEntity;
import io.meeds.commons.digest.model.DigestFrequency;

/**
 * One run of the digest sender job: the safety cleanup, then the selection of
 * the users due now, then one task per due user serving his frequencies one
 * after the other, on a small worker pool. Each occurrence is one transaction
 * of {@link DigestOccurrenceProcessor}: whatever fails in it rolls back, claim
 * included, and the user is served again at the next run.
 */
@Component
public class DigestSender {

  private static final Logger              LOG                    = LoggerFactory.getLogger(DigestSender.class);

  /**
   * A user served in the last hour can't be due again (the frequencies are
   * daily at best): the query leaves him out before the exact check in Java
   */
  private static final long                CANDIDATE_CUTOFF_HOURS = 1;

  /** The candidates are read by batches of this size, never the whole table at once */
  static final int                         CANDIDATE_BATCH_SIZE   = 500;

  private final DigestScheduleStorage      scheduleStorage;

  private final DigestDueCalculator        dueCalculator;

  private final DigestOccurrenceProcessor  occurrenceProcessor;

  private final int                        threads;

  private final int                        retentionDays;

  public DigestSender(DigestScheduleStorage scheduleStorage,
                      DigestDueCalculator dueCalculator,
                      DigestOccurrenceProcessor occurrenceProcessor,
                      @Value("${exo.notification.digest.threads:4}") int threads,
                      @Value("${exo.notification.digest.retention.days:8}") int retentionDays) {
    this.scheduleStorage = scheduleStorage;
    this.dueCalculator = dueCalculator;
    this.occurrenceProcessor = occurrenceProcessor;
    this.threads = Math.max(1, threads);
    this.retentionDays = Math.max(1, retentionDays);
  }

  public void processDueDigests() {
    // Whole seconds: every database keeps them exactly, so the watermark this
    // run writes is the one it reads back, whatever the column precision
    Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
    runInContainer(() -> cleanup(now));
    // The frequencies of one user are served one after the other by the same
    // task: the deletion at the end of the first must never race the reading of
    // the items by the second
    Map<Long, DueUser> dueUsers = new LinkedHashMap<>();
    runInContainer(() -> selectDueUsers(now, dueUsers));
    if (dueUsers.isEmpty()) {
      return;
    }
    LOG.info("Digest sender: {} users due", dueUsers.size());
    List<Runnable> tasks = new ArrayList<>();
    for (DueUser dueUser : dueUsers.values()) {
      tasks.add(() -> runInContainer(() -> {
        for (DigestFrequency frequency : dueUser.frequencies) {
          serve(dueUser.user, frequency, now);
        }
      }));
    }
    if (threads == 1) {
      tasks.forEach(Runnable::run);
    } else {
      // One run at a time by construction (the scheduler is single threaded),
      // and the claim protects the data even if two runs ever overlapped
      ExecutorService pool = Executors.newFixedThreadPool(threads);
      try {
        tasks.forEach(pool::execute);
      } finally {
        pool.shutdown();
      }
      awaitQuietly(pool);
    }
  }

  private void cleanup(Instant now) {
    try {
      int deleted = scheduleStorage.cleanup(now.minus(retentionDays, ChronoUnit.DAYS));
      if (deleted > 0) {
        LOG.info("Digest cleanup: {} waiting items deleted", deleted);
      }
    } catch (Exception e) {
      LOG.warn("Digest cleanup failed, the run goes on", e);
    }
  }

  /**
   * The indexed query narrows the candidates, batch by batch on the id (keyset:
   * a candidate claimed by another node while the scan runs shifts nothing);
   * the exact "past the send hour in his timezone, not served yet today or this
   * week" check is done here, in Java, portable across databases
   */
  private void selectDueUsers(Instant now, Map<Long, DueUser> dueUsers) {
    Instant cutoff = now.minus(CANDIDATE_CUTOFF_HOURS, ChronoUnit.HOURS);
    int batchSize = candidateBatchSize();
    for (DigestFrequency frequency : DigestFrequency.values()) {
      long afterId = 0;
      List<DigestUserEntity> batch;
      do {
        batch = scheduleStorage.findCandidates(frequency, cutoff, afterId, Limit.of(batchSize));
        for (DigestUserEntity user : batch) {
          if (dueCalculator.isDue(user, frequency, now)) {
            dueUsers.computeIfAbsent(user.getId(), id -> new DueUser(user)).frequencies.add(frequency);
          }
          afterId = user.getId();
        }
      } while (batch.size() == batchSize);
    }
  }

  /** How many candidates one query reads; a test lowers it */
  protected int candidateBatchSize() {
    return CANDIDATE_BATCH_SIZE;
  }

  /**
   * One occurrence, one transaction. A failure is logged and leaves the
   * occurrence untouched: the rollback gives it back, the next run serves it
   * again with everything it covers.
   */
  void serve(DigestUserEntity user, DigestFrequency frequency, Instant now) {
    try {
      occurrenceProcessor.serve(user, frequency, now);
    } catch (Exception e) {
      LOG.warn("The {} digest of {} can't be sent now, it will be retried at the next run: {}",
               frequency,
               user.getUserId(),
               e.getMessage());
      LOG.debug("Digest failure of {}", user.getUserId(), e);
    }
  }

  /**
   * Binds the portal container and a request lifecycle to the current thread,
   * the way the kernel does for a request: the workers run on a bare pool
   * thread that has none. The previous container, if any, is restored.
   */
  protected void runInContainer(Runnable task) {
    ExoContainer previous = ExoContainerContext.getCurrentContainerIfPresent();
    ExoContainer container = PortalContainer.getInstance();
    ExoContainerContext.setCurrentContainer(container);
    RequestLifeCycle.begin(container);
    try {
      task.run();
    } catch (Exception e) {
      LOG.warn("A digest task failed", e);
    } finally {
      RequestLifeCycle.end();
      ExoContainerContext.setCurrentContainer(previous);
    }
  }

  private void awaitQuietly(ExecutorService pool) {
    try {
      if (!pool.awaitTermination(1, TimeUnit.HOURS)) {
        LOG.warn("The digest sender didn't finish within an hour, the remaining occurrences are served at the next run");
        pool.shutdownNow();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      pool.shutdownNow();
    }
  }

  /** A due user and the frequencies to serve him, in order */
  private static final class DueUser {

    private final DigestUserEntity      user;

    private final List<DigestFrequency> frequencies = new ArrayList<>();

    private DueUser(DigestUserEntity user) {
      this.user = user;
    }
  }

}
