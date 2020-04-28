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
package org.exoplatform.commons.notification;

import org.exoplatform.commons.api.notification.stat.Statistics;
import org.exoplatform.commons.api.notification.stat.StatisticsCollector;
import org.exoplatform.commons.notification.impl.StatisticsService;
import org.exoplatform.commons.utils.CommonsUtils;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Oct 11, 2013  
 */
public class NotificationContextFactory {
  
  private final StatisticsService statisticsService;
  
  public NotificationContextFactory(StatisticsService statisticsService) {
    this.statisticsService = statisticsService;
  }
  
  public StatisticsService getStatisticsService() {
    return this.statisticsService;
  }
  
  public StatisticsCollector getStatisticsCollector() {
    return this.statisticsService.getStatisticsCollector();
  }
  
  public Statistics getStatistics() {
    return this.statisticsService.getStatistics();
  }
}
