/*
 * Copyright (C) 2016 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.commons.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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

  @Test
  public void testDailymotionURLAllowedInIFrame() throws Exception {
    String input = "<iframe allow=\"fullscreen\" frameborder=\"0\" src=\"https://www.dailymotion.com/video/x7zyezo?playlist=x6pibu\"></iframe>";
    String sanitized = HTMLSanitizer.sanitize(input);
    sanitized=sanitized.replaceAll("&#61;","=");
    assertEquals(input, sanitized);
  }

  @Test
  public void testYouTubeURLAllowedInIFrame() throws Exception {
    String input = "<iframe allow=\"fullscreen\" frameborder=\"0\" src=\"https://www.youtube.com/embed/RLY9uVbuk3Q?autohide=1&amp;controls=1&amp;showinfo=0\"></iframe>";
    String sanitized = HTMLSanitizer.sanitize(input);
    sanitized=sanitized.replaceAll("&#61;","=");
    assertEquals(input, sanitized);
  }

  @Test
  public void testVimeoURLAllowedInIFrame() throws Exception {
    String input = "<iframe allow=\"fullscreen\" frameborder=\"0\" src=\"https://player.vimeo.com/video/243244233\"></iframe>";
    String sanitized = HTMLSanitizer.sanitize(input);
    sanitized=sanitized.replaceAll("&#61;","=");
    assertEquals(input, sanitized);
  }

  @Test
  public void testNotAllowedURLInIFrame() throws Exception {
    String input = "<iframe allow=\"fullscreen\" frameborder=\"0\" src=\"https://www.udemy.com/course/java-the-complete-java-developer-course/\"></iframe>";
    String sanitized = HTMLSanitizer.sanitize(input);
    assertEquals("<iframe allow=\"fullscreen\" frameborder=\"0\" src=\"https://www.udemy.com/course/java-the-complete-java-developer-course/\"></iframe>", sanitized);
  }

  @Test
  public void testAllowTargetLinksSanitize() throws Exception {
    String input = "<a class=\"class\" href=\"url\" target=\"_blank\">link</a>";
    String sanitized = HTMLSanitizer.sanitize(input);
    assertEquals("<a class=\"class\" href=\"url\" target=\"_blank\" rel=\"nofollow noopener noreferrer\">link</a>", sanitized);
  }
  @Test
  public void testAllowedSpecialCharactersLinks(){
    String input = "https://www.economie.gouv.fr/entreprises/changement-janvier-2022?xtor=ES-29-[BIE_292_20220106]-20220106-[https://www.economie.gouv.fr/entreprises/changement-janvier-2022]";
    String sanitized = null;
    try {
      sanitized = HTMLSanitizer.sanitize(input);
    } catch (Exception e) {
      fail();
    }
    assertEquals("https://www.economie.gouv.fr/entreprises/changement-janvier-2022?xtor&#61;ES-29-[BIE_292_20220106]-20220106-[https://www.economie.gouv.fr/entreprises/changement-janvier-2022]", sanitized);
  }

  @Test
  public void testAllowPhoneLinks() throws Exception {
    String input = "<a class=\"class\" href=\"tel:+21612345678\" target=\"_self\">link</a>";
    String sanitized = HTMLSanitizer.sanitize(input);
    assertEquals("<a class=\"class\" href=\"tel:&#43;21612345678\" rel=\"nofollow\">link</a>", sanitized);
  }
}
