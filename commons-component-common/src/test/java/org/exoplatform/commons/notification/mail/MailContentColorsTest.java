/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.commons.notification.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class MailContentColorsTest {

  private static final String CONTENT = "<p style=\"color: #000000; font-weight: bold\">black</p>"
      + "<p style=\"color:rgb(255, 255, 255)\">white</p>"
      + "<p style=\"color: #ff0000\">red</p>"
      + "<div style=\"background-color:#000;color:#fff\">white on black</div>"
      + "<div bgcolor=\"#ffffff\"><span style=\"color:black\">inside a white cell</span></div>";

  @Test
  public void lightChangesNothing() {
    assertSame(CONTENT, MailContentColors.apply(CONTENT, "light"));
  }

  @Test
  public void automaticKeepsTheColourAndAddsTheTextRole() {
    String html = MailContentColors.apply(CONTENT, "auto");
    assertEquals("<p style=\"color: #000000; font-weight: bold\" class=\"mail-text\">black</p>"
        + "<p style=\"color:rgb(255, 255, 255)\" class=\"mail-text\">white</p>"
        + "<p style=\"color: #ff0000\">red</p>"
        + "<div style=\"background-color:#000;color:#fff\">white on black</div>"
        + "<div bgcolor=\"#ffffff\"><span style=\"color:black\">inside a white cell</span></div>", html);
  }

  @Test
  public void darkDropsTheBlackAndWhiteColours() {
    String html = MailContentColors.apply(CONTENT, "DARK");
    assertEquals("<p style=\"font-weight: bold\">black</p>"
        + "<p>white</p>"
        + "<p style=\"color: #ff0000\">red</p>"
        + "<div style=\"background-color:#000;color:#fff\">white on black</div>"
        + "<div bgcolor=\"#ffffff\"><span style=\"color:black\">inside a white cell</span></div>", html);
  }

  @Test
  public void aContentWithoutColourIsLeftAsWritten() {
    String html = "<p>plain <b>text</b></p>";
    assertSame(html, MailContentColors.apply(html, "dark"));
  }

}
