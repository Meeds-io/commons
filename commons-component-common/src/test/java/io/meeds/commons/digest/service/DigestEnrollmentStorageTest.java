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
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import io.meeds.commons.digest.dao.DigestItemDAO;
import io.meeds.commons.digest.dao.DigestUserDAO;
import io.meeds.commons.digest.entity.DigestUserEntity;
import io.meeds.commons.digest.model.DigestUserSettings;

/**
 * The work list follows the choices of the user: his row and his waiting items
 * leave with him, and the timezone copy follows his platform timezone.
 */
@RunWith(MockitoJUnitRunner.class)
public class DigestEnrollmentStorageTest {

  private static final String     USERNAME  = "ayoub";

  private static final String     TIME_ZONE = "Europe/Paris";

  @Mock
  private DigestUserDAO           digestUserDAO;

  @Mock
  private DigestItemDAO           digestItemDAO;

  private DigestEnrollmentStorage storage;

  @Before
  public void setUp() {
    storage = new DigestEnrollmentStorage(digestUserDAO, digestItemDAO);
  }

  @Test
  public void testLeavingTheDigestDeletesTheRowAndTheWaitingItems() {
    DigestUserEntity row = new DigestUserEntity(7L, USERNAME, true, true, TIME_ZONE, Instant.now(), Instant.now());
    when(digestUserDAO.findByUserId(USERNAME)).thenReturn(row);

    storage.enroll(USERNAME, new DigestUserSettings(false, null, false, null), TIME_ZONE);

    // Items first, then the row, then the flush the caller relies on
    InOrder inOrder = inOrder(digestItemDAO, digestUserDAO);
    inOrder.verify(digestItemDAO).deleteByUser(USERNAME);
    inOrder.verify(digestUserDAO).delete(row);
    inOrder.verify(digestUserDAO).flush();
  }

  @Test
  public void testLeavingTheDigestWithoutARowTouchesNothing() {
    when(digestUserDAO.findByUserId(USERNAME)).thenReturn(null);

    storage.enroll(USERNAME, new DigestUserSettings(false, null, false, null), TIME_ZONE);

    verify(digestItemDAO, never()).deleteByUser(any());
    verify(digestUserDAO, never()).delete(any());
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
