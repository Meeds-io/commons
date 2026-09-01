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
package io.meeds.commons.digest.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The work list of the digest sender job: one row per user having at least one
 * digest frequency enabled, nobody else. The category lists are not stored
 * here, they are read from the user settings.
 */
@Entity(name = "DigestUser")
@Table(name = "NTF_DIGEST_USERS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DigestUserEntity {

  @Id
  @SequenceGenerator(name = "SEQ_NTF_DIGEST_USER_ID", sequenceName = "SEQ_NTF_DIGEST_USER_ID", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_NTF_DIGEST_USER_ID")
  @Column(name = "ID")
  private Long    id;

  @Column(name = "USER_ID", nullable = false, unique = true)
  private String  userId;

  @Column(name = "DAILY", nullable = false)
  private boolean daily;

  @Column(name = "WEEKLY", nullable = false)
  private boolean weekly;

  /**
   * Copy of the user profile timezone, so the job can select the users to serve
   * in one query
   */
  @Column(name = "TIMEZONE")
  private String  timeZone;

  /**
   * Watermark of the daily digest: date of the last daily occurrence, or of the
   * moment the option was switched on before the first one
   */
  @Column(name = "DAILY_LAST_SENT")
  private Instant dailyLastSent;

  /**
   * Watermark of the weekly digest, same rule as the daily one
   */
  @Column(name = "WEEKLY_LAST_SENT")
  private Instant weeklyLastSent;

}
