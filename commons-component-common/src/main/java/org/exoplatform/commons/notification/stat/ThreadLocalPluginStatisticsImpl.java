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
package org.exoplatform.commons.notification.stat;

import java.util.concurrent.atomic.AtomicLong;

import org.exoplatform.commons.api.notification.stat.PluginStatistics;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Oct 10, 2013  
 */
public class ThreadLocalPluginStatisticsImpl implements PluginStatistics {
  
  private AtomicLong createMessageInfoCount = new AtomicLong();
  private AtomicLong createdNotificationInfoCount = new AtomicLong();
  private AtomicLong createDigestCount = new AtomicLong();

  @Override
  public long getCreateMessageInfoCount() {
    return createMessageInfoCount.get();
  }

  @Override
  public long getCreateNotificationInfoCount() {
    return createdNotificationInfoCount.get();
  }

  @Override
  public long getCreateDigestCount() {
    return createDigestCount.get();
  }
  
  @Override
  public String toString() {
    return new StringBuilder()
    .append("PluginStatistics[")
    .append("createMessageCount=").append(this.createMessageInfoCount)
    .append(",createNotificationCount=").append(this.createdNotificationInfoCount)
    .append(",createDigestCount=").append(this.createDigestCount)
    .append(']')
    .toString();
  }

  @Override
  public void incrementCreateMessageCount() {
    this.createMessageInfoCount.incrementAndGet();
  }

  @Override
  public void incrementCreateNotificationCount() {
    this.createdNotificationInfoCount.incrementAndGet();
  }

  @Override
  public void incrementCreateDigestCount() {
    this.createDigestCount.incrementAndGet();
  }
}

