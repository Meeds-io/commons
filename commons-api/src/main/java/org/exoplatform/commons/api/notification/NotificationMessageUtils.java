/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
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
package org.exoplatform.commons.api.notification;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.commons.api.notification.model.ArgumentLiteral;
import org.exoplatform.commons.api.notification.model.NotificationInfo;

public class NotificationMessageUtils {

  protected static final Log LOG = ExoLogger.getLogger(NotificationMessageUtils.class);

  /**
   * @deprecated Use {@link NotificationInfo#isRead()} instead
   */
  @Deprecated(forRemoval = true, since = "1.5.0")
  public final static ArgumentLiteral<String> READ_PORPERTY = new ArgumentLiteral<>(String.class, "read");

  /**
   * @deprecated Use {@link NotificationInfo#isOnPopOver()} instead
   */
  @Deprecated(forRemoval = true, since = "1.5.0")
  public final static ArgumentLiteral<String> SHOW_POPOVER_PROPERTY = new ArgumentLiteral<>(String.class, "showPopover");

  public final static ArgumentLiteral<String> NOT_HIGHLIGHT_COMMENT_PORPERTY = new ArgumentLiteral<>(String.class, "notHighlightComment");

}
