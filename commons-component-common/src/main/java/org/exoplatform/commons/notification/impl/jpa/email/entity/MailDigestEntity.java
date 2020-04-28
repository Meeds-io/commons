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
package org.exoplatform.commons.notification.impl.jpa.email.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.exoplatform.commons.api.persistence.ExoEntity;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Mar 07, 2017
 */
@Entity(name = "NotificationsMailDigestEntity")
@ExoEntity
@Table(name = "NTF_EMAIL_NOTIFS_DIGEST")
@NamedQueries({
  @NamedQuery(name = "NotificationsMailDigestEntity.deleteAllDigestsOfType", query = "DELETE FROM NotificationsMailDigestEntity m " +
      "WHERE m.type= :digestType"),
  @NamedQuery(name = "NotificationsMailDigestEntity.deleteDigestsOfTypeByNotificationsIds", query = "DELETE FROM NotificationsMailDigestEntity m " +
      "WHERE m.type= :digestType " +
      "AND m.notification.id IN (:notificationIds) "),
    @NamedQuery(name = "NotificationsMailDigestEntity.deleteAllDigests", query = "DELETE FROM NotificationsMailDigestEntity m ")
})
public class MailDigestEntity {
  @Id
  @Column(name = "EMAIL_NOTIF_DIGEST_ID")
  @SequenceGenerator(name="SEQ_NTF_EMAIL_DIGEST", sequenceName="SEQ_NTF_EMAIL_DIGEST")
  @GeneratedValue(strategy=GenerationType.AUTO, generator="SEQ_NTF_EMAIL_DIGEST")
  private long id;

  @ManyToOne
  @JoinColumn(name = "EMAIL_NOTIF_ID")
  private MailNotifEntity notification;

  @Column(name = "DIGEST_TYPE")
  private String type;

  public long getId() {
    return id;
  }

  public MailNotifEntity getNotification() {
    return notification;
  }

  public MailDigestEntity setNotification(MailNotifEntity notification) {
    this.notification = notification;
    return this;
  }

  public String getType() {
    return type;
  }

  public MailDigestEntity setType(String type) {
    this.type = type;
    return this;
  }
}