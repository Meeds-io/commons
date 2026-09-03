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

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.listener.Asynchronous;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;

import io.meeds.commons.digest.DigestService;

/**
 * The glue between the notification dispatcher and the digest: captures every
 * processed notification for the digests of its recipients, apart from the
 * notification itself. Asynchronous and fail-safe on purpose: a capture
 * failure is logged and dropped, it can never make the notification fail, and
 * no capture work ever sits on the notification path.
 */
@Asynchronous
public class DigestCaptureListener extends Listener<Object, NotificationInfo> {

  private static final Logger LOG = LoggerFactory.getLogger(DigestCaptureListener.class);

  @Override
  public void onEvent(Event<Object, NotificationInfo> event) {
    NotificationInfo notification = event.getData();
    try {
      // Looked up lazily: the digest service is a Spring bean reaching the
      // Kernel only once the Spring contexts are started
      DigestService digestService = ExoContainerContext.getService(DigestService.class);
      if (digestService != null) {
        digestService.capture(notification);
      }
    } catch (Exception e) {
      LOG.warn("Digest capture failed for notification '{}' of plugin '{}', the notification itself is not affected",
               notification == null ? null : notification.getId(),
               notification == null || notification.getKey() == null ? null : notification.getKey().getId(),
               e);
    }
  }

}
