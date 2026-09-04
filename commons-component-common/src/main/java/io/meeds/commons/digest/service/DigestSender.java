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
import org.springframework.stereotype.Component;

import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.service.QueueMessage;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;

import io.meeds.commons.digest.entity.DigestItemEntity;
import io.meeds.commons.digest.entity.DigestUserEntity;
import io.meeds.commons.digest.model.DigestFrequency;
import io.meeds.commons.digest.model.DigestUserSettings;

/**
 * One run of the digest sender: the safety cleanup, then every due user is
 * claimed, served and cleaned. Several servers may run it at the same time: the
 * claim (a guarded update of the watermark) makes sure an occurrence is served
 * once. The administrator switch gates only the email: when it is off the
 * occurrence is still claimed and the covered items still deleted, exactly as
 * if the user had unchecked every category.
 */
@Component
public class DigestSender {

  private static final Logger         LOG                    = LoggerFactory.getLogger(DigestSender.class);

  /**
   * A user served during the last hour was served by this very run or by the
   * previous one: the pre-filter only prunes those, the exact local calendar
   * check does the rest
   */
  private static final long           CANDIDATE_CUTOFF_HOURS = 1;

  private final DigestSettingStorage  settingStorage;

  private final DigestScheduleStorage scheduleStorage;

  private final DigestDueCalculator   dueCalculator;

  private final DigestMailBuilder     mailBuilder;

  private final QueueMessage          queueMessage;

  private final int                   threads;

  private final int                   retentionDays;

  public DigestSender(DigestSettingStorage settingStorage,
                      DigestScheduleStorage scheduleStorage,
                      DigestDueCalculator dueCalculator,
                      DigestMailBuilder mailBuilder,
                      QueueMessage queueMessage,
                      @Value("${exo.notification.digest.threads:4}") int threads,
                      @Value("${exo.notification.digest.retention.days:8}") int retentionDays) {
    this.settingStorage = settingStorage;
    this.scheduleStorage = scheduleStorage;
    this.dueCalculator = dueCalculator;
    this.mailBuilder = mailBuilder;
    this.queueMessage = queueMessage;
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
    runInContainer(() -> {
      Instant cutoff = now.minus(CANDIDATE_CUTOFF_HOURS, ChronoUnit.HOURS);
      for (DigestFrequency frequency : DigestFrequency.values()) {
        for (DigestUserEntity user : scheduleStorage.findCandidates(frequency, cutoff)) {
          if (dueCalculator.isDue(user, frequency, now)) {
            dueUsers.computeIfAbsent(user.getId(), id -> new DueUser(user)).frequencies.add(frequency);
          }
        }
      }
    });
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
   * Serves one occurrence of one user. The claim comes first; when the email
   * can't be built or queued, the occurrence is given back so the next run
   * serves it again: an item is late, never lost.
   */
  void serve(DigestUserEntity user, DigestFrequency frequency, Instant now) {
    Instant previous = frequency == DigestFrequency.DAILY ? user.getDailyLastSent() : user.getWeeklyLastSent();
    if (!scheduleStorage.claim(user.getId(), frequency, previous, now)) {
      return;
    }
    String username = user.getUserId();
    try {
      DigestUserSettings settings = settingStorage.getUserSettings(username);
      if (!settings.isDaily() && !settings.isWeekly()) {
        // A row with no enabled frequency in the settings has no owner any more:
        // the settings are the truth, and they only vanish with the account (a
        // deleted user is forgotten here, at his next occurrence)
        LOG.info("The digest settings of {} are gone, his digest data is deleted", username);
        scheduleStorage.forget(user.getId(), username);
        return;
      }
      if (settingStorage.isDigestAllowed()) {
        if (frequency == DigestFrequency.DAILY ? settings.isDaily() : settings.isWeekly()) {
          List<DigestItemEntity> items = scheduleStorage.findItems(username, previous, now);
          MessageInfo message = items.isEmpty() ? null : mailBuilder.build(user, frequency, settings, items, previous, now);
          if (message != null && !queueMessage.put(message)) {
            throw new IllegalStateException("The mail queue refused the message");
          }
        }
      }
    } catch (Exception e) {
      LOG.warn("The {} digest of {} can't be sent now, it will be retried at the next run: {}", frequency, username, e.getMessage());
      LOG.debug("Digest failure of {}", username, e);
      if (!scheduleStorage.release(user.getId(), frequency, now, previous)) {
        LOG.error("The {} occurrence of {} could not be given back, its items will be sent with the next digest or cleaned up",
                  frequency,
                  username);
      }
      return;
    }
    deleteCoveredItems(user.getId(), username);
  }

  /**
   * An item is deleted once every frequency the user enabled has passed over
   * it: up to the oldest watermark of his enabled frequencies. Read fresh, the
   * row may have changed since the candidates were listed.
   */
  private void deleteCoveredItems(long id, String username) {
    DigestUserEntity fresh = scheduleStorage.find(id);
    if (fresh == null) {
      return;
    }
    Instant covered = null;
    if (fresh.isDaily()) {
      covered = fresh.getDailyLastSent();
    }
    if (fresh.isWeekly() && fresh.getWeeklyLastSent() != null
        && (covered == null || fresh.getWeeklyLastSent().isBefore(covered))) {
      covered = fresh.getWeeklyLastSent();
    }
    if (covered != null) {
      scheduleStorage.deleteCoveredItems(username, covered);
    }
  }

  /**
   * The scheduler and the worker threads have no container nor request of
   * their own: each task gets the portal container and a request lifecycle,
   * like a web request would.
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
