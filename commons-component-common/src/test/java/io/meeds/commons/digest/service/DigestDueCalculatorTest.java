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
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.Test;

import io.meeds.commons.digest.entity.DigestUserEntity;
import io.meeds.commons.digest.model.DigestFrequency;

/**
 * The "is this user due now?" rule, in the user's local calendar. The
 * scenarios are the ones of the specification: the send hour, the catch-up
 * after a missed run, the first digest after opt-in, and the timezone changes
 * (the QA edge cases).
 */
public class DigestDueCalculatorTest {

  private static final ZoneId        PARIS      = ZoneId.of("Europe/Paris");

  private static final ZoneId        TOKYO      = ZoneId.of("Asia/Tokyo");

  private static final ZoneId        LOS_ANGELES = ZoneId.of("America/Los_Angeles");

  private final DigestDueCalculator calculator = new DigestDueCalculator(18, "FRIDAY");

  // 2026-09-03 is a Thursday, 2026-09-04 a Friday

  @Test
  public void testDailyIsDueOnceTheSendHourHasPassed() {
    DigestUserEntity user = user(PARIS, at(PARIS, "2026-09-02T18:05"), null);
    assertFalse(calculator.isDue(user, DigestFrequency.DAILY, at(PARIS, "2026-09-03T17:59").toInstant()));
    assertTrue(calculator.isDue(user, DigestFrequency.DAILY, at(PARIS, "2026-09-03T18:00").toInstant()));
  }

  @Test
  public void testDailyIsNeverSentTwiceTheSameLocalDay() {
    DigestUserEntity user = user(PARIS, at(PARIS, "2026-09-03T18:05"), null);
    assertFalse(calculator.isDue(user, DigestFrequency.DAILY, at(PARIS, "2026-09-03T19:00").toInstant()));
    assertFalse(calculator.isDue(user, DigestFrequency.DAILY, at(PARIS, "2026-09-03T23:59").toInstant()));
    assertTrue(calculator.isDue(user, DigestFrequency.DAILY, at(PARIS, "2026-09-04T18:00").toInstant()));
  }

  @Test
  public void testMissedRunIsCaughtUpByTheNextOne() {
    // The server was down at 18:00, the run of 23:00 serves the user
    DigestUserEntity user = user(PARIS, at(PARIS, "2026-09-02T18:05"), null);
    assertTrue(calculator.isDue(user, DigestFrequency.DAILY, at(PARIS, "2026-09-03T23:00").toInstant()));
  }

  @Test
  public void testFrequencySwitchedOnTodaySendsItsFirstDigestTomorrow() {
    // Opt-in at 10:00: the watermark is the opt-in moment, the first digest
    // comes the next day at 18:00 and covers everything since the opt-in
    DigestUserEntity user = user(PARIS, at(PARIS, "2026-09-03T10:00"), null);
    assertFalse(calculator.isDue(user, DigestFrequency.DAILY, at(PARIS, "2026-09-03T18:00").toInstant()));
    assertFalse(calculator.isDue(user, DigestFrequency.DAILY, at(PARIS, "2026-09-03T23:00").toInstant()));
    assertTrue(calculator.isDue(user, DigestFrequency.DAILY, at(PARIS, "2026-09-04T18:00").toInstant()));
  }

  @Test
  public void testWeeklyIsDueOnTheSendDayOnly() {
    DigestUserEntity user = user(PARIS, null, at(PARIS, "2026-08-28T18:05"));
    assertFalse(calculator.isDue(user, DigestFrequency.WEEKLY, at(PARIS, "2026-09-03T18:00").toInstant()));
    assertFalse(calculator.isDue(user, DigestFrequency.WEEKLY, at(PARIS, "2026-09-04T17:00").toInstant()));
    assertTrue(calculator.isDue(user, DigestFrequency.WEEKLY, at(PARIS, "2026-09-04T18:00").toInstant()));
  }

  @Test
  public void testWeeklyMissedOnFridayIsCaughtUpOnSaturday() {
    DigestUserEntity user = user(PARIS, null, at(PARIS, "2026-08-28T18:05"));
    assertTrue(calculator.isDue(user, DigestFrequency.WEEKLY, at(PARIS, "2026-09-05T09:00").toInstant()));
  }

  @Test
  public void testWeeklyIsNeverSentTwiceTheSameWeek() {
    DigestUserEntity user = user(PARIS, null, at(PARIS, "2026-09-04T18:05"));
    assertFalse(calculator.isDue(user, DigestFrequency.WEEKLY, at(PARIS, "2026-09-04T22:00").toInstant()));
    assertFalse(calculator.isDue(user, DigestFrequency.WEEKLY, at(PARIS, "2026-09-05T18:00").toInstant()));
    assertTrue(calculator.isDue(user, DigestFrequency.WEEKLY, at(PARIS, "2026-09-11T18:00").toInstant()));
  }

