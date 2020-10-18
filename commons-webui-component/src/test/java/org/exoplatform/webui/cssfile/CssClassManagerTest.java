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

import junit.framework.TestCase;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.json.JSONArray;

public class CssClassManagerTest extends TestCase {
  private CssClassManager cssClassManager ;

  public CssClassManagerTest() throws Exception {
    cssClassManager = new CssClassManager(null);
    //
    InitParams params = makeInitParams();
    //
    CssClassPlugin cssClassPlugin = new CssClassPlugin(params);
    cssClassManager.registerCssClassPlugin(cssClassPlugin);
    cssClassManager.initCssClassIconFile();
  }
  
  public void testGetDataJson() throws Exception {
    String jsonData = cssClassManager.getClassIconJsonData();
    try {
      new JSONArray(jsonData).toString();
      assertTrue(true);
    } catch (Exception e) {
      assertTrue(e.toString(), false);
    }
  }
  
  public void testGetCSSClassByFileName() {
    String actual = cssClassManager.getCSSClassByFileName("", CssClassManager.ICON_SIZE.ICON_16);
    String expected = "uiIcon16x16FileDefault uiIcon16x16nt_file";
    assertEquals(expected, actual);
    
    actual = cssClassManager.getCSSClassByFileName("test.xxx", null);
    expected = "uiIcon16x16FileDefault uiIcon16x16nt_file";
    assertEquals(expected, actual);
    
    actual = cssClassManager.getCSSClassByFileName("xxx", null);
    expected = "uiIcon16x16FileDefault uiIcon16x16nt_file";
    assertEquals(expected, actual);

    actual = cssClassManager.getCSSClassByFileName("test.jpg", null);
    expected = "uiIcon16x16FileJpg uiIcon16x16nt_file";
    assertEquals(expected, actual);

    actual = cssClassManager.getCSSClassByFileName("jpg", null);
    expected = "uiIcon16x16FileJpg uiIcon16x16nt_file";
    assertEquals(expected, actual);

    actual = cssClassManager.getCSSClassByFileName("file.less", null);
    expected = "uiIcon16x16FileCss uiIcon16x16nt_file";
    assertEquals(expected, actual);

    actual = cssClassManager.getCSSClassByFileName("test.pdf", CssClassManager.ICON_SIZE.ICON_24);
    expected = "uiIcon24x24FilePDF uiIcon24x24nt_file";
    assertEquals(expected, actual);
  }

  public void testGetCSSClassByFileType() {
    String actual = cssClassManager.getCSSClassByFileType("", CssClassManager.ICON_SIZE.ICON_16);
    String expected = "uiIcon16x16FileDefault uiIcon16x16nt_file";
    assertEquals(expected, actual);
    
    actual = cssClassManager.getCSSClassByFileType("typexxx", null);
    expected = "uiIcon16x16FileDefault uiIcon16x16nt_file";
    assertEquals(expected, actual);
    
    actual = cssClassManager.getCSSClassByFileType("typemp3", null);
    expected = "uiIcon16x16FileAudio uiIcon16x16nt_file";
    assertEquals(expected, actual);
    
    actual = cssClassManager.getCSSClassByFileType("typempg", null);
    expected = "uiIcon16x16FileVideo uiIcon16x16nt_file";
    assertEquals(expected, actual);
    
    actual = cssClassManager.getCSSClassByFileType("typeavi", CssClassManager.ICON_SIZE.ICON_24);
    expected = "uiIcon24x24FileVideo uiIcon24x24nt_file";
    assertEquals(expected, actual);
  }
  
  private InitParams makeInitParams() {
    
    InitParams params = new InitParams();
    ObjectParameter objectParameter = makeObjectParameter("mp3", "FileAudio", "typemp3, typeaudio, typewav");
    params.put(objectParameter.getName(), objectParameter);

    objectParameter = makeObjectParameter("pdf", "FilePDF", "typepdf, typepdd");
    params.put(objectParameter.getName(), objectParameter);

    objectParameter = makeObjectParameter("mpg", "FileVideo", "typempg, typempeg, typeavi");
    params.put(objectParameter.getName(), objectParameter);

    objectParameter = makeObjectParameter("jpg", "FileJpg", "typejpg, typejpeg, typejpng");
    params.put(objectParameter.getName(), objectParameter);

    objectParameter = makeObjectParameter("css", "FileCss", "typecss, FileLess");
    params.put(objectParameter.getName(), objectParameter);

    return params;
  }
  
  private ObjectParameter makeObjectParameter(String type, String cssClass, String groupFileTypes) {
    CssClassIconFile cssClassIconFile = new CssClassIconFile();
    cssClassIconFile.setType(type);
    cssClassIconFile.setCssClass(cssClass);
    cssClassIconFile.setGroupFileTypes(groupFileTypes);
    
    ObjectParameter objectParameter = new ObjectParameter();
    objectParameter.setName(type);
    objectParameter.setObject(cssClassIconFile);
    return objectParameter;
  }
  

}
