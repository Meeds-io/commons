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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import io.meeds.commons.digest.DigestService;
import jakarta.annotation.PreDestroy;

/**
 * The one scheduled job of the digest, hourly by default. Glue only: it hands
 * the run to the service. It never stops, whatever the administrator switch
 * says: the switch decides whether emails are produced, the job keeps the
 * watermarks and the waiting items in order. The run happens on its own
 * thread: the Spring scheduler thread is shared with the other jobs of the
 * webapp, and a run serving thousands of users must not delay them. One run at
 * a time: a run still going when the next hour comes is left alone, the hour
 * after serves what is due.
 */
@Configuration
@EnableScheduling
public class DigestSenderJob {

  private static final Logger   LOG     = LoggerFactory.getLogger(DigestSenderJob.class);

  @Autowired
  private DigestService         digestService;

  private final ExecutorService runner  = Executors.newSingleThreadExecutor(task -> {
                                          Thread thread = new Thread(task, "digest-sender");
                                          thread.setDaemon(true);
                                          return thread;
                                        });

  private final AtomicBoolean   running = new AtomicBoolean();

  @Scheduled(cron = "${exo.notification.digest.job.expression:0 0 * * * ?}")
  public void run() {
    if (!running.compareAndSet(false, true)) {
      LOG.warn("The previous digest run is still going, this occurrence of the job is skipped");
      return;
    }
    try {
      runner.execute(() -> {
        try {
          digestService.processDueDigests();
        } catch (Exception e) {
          LOG.error("The digest run failed", e);
        } finally {
          running.set(false);
        }
      });
    } catch (RejectedExecutionException e) {
      // The webapp is stopping, nothing runs any more
      running.set(false);
    }
  }

  /**
   * @return true while a run is going
   */
  public boolean isRunning() {
    return running.get();
  }

  @PreDestroy
  public void stop() {
    runner.shutdownNow();
  }

}
