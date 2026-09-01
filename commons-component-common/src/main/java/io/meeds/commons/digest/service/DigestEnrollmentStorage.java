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

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.meeds.commons.digest.dao.DigestUserDAO;
import io.meeds.commons.digest.entity.DigestUserEntity;
import io.meeds.commons.digest.model.DigestUserSettings;

/**
 * Keeps NTF_DIGEST_USERS, the work list of the digest sender job, in sync with
 * the choices of the users: the row holds only what the job needs to answer
 * "which users are due now?".
 */
@Component
public class DigestEnrollmentStorage {

  private final DigestUserDAO digestUserDAO;

  public DigestEnrollmentStorage(DigestUserDAO digestUserDAO) {
    this.digestUserDAO = digestUserDAO;
  }

  /**
   * Enrolls a user in the digest sending, or removes him when he wants no
   * digest any more. The write is flushed before returning, so that a caller
   * saving the user settings afterwards only does it once the enrollment really
   * succeeded.
   *
   * @param username the user saving his choices
   * @param settings the chosen frequencies
   * @param timeZone the timezone to send his digest on, may be null
   */
  @Transactional
  public void enroll(String username, DigestUserSettings settings, String timeZone) {
    DigestUserEntity digestUser = digestUserDAO.findByUserId(username);
    if (!settings.isDaily() && !settings.isWeekly()) {
      // The work list keeps only the users having a digest enabled
      if (digestUser != null) {
        digestUserDAO.delete(digestUser);
        digestUserDAO.flush();
      }
      return;
    }
    if (digestUser == null) {
      digestUser = new DigestUserEntity();
      digestUser.setUserId(username);
    }
    Instant now = Instant.now();
    // A frequency switched on starts covering the notifications received from
    // now on, whether it is the first enablement or a re enablement after a
    // pause: no catch up of the pause
    if (settings.isDaily() && !digestUser.isDaily()) {
      digestUser.setDailyLastSent(now);
    }
    if (settings.isWeekly() && !digestUser.isWeekly()) {
      digestUser.setWeeklyLastSent(now);
    }
    digestUser.setDaily(settings.isDaily());
    digestUser.setWeekly(settings.isWeekly());
    digestUser.setTimeZone(timeZone);
    digestUserDAO.saveAndFlush(digestUser);
  }

}
