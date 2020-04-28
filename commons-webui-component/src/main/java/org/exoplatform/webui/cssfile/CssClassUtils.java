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
package org.exoplatform.webui.cssfile;

import org.exoplatform.container.PortalContainer;

public class CssClassUtils {

  public static CssClassManager getCssClassManager() {
    return (CssClassManager) PortalContainer.getInstance().getComponentInstanceOfType(CssClassManager.class);
  }

  public static String getCSSClassByFileType(String fileType, CssClassManager.ICON_SIZE size) {
    return getCssClassManager().getCSSClassByFileType(fileType, size);
  }

  public static String getCSSClassByFileName(String fileName, CssClassManager.ICON_SIZE size) {
    return getCssClassManager().getCSSClassByFileName(fileName, size);
  }
  
  public static String getCSSClassByFileNameAndFileType(String fileName, String fileType, CssClassManager.ICON_SIZE size) {
    String cssClass = getCSSClassByFileName(fileName, size);
    if (cssClass.indexOf(CssClassIconFile.DEFAULT_CSS) >= 0) {
      cssClass = getCSSClassByFileType(fileType, size);
    }

    return cssClass;
  }

  public static String getFileExtension(String fileName) {
    if (fileName != null && fileName.trim().length() > 0) {
      return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    } else {
      return CssClassIconFile.DEFAULT_TYPE;
    }
  }
}
