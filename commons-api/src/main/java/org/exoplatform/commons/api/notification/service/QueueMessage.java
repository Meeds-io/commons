/*
 * Copyright (C) 2003-${year} eXo Platform SAS.
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.api.notification.service;

import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.services.mail.Message;

public interface QueueMessage {

  /**
   * Puts the message into the queue
   * @param message
   * @return
   */
  boolean put(MessageInfo message);
  
  /**
   * Peek the message from queue and send
   */
  void send() ;

  /**
   * Sends the message using mail service
   * @param message the message to be sent
   * @return true if the message is sent or mail service is off
   */
  boolean sendMessage(Message message) throws Exception;
}
