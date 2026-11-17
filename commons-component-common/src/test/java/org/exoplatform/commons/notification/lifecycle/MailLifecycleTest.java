/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
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
package org.exoplatform.commons.notification.lifecycle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.service.QueueMessage;
import org.exoplatform.commons.notification.NotificationContextFactory;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.portal.branding.BrandingService;
import org.exoplatform.commons.api.notification.stat.Statistics;

public class MailLifecycleTest {

  private static final String   BOUND = "<table class=\"mail-bg\"><tr><td class=\"mail-surface mail-text\">bound</td></tr></table>";

  private MockedStatic<CommonsUtils> commonsUtils;

  private QueueMessage               queue;

  private MailLifecycle              lifecycle;

  private AbstractTemplateBuilder    builder;

  private NotificationContext        ctx;

  @Before
  public void setUp() {
    commonsUtils = mockStatic(CommonsUtils.class);
    NotificationContextFactory factory = mock(NotificationContextFactory.class);
    Statistics statistics = mock(Statistics.class);
    when(factory.getStatistics()).thenReturn(statistics);
    queue = mock(QueueMessage.class);
    BrandingService branding = mock(BrandingService.class);
    when(branding.getDisplayMode("mary")).thenReturn("dark");
    commonsUtils.when(() -> CommonsUtils.getService(NotificationContextFactory.class)).thenReturn(factory);
    commonsUtils.when(() -> CommonsUtils.getService(QueueMessage.class)).thenReturn(queue);
    commonsUtils.when(() -> CommonsUtils.getService(BrandingService.class)).thenReturn(branding);

    lifecycle = new MailLifecycle();
    AbstractChannel channel = mock(AbstractChannel.class);
    builder = mock(AbstractTemplateBuilder.class);
    when(channel.getTemplateBuilder(any(PluginKey.class))).thenReturn(builder);
    lifecycle.setChannel(channel);

    NotificationInfo notification = new NotificationInfo().key("TestPlugin").to("mary");
    ctx = mock(NotificationContext.class);
    when(ctx.getNotificationInfo()).thenReturn(notification);
  }

  @After
  public void tearDown() {
    commonsUtils.close();
  }

  @Test
  public void anInstantMailIsSentInTheDocumentOfItsRecipientsMode() throws Exception {
    when(builder.buildMessage(ctx)).thenReturn(new MessageInfo().to("mary@acme.io").body(BOUND).footer("<tr>footer</tr>"));
    lifecycle.send(ctx);
    ArgumentCaptor<MessageInfo> sent = ArgumentCaptor.forClass(MessageInfo.class);
    verify(queue).sendMessage(sent.capture());
    String body = sent.getValue().makeEmailNotification().getBody();
    assertTrue(body, body.startsWith("<!DOCTYPE html>"));
    assertTrue(body, body.contains("content=\"dark\""));
    assertTrue(body, body.contains(BOUND + "<tr>footer</tr>"));
    assertNull("the footer is in the document", sent.getValue().getFooter());
  }

  @Test
  public void aBodyAlreadyAWholeDocumentIsSentAsIs() throws Exception {
    for (String document : new String[] { "<!DOCTYPE html><html><body>" + BOUND + "</body></html>",
        "<html><body>" + BOUND + "</body></html>" }) {
      QueueMessage queueOfTheMail = mock(QueueMessage.class);
      commonsUtils.when(() -> CommonsUtils.getService(QueueMessage.class)).thenReturn(queueOfTheMail);
      when(builder.buildMessage(ctx)).thenReturn(new MessageInfo().to("mary@acme.io").body(document));
      lifecycle.send(ctx);
      ArgumentCaptor<MessageInfo> sent = ArgumentCaptor.forClass(MessageInfo.class);
      verify(queueOfTheMail).sendMessage(sent.capture());
      assertEquals(document, sent.getValue().makeEmailNotification().getBody());
    }
  }

}
