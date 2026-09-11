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
package io.meeds.commons.digest.listener;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.services.listener.Event;

import io.meeds.commons.digest.DigestService;

/**
 * The timezone event reaches the digest, and a failure there never escapes.
 */
@RunWith(MockitoJUnitRunner.class)
public class DigestTimeZoneListenerTest {

  @Mock
  private DigestService digestService;

  @Test
  public void testSavedTimeZoneIsHandedToTheDigest() {
    new DigestTimeZoneListener(digestService).onEvent(new Event<>("social.timeZone.saved", "ayoub", "Asia/Tokyo"));

    verify(digestService).updateTimeZone("ayoub", "Asia/Tokyo");
  }

  @Test
  public void testFailureIsLoggedNotThrown() {
    doThrow(new IllegalStateException("db down")).when(digestService).updateTimeZone("ayoub", "Asia/Tokyo");

    new DigestTimeZoneListener(digestService).onEvent(new Event<>("social.timeZone.saved", "ayoub", "Asia/Tokyo"));

    // The event is handed over, the failure stays inside: the timezone save of
    // the user must not fail because the digest is unavailable
    verify(digestService).updateTimeZone("ayoub", "Asia/Tokyo");
  }

}
