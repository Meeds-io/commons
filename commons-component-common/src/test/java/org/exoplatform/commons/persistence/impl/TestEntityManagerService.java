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
package org.exoplatform.commons.persistence.impl;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.jpa.CommonsDAOJPAImplTest;

import javax.persistence.EntityManager;

public class TestEntityManagerService extends CommonsDAOJPAImplTest {

  private EntityManagerService service;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    service = PortalContainer.getInstance().getComponentInstanceOfType(EntityManagerService.class);
  }

  public void testServiceInitialize() {
    assertNotNull(service);

    EntityManager em1 = service.getEntityManager();
    assertNotNull(em1);
  }

  public void testStartLifecycleTwice() {
    EntityManager em1 = service.getEntityManager();
    assertNotNull(em1);

    //
    RequestLifeCycle.begin(service);
    EntityManager em2 = service.getEntityManager();
    assertSame(em2, em1);

    RequestLifeCycle.end();
    EntityManager em3 = service.getEntityManager();
    assertNotNull(em3);
    assertSame(em3, em1);
  }

  public void testEntityManagerOutsideDefaultLifecycle() {
    EntityManager em1 = service.getEntityManager();
    assertNotNull(em1);

    //
    A a = new A(em1);
    a.run();
    assertTrue(a.succeed);
    assertTrue(a.sameEntity);

    // Try to run in a different thread (out of default lifecycle)
    a = new A(em1);
    Thread t = new Thread(a);
    t.start();
    try {
      t.join();
    } catch (InterruptedException e) {
      fail("Thread is interrupted");
    }
    assertTrue(a.succeed);
    assertFalse(a.sameEntity);
  }

  class A implements Runnable {

    private EntityManager em1;

    private boolean       succeed;

    private boolean       sameEntity;

    public A(EntityManager em) {
      this.em1 = em;
    }

    @Override
    public void run() {
      RequestLifeCycle.begin(service);

      EntityManager em2 = service.getEntityManager();
      if (em2 != null && em2.isOpen()) {
        succeed = true;
      }
      sameEntity = em2 == em1;

      RequestLifeCycle.end();
    }
  }
}
