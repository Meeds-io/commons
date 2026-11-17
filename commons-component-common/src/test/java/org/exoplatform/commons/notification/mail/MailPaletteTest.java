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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.portal.branding.BrandingService;
import org.exoplatform.portal.branding.MailDocumentShell;

public class MailPaletteTest {

  private static final Map<String, String> LIGHT = Map.of(MailDocumentShell.SURFACE,
                                                          "#FFFFFF",
                                                          MailDocumentShell.TEXT,
                                                          "#20282C",
                                                          MailDocumentShell.LOGO_PATH,
                                                          "/portal/rest/v1/platform/branding/logo?v=7",
                                                          MailDocumentShell.LOGO_IS_SVG,
                                                          "false",
                                                          MailDocumentShell.COMPANY_NAME,
                                                          "ACME <&>");

  private static final Map<String, String> DARK  = Map.of(MailDocumentShell.SURFACE,
                                                          "#1E2328",
                                                          MailDocumentShell.TEXT,
                                                          "#E6E9EB",
                                                          MailDocumentShell.LOGO_PATH,
                                                          "/portal/rest/v1/platform/branding/darkLogo?v=9",
                                                          MailDocumentShell.LOGO_IS_SVG,
                                                          "true",
                                                          MailDocumentShell.COMPANY_NAME,
                                                          "ACME <&>");

  @Test
  public void aDarkRecipientGetsTheDarkPaletteInline() {
    Map<String, String> palette = MailPalette.forRecipient(branding("dark"), "mary");
    assertEquals("#1E2328", palette.get(MailDocumentShell.SURFACE));
    assertEquals("dark", palette.get(MailPalette.MODE));
    assertEquals("a key the theme does not give keeps its default", "#F0F0F0", palette.get(MailDocumentShell.BACKGROUND));
  }

  @Test
  public void anAutomaticRecipientGetsTheLightPaletteInlineAndBothLogos() {
    Map<String, String> palette = MailPalette.forRecipient(branding("AUTO"), "mary");
    assertEquals("#FFFFFF", palette.get(MailDocumentShell.SURFACE));
    assertEquals("auto", palette.get(MailPalette.MODE));
    String logo = MailPalette.logoHtml(palette, "https://acme.io");
    assertTrue(logo, logo.contains("<img class=\"mail-logo-light\" src=\"https://acme.io/portal/rest/v1/platform/branding/logo?v=7\""));
    assertTrue("the dark version is an SVG: the company name, hidden until the dark media query shows it",
               logo.contains("<span class=\"mail-logo-dark\" style=\"display:none;mso-hide:all;"));
    assertTrue("the company name is escaped", logo.contains("ACME &lt;&amp;&gt;"));
    assertFalse(logo.contains("ACME <&>"));
  }

  @Test
  public void anExplicitModeShowsOneLogoAndAnSvgOneAsTheCompanyName() {
    String light = MailPalette.logoHtml(MailPalette.forRecipient(branding("light"), "mary"), "https://acme.io/");
    assertEquals("<img src=\"https://acme.io/portal/rest/v1/platform/branding/logo?v=7\" alt=\"ACME &lt;&amp;&gt;\" height=\"32\" style=\"height:32px;border:0;\">",
                 light);
    String dark = MailPalette.logoHtml(MailPalette.forRecipient(branding("dark"), "mary"), "https://acme.io");
    assertEquals("<span style=\"font-weight:bold;font-size:18px;\">ACME &lt;&amp;&gt;</span>", dark);
  }

  @Test
  public void aFailingPaletteNeverStopsTheMail() {
    BrandingService brandingService = mock(BrandingService.class);
    when(brandingService.getDisplayMode("mary")).thenThrow(new IllegalStateException("down"));
    Map<String, String> palette = MailPalette.forRecipient(brandingService, "mary");
    assertEquals(MailDocumentShell.LIGHT_DEFAULTS.get(MailDocumentShell.SURFACE), palette.get(MailDocumentShell.SURFACE));
    assertEquals("light", palette.get(MailPalette.MODE));
    String document = MailPalette.wrap(brandingService, "<table></table>", "mary");
    assertTrue(document, document.contains("content=\"light\"") && document.contains("<table></table>"));
  }

  @Test
  public void aContextWithoutPaletteGetsTheBuiltInLightOne() {
    Map<String, String> palette = MailPalette.of(new TemplateContext());
    assertEquals("#FFFFFF", palette.get(MailDocumentShell.SURFACE));
    assertEquals("light", palette.get(MailPalette.MODE));
    TemplateContext ctx = new TemplateContext();
    ctx.put(MailPalette.CONTEXT_KEY, Map.of(MailDocumentShell.SURFACE, "#000000"));
    assertEquals("#000000", MailPalette.of(ctx).get(MailDocumentShell.SURFACE));
  }

  @Test
  public void theDarkBlockIsWrittenForAnAutomaticRecipientOnly() {
    String document = MailPalette.wrap(branding("auto"), "<table></table>", "mary");
    assertTrue(document, document.contains("@media (prefers-color-scheme: dark)"));
    assertTrue(document, document.contains(".mail-surface{background-color:#1E2328 !important;}"));
    BrandingService dark = branding("dark");
    document = MailPalette.wrap(dark, "<table></table>", "mary");
    assertFalse(document, document.contains("@media"));
    assertTrue(document, document.contains("content=\"dark\""));
    verify(dark, never()).getMailPalette(true);
  }

