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
package org.exoplatform.commons.testing;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import junit.framework.TestSuite;

import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.KernelBootstrap;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by The eXo Platform SAS
 * Author : thanh_vucong
 *          thanh_vucong@exoplatform.com
 * Nov 12, 2012  
 */
@RunWith(Suite.class)
public abstract class BaseExoContainerTestSuite {

  /** . */
  private static KernelBootstrap bootstrap;

  /** . */
  private static final Map<Class<?>, AtomicLong> counters = new HashMap<>();
  
  private static Class<?> testCaseClazz = null;
  
  @BeforeClass
  public static void setUp() throws Exception {
    beforeSetup();
  }

  @AfterClass
  public static void tearDown() {
    afterTearDown();
  }
  
  protected static synchronized void initConfiguration(Class<?> clazz) {
    ConfigTestCase config = clazz.getAnnotation(ConfigTestCase.class);
    testCaseClazz = (config != null) ? config.value() : AbstractKernelTest.class;
  }
  
  protected static synchronized void beforeSetup() throws Exception {
    Class<?> key = testCaseClazz;

    //
    if (!counters.containsKey(testCaseClazz))
    {
       counters.put(key, new AtomicLong(new TestSuite(testCaseClazz).testCount()));

       //
       bootstrap = new KernelBootstrap(Thread.currentThread().getContextClassLoader());

       // Configure ourselves
       bootstrap.addConfiguration(testCaseClazz);

       //
       bootstrap.boot();
       BaseExoTestCase.ownBootstrap = bootstrap;
    }
  }
  
  protected static synchronized void afterTearDown() {
    Class<?> key = testCaseClazz;

    //
    if (counters.get(key).decrementAndGet() == 0)
    {
       bootstrap.dispose();

       //
       bootstrap = null;
       
       BaseExoTestCase.ownBootstrap = null;
    }
  }

}

