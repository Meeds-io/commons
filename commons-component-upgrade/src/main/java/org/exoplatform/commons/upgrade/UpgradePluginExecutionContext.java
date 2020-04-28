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
package org.exoplatform.commons.upgrade;

public class UpgradePluginExecutionContext {
  String version;

  int    executionCount;

  public UpgradePluginExecutionContext(String version, int executionCount) {
    this.version = version;
    this.executionCount = executionCount;
  }

  public UpgradePluginExecutionContext(String versionAndExecutionCount) {
    if (versionAndExecutionCount.indexOf(";") < 0) {
      this.version = versionAndExecutionCount;
    } else {
      String[] versionAndCountArray = versionAndExecutionCount.split(";");
      this.version = versionAndCountArray[0];
      this.executionCount = Integer.parseInt(versionAndCountArray[1]);
    }
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public int getExecutionCount() {
    return executionCount;
  }

  public void setExecutionCount(int executionCount) {
    this.executionCount = executionCount;
  }

  @Override
  public String toString() {
    return this.version + ";" + this.executionCount;
  }
}