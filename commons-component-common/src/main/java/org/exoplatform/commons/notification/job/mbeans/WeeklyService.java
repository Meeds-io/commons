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
package org.exoplatform.commons.notification.job.mbeans;

import org.exoplatform.management.annotations.ManagedBy;
import org.picocontainer.Startable;

@ManagedBy(WeeklyJobManager.class)
public class WeeklyService implements Startable {
  
  private WeeklyJobManager manager;
  
  private static boolean STARTED = false;
  
  public WeeklyService() {}
  
  public void setManager(WeeklyJobManager manager) {
    this.manager = manager;
  }

  /**
   * Register this instance as managed object
   */
  public void start() {
    manager.register(this);
  }

  @Override
  public void stop() {
  }
  
  /**
   * Change state of service to ON
   */
  public void on() {
    STARTED = true;
  }
  
  /**
   * Change state of service to OFF
   */
  public void off() {
    STARTED = false;
  }
  
  /**
   * Get state of service
   * @return 
   */
  public static boolean isStarted() {
    return STARTED;
  }

}
