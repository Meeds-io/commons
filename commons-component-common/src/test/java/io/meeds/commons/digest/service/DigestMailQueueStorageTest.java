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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.service.QueueMessage;
import org.exoplatform.commons.notification.impl.jpa.email.entity.MailQueueEntity;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.ListenerService;

import io.meeds.commons.digest.dao.DigestMailQueueDAO;

/**
 * The digest writer of the mail queue: the row it writes has the shape the
 * legacy writer gives it and the sender job reads, the queue event is fired,
 * and a message with no usable recipient is refused loudly (the builder never
 * hands one over).
 */
@RunWith(MockitoJUnitRunner.class)
public class DigestMailQueueStorageTest {

  @Mock
  private DigestMailQueueDAO     mailQueueDAO;

  @Mock
  private ListenerService        listenerService;

  private DigestMailQueueStorage storage;

  @Before
  public void setUp() {
    storage = new DigestMailQueueStorage(mailQueueDAO, listenerService);
  }

  @Test
  public void testQueuedRowHasTheShapeTheSenderJobReads() throws Exception {
    MessageInfo message = new MessageInfo().pluginId("digest")
                                           .from("Platform<noreply@example.com>")
                                           .to("Ayoub Z<ayoub@example.com>")
                                           .subject("Your daily recap")
                                           .body("<p>hi</p>")
                                           .footer("footer")
                                           .end();

    storage.enqueue(message);

    ArgumentCaptor<MailQueueEntity> row = ArgumentCaptor.forClass(MailQueueEntity.class);
    verify(mailQueueDAO).save(row.capture());
    // The seven columns JPAQueueMessageImpl.saveMessageInfo writes
    assertEquals("digest", row.getValue().getType());
    assertEquals("Platform<noreply@example.com>", row.getValue().getFrom());
    assertEquals("Ayoub Z<ayoub@example.com>", row.getValue().getTo());
    assertEquals("Your daily recap", row.getValue().getSubject());
    assertEquals("<p>hi</p>", row.getValue().getBody());
    assertEquals("footer", row.getValue().getFooter());
    assertNotNull(row.getValue().getCreationDate());

    ArgumentCaptor<Event<QueueMessage, String>> event = ArgumentCaptor.forClass(Event.class);
    verify(listenerService).broadcast(event.capture());
    assertEquals(QueueMessage.MESSAGE_SENT_FROM_QUEUE, event.getValue().getEventName());
  }

  @Test
  public void testMessageWithoutRecipientIsRefused() {
    assertThrows(IllegalArgumentException.class, () -> storage.enqueue(null));
    assertThrows(IllegalArgumentException.class, () -> storage.enqueue(new MessageInfo().to(" ").end()));
    verify(mailQueueDAO, never()).save(any());
  }

  @Test
  public void testMessageWithAnInvalidRecipientIsRefused() {
    // A "Lastname, Firstname" display name splits into two addresses for the
    // strict parser: the builder skips such a recipient upstream, the queue
    // never accepts him silently
    assertThrows(IllegalArgumentException.class, () -> storage.enqueue(new MessageInfo().to("Smith, John<john@example.com>").end()));
    verify(mailQueueDAO, never()).save(any());
  }

}
