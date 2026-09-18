/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
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
package org.exoplatform.commons.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.owasp.html.AttributePolicy;
import org.owasp.html.CssSchema;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.HtmlSanitizer;
import org.owasp.html.HtmlStreamEventReceiver;
import org.owasp.html.HtmlStreamRenderer;

import com.google.javascript.jscomp.jarjar.com.google.common.collect.ImmutableMap;
import com.google.javascript.jscomp.jarjar.com.google.common.collect.ImmutableSet;

/**
 * Prevent XSS/XEE attacks by encoding user HTML inputs. This class will be used
 * to encode data in in presentation layer.
 *
 */

abstract public class HTMLSanitizer {

  // Some common regular expression definitions.

  // The 16 colors defined by the HTML Spec (also used by the CSS Spec)
  private static final Pattern                                                COLOR_NAME                = Pattern.compile("(?:aqua|black|blue|fuchsia|gray|grey|green|lime|maroon|navy|olive|purple"
                                                                                                           + "|red|silver|teal|white|yellow)");

  // HTML/CSS Spec allows 3 or 6 digit hex to specify color
  private static final Pattern                                                COLOR_CODE               = Pattern.compile("(?:#(?:[0-9a-fA-F]{3}(?:[0-9a-fA-F]{3})?))");

  private static final Pattern                                                NUMBER_OR_PERCENT        = Pattern.compile("[0-9]+%?");

  private static final Pattern                                                HTML_ID                  = Pattern.compile("[a-zA-Z0-9\\:\\-_\\.]+");

  // force non-empty with a '+' at the end instead of '*'
  private static final Pattern                                                HTML_TITLE               = Pattern.compile("[\\p{L}\\p{N}\\s\\-_',:\\[\\]!\\./\\\\\\(\\)&]*");

  private static final Pattern                                                HTML_CLASS               = Pattern.compile("[a-zA-Z0-9\\s,\\-_]+");

  // URLs are accepted decoded as well as percent-encoded: some browsers (Safari) copy them
  // with raw spaces, accents and typographic punctuation (quotes \p{Pi}\p{Pf}, dashes \p{Pd})
  private static final Pattern                                                ONSITE_URL               = Pattern.compile("(?:[\\p{L}\\p{N} \\\\\\.\\#@\\$%\\+&;\\-_~,\\?=/!:'\\p{Pi}\\p{Pf}\\p{Pd}]+|\\#(\\w)+)");

  private static final Pattern                                                OFFSITE_URL              = Pattern.compile("\\s*(?:(?:ht|f)tps?:\\/\\/|mailto:)[\\p{L}\\p{N}][\\p{L}\\p{N} \\p{Zs}\\.\\[\\]\\#@\\$%\\+&;:\\-_~,\\?=\\/!\\(\\)\\*'\\p{Pi}\\p{Pf}\\p{Pd}]*+\\s*");

  private static final Pattern                                                NUMBER                   = Pattern.compile("[+-]?(?:(?:[0-9]+(?:\\.[0-9]*)?)|\\.[0-9]+)");

  private static final Pattern                                                NAME                     = Pattern.compile("[a-zA-Z0-9\\-_\\$]+");

  private static final Pattern                                                ALIGN                    = Pattern.compile("(?i)center|left|right|justify|char");

  private static final Pattern                                                VALIGN                   = Pattern.compile("(?i)baseline|bottom|middle|top");

  private static final Predicate<String>                                      COLOR_NAME_OR_COLOR_CODE = matchesEither(COLOR_NAME,
                                                                                                                       COLOR_CODE);

  private static final Predicate<String>                                      ONSITE_OR_OFFSITE_URL    = matchesEither(ONSITE_URL,
                                                                                                                       OFFSITE_URL);

  private static final Pattern                                                HISTORY_BACK             = Pattern.compile("(?:javascript:)?\\Qhistory.go(-1)\\E");

