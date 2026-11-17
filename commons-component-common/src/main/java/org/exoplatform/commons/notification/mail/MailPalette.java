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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.portal.branding.BrandingService;
import org.exoplatform.portal.branding.MailDocumentShell;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * The colours a mail is written with, for its recipient: the theme in use's
 * palette of the recipient's display mode (the light one inline in
 * Automatic, the dark one then coming from the document shell's media
 * query), its logos and the company name. A template reads it from its
 * context ({@link #of(Map)}), which the builder that knows the recipient
 * fills ({@link #bind(TemplateContext, String)}); a context without it gets
 * the built-in light palette.
 */
public final class MailPalette {

  /** The template context key holding the palette */
  public static final String CONTEXT_KEY      = "MAIL_PALETTE";

  /** The recipient's mode: auto, light or dark */
  public static final String MODE             = "mode";

  /** The dark logo's path, in Automatic only */
  public static final String DARK_LOGO_PATH   = "darkLogoPath";

  /** Whether the dark logo is an SVG, in Automatic only */
  public static final String DARK_LOGO_IS_SVG = "darkLogoIsSvg";

  private static final Log   LOG              = ExoLogger.getLogger(MailPalette.class);

  private static final Pattern PALETTE_CLASS  =
                                             Pattern.compile("class=\"[^\"]*\\bmail-(bg|surface|text|secondary-text|link|border)\\b");

  private MailPalette() {
    // utility
  }

  /**
   * @param ctx a template context filled with
   *          {@link #bind(TemplateContext, String)}, or any other
   * @return the bound palette, or the built-in light one
   */
  @SuppressWarnings("unchecked")
  public static Map<String, String> of(Map<String, ?> ctx) {
    Object palette = ctx == null ? null : ctx.get(CONTEXT_KEY);
    return palette instanceof Map ? (Map<String, String>) palette : lightDefaults();
  }

  /**
   * @param ctx a template context
   * @return true when a palette was bound to it with
   *         {@link #bind(TemplateContext, String)}
   */
  public static boolean isBound(Map<String, ?> ctx) {
    return ctx != null && ctx.get(CONTEXT_KEY) instanceof Map;
  }

  /**
   * Writes the class attribute of a template element coloured with the
   * palette's role classes, only when a palette was bound to the context: a
   * mail without one is written in literal light colours and must not be
   * taken for a palette-bound body.
   *
   * @param ctx a template context
   * @param roleClasses the role classes, space separated
   * @return the class attribute with a leading space, or an empty string
   */
  public static String roleClassAttribute(Map<String, ?> ctx, String roleClasses) {
    return isBound(ctx) ? " class=\"" + roleClasses + "\"" : "";
  }

  /**
   * Binds the palette of a recipient into a template context.
   *
   * @param ctx the template context of the recipient's mail
   * @param username the recipient
   */
  public static void bind(TemplateContext ctx, String username) {
    ctx.put(CONTEXT_KEY, forRecipient(username));
  }

  /**
   * @param username the recipient
   * @return the palette the recipient's mail is written with
   */
  public static Map<String, String> forRecipient(String username) {
    return forRecipient(brandingService(), username);
  }

  /**
   * @param brandingService the branding service, null for the built-in light
   *          palette
   * @param username the recipient
   * @return the palette the recipient's mail is written with
   */
  public static Map<String, String> forRecipient(BrandingService brandingService, String username) {
    Map<String, String> palette = lightDefaults();
    if (brandingService == null) {
      return palette;
    }
    try {
      String mode = mode(brandingService, username);
      putAll(palette, brandingService.getMailPalette("dark".equals(mode)));
      palette.put(MODE, mode);
      if ("auto".equals(mode)) {
        Map<String, String> dark = brandingService.getMailPalette(true);
        if (dark != null) {
          putIfNotBlank(palette, DARK_LOGO_PATH, dark.get(MailDocumentShell.LOGO_PATH));
          putIfNotBlank(palette, DARK_LOGO_IS_SVG, dark.get(MailDocumentShell.LOGO_IS_SVG));
        }
      }
    } catch (RuntimeException e) {
      // one recipient's palette never stops the mail: it is written in the built-in light colours
      LOG.warn("The mail palette of {} can't be read, the light defaults are used", username, e);
      return lightDefaults();
    }
    return palette;
  }

  /**
   * Wraps a mail's HTML body in the document shell of its recipient's mode; a
   * body with literal colours, none of the palette's role classes, is wrapped
   * in light.
   *
   * @param html the mail's HTML body, footer included
   * @param username the recipient
   * @return the mail document
   */
  public static String wrap(String html, String username) {
    return wrapForRecipient(brandingService(), html, username);
  }

  static String wrapForRecipient(BrandingService brandingService, String html, String username) {
    // a body with literal colours stays light: the dark mode would only darken the document around it
    return isPaletteBound(html) ? wrap(brandingService, html, username) : MailDocumentShell.wrap(html, "light", null);
  }

  /**
   * Wraps a mail's HTML body in the document shell of a display mode already
   * read for the recipient, typically the one of the palette the body was
   * written with.
   *
   * @param html the mail's HTML body, footer included
   * @param mode the display mode: auto, light or dark
   * @return the mail document
   */
  public static String wrapInMode(String html, String mode) {
    return wrapInMode(brandingService(), html, mode);
  }

  static String wrapInMode(BrandingService brandingService, String html, String mode) {
    String documentMode = "light".equals(mode) || "dark".equals(mode) ? mode : "auto";
    try {
      return MailDocumentShell.wrap(html,
                                    documentMode,
                                    "auto".equals(documentMode) && brandingService != null ? brandingService.getMailPalette(true) : null);
    } catch (RuntimeException e) {
      LOG.warn("The mail can't be wrapped in its display mode, it is sent in light", e);
      return MailDocumentShell.wrap(html, "light", null);
    }
  }

  /**
   * @param html a mail's HTML body
   * @return true when the body is written with the palette's role classes
   *         (mail-bg, mail-surface, mail-text...), so that the document shell
   *         can recolour it; a body with literal colours is not
   */
  public static boolean isPaletteBound(String html) {
    return html != null && PALETTE_CLASS.matcher(html).find();
  }

  /**
   * Wraps a notification message, body and footer, in one document of its
   * recipient's display mode: the mail channel joins them when the message is
   * sent, so the footer is moved into the document first.
   *
   * @param message the built message
   * @param username the recipient
   */
  public static void wrapMessage(MessageInfo message, String username) {
    wrapMessage(brandingService(), message, username);
  }

  static void wrapMessage(BrandingService brandingService, MessageInfo message, String username) {
    String html = StringUtils.defaultString(message.getBody()) + StringUtils.defaultString(message.getFooter());
    message.body(wrapForRecipient(brandingService, html, username)).footer(null);
  }

  static String wrap(BrandingService brandingService, String html, String username) {
    if (brandingService == null) {
      return MailDocumentShell.wrap(html, "light", null);
    }
    try {
      String mode = mode(brandingService, username);
      return MailDocumentShell.wrap(html, mode, "auto".equals(mode) ? brandingService.getMailPalette(true) : null);
    } catch (RuntimeException e) {
      LOG.warn("The mail of {} can't be wrapped in its display mode, it is sent in light", username, e);
      return MailDocumentShell.wrap(html, "light", null);
    }
  }

  /**
   * The logo block of a mail header: the logo of the palette's version, the
   * company name instead of an SVG logo (most mail clients show no SVG), and
   * in Automatic both versions, the dark one shown by the dark media query.
   *
   * @param palette a palette from {@link #of(Map)}
   * @return the HTML block, empty when there is neither a logo nor a name
   */
  public static String logoHtml(Map<String, String> palette) {
    return logoHtml(palette, CommonsUtils.getCurrentDomain());
  }

  static String logoHtml(Map<String, String> palette, String domain) {
    String name = StringUtils.defaultString(palette.get(MailDocumentShell.COMPANY_NAME));
    // the light block is tagged only when a dark sibling is written: the dark media query hides every tagged block
    boolean withDark = "auto".equals(palette.get(MODE)) && StringUtils.isNotBlank(palette.get(DARK_LOGO_PATH));
    String light = logo(palette.get(MailDocumentShell.LOGO_PATH),
                        Boolean.parseBoolean(palette.get(MailDocumentShell.LOGO_IS_SVG)),
                        name,
                        domain,
                        withDark ? "mail-logo-light" : null,
                        null);
    if (!withDark) {
      return light;
    }
    // hidden inline, so a client that ignores the media query shows the light logo only
    return light + logo(palette.get(DARK_LOGO_PATH),
                        Boolean.parseBoolean(palette.get(DARK_LOGO_IS_SVG)),
                        name,
                        domain,
                        "mail-logo-dark",
                        "display:none;mso-hide:all;");
  }

  private static String logo(String path, boolean svg, String name, String domain, String cssClass, String hiddenStyle) {
    String classAttribute = cssClass == null ? "" : " class=\"" + cssClass + "\"";
    String style = StringUtils.defaultString(hiddenStyle);
    if (StringUtils.isBlank(path) || svg) {
      return StringUtils.isBlank(name) ? "" :
                                       "<span" + classAttribute + " style=\"" + style
                                           + "font-weight:bold;font-size:18px;\">" + StringEscapeUtils.escapeHtml4(name)
                                           + "</span>";
    }
    String url = path.startsWith("/") ? StringUtils.removeEnd(domain, "/") + path : path;
    return "<img" + classAttribute + " src=\"" + StringEscapeUtils.escapeHtml4(url) + "\" alt=\""
        + StringEscapeUtils.escapeHtml4(name) + "\" height=\"32\" style=\"" + style + "height:32px;border:0;\">";
  }

  private static String mode(BrandingService brandingService, String username) {
    String mode = brandingService.getDisplayMode(username);
    mode = mode == null ? "auto" : mode.toLowerCase(Locale.ROOT);
    return "light".equals(mode) || "dark".equals(mode) ? mode : "auto";
  }

  private static Map<String, String> lightDefaults() {
    Map<String, String> palette = new HashMap<>(MailDocumentShell.LIGHT_DEFAULTS);
    palette.put(MODE, "light");
    return palette;
  }

  private static void putAll(Map<String, String> palette, Map<String, String> values) {
    if (values != null) {
      values.forEach((key, value) -> putIfNotBlank(palette, key, value));
    }
  }

  private static void putIfNotBlank(Map<String, String> map, String key, String value) {
    if (StringUtils.isNotBlank(value)) {
      map.put(key, value);
    }
  }

  private static BrandingService brandingService() {
    return CommonsUtils.getService(BrandingService.class);
  }

}
