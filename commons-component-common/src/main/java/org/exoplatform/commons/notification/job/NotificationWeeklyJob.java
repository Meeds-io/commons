/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Affero General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.notification.job;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.service.storage.NotificationService;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.notification.job.mbeans.WeeklyService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.PortalContainer;

public class NotificationWeeklyJob extends NotificationJob {

  public NotificationWeeklyJob() {
    this(PortalContainer.getInstance());
  }

  public NotificationWeeklyJob(ExoContainer exoContainer) {
    super(exoContainer);
  }

  @Override
  protected void processSendNotification() throws Exception {
    if (WeeklyService.isStarted() == false) {
      LOG.info("Starting run job to send weekly email notification ... ");
      NotificationContext context = NotificationContextImpl.cloneInstance();
      context.append(JOB_DAILY, false);
      context.append(JOB_WEEKLY, true);
      CommonsUtils.getService(NotificationService.class).digest(context);
    }
  }
  
}
