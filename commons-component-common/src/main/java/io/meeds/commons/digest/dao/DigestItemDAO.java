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

import io.meeds.commons.digest.entity.DigestItemEntity;

@Repository
public interface DigestItemDAO extends JpaRepository<DigestItemEntity, Long> {

  String DELETE_COVERED_QUERY    = "DELETE FROM DigestItem i WHERE i.userId = :userId AND i.itemDate <= :until";

  String DELETE_OLDER_THAN_QUERY = "DELETE FROM DigestItem i WHERE i.itemDate < :before";

  String DELETE_ORPHANS_QUERY    = "DELETE FROM DigestItem i WHERE i.userId NOT IN (SELECT u.userId FROM DigestUser u)";

  String DELETE_BY_USER_QUERY    = "DELETE FROM DigestItem i WHERE i.userId = :userId";

  /** Tells whether the same notification is already waiting for this recipient */
  boolean existsByUserIdAndPluginIdAndParams(String userId, String pluginId, String params);

  /** The waiting items of a recipient about one object, matched on a parameter */
  List<DigestItemEntity> findByUserIdAndPluginIdAndParamsContaining(String userId, String pluginId, String paramsFragment);

  /**
   * The items of one digest occurrence: what the recipient received after the
   * previous watermark and up to the claim moment, most recent first
   */
  List<DigestItemEntity> findByUserIdAndItemDateGreaterThanAndItemDateLessThanEqualOrderByItemDateDesc(String userId,
                                                                                                        Instant after,
                                                                                                        Instant until);

  /** Deletes the items every enabled frequency of the recipient has covered */
  @Modifying
  @Query(DELETE_COVERED_QUERY)
  int deleteCovered(@Param("userId") String userId, @Param("until") Instant until);

  /** Safety cleanup: items older than the retention, whatever happened */
  @Modifying
  @Query(DELETE_OLDER_THAN_QUERY)
  int deleteOlderThan(@Param("before") Instant before);

  /** Safety cleanup: items of users who have no digest enabled any more */
  @Modifying
  @Query(DELETE_ORPHANS_QUERY)
  int deleteOrphans();

  /** Every waiting item of one user, when the user leaves the digest for good */
  @Modifying
  @Query(DELETE_BY_USER_QUERY)
  int deleteByUser(@Param("userId") String userId);

}
