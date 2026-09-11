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

import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.meeds.commons.digest.dao.DigestItemDAO;
import io.meeds.commons.digest.dao.DigestUserDAO;
import io.meeds.commons.digest.entity.DigestItemEntity;
import io.meeds.commons.digest.entity.DigestUserEntity;
import io.meeds.commons.digest.model.DigestFrequency;

/**
 * The work list of the digest sender job: who is a candidate, the claim of an
 * occurrence, the waiting items of a user and their deletion.
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
   * One batch of the candidates of a frequency: the frequency is on and the
   * watermark is older than the cutoff, ids after the last one seen, ascending.
   * The exact "due now in his timezone" check is done in Java by the caller,
   * the query only narrows the set.
   *
   * @param frequency daily or weekly
   * @param cutoff the watermark must be before it
   * @param afterId only rows with a greater id, 0 for the first batch
   * @param limit how many rows at most
   * @return the candidates, by ascending id
   */
  public List<DigestUserEntity> findCandidates(DigestFrequency frequency, Instant cutoff, long afterId, Limit limit) {
    return frequency == DigestFrequency.DAILY ? digestUserDAO.findByDailyTrueAndDailyLastSentBeforeAndIdGreaterThanOrderByIdAsc(cutoff, afterId, limit)
                                              : digestUserDAO.findByWeeklyTrueAndWeeklyLastSentBeforeAndIdGreaterThanOrderByIdAsc(cutoff, afterId, limit);
  }

  public DigestUserEntity find(long id) {
    return digestUserDAO.findById(id).orElse(null);
  }

  /**
   * Claims an occurrence: moves the watermark of the frequency to now, only if
   * it still holds the value the caller read. Zero rows means another worker,
   * or another server, took it first. Joins the transaction of the caller when
   * there is one: the claim then commits, or rolls back, with what the caller
   * does with the occurrence.
   *
   * @param userId the work list row id
   * @param frequency daily or weekly
   * @param expected the watermark the caller read
   * @param now the new watermark
   * @return true when this caller got the occurrence
   */
  @Transactional
  public boolean claim(long userId, DigestFrequency frequency, Instant expected, Instant now) {
    return updateWatermark(userId, frequency, expected, now) == 1;
  }

  /**
   * The waiting items of a user in a window, most recent first
   *
   * @param username the recipient
   * @param after excluded lower bound, the previous watermark
   * @param until included upper bound, the new watermark
   * @return the items
   */
  public List<DigestItemEntity> findItems(String username, Instant after, Instant until) {
    return digestItemDAO.findByUserIdAndItemDateGreaterThanAndItemDateLessThanEqualOrderByItemDateDesc(username,
                                                                                                        after,
                                                                                                        until);
  }

  /**
   * Deletes the items every enabled frequency of the user has covered
   *
   * @param username the recipient
   * @param coveredUntil the oldest watermark of his enabled frequencies
   * @return how many items were deleted
   */
  @Transactional
  public int deleteCoveredItems(String username, Instant coveredUntil) {
    return digestItemDAO.deleteCovered(username, coveredUntil);
  }

  /**
   * Forgets a user for good: his waiting items and his work list row
   *
   * @param id the work list row id
   * @param username the user
   */
  @Transactional
  public void forget(long id, String username) {
    digestItemDAO.deleteByUser(username);
    digestUserDAO.deleteById(id);
  }

  /**
   * Safety cleanup, the first step of every run: the items of users who have
   * no digest enabled any more, and the items older than the retention
   *
   * @param retentionLimit items older than this are deleted whatever happened
   * @return how many items were deleted
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
