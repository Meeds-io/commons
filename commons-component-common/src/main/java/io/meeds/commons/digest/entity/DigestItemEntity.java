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
 * A notification waiting for the digest of one recipient: one row per
 * notification and per recipient, shared by his daily and his weekly digest,
 * never duplicated per frequency. Written by the capture listener when an
 * eligible notification happens, read and deleted by the hourly sender job at
 * send time. PARAMS holds ids only: names, titles and links are built fresh at
 * send time, so renamed objects show their current name and deleted objects
 * are skipped cleanly.
 */
@Entity(name = "DigestItem")
@Table(name = "NTF_DIGEST_ITEMS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DigestItemEntity {

  @Id
  @SequenceGenerator(name = "SEQ_NTF_DIGEST_ITEM_ID", sequenceName = "SEQ_NTF_DIGEST_ITEM_ID", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_NTF_DIGEST_ITEM_ID")
  @Column(name = "ID")
  private Long    id;

  @Column(name = "USER_ID", nullable = false)
  private String  userId;

  @Column(name = "PLUGIN_ID", nullable = false)
  private String  pluginId;

  @Column(name = "CATEGORY", nullable = false)
  private String  category;

  @Column(name = "ITEM_DATE", nullable = false)
  private Instant itemDate;

  @Column(name = "PARAMS")
  private String  params;

}
