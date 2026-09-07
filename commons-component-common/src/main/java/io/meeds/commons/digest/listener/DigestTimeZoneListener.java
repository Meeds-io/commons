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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.listener.Asynchronous;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;

import io.meeds.commons.digest.DigestService;

/**
 * The glue between the platform timezone of a user and the digest: when the
 * platform saves a user's timezone (event social.timeZone.saved, source = the
 * username, data = the zone id), the copy the digest sender job keeps in its
 * work list is refreshed, so the next occurrence is computed in the new
 * timezone. Fail-safe: a failure is logged, the timezone save itself is never
 * affected.
 */
@Asynchronous
public class DigestTimeZoneListener extends Listener<String, String> {

  private static final Logger LOG = LoggerFactory.getLogger(DigestTimeZoneListener.class);

  private DigestService       digestService;

  public DigestTimeZoneListener() {
    // Looked up lazily: the digest service is a Spring bean reaching the Kernel
    // only once the Spring contexts are started
  }

  DigestTimeZoneListener(DigestService digestService) {
    this.digestService = digestService;
  }

  @Override
  public void onEvent(Event<String, String> event) {
    String username = event.getSource();
    try {
      DigestService service = getDigestService();
      if (service != null) {
        service.updateTimeZone(username, event.getData());
      }
    } catch (Exception e) {
      LOG.warn("The digest timezone of {} could not be refreshed, the next digest keeps the previous one", username, e);
    }
  }

  private DigestService getDigestService() {
    if (digestService == null) {
      digestService = ExoContainerContext.getService(DigestService.class);
    }
    return digestService;
  }

}
