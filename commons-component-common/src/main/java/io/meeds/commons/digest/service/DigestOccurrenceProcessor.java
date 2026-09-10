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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import org.exoplatform.commons.api.notification.model.MessageInfo;

import io.meeds.commons.digest.entity.DigestItemEntity;
import io.meeds.commons.digest.entity.DigestUserEntity;
import io.meeds.commons.digest.model.DigestFrequency;
import io.meeds.commons.digest.model.DigestUserSettings;

/**
 * One occurrence of one frequency for one user, as a single transaction: the
 * claim of the occurrence (the guarded move of the watermark), the reading of
 * the waiting items, the building of the email, its row in the mail queue and
 * the deletion of the covered items commit together, or roll back together.
 * A crash in the middle leaves the watermark where it was, and the user is
 * simply served again at the next run: an item is late, never lost, and never
 * sent twice.
 */
@Component
public class DigestOccurrenceProcessor {

  private static final Logger         LOG = LoggerFactory.getLogger(DigestOccurrenceProcessor.class);

  private final DigestSettingStorage  settingStorage;

  private final DigestScheduleStorage scheduleStorage;

  private final DigestMailBuilder     mailBuilder;

  private final DigestMailQueueStorage mailQueueStorage;

  public DigestOccurrenceProcessor(DigestSettingStorage settingStorage,
                                   DigestScheduleStorage scheduleStorage,
                                   DigestMailBuilder mailBuilder,
                                   DigestMailQueueStorage mailQueueStorage) {
    this.settingStorage = settingStorage;
    this.scheduleStorage = scheduleStorage;
    this.mailBuilder = mailBuilder;
    this.mailQueueStorage = mailQueueStorage;
  }

  /**
   * Serves one occurrence. Any exception rolls the whole occurrence back,
   * claim included, and reaches the caller.
   *
   * @param user the work list row, as read when the candidates were selected
   * @param frequency the frequency due
   * @param now the new watermark, the moment of the run
   * @return true when this worker got the occurrence (whether or not an email
   *         went out), false when another worker took it first
   */
  @Transactional(rollbackFor = Exception.class)
  public boolean serve(DigestUserEntity user, DigestFrequency frequency, Instant now) {
    Instant previous = frequency == DigestFrequency.DAILY ? user.getDailyLastSent() : user.getWeeklyLastSent();
    if (!scheduleStorage.claim(user.getId(), frequency, previous, now)) {
      return false;
    }
    String username = user.getUserId();
    DigestUserSettings settings = settingStorage.getUserSettings(username);
    if (!settings.isDaily() && !settings.isWeekly()) {
      // A row with no enabled frequency in the settings has no owner any more:
      // the settings are the truth, and they only vanish with the account (a
      // deleted user is forgotten here, at his next occurrence)
      LOG.debug("The digest settings of {} are gone, his digest data is deleted", username);
      scheduleStorage.forget(user.getId(), username);
      return true;
    }
    if (settingStorage.isDigestAllowed() && (frequency == DigestFrequency.DAILY ? settings.isDaily() : settings.isWeekly())) {
      List<DigestItemEntity> items = scheduleStorage.findItems(username, previous, now);
      MessageInfo message = items.isEmpty() ? null : mailBuilder.build(user, frequency, settings, items, previous, now);
      if (message == null) {
        LOG.debug("No {} digest for {}: nothing to say since {} ({} waiting items in the window)",
                  frequency,
                  username,
                  previous,
                  items.size());
      } else if (mailQueueStorage.enqueue(message)) {
        LOG.debug("The {} digest of {} is in the mail queue: {}", frequency, username, message.getSubject());
      } else {
        throw new IllegalStateException("The mail queue refused the message");
      }
    }
    deleteCoveredItems(user.getId(), username);
    return true;
  }

  /**
   * Deletes the items every enabled frequency has passed over: up to the oldest
   * watermark of the enabled frequencies, read fresh so that the claim just
   * made is taken into account
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

}
