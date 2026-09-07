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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import io.meeds.commons.digest.dao.DigestUserDAO;
import io.meeds.commons.digest.entity.DigestUserEntity;

/**
 * The timezone copy of the work list follows the platform timezone of the
 * user.
 */
@RunWith(MockitoJUnitRunner.class)
public class DigestEnrollmentStorageTest {

  private static final String     USERNAME  = "ayoub";

  private static final String     TIME_ZONE = "Europe/Paris";

  @Mock
  private DigestUserDAO           digestUserDAO;

  private DigestEnrollmentStorage storage;

  @Before
  public void setUp() {
    storage = new DigestEnrollmentStorage(digestUserDAO);
  }

  @Test
  public void testTimeZoneChangeRefreshesTheCopyOfAnEnrolledUser() {
    DigestUserEntity row = new DigestUserEntity(7L, USERNAME, true, false, TIME_ZONE, Instant.now(), null);
    when(digestUserDAO.findByUserId(USERNAME)).thenReturn(row);

    storage.updateTimeZone(USERNAME, "Asia/Tokyo");

    ArgumentCaptor<DigestUserEntity> captor = ArgumentCaptor.forClass(DigestUserEntity.class);
    verify(digestUserDAO).saveAndFlush(captor.capture());
    assertEquals("Asia/Tokyo", captor.getValue().getTimeZone());
  }

  @Test
  public void testTimeZoneChangeOfAUserWithoutDigestIsIgnored() {
    when(digestUserDAO.findByUserId(USERNAME)).thenReturn(null);

    storage.updateTimeZone(USERNAME, "Asia/Tokyo");

    verify(digestUserDAO, never()).saveAndFlush(any());
  }

  @Test
  public void testSameTimeZoneWritesNothing() {
    DigestUserEntity row = new DigestUserEntity(7L, USERNAME, true, false, TIME_ZONE, Instant.now(), null);
    when(digestUserDAO.findByUserId(USERNAME)).thenReturn(row);

    storage.updateTimeZone(USERNAME, TIME_ZONE);

    verify(digestUserDAO, never()).saveAndFlush(any());
  }

}