  private static final Pattern                                                ONE_CHAR                 = Pattern.compile(".?",
                                                                                                                         Pattern.DOTALL);

  @SuppressWarnings("unchecked")
  private static final Collection<String>                                     CUSTOM_ALLOWED_STYLES    =
                                                                                                    (Collection<String>) CollectionUtils.union(CssSchema.DEFAULT.allowedProperties(),
                                                                                                                                               Arrays.asList(new String[]{"float", "display", "clear", "position", "left", "top"}));

  /**
   * The permission-policy features an embedded player needs <em>to render the media</em>.
   * That is the criterion, and it is narrower than "whatever the provider asks for": the
   * question is not whether a feature is denied by default, which is true of almost all of
   * them, but what the framed origin could do to the visitor with it. A note or article
   * body is authored by any platform user, this policy places no constraint on the origin
   * that body embeds, and the same policy governs ten repos' content.
   * <p>
   * So rendering features are granted — <code>autoplay</code>, <code>encrypted-media</code>
   * (DRM playback), <code>fullscreen</code>, <code>picture-in-picture</code>,
   * <code>web-share</code> — and features that reach the visitor are not, whoever asks:
   * <code>camera</code>, <code>microphone</code>, <code>geolocation</code>,
   * <code>display-capture</code>, <code>payment</code>, and two that providers do ask for:
   * <code>clipboard-write</code> (writes the visitor's clipboard) and
   * <code>accelerometer</code>/<code>gyroscope</code> (the Generic Sensor API and
   * DeviceMotion/DeviceOrientation events — device-motion telemetry to an origin this
   * policy does not constrain). YouTube requests all three; Vimeo and Dailymotion request
   * neither sensor. The measured cost of refusing the sensors is 360-degree/VR orientation
   * control, not playback and not fullscreen; restoring them is one entry here, and needs
   * the trade-off written down.
   * <p>
   * <code>web-share</code> is kept for fidelity with the providers' own embed code, though
   * it was measured unimplemented on Chrome/Linux desktop, where it is simply skipped.
   */
  private static final Set<String>                                            ALLOWED_IFRAME_FEATURES   = Set.of("autoplay",
                                                                                                                 "encrypted-media",
                                                                                                                 "fullscreen",
                                                                                                                 "picture-in-picture",
                                                                                                                 "web-share");

  /**
   * The values accepted for the boolean <code>allowfullscreen</code> attribute. HTML §2.3.2
   * defines only three — absent, empty, or an ASCII case-insensitive match of the
   * attribute's own name, with no surrounding whitespace — and explicitly forbids
   * <code>true</code>/<code>false</code> on a boolean attribute. <code>true</code> is
   * tolerated here deliberately — a template engine rendering a boolean binding can
   * serialise it that way, and a browser reads any present <code>allowfullscreen</code> as
   * true whatever its value. <code>false</code> is dropped, which is the safe direction
   * even though HTML would read it as "attribute present, therefore true". Anything else is a producer's mistake and is dropped rather than echoed back,
   * because a note body is compiled as a Vue template, not only parsed as HTML.
   */
  private static final Pattern                                                ALLOW_FULL_SCREEN_VALUE   =
                                                                                                    Pattern.compile("(?i)^(|true|allowfullscreen)$");

