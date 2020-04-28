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

import org.exoplatform.commons.api.notification.stat.QueueStatistics;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Oct 11, 2013  
 */
public class ThreadLocalQueueStatisticsImpl implements QueueStatistics {

  private AtomicLong putCount = new AtomicLong();
  private AtomicLong pollCount = new AtomicLong();
  
  @Override
  public long getPollCount() {
    return pollCount.get();
  }

  @Override
  public long getPutCount() {
    return putCount.get();
  }

  @Override
  public void incrementPollCount() {
    pollCount.incrementAndGet();
  }

  @Override
  public void incrementPutCount() {
    putCount.incrementAndGet();
  }
  
  @Override
  public String toString() {
    return new StringBuilder()
        .append("QueueStatistics[")
        .append("putCount=").append(this.putCount)
        .append(",pollCount=").append(this.pollCount)
        .append(']')
        .toString();
  }

}
