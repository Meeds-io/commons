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

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.meeds.commons.digest.dao.DigestItemDAO;
import io.meeds.commons.digest.dao.DigestUserDAO;
import io.meeds.commons.digest.entity.DigestItemEntity;
import io.meeds.commons.digest.entity.DigestUserEntity;
import io.meeds.commons.digest.model.DigestFrequency;

/**
 * The tables as the sender job uses them: who is a candidate, the claim of an
 * occurrence, the items of a window, and the deletions.
 */
@Component
public class DigestScheduleStorage {

  private final DigestUserDAO digestUserDAO;

  private final DigestItemDAO digestItemDAO;

  public DigestScheduleStorage(DigestUserDAO digestUserDAO, DigestItemDAO digestItemDAO) {
    this.digestUserDAO = digestUserDAO;
    this.digestItemDAO = digestItemDAO;
  }

  /**
   * @param frequency the digest to serve
   * @param cutoff only the users whose watermark is older than this are
   *          candidates, the exact local time check is done by the caller
   * @return the pre-filtered candidates, one indexed query
   */
  public List<DigestUserEntity> findCandidates(DigestFrequency frequency, Instant cutoff) {
    return frequency == DigestFrequency.DAILY ? digestUserDAO.findByDailyTrueAndDailyLastSentBefore(cutoff)
                                              : digestUserDAO.findByWeeklyTrueAndWeeklyLastSentBefore(cutoff);
  }

  public DigestUserEntity find(long id) {
    return digestUserDAO.findById(id).orElse(null);
  }

  /**
   * Claims an occurrence: moves the watermark to now, only if it still has the
   * value that was read. Of several workers or servers doing it at once, exactly
   * one succeeds.
   *
   * @return true when this caller got the occurrence, false when another one
   *         took it
   */
  @Transactional
  public boolean claim(long userId, DigestFrequency frequency, Instant expected, Instant now) {
    return updateWatermark(userId, frequency, expected, now) == 1;
  }

  /**
   * Gives an occurrence back after a failure: the watermark returns to its
   * previous value, so the next run serves the user again instead of losing his
   * items.
   *
   * @return true when the watermark went back, false when it no longer held the
   *         claimed value
   */
  @Transactional
  public boolean release(long userId, DigestFrequency frequency, Instant claimed, Instant previous) {
    return updateWatermark(userId, frequency, claimed, previous) == 1;
  }

  /**
   * @return the items of one occurrence, most recent first: received after the
   *         previous watermark, up to the claim moment
   */
  public List<DigestItemEntity> findItems(String username, Instant after, Instant until) {
    return digestItemDAO.findByUserIdAndItemDateGreaterThanAndItemDateLessThanEqualOrderByItemDateDesc(username,
                                                                                                        after,
                                                                                                        until);
  }

  /**
   * Deletes the items every enabled frequency of the user has passed over. An
   * item filtered out or skipped at an occurrence counts as covered too.
   */
  @Transactional
  public int deleteCoveredItems(String username, Instant coveredUntil) {
    return digestItemDAO.deleteCovered(username, coveredUntil);
  }

  /**
   * The safety cleanup, first step of every run: items of users who have no
   * digest enabled any more, and items older than the retention. The table can
   * never grow forever, whatever happens.
   *
   * @return how many rows went away
   */
  @Transactional
  public int cleanup(Instant retentionLimit) {
    return digestItemDAO.deleteOrphans() + digestItemDAO.deleteOlderThan(retentionLimit);
  }

  private int updateWatermark(long userId, DigestFrequency frequency, Instant expected, Instant value) {
    return frequency == DigestFrequency.DAILY ? digestUserDAO.updateDailyWatermark(userId, expected, value)
                                              : digestUserDAO.updateWeeklyWatermark(userId, expected, value);
  }

}
