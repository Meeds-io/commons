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

import org.exoplatform.commons.api.persistence.ExoEntity;

import javax.persistence.*;
import java.util.Calendar;

@Entity(name = "NotificationsMailQueueEntity")
@ExoEntity
@Table(name = "EMAIL_QUEUE")
@NamedQueries({
    @NamedQuery(name = "NotificationsMailQueueEntity.getMessagesInQueue", query = "SELECT m FROM NotificationsMailQueueEntity m " +
        "ORDER BY m.creationDate ASC "),
})
public class MailQueueEntity {
  @Id
  @Column(name = "EMAIL_ID")
  @SequenceGenerator(name="SEQ_NTF_EMAIL_QUEUE", sequenceName="SEQ_NTF_EMAIL_QUEUE")
  @GeneratedValue(strategy= GenerationType.AUTO, generator="SEQ_NTF_EMAIL_QUEUE")
  private long id;

  @Column(name = "CREATION_DATE")
  private Calendar creationDate;

  @Column(name = "TYPE")
  private String type;

  @Column(name = "SENDER")
  private String from;

  @Column(name = "RECEIVER")
  private String to;

  @Column(name = "SUBJECT")
  private String subject;

  @Column(name = "BODY")
  private String body;

  @Column(name = "FOOTER")
  private String footer;

  public long getId() {
    return id;
  }

  public Calendar getCreationDate() {
    return creationDate;
  }

  public MailQueueEntity setCreationDate(Calendar creationDate) {
    this.creationDate = creationDate;
    return this;
  }

  public String getType() {
    return type;
  }

  public MailQueueEntity setType(String type) {
    this.type = type;
    return this;
  }

  public String getFrom() {
    return from;
  }

  public MailQueueEntity setFrom(String from) {
    this.from = from;
    return this;
  }

  public String getTo() {
    return to;
  }

  public MailQueueEntity setTo(String to) {
    this.to = to;
    return this;
  }

  public String getSubject() {
    return subject;
  }

  public MailQueueEntity setSubject(String subject) {
    this.subject = subject;
    return this;
  }

  public String getBody() {
    return body;
  }

  public MailQueueEntity setBody(String body) {
    this.body = body;
    return this;
  }

  public String getFooter() {
    return footer;
  }

  public MailQueueEntity setFooter(String footer) {
    this.footer = footer;
    return this;
  }

}
