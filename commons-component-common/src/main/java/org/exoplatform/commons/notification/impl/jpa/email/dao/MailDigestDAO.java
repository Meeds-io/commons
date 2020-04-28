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

import java.util.Set;

import javax.persistence.Query;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.notification.impl.jpa.email.entity.MailDigestEntity;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;

public class MailDigestDAO extends GenericDAOJPAImpl<MailDigestEntity, Long> {
  @ExoTransactional
  public void deleteAllDigestsOfType(String type) {
    Query query = getEntityManager().createNamedQuery("NotificationsMailDigestEntity.deleteAllDigestsOfType").setParameter("digestType", type);
    query.executeUpdate();
  }

  @ExoTransactional
  public void deleteDigestsOfTypeByNotificationsIds(Set<Long> mailNotifsIds, String type) {
    Query query = getEntityManager().createNamedQuery("NotificationsMailDigestEntity.deleteDigestsOfTypeByNotificationsIds")
        .setParameter("digestType", type)
        .setParameter("notificationIds", mailNotifsIds);
    query.executeUpdate();
  }

  @ExoTransactional
  public void deleteAllDigests() {
    Query query = getEntityManager().createNamedQuery("NotificationsMailDigestEntity.deleteAllDigests");
    query.executeUpdate();
  }
}
