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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.notification.impl.jpa.email.entity.MailNotifEntity;
import org.exoplatform.commons.notification.impl.jpa.email.entity.MailParamEntity;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;

public class MailParamDAO extends GenericDAOJPAImpl<MailParamEntity, Long> {

  @ExoTransactional
  public void deleteParamsOfNotifications(List<MailNotifEntity> allNotificationsWithoutDigests) {
    List<Long> ids = new ArrayList<>();
    for (MailNotifEntity mailNotifEntity : allNotificationsWithoutDigests) {
      ids.add(mailNotifEntity.getId());
    }
    Query query = getEntityManager().createNamedQuery("NotificationsMailParamsEntity.deleteParamsOfNotifications").setParameter("notifications", ids);
    query.executeUpdate();
  }

}
