/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.exoplatform.commons.notification.impl.service.storage.cache.model;

import java.io.Serializable;

public class SimpleCacheData<T> implements Serializable {
  private static final long serialVersionUID = 1L;
  private final T           t;

  public SimpleCacheData(final T t) {
    this.t = t;
  }

  public T build() {
    return t;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SimpleCacheData)) return false;

    SimpleCacheData<?> that = (SimpleCacheData<?>) o;

    return t != null ? t.equals(that.t) : that.t == null;

  }

  @Override
  public int hashCode() {
    return t != null ? t.hashCode() : 0;
  }
}