  /**
   * Filters an iframe <code>allow</code> attribute down to {@link #ALLOWED_IFRAME_FEATURES}.
   * Providers send a feature list such as
   * <code>accelerometer *; encrypted-media *; fullscreen *;</code>; each kept feature
   * is re-emitted without its origin allow-list, so it falls back to the spec default
   * <code>'src'</code> — the framed document itself, rather than the origins the author
   * listed. Note what that does and does not bound: the direct grant goes to the framed
   * origin only, and that origin may itself delegate the feature onward to whatever it
   * embeds (measured). The bound is on what a page author can grant, not on where the
   * capability ends up. Returns
   * <code>null</code> (drop the attribute) when nothing survives.
   * <p>
   * That <code>'src'</code> narrowing describes <strong>this attribute only</strong>. The
   * boolean <code>allowfullscreen</code> allowed alongside it is specified differently:
   * Permissions Policy §9.4 sets the container policy for <code>fullscreen</code> to the
   * special value <code>*</code> <em>when the <code>allow</code> attribute declares no
   * <code>fullscreen</code> entry of its own</em> — where both are present, the
   * <code>allow</code> entry stands and the boolean adds nothing. Chromium was measured to
   * narrow both shapes to <code>'src'</code> in practice. The two shapes are therefore not equivalent on paper.
   */
  private static final AttributePolicy                                        IFRAME_ALLOW_POLICY       =
                                                                                                    (elementName,
                                                                                                     attributeName,
                                                                                                     value) -> {
                                                                                                      if (value == null) {
                                                                                                        return null;
                                                                                                      }
                                                                                                      String features =
                                                                                                                      Arrays.stream(value.split(";"))
                                                                                                                            .map(String::trim)
                                                                                                                            .filter(feature -> !feature.isEmpty())
                                                                                                                            .map(feature -> feature.split("\\s+")[0].toLowerCase(Locale.ROOT))
                                                                                                                            .filter(ALLOWED_IFRAME_FEATURES::contains)
                                                                                                                            .distinct()
                                                                                                                            .collect(Collectors.joining("; "));
                                                                                                      return features.isEmpty() ? null : features;
                                                                                                    };


  private static final CssSchema.Property                                     ASPECT_RATIO_PROPERTY       =
                                                                                                    new CssSchema.Property(5,
                                                                                                                           ImmutableSet.of("auto",
                                                                                                                                           "inherit"),
                                                                                                                           ImmutableMap.of());

