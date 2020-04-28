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

/**
 * A class implementing this interface is executed will be started (execution of the method start())
 * during the startup of the platform but on one node only if running in cluster.
 * If the node running the cluster aware service is stopped during the execution of the service,
 * it will be started again on another cluster node, unless the isDone() method returns true.
 */
public interface StartableClusterAware {
    /**
     * Start service by the current node
     */
    void start();

    /**
     * Check if service is done
     * @return
     */
    boolean isDone();

    /**
     * Stop service by the current node
     */
    default void stop() {}
}
