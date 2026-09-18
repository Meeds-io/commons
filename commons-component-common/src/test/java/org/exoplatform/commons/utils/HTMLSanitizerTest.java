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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Locale;

import org.junit.Test;

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

  @Test
  public void testAllowTypographicPunctuationInLinks() throws Exception {
    // Safari copies URLs decoded: spaces, accents and typographic apostrophes (U+2019)
    // reach the sanitizer raw instead of percent-encoded (EXO-89915); the sanitizer re-encodes spaces
    String input = "<a href=\"https://www.inria.fr/sites/default/files/2026-04/Charte d\u2019engagement Inria pour l\u2019inclusivit\u00e9 LGBTI+_0.pdf\">link</a>";
    String sanitized = HTMLSanitizer.sanitize(input);
    assertEquals("<a href=\"https://www.inria.fr/sites/default/files/2026-04/Charte%20d\u2019engagement%20Inria%20pour%20l\u2019inclusivit\u00e9%20LGBTI&#43;_0.pdf\" rel=\"nofollow\">link</a>",
                 sanitized);

    input = "<a href=\"/portal/rest/documents/Charte d\u2019engagement \u2013 2026.pdf\">link</a>";
    sanitized = HTMLSanitizer.sanitize(input);
    assertEquals("<a href=\"/portal/rest/documents/Charte%20d\u2019engagement%20\u2013%202026.pdf\" rel=\"nofollow\">link</a>", sanitized);

    // the widened character classes must not open the protocol or attribute boundary
    assertEquals("link", HTMLSanitizer.sanitize("<a href=\"javascript:alert(\u2019x\u2019)\">link</a>"));
    assertEquals("link", HTMLSanitizer.sanitize("<a href=\"data:text/html,\u2019<script>\u2019\">link</a>"));
    assertEquals("<a href=\"https://x.fr/a\u2019\" rel=\"nofollow\">link</a>",
                 HTMLSanitizer.sanitize("<a href=\"https://x.fr/a\u2019\" onmouseover=\"alert(1)\">link</a>"));
  }               
  /**
   * EXO-90272 — an oEmbed provider marks its player fullscreen-capable with the boolean
   * <code>allowfullscreen</code> attribute, which the policy used to drop, leaving the
   * framed document with no fullscreen permission (Calameo hides its button, YouTube
   * logs a permissions-policy violation).
   */
  @Test
  public void testAllowFullScreenAttributeKeptOnIFrame() throws Exception {
    String input = "<iframe src=\"https://www.youtube.com/embed/RLY9uVbuk3Q\" allowfullscreen></iframe>";
    String sanitized = HTMLSanitizer.sanitize(input);
    assertEquals("<iframe src=\"https://www.youtube.com/embed/RLY9uVbuk3Q\" allowfullscreen=\"allowfullscreen\"></iframe>",
                 sanitized);
  }

  /**
   * EXO-90272 — a provider sends a whole feature list; the safe ones survive, each
   * stripped of its origin allow-list so it falls back to the spec default 'src'.
   */
  @Test
  public void testProviderFeatureListKeptOnIFrame() throws Exception {
    // clipboard-write is asked for by the provider and deliberately refused: its default
    // allow-list is 'self', so it is a capability grant, not a rendering capability.
    String input = "<iframe src=\"https://www.youtube.com/embed/RLY9uVbuk3Q\" allow=\"accelerometer *; clipboard-write *; encrypted-media *; gyroscope *; picture-in-picture *; web-share *;\"></iframe>";
    String sanitized = HTMLSanitizer.sanitize(input);
    assertEquals("<iframe src=\"https://www.youtube.com/embed/RLY9uVbuk3Q\" allow=\"accelerometer; encrypted-media; gyroscope; picture-in-picture; web-share\"></iframe>",
                 sanitized);
  }

  /**
   * EXO-90272 — the body carrying the iframe is user-authored, so a feature that grants
   * access to the visitor's devices or wallet is dropped whatever the provider asks for.
   */
  @Test
  public void testDangerousIFrameFeaturesDropped() throws Exception {
    String input = "<iframe src=\"https://www.youtube.com/embed/RLY9uVbuk3Q\" allow=\"camera *; microphone; geolocation 'self'; display-capture; payment; fullscreen\"></iframe>";
    String sanitized = HTMLSanitizer.sanitize(input);
    assertEquals("<iframe src=\"https://www.youtube.com/embed/RLY9uVbuk3Q\" allow=\"fullscreen\"></iframe>", sanitized);
  }

  /**
   * Not a regression pin: the previous policy dropped this value too (its regex matched
   * nothing but the bare word). It is a guard against a later widening of
   * {@link HTMLSanitizer} that would let an unknown feature through — it fails only if
   * someone relaxes the filter, which is exactly when it is wanted.
   */
  @Test
  public void testAllowAttributeDroppedWhenNoSafeFeatureRemains() throws Exception {
    String input = "<iframe src=\"https://www.youtube.com/embed/RLY9uVbuk3Q\" allow=\"camera; microphone\"></iframe>";
    String sanitized = HTMLSanitizer.sanitize(input);
    assertEquals("<iframe src=\"https://www.youtube.com/embed/RLY9uVbuk3Q\"></iframe>", sanitized);
  }

  /**
   * EXO-90272 — the shape the notes editor actually stores: the oEmbed widget saved as a
   * <code>div.embed-wrapper</code> wrapping the provider's iframe. This is the case
   * reported on a note and on an article.
   */
  @Test
  public void testEmbedWrapperKeepsFullScreenCapability() throws Exception {
    String input = "<div class=\"embed-wrapper\" data-url=\"url\"><div><iframe src=\"https://v.calameo.com/?bkcode=007393684777abcf55de3\" allowfullscreen allow=\"encrypted-media *;\"></iframe></div></div>";
    String sanitized = HTMLSanitizer.sanitize(input);
    assertEquals("<div class=\"embed-wrapper\" data-url=\"url\"><div><iframe src=\"https://v.calameo.com/?bkcode&#61;007393684777abcf55de3\" allowfullscreen=\"allowfullscreen\" allow=\"encrypted-media\"></iframe></div></div>",
                 sanitized);
  }

  /**
   * EXO-90272 — the four parsing properties the filter's safety rests on, pinned because a
   * later refactor could drop any of them silently: the value is lower-cased with
   * {@link java.util.Locale#ROOT} (a Turkish default locale would otherwise break
   * PICTURE-IN-PICTURE), the policy sees the entity-decoded value, the separator is the
   * semicolon and not the comma of the HTTP header grammar, and a look-alike token is not
   * a token.
   */
  @Test
  public void testAllowFeatureMatchingIsCaseFoldedAndEntityDecoded() throws Exception {
    // Under a Turkish default locale "PICTURE-IN-PICTURE".toLowerCase() is
    // "pıcture-ın-pıcture" and the feature would be silently lost, so the case folding is
    // asserted in the environment that can actually see it fail -- CI runs in en_US, where
    // a locale-sensitive lower-casing passes.
    Locale previous = Locale.getDefault();
    try {
      Locale.setDefault(Locale.forLanguageTag("tr-TR"));
      assertEquals("<iframe src=\"https://www.youtube.com/embed/x\" allow=\"fullscreen; picture-in-picture\"></iframe>",
                   HTMLSanitizer.sanitize("<iframe src=\"https://www.youtube.com/embed/x\" allow=\"FULLSCREEN; CAMERA; PICTURE-IN-PICTURE\"></iframe>"));
    } finally {
      Locale.setDefault(previous);
    }
    assertEquals("<iframe src=\"https://www.youtube.com/embed/x\" allow=\"fullscreen\"></iframe>",
                 HTMLSanitizer.sanitize("<iframe src=\"https://www.youtube.com/embed/x\" allow=\"&#102;ullscreen\"></iframe>"));
  }

  @Test
  public void testAllowFeatureSeparatorIsSemicolonAndLookAlikesAreRejected() throws Exception {
    // "camera,fullscreen" is one unknown token: the comma separates HTTP header entries,
    // not the entries of this attribute, so nothing survives and the attribute is dropped.
    assertEquals("<iframe src=\"https://www.youtube.com/embed/x\"></iframe>",
                 HTMLSanitizer.sanitize("<iframe src=\"https://www.youtube.com/embed/x\" allow=\"camera,fullscreen\"></iframe>"));
    // fullwidth U+FF46 is not an "f"
    assertEquals("<iframe src=\"https://www.youtube.com/embed/x\"></iframe>",
                 HTMLSanitizer.sanitize("<iframe src=\"https://www.youtube.com/embed/x\" allow=\"\uFF46ullscreen\"></iframe>"));
  }

  /**
   * EXO-90272 — <code>allowfullscreen</code> is a boolean attribute; a value other than the
   * ones HTML defines is a producer mistake and is not echoed back, because a note body is
   * compiled as a Vue template and not only parsed as HTML.
   */
  @Test
  public void testAllowFullScreenRejectsAnArbitraryValue() throws Exception {
    assertEquals("<iframe src=\"https://www.youtube.com/embed/x\"></iframe>",
                 HTMLSanitizer.sanitize("<iframe src=\"https://www.youtube.com/embed/x\" allowfullscreen=\"javascript:alert(1)\"></iframe>"));
    assertEquals("<iframe src=\"https://www.youtube.com/embed/x\" allowfullscreen=\"true\"></iframe>",
                 HTMLSanitizer.sanitize("<iframe src=\"https://www.youtube.com/embed/x\" allowfullscreen=\"true\"></iframe>"));
  }

  /**
   * EXO-90272 — the capabilities that must stay refused whatever an embedded origin asks
   * for. This is the security contract of {@link HTMLSanitizer}'s iframe policy.
   */
  @Test
  public void testDeviceAndPaymentFeaturesNeverGranted() throws Exception {
    String input = "<iframe src=\"https://www.youtube.com/embed/x\" allow=\"camera; microphone; geolocation; display-capture; payment; clipboard-write; usb; midi; screen-wake-lock\"></iframe>";
    assertEquals("<iframe src=\"https://www.youtube.com/embed/x\"></iframe>", HTMLSanitizer.sanitize(input));
  }
}
