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
package org.exoplatform.jpa.notifications.email.dao;

import org.exoplatform.commons.notification.impl.jpa.email.entity.MailQueueEntity;
import org.exoplatform.jpa.CommonsDAOJPAImplTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class MailQueueDAOTest extends CommonsDAOJPAImplTest {
  @Before
  public void setUp() throws Exception {
    super.setUp();
    mailQueueDAO.deleteAll();
  }

  @After
  public void tearDown()  {
    mailQueueDAO.deleteAll();
  }

  @Test
  public void testFindAllByOffsetAndLimit() {
    //Given
    mailQueueDAO.create(new MailQueueEntity().setBody("1"));
    mailQueueDAO.create(new MailQueueEntity().setBody("2"));
    mailQueueDAO.create(new MailQueueEntity().setBody("3"));
    mailQueueDAO.create(new MailQueueEntity().setBody("4"));
    mailQueueDAO.create(new MailQueueEntity().setBody("5"));

    //When
    List<MailQueueEntity> mailQueueEntities1 = mailQueueDAO.findAll(0, 10);
    List<MailQueueEntity> mailQueueEntities2 = mailQueueDAO.findAll(2, 4);

    //Then
    assertThat(mailQueueEntities1.size(), is(5));
    assertEquals(mailQueueEntities2.get(0).getBody(), "3");
  }
}
