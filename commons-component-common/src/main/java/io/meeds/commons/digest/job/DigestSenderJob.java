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

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import io.meeds.commons.digest.DigestService;

/**
 * The one scheduled job of the digest, hourly by default. Glue only: it hands
 * the run to the service. It never stops, whatever the administrator switch
 * says: the switch decides whether emails are produced, the job keeps the
 * watermarks and the waiting items in order.
 */
@Configuration
@EnableScheduling
public class DigestSenderJob {

  private final DigestService digestService;

  public DigestSenderJob(DigestService digestService) {
    this.digestService = digestService;
  }

  @Scheduled(cron = "${exo.notification.digest.job.expression:0 0 * * * ?}")
  public void run() {
    digestService.processDueDigests();
  }

}
