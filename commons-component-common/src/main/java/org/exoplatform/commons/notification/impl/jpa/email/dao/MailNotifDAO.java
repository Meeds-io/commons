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
package org.exoplatform.commons.notification.impl.jpa.email.dao;

import java.util.Calendar;
import java.util.List;

import javax.persistence.TypedQuery;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.notification.impl.jpa.email.entity.MailNotifEntity;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;

public class MailNotifDAO extends GenericDAOJPAImpl<MailNotifEntity, Long> {

  @ExoTransactional
  public List<MailNotifEntity> getNotifsByPluginAndDay(String pluginId, String dayName) {
    Calendar cal = Calendar.getInstance();
    TypedQuery<MailNotifEntity> query = getEntityManager()
                                                          .createNamedQuery("NotificationsMailNotifEntity.getNotifsByPluginAndDay",
                                                                            MailNotifEntity.class)
                                                          .setParameter("day", Integer.parseInt(dayName))
                                                          .setParameter("month", cal.get(Calendar.MONTH) + 1)
                                                          .setParameter("year", cal.get(Calendar.YEAR))
                                                          .setParameter("pluginId", pluginId);
    return query.getResultList();
  }

  @ExoTransactional
  public List<MailNotifEntity> getNotifsByPluginAndWeek(String pluginId, Calendar oneWeekAgo) {
    TypedQuery<MailNotifEntity> query = getEntityManager()
                                                          .createNamedQuery("NotificationsMailNotifEntity.getNotifsByPluginAndWeek",
                                                                            MailNotifEntity.class)
                                                          .setParameter("date", oneWeekAgo)
                                                          .setParameter("pluginId", pluginId);
    return query.getResultList();
  }

  @ExoTransactional
  public List<MailNotifEntity> getAllNotificationsWithoutDigests(int offset, int limit) {
    TypedQuery<MailNotifEntity> query = getEntityManager()
                                                          .createNamedQuery("NotificationsMailNotifEntity.getAllNotificationsWithoutDigests",
                                                                            MailNotifEntity.class)
                                                          .setFirstResult(offset)
                                                          .setMaxResults(limit);
    return query.getResultList();
  }
}