  @Test
  public void theFooterGoesIntoTheDocumentOnce() {
    MessageInfo message = new MessageInfo().body("<table>body</table>").footer("<tr>footer</tr>");
    MailPalette.wrapMessage(branding("light"), message, "mary");
    String sent = message.makeEmailNotification().getBody();
    assertTrue(sent, sent.startsWith("<!DOCTYPE html>"));
    assertTrue(sent, sent.endsWith("</body></html>"));
    assertTrue(sent, sent.contains("<table>body</table><tr>footer</tr>"));
  }

  @Test
  public void aBodyWithLiteralColoursStaysLightWhateverTheRecipientMode() {
    for (String mode : new String[] { "dark", "auto" }) {
      MessageInfo message = new MessageInfo().body("<table style=\"color:#333\">literal</table>").footer("<tr>footer</tr>");
      MailPalette.wrapMessage(branding(mode), message, "mary");
      String sent = message.makeEmailNotification().getBody();
      assertTrue(mode, sent.contains("content=\"light\""));
      assertFalse(mode, sent.contains("content=\"dark\""));
      assertFalse(mode, sent.contains("@media"));
    }
  }

  @Test
  public void aPaletteBoundBodyIsWrappedInTheRecipientMode() {
    String body = "<table class=\"mail-bg\"><tr><td class=\"mail-surface mail-text\">bound</td></tr></table>";
    MessageInfo dark = new MessageInfo().body(body);
    MailPalette.wrapMessage(branding("dark"), dark, "mary");
    assertTrue(dark.makeEmailNotification().getBody().contains("content=\"dark\""));
    MessageInfo auto = new MessageInfo().body(body);
    MailPalette.wrapMessage(branding("auto"), auto, "mary");
    assertTrue(auto.makeEmailNotification().getBody().contains("@media (prefers-color-scheme: dark)"));
  }

  @Test
  public void theRecipientWrapGatesOnThePaletteRoleClasses() {
    String unbound = MailPalette.wrapForRecipient(branding("dark"), "<table style=\"color:#333\"></table>", "mary");
    assertTrue(unbound, unbound.contains("content=\"light\"") && !unbound.contains("content=\"dark\""));
    String bound = MailPalette.wrapForRecipient(branding("dark"), "<td class=\"mail-text\"></td>", "mary");
    assertTrue(bound, bound.contains("content=\"dark\""));
  }

  @Test
  public void aDarkRecipientGetsALiteralBodyUnderTheUnboundSharedHeaderInLight() throws Exception {
    String body = NotificationHeaderTemplateTest.render(null) + "<table><tr><td style=\"color:#333\">Task assigned</td></tr></table>";
    String document = MailPalette.wrapForRecipient(branding("dark"), body, "mary");
    assertTrue(document, document.contains("content=\"light\"") && !document.contains("content=\"dark\""));
  }

  @Test
  public void anAutomaticMailWithoutLogoKeepsTheCompanyNameVisibleOnADarkDevice() {
    Map<String, String> palette = new HashMap<>();
    palette.put(MailPalette.MODE, "auto");
    palette.put(MailDocumentShell.COMPANY_NAME, "ACME");
    String logo = MailPalette.logoHtml(palette, "https://acme.io");
    assertEquals("<span style=\"font-weight:bold;font-size:18px;\">ACME</span>", logo);
    assertFalse("the dark block hides every mail-logo-light block", logo.contains("mail-logo-light"));
    // a light logo only: still no dark sibling, so no class
    palette.put(MailDocumentShell.LOGO_PATH, "/portal/rest/v1/platform/branding/logo?v=7");
    palette.put(MailDocumentShell.LOGO_IS_SVG, "false");
    logo = MailPalette.logoHtml(palette, "https://acme.io");
    assertFalse(logo, logo.contains("mail-logo-light") || logo.contains("mail-logo-dark"));
    // a dark logo: both blocks, the light one tagged
    palette.put(MailPalette.DARK_LOGO_PATH, "/portal/rest/v1/platform/branding/darkLogo?v=9");
    palette.put(MailPalette.DARK_LOGO_IS_SVG, "false");
    logo = MailPalette.logoHtml(palette, "https://acme.io");
    assertTrue(logo, logo.contains("class=\"mail-logo-light\"") && logo.contains("class=\"mail-logo-dark\""));
  }

  @Test
  public void aModeAlreadyReadWrapsWithoutReadingItAgain() {
    BrandingService dark = branding("dark");
    String document = MailPalette.wrapInMode(dark, "<table></table>", "dark");
    assertTrue(document, document.contains("content=\"dark\""));
    verify(dark, never()).getDisplayMode("mary");
  }

  private BrandingService branding(String mode) {
    BrandingService brandingService = mock(BrandingService.class);
    when(brandingService.getDisplayMode("mary")).thenReturn(mode);
    lenient().when(brandingService.getMailPalette(false)).thenReturn(new HashMap<>(LIGHT));
    lenient().when(brandingService.getMailPalette(true)).thenReturn(new HashMap<>(DARK));
    return brandingService;
  }

}
