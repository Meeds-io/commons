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
package io.meeds.commons.digest.dao;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.meeds.commons.digest.entity.DigestUserEntity;

@Repository
public interface DigestUserDAO extends JpaRepository<DigestUserEntity, Long> {

  String UPDATE_DAILY_WATERMARK_QUERY  =
                                      "UPDATE DigestUser u SET u.dailyLastSent = :value WHERE u.id = :id AND u.dailyLastSent = :expected";

  String UPDATE_WEEKLY_WATERMARK_QUERY =
                                      "UPDATE DigestUser u SET u.weeklyLastSent = :value WHERE u.id = :id AND u.weeklyLastSent = :expected";

  DigestUserEntity findByUserId(String userId);

  void deleteByUserId(String userId);

  /** The daily candidates of the sender job: daily on, watermark old enough */
  List<DigestUserEntity> findByDailyTrueAndDailyLastSentBefore(Instant cutoff);

  /** The weekly candidates of the sender job: weekly on, watermark old enough */
  List<DigestUserEntity> findByWeeklyTrueAndWeeklyLastSentBefore(Instant cutoff);

  /**
   * The guarded update behind the claim: the watermark moves only when it still
   * has the value the caller read, so that of two workers reading the same row,
   * exactly one gets 1 row updated and the other 0.
   */
  @Modifying
  @Query(UPDATE_DAILY_WATERMARK_QUERY)
  int updateDailyWatermark(@Param("id") long id, @Param("expected") Instant expected, @Param("value") Instant value);

  @Modifying
  @Query(UPDATE_WEEKLY_WATERMARK_QUERY)
  int updateWeeklyWatermark(@Param("id") long id, @Param("expected") Instant expected, @Param("value") Instant value);

}
