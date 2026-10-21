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
package io.meeds.commons.http;

/**
 * One request's outcome inside {@link SafeHttpFetcher}: a response, or the
 * {@code Location} of a redirect to follow.
 *
 * @param response the response, null for a redirect
 * @param redirect the {@code Location} answered, null for a response
 */
record SafeFetchHop(SafeFetchResponse response, String redirect) {

  /**
   * A hop that answered.
   *
   * @param response the response
   * @return the hop
   */
  static SafeFetchHop answered(SafeFetchResponse response) {
    return new SafeFetchHop(response, null);
  }

  /**
   * A hop that redirects.
   *
   * @param location the {@code Location} answered
   * @return the hop
   */
  static SafeFetchHop redirected(String location) {
    return new SafeFetchHop(null, location);
  }

}
