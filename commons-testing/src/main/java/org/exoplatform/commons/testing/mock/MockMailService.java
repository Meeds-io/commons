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
package org.exoplatform.commons.testing.mock;

import java.util.concurrent.Future;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.exoplatform.services.mail.MailService;
import org.exoplatform.services.mail.Message;

public class MockMailService implements MailService {

  @Override
  public Session getMailSession() {
    return null;
  }

  @Override
  public String getOutgoingMailServer() {
    return null;
  }

  @Override
  public void sendMessage(String from, String to, String subject, String body) throws Exception {

  }

  @Override
  public void sendMessage(Message message) throws Exception {

  }

  @Override
  public void sendMessage(MimeMessage message) throws Exception {

  }

  @Override
  public Future<Boolean> sendMessageInFuture(String from, String to, String subject, String body) {
    return null;
  }

  @Override
  public Future<Boolean> sendMessageInFuture(Message message) {
    return null;
  }

  @Override
  public Future<Boolean> sendMessageInFuture(MimeMessage message) {
    return null;
  }

}
