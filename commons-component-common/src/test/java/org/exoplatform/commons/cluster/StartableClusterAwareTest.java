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
package org.exoplatform.commons.cluster;

import org.exoplatform.commons.testing.BaseCommonsTestCase;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;

@ConfiguredBy({
    @ConfigurationUnit(scope = ContainerScope.ROOT, path = "conf/configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.commons.component.core-dependencies-configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.commons.component.core-configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.commons.component.core-local-test-configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/test-cluster-configuration.xml") })
public class StartableClusterAwareTest extends BaseCommonsTestCase {

  public static int nbRunningA = 0;

  public static int nbRunningB = 0;

  public static int nbRunningC = 0;

  public void testRunningService() {
    // Check service is started
    assertEquals(1, nbRunningA);
    assertEquals(1, nbRunningB);
    assertEquals(0, nbRunningC);
    try {
      Thread.sleep(200);
    } catch (InterruptedException e) {
    }
    // Check service is not executed again
    assertEquals(1, nbRunningA);
    assertEquals(1, nbRunningB);
    assertEquals(0, nbRunningC);
  }

  public static class ClassA implements StartableClusterAware {
    private boolean isDone = false;

    @Override
    public void start() {
      nbRunningA++;
      isDone = true;
    }

    @Override
    public boolean isDone() {
      return isDone;
    }
  }

  public static class ClassB implements StartableClusterAware {
    private boolean isDone = false;

    @Override
    public void start() {
      nbRunningB++;
      isDone = true;
    }

    @Override
    public boolean isDone() {
      return isDone;
    }
  }

  public static class ClassC implements StartableClusterAware {

    @Override
    public void start() {
      nbRunningB++;
    }

    @Override
    public boolean isDone() {
      return true;
    }
  }
}
