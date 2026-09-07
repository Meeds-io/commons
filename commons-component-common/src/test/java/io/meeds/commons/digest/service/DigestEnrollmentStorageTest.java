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
import java.time.temporal.ChronoUnit;
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
 * The work list row on Apply: created, updated or removed, and the watermark
 * of a frequency switched on, which restarts at the switch so that the first
 * digest covers from there (decided rule: it goes out at the next occurrence
 * whose day is after the switch, never the same day).
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
  public void testFirstEnrollmentCreatesTheRowWithTheWatermarkAtTheSwitch() {
    when(digestUserDAO.findByUserId(USERNAME)).thenReturn(null);

    storage.enroll(USERNAME, new DigestUserSettings(true, List.of("spaces"), false, List.of()), TIME_ZONE);

    DigestUserEntity saved = saved();
    assertEquals(USERNAME, saved.getUserId());
    assertTrue(saved.isDaily());
    assertFalse(saved.isWeekly());
    assertEquals(TIME_ZONE, saved.getTimeZone());
    assertNotNull(saved.getDailyLastSent());
    assertNull("The weekly is off: no watermark for it", saved.getWeeklyLastSent());
  }

  @Test
  public void testSwitchingAFrequencyBackOnRestartsItsWatermarkOnly() {
    Instant sentEarlierToday = Instant.now().minus(1, ChronoUnit.HOURS);
    DigestUserEntity row = new DigestUserEntity(7L, USERNAME, false, true, TIME_ZONE, sentEarlierToday, sentEarlierToday);
    when(digestUserDAO.findByUserId(USERNAME)).thenReturn(row);

    storage.enroll(USERNAME, new DigestUserSettings(true, List.of("spaces"), true, List.of("spaces")), TIME_ZONE);

    DigestUserEntity saved = saved();
    assertTrue(saved.isDaily());
    assertTrue("The watermark restarts at the switch: no catch up of the pause", saved.getDailyLastSent().isAfter(sentEarlierToday));
    assertEquals("The weekly, untouched, keeps its watermark", sentEarlierToday, saved.getWeeklyLastSent());
  }

  @Test
  public void testChangingCategoriesOnlyLeavesTheWatermarksAlone() {
    Instant yesterday = Instant.now().minus(1, ChronoUnit.DAYS);
    DigestUserEntity row = new DigestUserEntity(7L, USERNAME, true, true, TIME_ZONE, yesterday, yesterday);
    when(digestUserDAO.findByUserId(USERNAME)).thenReturn(row);

    storage.enroll(USERNAME, new DigestUserSettings(true, List.of("feed"), true, List.of("feed")), TIME_ZONE);

    DigestUserEntity saved = saved();
    assertEquals(yesterday, saved.getDailyLastSent());
    assertEquals(yesterday, saved.getWeeklyLastSent());
  }

  @Test
  public void testSwitchingBothOffRemovesTheRow() {
    DigestUserEntity row = new DigestUserEntity(7L, USERNAME, true, true, TIME_ZONE, Instant.now(), Instant.now());
    when(digestUserDAO.findByUserId(USERNAME)).thenReturn(row);

    storage.enroll(USERNAME, new DigestUserSettings(false, List.of(), false, List.of()), TIME_ZONE);

    verify(digestUserDAO).delete(row);
    verify(digestUserDAO, never()).saveAndFlush(any());
  }

  private DigestUserEntity saved() {
    ArgumentCaptor<DigestUserEntity> captor = ArgumentCaptor.forClass(DigestUserEntity.class);
    verify(digestUserDAO).saveAndFlush(captor.capture());
    return captor.getValue();
  }

}