  @Test
  public void testBothFrequenciesAreDueOnFriday() {
    DigestUserEntity user = user(PARIS, at(PARIS, "2026-09-03T18:05"), at(PARIS, "2026-08-28T18:05"));
    Instant friday = at(PARIS, "2026-09-04T18:00").toInstant();
    assertTrue(calculator.isDue(user, DigestFrequency.DAILY, friday));
    assertTrue(calculator.isDue(user, DigestFrequency.WEEKLY, friday));
  }

  @Test
  public void testSendHourIsTheUserOne() {
    // 18:00 in Tokyo is 11:00 in Paris: the Tokyo user is due, the Paris one not
    Instant now = at(TOKYO, "2026-09-03T18:00").toInstant();
    assertTrue(calculator.isDue(user(TOKYO, at(TOKYO, "2026-09-02T18:05"), null), DigestFrequency.DAILY, now));
    assertFalse(calculator.isDue(user(PARIS, at(PARIS, "2026-09-02T18:05"), null), DigestFrequency.DAILY, now));
  }

  @Test
  public void testWestwardTimezoneChangeAfterTheDigestGivesNoSecondEmailTheSameDay() {
    // Served at 18:05 in Paris, then the user lands in Los Angeles where it is
    // still the same local day and 18:00 comes again: no second email
    ZonedDateTime servedInParis = at(PARIS, "2026-09-03T18:05");
    DigestUserEntity user = user(LOS_ANGELES, servedInParis, null);
    assertFalse(calculator.isDue(user, DigestFrequency.DAILY, at(LOS_ANGELES, "2026-09-03T18:00").toInstant()));
    assertFalse(calculator.isDue(user, DigestFrequency.DAILY, at(LOS_ANGELES, "2026-09-03T23:00").toInstant()));
    assertTrue(calculator.isDue(user, DigestFrequency.DAILY, at(LOS_ANGELES, "2026-09-04T18:00").toInstant()));
  }

  @Test
  public void testEastwardTimezoneChangeBeforeTheDigestSlidesItByOneLocalDay() {
    // Served yesterday at 18:05 in Paris, the user is now in Tokyo: the
    // watermark falls today in his new local calendar, so today's 18:00 in
    // Tokyo is skipped and tomorrow's covers the gap
    DigestUserEntity user = user(TOKYO, at(PARIS, "2026-09-02T18:05"), null);
    assertFalse(calculator.isDue(user, DigestFrequency.DAILY, at(TOKYO, "2026-09-03T18:00").toInstant()));
    assertTrue(calculator.isDue(user, DigestFrequency.DAILY, at(TOKYO, "2026-09-04T18:00").toInstant()));
  }

  @Test
  public void testUnknownOrMissingTimezoneUsesTheServerOne() {
    assertEquals(ZoneId.systemDefault(), DigestDueCalculator.zoneOf(null));
    assertEquals(ZoneId.systemDefault(), DigestDueCalculator.zoneOf("Mars/Olympus"));
    assertEquals(PARIS, DigestDueCalculator.zoneOf("Europe/Paris"));
  }

  @Test
  public void testMissingWatermarkIsNeverDue() {
    DigestUserEntity user = user(PARIS, null, null);
    assertFalse(calculator.isDue(user, DigestFrequency.DAILY, Instant.now()));
    assertFalse(calculator.isDue(user, DigestFrequency.WEEKLY, Instant.now()));
  }

  @Test
  public void testBadWeeklyDayPropertyFallsBackToFriday() {
    DigestDueCalculator lenient = new DigestDueCalculator(18, "someday");
    DigestUserEntity user = user(PARIS, null, at(PARIS, "2026-08-28T18:05"));
    assertTrue(lenient.isDue(user, DigestFrequency.WEEKLY, at(PARIS, "2026-09-04T18:00").toInstant()));
    assertFalse(lenient.isDue(user, DigestFrequency.WEEKLY, at(PARIS, "2026-09-03T18:00").toInstant()));
  }

  private static ZonedDateTime at(ZoneId zone, String localDateTime) {
    return ZonedDateTime.of(java.time.LocalDateTime.parse(localDateTime), zone);
  }

  private static DigestUserEntity user(ZoneId zone, ZonedDateTime dailyLastSent, ZonedDateTime weeklyLastSent) {
    return new DigestUserEntity(1L,
                                "user",
                                dailyLastSent != null,
                                weeklyLastSent != null,
                                zone.getId(),
                                dailyLastSent == null ? null : dailyLastSent.toInstant(),
                                weeklyLastSent == null ? null : weeklyLastSent.toInstant());
  }

}
