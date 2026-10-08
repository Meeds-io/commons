/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
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
package org.exoplatform.commons.api.notification.service;

import java.util.List;

/**
 * Lists the users a send-all notification walks, page by page: the enabled
 * users of the platform, the internal ones only when asked. A send-all uses
 * the implementation registered in the portal container (a Kernel component,
 * or a Spring bean whose class carries {@code @Service}, which the Kernel
 * bridge exports), and walks the enabled users of the organization service
 * when there is none. At most one implementation may be registered: with two,
 * every send-all active on a channel fails.
 */
public interface SendAllRecipientProvider {

  /**
   * @param internalsOnly whether the external users are left out
   * @param afterUsername the last username of the previous page, excluded, or
   *          null for the first page
   * @param limit the maximum number of usernames returned
   * @return the usernames that follow {@code afterUsername}, in ascending
   *         order; fewer than {@code limit} only on the last page
   */
  List<String> getRecipients(boolean internalsOnly, String afterUsername, int limit);

}
