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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.portal.branding.MailDocumentShell;

import groovy.text.GStringTemplateEngine;

/**
 * The shared header of the notification mails, rendered as the mail channel
 * renders it: through the template engine.
 */
public class NotificationHeaderTemplateTest {

  private static final String TEMPLATES = "../commons-extension-webapp/src/main/webapp/WEB-INF/notification/templates/mail/";

  @Test
  public void theHeaderIsWrittenInTheBoundPaletteWithTheLogo() throws Exception {
    Map<String, String> palette = new HashMap<>(MailDocumentShell.LIGHT_DEFAULTS);
    palette.put(MailDocumentShell.BACKGROUND, "#14181B");
    palette.put(MailDocumentShell.SURFACE, "#1E2328");
    palette.put(MailDocumentShell.LINK, "#5FA9AC");
    palette.put(MailDocumentShell.BORDER, "#3A4148");
    palette.put(MailDocumentShell.LOGO_PATH, "/portal/rest/v1/platform/branding/darkLogo?v=9");
    palette.put(MailDocumentShell.COMPANY_NAME, "ACME");
    palette.put(MailPalette.MODE, "dark");
    String html = render(palette);
    assertTrue(html, html.contains("bgcolor=\"#1E2328\""));
    assertTrue(html, html.contains("class=\"mail-bg mail-link\""));
    assertTrue(html, html.contains("color:#5FA9AC"));
    assertTrue(html, html.contains("/portal/rest/v1/platform/branding/darkLogo?v=9"));
    assertTrue(html, html.contains("New activity"));
    assertFalse("no light literal left", html.contains("#efefef") || html.contains("#2f5e92") || html.contains("#ffffff"));
  }

  @Test
  public void aContextWithoutPaletteRendersInTheLightDefaults() throws Exception {
    String html = render(null);
    assertTrue(html, html.contains("bgcolor=\"#FFFFFF\""));
    assertFalse(html, html.contains("null"));
    assertFalse("no role class without a bound palette: " + html, html.contains("mail-"));
    assertFalse(html, MailPalette.isPaletteBound(html));
  }

  @Test
  public void aBoundPaletteMakesTheHeaderPaletteBound() throws Exception {
    String html = render(new HashMap<>(MailDocumentShell.LIGHT_DEFAULTS));
    assertTrue(html, html.contains("class=\"mail-surface mail-border\""));
    assertTrue(html, html.contains("class=\"mail-bg\""));
    assertTrue(html, MailPalette.isPaletteBound(html));
  }

  static String render(Map<String, String> palette) throws Exception {
    StringWriter writer = new StringWriter();
    TemplateContext ctx = new TemplateContext();
    ctx.put("_writer", writer);
    ctx.put("_templateContext", ctx);
    ctx.put("header_title", "New activity");
    if (palette != null) {
      ctx.put(MailPalette.CONTEXT_KEY, palette);
    }
    new GStringTemplateEngine().createTemplate(read("NotificationHeader.gtmpl")).make(ctx).writeTo(writer);
    return writer.toString();
  }

  private static String read(String name) throws Exception {
    return Files.readString(Paths.get(TEMPLATES + name), StandardCharsets.UTF_8);
  }

}
