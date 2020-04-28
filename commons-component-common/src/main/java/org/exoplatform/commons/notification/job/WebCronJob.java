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
package org.exoplatform.commons.notification.job;

import org.exoplatform.commons.utils.ExoProperties;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.scheduler.CronJob;
import org.quartz.JobDataMap;

public class WebCronJob extends CronJob {
  public static final String LIVE_DAYS_KEY = "liveDays";
  private JobDataMap jdatamap_;

  public WebCronJob(InitParams params) throws Exception {
    super(params);
    ExoProperties props = params.getPropertiesParam("web.info").getProperties();
    jdatamap_ = new JobDataMap();
    String days = props.getProperty(LIVE_DAYS_KEY).trim();
    jdatamap_.put(LIVE_DAYS_KEY, days);
  }

  public JobDataMap getJobDataMap() {
    return jdatamap_;
  }
}
