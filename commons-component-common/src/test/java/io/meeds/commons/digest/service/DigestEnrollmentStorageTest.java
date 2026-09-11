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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import io.meeds.commons.digest.dao.DigestUserDAO;
import io.meeds.commons.digest.entity.DigestUserEntity;
import io.meeds.commons.digest.model.DigestUserSettings;

/**
 * The enrollment rules of the work list: the row is created on the first
 * enablement with its watermarks at now, a frequency turned back on gets a
 * fresh watermark, a change of categories keeps the watermarks, both
 * frequencies off delete the row; and the timezone copy follows the platform
 * timezone of the user.
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
  public void testFirstEnablementCreatesTheRowWithBothWatermarksAtNow() {
    when(digestUserDAO.findByUserId(USERNAME)).thenReturn(null);
    Instant before = Instant.now();

    storage.enroll(USERNAME, new DigestUserSettings(true, List.of("spaces"), true, List.of("feed")), TIME_ZONE);

    DigestUserEntity row = savedRow();
    assertEquals(USERNAME, row.getUserId());
    assertTrue(row.isDaily());
    assertTrue(row.isWeekly());
    assertEquals(TIME_ZONE, row.getTimeZone());
    // The first digest covers "since the option was switched on", nothing before
    assertNotNull(row.getDailyLastSent());
    assertNotNull(row.getWeeklyLastSent());
    assertFalse(row.getDailyLastSent().isBefore(before));
    assertFalse(row.getWeeklyLastSent().isBefore(before));
  }

  @Test
  public void testCategoryOnlyChangeKeepsTheWatermarksAndRefreshesTheTimeZone() {
    Instant watermark = Instant.parse("2026-09-02T16:05:00Z");
    DigestUserEntity row = new DigestUserEntity(7L, USERNAME, true, false, TIME_ZONE, watermark, null);
    when(digestUserDAO.findByUserId(USERNAME)).thenReturn(row);

    storage.enroll(USERNAME, new DigestUserSettings(true, List.of("feed"), false, List.of()), "Asia/Tokyo");

    DigestUserEntity saved = savedRow();
    assertEquals("The daily watermark must not move on a change of categories", watermark, saved.getDailyLastSent());
    assertNull(saved.getWeeklyLastSent());
    assertEquals("Asia/Tokyo", saved.getTimeZone());
  }

  @Test
  public void testFrequencyTurnedBackOnGetsAFreshWatermarkAndTheOtherKeepsItsOwn() {
    // Daily was on, weekly off since a pause: turning weekly back on must not
    // send a giant catch-up email, its watermark restarts now
    Instant dailyWatermark = Instant.parse("2026-09-02T16:05:00Z");
    Instant staleWeekly = Instant.parse("2026-06-01T16:05:00Z");
    DigestUserEntity row = new DigestUserEntity(7L, USERNAME, true, false, TIME_ZONE, dailyWatermark, staleWeekly);
    when(digestUserDAO.findByUserId(USERNAME)).thenReturn(row);
    Instant before = Instant.now();

    storage.enroll(USERNAME, new DigestUserSettings(true, List.of("spaces"), true, List.of("spaces")), TIME_ZONE);

    DigestUserEntity saved = savedRow();
    assertTrue(saved.isWeekly());
    assertEquals(dailyWatermark, saved.getDailyLastSent());
    assertFalse("The re-enabled frequency restarts from now, not from the stale watermark",
                saved.getWeeklyLastSent().isBefore(before));
  }

  @Test
  public void testBothFrequenciesOffDeleteTheRow() {
    DigestUserEntity row = new DigestUserEntity(7L, USERNAME, true, true, TIME_ZONE, Instant.now(), Instant.now());
    when(digestUserDAO.findByUserId(USERNAME)).thenReturn(row);

    storage.enroll(USERNAME, new DigestUserSettings(false, List.of(), false, List.of()), TIME_ZONE);

    verify(digestUserDAO).delete(row);
    verify(digestUserDAO, never()).saveAndFlush(any());
  }

  @Test
  public void testBothFrequenciesOffForAUserWithNoRowWritesNothing() {
    when(digestUserDAO.findByUserId(USERNAME)).thenReturn(null);

    storage.enroll(USERNAME, new DigestUserSettings(false, List.of(), false, List.of()), TIME_ZONE);

    verify(digestUserDAO, never()).delete(any());
    verify(digestUserDAO, never()).saveAndFlush(any());
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

  private DigestUserEntity savedRow() {
    ArgumentCaptor<DigestUserEntity> captor = ArgumentCaptor.forClass(DigestUserEntity.class);
    verify(digestUserDAO).saveAndFlush(captor.capture());
    return captor.getValue();
  }

}
