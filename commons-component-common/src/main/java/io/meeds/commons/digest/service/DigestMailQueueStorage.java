/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.commons.digest.service;

import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.service.QueueMessage;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.notification.impl.jpa.email.entity.MailQueueEntity;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.ListenerService;

import io.meeds.commons.digest.dao.DigestMailQueueDAO;

/**
 * Puts a digest email in the platform mail queue, with the same row shape and
 * the same checks as the legacy {@code QueueMessage.put}, but through the
 * Spring EntityManager: called inside the transaction that claims the
 * occurrence, the queue row and the watermark commit, or roll back, together.
 * The {@code SendEmailNotificationJob} then sends it like any other email.
 */
@Component
public class DigestMailQueueStorage {

  private static final Logger      LOG = LoggerFactory.getLogger(DigestMailQueueStorage.class);

  private final DigestMailQueueDAO mailQueueDAO;

  private final ListenerService    listenerService;

  public DigestMailQueueStorage(DigestMailQueueDAO mailQueueDAO, ListenerService listenerService) {
    this.mailQueueDAO = mailQueueDAO;
    this.listenerService = listenerService;
  }

  /**
   * Queues the message in the transaction of the caller.
   *
   * @param message the digest email
   * @return true when the message is queued, false when it has no valid
   *         recipient address (then nothing is written)
   */
  public boolean enqueue(MessageInfo message) {
    if (message == null || StringUtils.isBlank(message.getTo())) {
      return false;
    }
    if (!NotificationUtils.isValidEmailAddresses(message.getTo())) {
      LOG.warn("The email {} is not valid for sending the digest", message.getTo());
      return false;
    }
    MailQueueEntity entity = new MailQueueEntity();
    entity.setType(message.getPluginId());
    entity.setFrom(message.getFrom());
    entity.setTo(message.getTo());
    entity.setSubject(message.getSubject());
    entity.setBody(message.getBody());
    entity.setFooter(message.getFooter());
    entity.setCreationDate(Calendar.getInstance());
    mailQueueDAO.save(entity);
    broadcastQueued(message);
    return true;
  }

  /** The same event the legacy queue fires, the queue capacity manager counts it */
  private void broadcastQueued(MessageInfo message) {
    try {
      listenerService.broadcast(new Event<QueueMessage, String>(QueueMessage.MESSAGE_SENT_FROM_QUEUE, null, message.getId()));
    } catch (Exception e) {
      LOG.debug("Error broadcasting the queued digest event of {}", message.getTo(), e);
    }
  }

}
