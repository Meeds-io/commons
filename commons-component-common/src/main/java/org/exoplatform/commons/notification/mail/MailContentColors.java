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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * User content printed in a mail (an activity, a comment) keeps the colours
 * its author chose, except black and white text, which would vanish on the
 * other version's background: in Automatic such an element gets the text role
 * class, so only the dark media query overrides it; in Dark its colour is
 * dropped, so it takes the palette's text colour; in Light nothing changes.
 * An element that sets its own background, or lies inside one within the
 * content, keeps its colours as written.
 */
public final class MailContentColors {

  /** The role class the dark media query gives the dark text colour */
  public static final String  TEXT_ROLE_CLASS = "mail-text";

  private static final Pattern BLACK          = Pattern.compile("#000(000)?|black|rgba?\\(\\s*0\\s*,\\s*0\\s*,\\s*0\\s*(,\\s*1(\\.0+)?\\s*)?\\)");

  private static final Pattern WHITE          =
                                     Pattern.compile("#fff(fff)?|white|rgba?\\(\\s*255\\s*,\\s*255\\s*,\\s*255\\s*(,\\s*1(\\.0+)?\\s*)?\\)");

  private MailContentColors() {
    // utility
  }

  /**
   * @param html the user content, an HTML fragment
   * @param mode the recipient's display mode: auto, light or dark
   * @return the content for that mode
   */
  public static String apply(String html, String mode) {
    String effectiveMode = mode == null ? "light" : mode.toLowerCase(Locale.ROOT);
    if (StringUtils.isBlank(html) || "light".equals(effectiveMode) || html.indexOf("color") < 0) {
      return html;
    }
    boolean dark = "dark".equals(effectiveMode);
    Document document = Jsoup.parseBodyFragment(html);
    document.outputSettings().prettyPrint(false);
    boolean changed = false;
    for (Element element : document.body().select("[style]")) {
      List<String> declarations = declarations(element.attr("style"));
      int colorIndex = blackOrWhiteColor(declarations);
      if (colorIndex < 0 || hasBackground(element)) {
        continue;
      }
      if (dark) {
        declarations.remove(colorIndex);
        String style = String.join(";", declarations);
        if (style.isBlank()) {
          element.removeAttr("style");
        } else {
          element.attr("style", style);
        }
      } else {
        element.addClass(TEXT_ROLE_CLASS);
      }
      changed = true;
    }
    return changed ? document.body().html() : html;
  }

  private static List<String> declarations(String style) {
    List<String> declarations = new ArrayList<>();
    for (String declaration : style.split(";")) {
      if (StringUtils.isNotBlank(declaration)) {
        declarations.add(declaration.trim());
      }
    }
    return declarations;
  }

  private static int blackOrWhiteColor(List<String> declarations) {
    for (int i = 0; i < declarations.size(); i++) {
      String declaration = declarations.get(i);
      int colon = declaration.indexOf(':');
      if (colon > 0 && "color".equalsIgnoreCase(declaration.substring(0, colon).trim())) {
        String value = StringUtils.removeEndIgnoreCase(declaration.substring(colon + 1).trim(), "!important")
                                  .trim()
                                  .toLowerCase(Locale.ROOT);
        if (BLACK.matcher(value).matches() || WHITE.matcher(value).matches()) {
          return i;
        }
      }
    }
    return -1;
  }

  /** The element or one of its ancestors within the content sets a background. */
  private static boolean hasBackground(Element element) {
    for (Element current = element; current != null && !"body".equals(current.tagName()); current = current.parent()) {
      if (current.hasAttr("bgcolor")) {
        return true;
      }
      for (String declaration : declarations(current.attr("style"))) {
        int colon = declaration.indexOf(':');
        if (colon > 0 && declaration.substring(0, colon).trim().toLowerCase(Locale.ROOT).startsWith("background")) {
          return true;
        }
      }
    }
    return false;
  }

}
