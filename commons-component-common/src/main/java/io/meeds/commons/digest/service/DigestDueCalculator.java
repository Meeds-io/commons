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

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.meeds.commons.digest.entity.DigestUserEntity;
import io.meeds.commons.digest.model.DigestFrequency;

/**
 * Answers "is this user due now?" in his own local calendar, in Java and not
 * in SQL, so that the rule is the same on every database. The rule: the most
 * recent scheduled occurrence (the send hour of the send day) has passed since
 * his watermark, and his watermark is not from that occurrence's day already.
 * Consequences, all wanted: at most one daily digest per local day and one
 * weekly per local week, always at the send hour or later; a run missed at the
 * send hour is caught up by the next run; a frequency switched on today sends
 * its first digest tomorrow; a timezone change never causes a second email the
 * same local day.
 */
@Component
public class DigestDueCalculator {

  private final int       sendHour;

  private final DayOfWeek weeklyDay;

  public DigestDueCalculator(@Value("${exo.notification.digest.daily.hour:18}") int sendHour,
                             @Value("${exo.notification.digest.weekly.day:FRIDAY}") String weeklyDay) {
    this.sendHour = Math.min(Math.max(sendHour, 0), 23);
    this.weeklyDay = parseDay(weeklyDay);
  }

  public boolean isDue(DigestUserEntity user, DigestFrequency frequency, Instant now) {
    Instant watermark = frequency == DigestFrequency.DAILY ? user.getDailyLastSent() : user.getWeeklyLastSent();
    if (watermark == null || !watermark.isBefore(now)) {
      return false;
    }
    ZoneId zone = zoneOf(user.getTimeZone());
    ZonedDateTime localNow = now.atZone(zone);
    LocalDate today = localNow.toLocalDate();
    // The most recent scheduled occurrence: today at the send hour when it has
    // passed, otherwise the previous send day
    ZonedDateTime lastOccurrence = today.atTime(sendHour, 0).atZone(zone);
    if (lastOccurrence.isAfter(localNow)) {
      lastOccurrence = lastOccurrence.minusDays(1);
    }
    if (frequency == DigestFrequency.WEEKLY) {
      while (lastOccurrence.getDayOfWeek() != weeklyDay) {
        lastOccurrence = lastOccurrence.minusDays(1);
      }
    }
    if (!watermark.isBefore(lastOccurrence.toInstant())) {
      return false;
    }
    // A watermark from the occurrence's own day (served, or switched on, that
    // day) means that occurrence is done: never twice the same local day, and
    // never before the send hour, whatever the timezone changes
    return watermark.atZone(zone).toLocalDate().isBefore(lastOccurrence.toLocalDate());
  }

  /**
   * @param timeZone the timezone stored for the user, may be null or unknown
   * @return that timezone, or the server one when the user has none (assumption
   *         A1 of the specification)
   */
  public static ZoneId zoneOf(String timeZone) {
    if (StringUtils.isBlank(timeZone)) {
      return ZoneId.systemDefault();
    }
    try {
      return ZoneId.of(timeZone);
    } catch (DateTimeException e) {
      return ZoneId.systemDefault();
    }
  }

  private static DayOfWeek parseDay(String weeklyDay) {
    try {
      return DayOfWeek.valueOf(StringUtils.upperCase(StringUtils.trim(weeklyDay)));
    } catch (IllegalArgumentException | NullPointerException e) {
      return DayOfWeek.FRIDAY;
    }
  }

}
