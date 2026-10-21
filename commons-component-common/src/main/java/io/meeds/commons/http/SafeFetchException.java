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

import java.io.IOException;

/**
 * A URL {@link SafeHttpFetcher} read nothing usable from, with the
 * {@link SafeFetchFailure} saying why and, for an error status, the status.
 * An {@link IOException} so that it travels out of the HTTP client's response
 * handler unchanged.
 */
public class SafeFetchException extends IOException {

  private static final long      serialVersionUID = -3016927398415581533L;

  private final SafeFetchFailure failure;

  private final int              status;

  /**
   * Builds the exception for a failure.
   *
   * @param failure why nothing was read
   */
  public SafeFetchException(SafeFetchFailure failure) {
    this(failure, -1, null);
  }

  /**
   * Builds the exception for a failure, keeping its cause for the logs.
   *
   * @param failure why nothing was read
   * @param cause what failed
   */
  public SafeFetchException(SafeFetchFailure failure, Throwable cause) {
    this(failure, -1, cause);
  }

  /**
   * Builds the exception for an answer whose status is the failure.
   *
   * @param failure why nothing was read
   * @param status the HTTP status answered, -1 when none was
   */
  public SafeFetchException(SafeFetchFailure failure, int status) {
    this(failure, status, null);
  }

  /**
   * Builds the exception.
   *
   * @param failure why nothing was read
   * @param status the HTTP status answered, -1 when none was
   * @param cause what failed, or null
   */
  private SafeFetchException(SafeFetchFailure failure, int status, Throwable cause) {
    super(status >= 0 ? failure.getDescription() + " (HTTP " + status + ")" : failure.getDescription(), cause);
    this.failure = failure;
    this.status = status;
  }

  /**
   * @return why nothing was read
   */
  public SafeFetchFailure getFailure() {
    return failure;
  }

  /**
   * @return the HTTP status answered when the status is the failure, -1
   *         otherwise
   */
  public int getStatus() {
    return status;
  }

}