  /** A policy definition that matches the minimal HTML that eXo allows. */
  public static final Function<HtmlStreamEventReceiver, HtmlSanitizer.Policy> POLICY_DEFINITION        = new HtmlPolicyBuilder()
                                                                                                       // Allow
                                                                                                       // these
                                                                                                       // tags.
                                                                                                       .allowAttributes("id")
                                                                                                                                .matching(HTML_ID)
                                                                                                                                .globally()
                                                                                                                                .allowAttributes("class")
                                                                                                                                .matching(HTML_CLASS)
                                                                                                                                .globally()
                                                                                                                                .allowAttributes("lang")
                                                                                                                                .matching(Pattern.compile("[a-zA-Z]{2,20}"))
                                                                                                                                .globally()
                                                                                                                                .allowAttributes("title")
                                                                                                                                .matching(HTML_TITLE)
                                                                                                                                .globally()
                                                                                                                                .allowStyling(CssSchema.withProperties(CUSTOM_ALLOWED_STYLES))
                                                                                                                                .allowStyling(CssSchema.withProperties(Map.of("aspect-ratio",
                                                                                                                                                                                 ASPECT_RATIO_PROPERTY)))
                                                                                                                                .allowAttributes("align")
                                                                                                                                .matching(ALIGN)
                                                                                                                                .onElements("p")
                                                                                                                                .allowAttributes("for")
                                                                                                                                .matching(HTML_ID)
                                                                                                                                .onElements("label")
                                                                                                                                .allowAttributes("color")
                                                                                                                                .matching(COLOR_NAME_OR_COLOR_CODE)
                                                                                                                                .onElements("font")
                                                                                                                                .allowAttributes("face")
                                                                                                                                .matching(Pattern.compile("[\\w;, \\-]+"))
                                                                                                                                .onElements("font")
                                                                                                                                .allowAttributes("size")
                                                                                                                                .matching(NUMBER)
                                                                                                                                .onElements("font")
                                                                                                                                .allowAttributes("href")
                                                                                                                                .matching(ONSITE_OR_OFFSITE_URL)
                                                                                                                                .onElements("a")
                                                                                                                                .allowStandardUrlProtocols()
                                                                                                                                .allowAttributes("data-identity-id")
                                                                                                                                .globally()
                                                                                                                                .allowAttributes("data-role")
                                                                                                                                .globally()
                                                                                                                                .allowAttributes("data-object")
                                                                                                                                .globally()
                                                                                                                                .allowAttributes("nohref")
                                                                                                                                .onElements("a")
                                                                                                                                .allowAttributes("target")
                                                                                                                                .matching(true, "_blank")
                                                                                                                                .onElements("a")
                                                                                                                                .requireRelNofollowOnLinks()
                                                                                                                                .allowAttributes("name", "rel")
                                                                                                                                .matching(NAME)
                                                                                                                                .onElements("a")
                                                                                                                                .allowAttributes("onfocus",
                                                                                                                                        "onblur",
                                                                                                                                        "onclick",
                                                                                                                                        "onmousedown",
                                                                                                                                        "onmouseup")
                                                                                                                                .matching(HISTORY_BACK)
                                                                                                                                .onElements("a")
                                                                                                                                .allowStandardUrlProtocols()
                                                                                                                                .allowUrlProtocols("tel","ftp")
                                                                                                                                .requireRelNofollowOnLinks()
                                                                                                                                .allowAttributes("src")
                                                                                                                                .matching(ONSITE_OR_OFFSITE_URL)
                                                                                                                                .onElements("img")
                                                                                                                                .allowAttributes("archived_cke_uploadId")
                                                                                                                                .onElements("img")
                                                                                                                                .allowAttributes("src")
                                                                                                                                .matching(ONSITE_OR_OFFSITE_URL)
                                                                                                                                .onElements("img")
                                                                                                                                .allowAttributes("referrerpolicy", "data-plugin-name")
                                                                                                                                .onElements("img")
                                                                                                                                .allowAttributes("name")
                                                                                                                                .matching(NAME)
                                                                                                                                .onElements("img")
                                                                                                                                .allowAttributes("alt")
                                                                                                                                .matching(HTML_TITLE)
                                                                                                                                .onElements("img")
                                                                                                                                .allowAttributes("border",
                                                                                                                                        "hspace",
                                                                                                                                        "vspace")
                                                                                                                                .matching(NUMBER)
                                                                                                                                .onElements("img")
                                                                                                                                .allowAttributes("width", "height")
                                                                                                                                .matching(NUMBER_OR_PERCENT)
                                                                                                                                .onElements("img")
                                                                                                                                .allowAttributes("border",
                                                                                                                                        "cellpadding",
                                                                                                                                        "cellspacing")
                                                                                                                                .matching(NUMBER)
                                                                                                                                .onElements("table")
                                                                                                                                .allowAttributes("bgcolor")
                                                                                                                                .matching(COLOR_NAME_OR_COLOR_CODE)
                                                                                                                                .onElements("table")
                                                                                                                                .allowAttributes("background")
                                                                                                                                .matching(ONSITE_URL)
                                                                                                                                .onElements("table")
                                                                                                                                .allowAttributes("align")
                                                                                                                                .matching(ALIGN)
                                                                                                                                .onElements("table")
                                                                                                                                .allowAttributes("noresize")
                                                                                                                                .matching(Pattern.compile("(?i)noresize"))
                                                                                                                                .onElements("table")
                                                                                                                                .allowAttributes("summary")
                                                                                                                                .onElements("table")
                                                                                                                                .allowAttributes("background")
                                                                                                                                .matching(ONSITE_URL)
                                                                                                                                .onElements("td",
                                                                                                                                        "th",
                                                                                                                                        "tr")
                                                                                                                                .allowAttributes("bgcolor")
                                                                                                                                .matching(COLOR_NAME_OR_COLOR_CODE)
                                                                                                                                .onElements("td",
                                                                                                                                        "th")
                                                                                                                                .allowAttributes("abbr")
                                                                                                                                .matching(HTML_TITLE)
                                                                                                                                .onElements("td",
                                                                                                                                        "th")
                                                                                                                                .allowAttributes("axis",
                                                                                                                                        "headers")
                                                                                                                                .matching(NAME)
                                                                                                                                .onElements("td",
                                                                                                                                        "th")
                                                                                                                                .allowAttributes("scope")
                                                                                                                                .matching(Pattern.compile("(?i)(?:row|col)(?:group)?"))
                                                                                                                                .onElements("td",
                                                                                                                                        "th")
                                                                                                                                .allowAttributes("nowrap")
                                                                                                                                .onElements("td",
                                                                                                                                        "th")
                                                                                                                                .allowAttributes("height",
                                                                                                                                        "width")
                                                                                                                                .matching(NUMBER_OR_PERCENT)
                                                                                                                                .onElements("table",
                                                                                                                                        "td",
                                                                                                                                        "th",
                                                                                                                                        "tr",
                                                                                                                                        "img")
                                                                                                                                .allowAttributes("align")
                                                                                                                                .matching(ALIGN)
                                                                                                                                .onElements("thead",
                                                                                                                                        "tbody",
                                                                                                                                        "tfoot",
                                                                                                                                        "img",
                                                                                                                                        "td",
                                                                                                                                        "th",
                                                                                                                                        "tr",
                                                                                                                                        "colgroup",
                                                                                                                                        "col")
                                                                                                                                .allowAttributes("valign")
                                                                                                                                .matching(VALIGN)
                                                                                                                                .onElements("thead",
                                                                                                                                        "tbody",
                                                                                                                                        "tfoot",
                                                                                                                                        "td",
                                                                                                                                        "th",
                                                                                                                                        "tr",
                                                                                                                                        "colgroup",
                                                                                                                                        "col")
                                                                                                                                .allowAttributes("charoff")
                                                                                                                                .matching(NUMBER_OR_PERCENT)
                                                                                                                                .onElements("td",
                                                                                                                                        "th",
                                                                                                                                        "tr",
                                                                                                                                        "colgroup",
                                                                                                                                        "col",
                                                                                                                                        "thead",
                                                                                                                                        "tbody",
                                                                                                                                        "tfoot")
                                                                                                                                .allowAttributes("char")
                                                                                                                                .matching(ONE_CHAR)
                                                                                                                                .onElements("td",
                                                                                                                                        "th",
                                                                                                                                        "tr",
                                                                                                                                        "colgroup",
                                                                                                                                        "col",
                                                                                                                                        "thead",
                                                                                                                                        "tbody",
                                                                                                                                        "tfoot")
                                                                                                                                .allowAttributes("colspan",
                                                                                                                                        "rowspan")
                                                                                                                                .matching(NUMBER)
                                                                                                                                .onElements("td",
                                                                                                                                        "th")
                                                                                                                                .allowAttributes("span",
                                                                                                                                        "width")
                                                                                                                                .matching(NUMBER_OR_PERCENT)
                                                                                                                                .onElements("colgroup",
                                                                                                                                        "col")
                                                                                                                                .allowElements("a",
                                                                                                                                        "oembed",
                                                                                                                                        "label",
                                                                                                                                        "noscript",
                                                                                                                                        "h1",
                                                                                                                                        "h2",
                                                                                                                                        "h3",
                                                                                                                                        "h4",
                                                                                                                                        "h5",
                                                                                                                                        "h6",
                                                                                                                                        "p",
                                                                                                                                        "i",
                                                                                                                                        "b",
                                                                                                                                        "u",
                                                                                                                                        "s",
                                                                                                                                        "strong",
                                                                                                                                        "em",
                                                                                                                                        "small",
                                                                                                                                        "big",
                                                                                                                                        "pre",
                                                                                                                                        "code",
                                                                                                                                        "cite",
                                                                                                                                        "samp",
                                                                                                                                        "sub",
                                                                                                                                        "sup",
                                                                                                                                        "strike",
                                                                                                                                        "del",
                                                                                                                                        "tt",
                                                                                                                                        "center",
                                                                                                                                        "blockquote",
                                                                                                                                        "hr",
                                                                                                                                        "br",
                                                                                                                                        "col",
                                                                                                                                        "figure",
                                                                                                                                        "font",
                                                                                                                                        "map",
                                                                                                                                        "mark",
                                                                                                                                        "span",
                                                                                                                                        "div",
                                                                                                                                        "img",
                                                                                                                                        "ul",
                                                                                                                                        "ol",
                                                                                                                                        "li",
                                                                                                                                        "dd",
                                                                                                                                        "dt",
                                                                                                                                        "dl",
                                                                                                                                        "tbody",
                                                                                                                                        "thead",
                                                                                                                                        "tfoot",
                                                                                                                                        "table",
                                                                                                                                        "td",
                                                                                                                                        "th",
                                                                                                                                        "tr",
                                                                                                                                        "colgroup",
                                                                                                                                        "fieldset",
                                                                                                                                        "legend",
                                                                                                                                        "ins",
                                                                                                                                        "exo-wiki-children-pages",
                                                                                                                                        "exo-wiki-include-page",
                                                                                                                                        "content-link",
                                                                                                                                        "iframe")
                                                                                                                                .allowAttributes("page-name").onElements("exo-wiki-include-page")
                                                                                                                                .allowElements("figcaption")
                                                                                                                                .allowAttributes("class")
                                                                                                                                .matching(HTML_CLASS)
                                                                                                                                .onElements("figcaption")
                                                                                                                                 //Allows the named elements for xwiki input
                                                                                                                                .allowElements("wikiimage","wikilink","wikimacro")
                                                                                                                                .allowAttributes("wikiparam")
                                                                                                                                .globally()
                                                                                                                                .allowAttributes("src")
                                                                                                                                .onElements("iframe")
                                                                                                                                .allowAttributes("allow")
                                                                                                                                .matching(IFRAME_ALLOW_POLICY)
                                                                                                                                .onElements("iframe")
                                                                                                                                .allowAttributes("allowfullscreen")
                                                                                                                                .matching(ALLOW_FULL_SCREEN_VALUE)
                                                                                                                                .onElements("iframe")
                                                                                                                                .allowAttributes("frameborder")
                                                                                                                                .onElements("iframe")
                                                                                                                                .allowAttributes("contenteditable", "data-url")
                                                                                                                                .globally()
                                                                                                                                .allowAttributes("v-identity-popover")
                                                                                                                                .globally()
                                                                                                                                .allowAttributes("is")
                                                                                                                                .globally()
                                                                                                                                .allowElements("caption")
                                                                                                                                .toFactory();

  /**
   * This service reads HTML from input forms and writes sanitized content to a
   * StringBuffer
   * 
   * @param html The <code>String</code> object
   * @return The sanitized HTML to store in DB layer
   */
  public static String sanitize(String html) {
    StringBuilder sb = new StringBuilder();
    // Set up an output channel to receive the sanitized HTML.
    HtmlStreamRenderer renderer = HtmlStreamRenderer.create(sb,
                                                            // Our HTML parser
                                                            // is very lenient,
                                                            // but this receives
                                                            // notifications on
                                                            // truly bizarre
                                                            // inputs.
                                                            x -> {
                                                              throw new AssertionError(x);
                                                            });

    // Use the policy defined above to sanitize the HTML.
    HtmlSanitizer.sanitize(html, POLICY_DEFINITION.apply(renderer));
    return sb.toString();
  }

  private static Predicate<String> matchesEither(final Pattern a, final Pattern b) {
    return s -> a.matcher(s).matches() || b.matcher(s).matches();
  }

}
