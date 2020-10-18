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
package org.exoplatform.commons.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Created by kmenzli on 16/06/16.
 */
public class HTMLSanitizerTest {

  @Test
  public void testEmpty() throws Exception {
    assertEquals("", HTMLSanitizer.sanitize(""));
    assertEquals("", HTMLSanitizer.sanitize(null));
  }

  @Test
  public void testEncodeImg() throws Exception {
    String input1 = "<img alt='crying' height='23' src='http://localhost:8080/CommonsResources/ckeditor/plugins/smiley/images/cry_smile.png' title='crying' width='23' onerror='alert('XSS')' onmousemove='alert('XSS1')'/>";
    assertEquals("<img alt=\"crying\" height=\"23\" src=\"http://localhost:8080/CommonsResources/ckeditor/plugins/smiley/images/cry_smile.png\" title=\"crying\" width=\"23\" />",
            HTMLSanitizer.sanitize(input1));
  }

  @Test
  public void testSanitizeRemovesScripts() throws Exception {
    String input = "<p>Hello World</p>" + "<script language=\"text/javascript\">alert(\"bad\");</script>";
    String sanitized = HTMLSanitizer.sanitize(input);
    assertEquals("<p>Hello World</p>", sanitized);
  }

  @Test
  public void testSanitizeRemovesOnclick() throws Exception {
    String input = "<p onclick=\"alert(\"bad\");\">Hello World</p>";
    String sanitized = HTMLSanitizer.sanitize(input);
    assertEquals("<p>Hello World</p>", sanitized);
  }

  @Test
  public void testTextAllowedInLinks() throws Exception {
    String input = "<a href=\"../good.html\">click here</a>";
    String sanitized = HTMLSanitizer.sanitize(input);
    assertEquals("<a href=\"../good.html\" rel=\"nofollow\">click here</a>", sanitized);
  }

  @Test
  public void testStarAllowedInImageLinks() throws Exception {
    String input = "https://cdn-images-1.medium.com/max/800/0*ssnGrTXEfHtQQ-tJ";
    String sanitized = HTMLSanitizer.sanitize(input);
    assertEquals(input, sanitized);
  }
}
