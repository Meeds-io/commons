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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
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
    assertEquals("", sanitized);
  }

  /**
   * EXO-90558 — an embed as the notes editor stores it (<code>preserveEmbedded</code>): a
   * styled wrapper around the iframe of the HTML that
   * <code>ckeditor.iframe.ly/api/oembed?omit_script=1</code> answered for
   * <code>https://www.youtube.com/watch?v=iBd1r5VOK2c</code> (2026-10-06), iframely's
   * protocol-relative <code>//if-cdn.com/&lt;id&gt;</code>, not YouTube's player. It keeps
   * its iframe with the default allowed hosts.
   */
  @Test
  public void testEditorEmbedFromIframelyKept() throws Exception {
    String iframe = "<iframe src=\"//if-cdn.com/cGy0Wq3T\" style=\"top: 0; left: 0; width: 100%; height: 100%; position: absolute; border: 0;\""
        + " allowfullscreen scrolling=\"no\""
        + " allow=\"accelerometer *; clipboard-write *; encrypted-media *; gyroscope *; picture-in-picture *; web-share *;\"></iframe>";
    String stored = "<div data-url=\"https://www.youtube.com/watch?v&#61;iBd1r5VOK2c\""
        + " style=\"min-height: 168.68932038834953px; min-width: 300px; width: 100%; margin-bottom: 10px; aspect-ratio: 1.7784172661870503;\""
        + " class=\"embed-wrapper d-flex position-relative ml-auto mr-auto\">" + iframe + "</div>";
    String sanitized = HTMLSanitizer.sanitize(stored);
    assertTrue(sanitized, sanitized.contains("<iframe src=\"//if-cdn.com/cGy0Wq3T\""));
    assertTrue(HTMLSanitizer.isAllowedIframeSrc("//if-cdn.com/eCZPIqYd"));
    assertTrue(HTMLSanitizer.isAllowedIframeSrc("https://if-cdn.com/74HLuw1D"));
  }

  /**
   * EXO-90558 — the other default hosts, bare and inside a wrapper, keep their
   * <code>src</code>; a protocol-relative source is kept, and a query may carry characters a
   * browser accepts unencoded.
   */
  @Test
  public void testDefaultEmbedProvidersKeptInIFrame() throws Exception {
    for (String src : List.of("https://www.youtube.com/embed/x",
                              "https://www.youtube-nocookie.com/embed/x",
                              "https://player.vimeo.com/video/1",
                              "https://www.dailymotion.com/embed/video/x",
                              "https://geo.dailymotion.com/player.html",
                              "https://v.calameo.com/",
                              "https://cdn.iframe.ly/api/iframe",
                              "//cdn.iframe.ly/api/iframe",
                              "HTTPS://WWW.YOUTUBE.COM/embed/x")) {
      String input = "<div style=\"position:relative\"><iframe src=\"" + src + "\"></iframe></div>";
      assertEquals(src, input, HTMLSanitizer.sanitize(input));
    }
    assertEquals("<iframe src=\"https://www.youtube.com/embed/x?list&#61;a|b&amp;c&#61;^1&amp;d&#61;&#96;e \"></iframe>",
                 HTMLSanitizer.sanitize("<iframe src=\"https://www.youtube.com/embed/x?list=a|b&amp;c=^1&amp;d=`e\"></iframe>"));
  }

  /**
   * EXO-90558 — an iframe is kept only when its <code>src</code> host is exactly an allowed
   * one: not a sub-domain, not a look-alike suffix, not the user-info part (entity-encoded
   * included, the policy seeing the decoded value), not over plain http or another scheme,
   * and not a relative or unparseable URL. The refused iframe is dropped, its wrapper kept.
   */
  @Test
  public void testNotAllowedIFrameSrcDropped() throws Exception {
    for (String src : List.of("https://evil.example/x",
                              "https://www.youtube.com.evil.example/embed/x",
                              "https://evil.www.youtube.com/embed/x",
                              "https://www.youtube.com@evil.example/embed/x",
                              "https://www.youtube.com&#64;evil.example/embed/x",
                              "https://evil.example\\@www.youtube.com/embed/x",
                              "http://www.youtube.com/embed/x",
                              "javascript:alert(1)",
                              "data:text/html,x",
                              "/portal/dw",
                              "")) {
      assertEquals(src,
                   "<div class=\"embed-wrapper\"></div>",
                   HTMLSanitizer.sanitize("<div class=\"embed-wrapper\"><iframe frameborder=\"0\" src=\"" + src + "\"></iframe></div>"));
      if (!src.contains("&#64;")) {
        assertFalse(src, HTMLSanitizer.isAllowedIframeSrc(src));
      }
    }
    assertFalse(HTMLSanitizer.isAllowedIframeSrc(null));
  }

  /**
   * EXO-90558 — the system property replaces the default list, so a host can be added and
   * a default one removed; set empty, it allows none.
   */
  @Test
  public void testAllowedIframeHostsFromSystemProperty() throws Exception {
    try {
      PropertyManager.setProperty(HTMLSanitizer.IFRAME_ALLOWED_HOSTS_PROPERTY, " W.SoundCloud.com , player.vimeo.com,,");
      assertEquals(List.of("w.soundcloud.com", "player.vimeo.com"), HTMLSanitizer.getAllowedIframeHosts());
      assertEquals("<iframe src=\"https://w.soundcloud.com/player/\"></iframe>",
                   HTMLSanitizer.sanitize("<iframe src=\"https://w.soundcloud.com/player/\"></iframe>"));
      assertEquals("", HTMLSanitizer.sanitize("<iframe src=\"https://www.youtube.com/embed/x\"></iframe>"));

      PropertyManager.setProperty(HTMLSanitizer.IFRAME_ALLOWED_HOSTS_PROPERTY, "");
      assertEquals(List.of(), HTMLSanitizer.getAllowedIframeHosts());
      assertFalse(HTMLSanitizer.isAllowedIframeSrc("https://www.youtube.com/embed/x"));
    } finally {
      System.clearProperty(HTMLSanitizer.IFRAME_ALLOWED_HOSTS_PROPERTY);
      PropertyManager.refresh();
    }
    assertEquals(HTMLSanitizer.DEFAULT_IFRAME_ALLOWED_HOSTS, HTMLSanitizer.getAllowedIframeHosts());
    assertTrue(HTMLSanitizer.isAllowedIframeSrc("https://www.youtube.com/embed/x"));
  }

  /**
   * EXO-90558 — a <code>*.</code> entry allows every sub-domain of its domain, at any depth,
   * and neither the domain itself nor a host that merely ends with the same characters.
   */
  @Test
  public void testWildcardAllowedIframeHost() throws Exception {
    try {
      PropertyManager.setProperty(HTMLSanitizer.IFRAME_ALLOWED_HOSTS_PROPERTY, "www.youtube.com, *.SharePoint.com");
      for (String src : List.of("https://contoso.sharepoint.com/sites/x/_layouts/15/embed.aspx?UniqueId=1",
                                "https://contoso.my.sharepoint.com/personal/x",
                                "//CONTOSO.SHAREPOINT.COM/x",
                                "https://www.youtube.com/embed/x")) {
        assertTrue(src, HTMLSanitizer.isAllowedIframeSrc(src));
      }
      for (String src : List.of("https://sharepoint.com/x",
                                "https://evilsharepoint.com/x",
                                "https://contoso.sharepoint.com.evil.example/x",
                                "https://contoso.sharepoint.com@evil.example/x",
                                "http://contoso.sharepoint.com/x",
                                "https://youtube.com/embed/x")) {
        assertFalse(src, HTMLSanitizer.isAllowedIframeSrc(src));
      }
      assertEquals("<iframe src=\"https://contoso.sharepoint.com/x\"></iframe>",
                   HTMLSanitizer.sanitize("<iframe src=\"https://contoso.sharepoint.com/x\"></iframe>"));
      assertEquals("", HTMLSanitizer.sanitize("<iframe src=\"https://sharepoint.com/x\"></iframe>"));
    } finally {
      System.clearProperty(HTMLSanitizer.IFRAME_ALLOWED_HOSTS_PROPERTY);
      PropertyManager.refresh();
    }
  }

  /**
   * EXO-90558 — the allowed hosts are written into a page script by the portal head, so an
   * entry that is neither a plain host name nor a <code>*.</code> wildcard over a domain of
   * two labels at least is dropped rather than echoed.
   */
  @Test
  public void testInvalidAllowedIframeHostIgnored() {
    assertEquals(List.of("w.soundcloud.com", "*.sharepoint.com", "*.my.sharepoint.com"),
                 HTMLSanitizer.parseAllowedIframeHosts("w.soundcloud.com, \"];alert(1);//, *.sharepoint.com, https://x.example/, -x.example, *, *.com, a.*.com, *sharepoint.com, *.*.com, *.-x.com, *.my.sharepoint.com, *.a..com, *.a-.com, *.com., *."));
    assertEquals(HTMLSanitizer.DEFAULT_IFRAME_ALLOWED_HOSTS, HTMLSanitizer.parseAllowedIframeHosts(null));
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
   * EXO-90272 — the fixtures are the oEmbed output of the providers themselves, fetched
   * verbatim but for the <code>title</code>, normalised so the fixture does not break when
   * a provider retitles a video. This defect was caused by a suite whose fixtures encoded a
   * shape no producer emits, so these are taken from the producers. Each of the three keeps its fullscreen capability: YouTube through the
   * boolean attribute, Vimeo through the feature list, Dailymotion through both.
   */
  @Test
  public void testRealProviderEmbedsKeepFullScreenCapability() throws Exception {
    // https://www.youtube.com/oembed?url=...&format=json
    assertEquals("<iframe src=\"https://www.youtube.com/embed/MSBV91xceEw?feature&#61;oembed\" frameborder=\"0\" allow=\"autoplay; encrypted-media; picture-in-picture\" allowfullscreen=\"allowfullscreen\" title=\"t\"></iframe>",
                 HTMLSanitizer.sanitize("<iframe width=\"200\" height=\"113\" src=\"https://www.youtube.com/embed/MSBV91xceEw?feature=oembed\" frameborder=\"0\" allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share\" referrerpolicy=\"strict-origin-when-cross-origin\" allowfullscreen title=\"t\"></iframe>"));
    // https://www.dailymotion.com/services/oembed?url=...&format=json
    assertEquals("<iframe frameborder=\"0\" src=\"https://geo.dailymotion.com/player.html?video&#61;x2jvvep&amp;\" allowfullscreen=\"allowfullscreen\" allow=\"autoplay; fullscreen; picture-in-picture\"></iframe>",
                 HTMLSanitizer.sanitize("<iframe frameborder=\"0\" width=\"480\" height=\"269\" src=\"https://geo.dailymotion.com/player.html?video=x2jvvep&\" allowfullscreen allow=\"autoplay; fullscreen; picture-in-picture; web-share\"></iframe>"));
    // https://vimeo.com/api/oembed.json?url=...
    assertEquals("<iframe src=\"https://player.vimeo.com/video/243244233?app_id&#61;122963\" frameborder=\"0\" allow=\"autoplay; fullscreen; picture-in-picture; encrypted-media\" title=\"t\"></iframe>",
                 HTMLSanitizer.sanitize("<iframe src=\"https://player.vimeo.com/video/243244233?app_id=122963\" width=\"426\" height=\"240\" frameborder=\"0\" allow=\"autoplay; fullscreen; picture-in-picture; clipboard-write; encrypted-media; web-share\" referrerpolicy=\"strict-origin-when-cross-origin\" title=\"t\"></iframe>"));
  }

  /** EXO-90272 — a feature repeated, in any case, is emitted once. */
  @Test
  public void testDuplicateFeaturesCollapse() throws Exception {
    assertEquals("<iframe src=\"https://www.youtube.com/embed/x\" allow=\"fullscreen\"></iframe>",
                 HTMLSanitizer.sanitize("<iframe src=\"https://www.youtube.com/embed/x\" allow=\"fullscreen; fullscreen; FULLSCREEN\"></iframe>"));
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
   * for. This is the security contract of {@link HTMLSanitizer}'s iframe policy: a feature
   * that reaches the visitor rather than rendering the media is dropped. accelerometer and
   * gyroscope are in this list deliberately — YouTube asks for both, and they carry
   * device-motion telemetry to a third-party origin — and so is
   * web-share, which hands the platform share sheet to the framed origin and mirrors
   * clipboard-write.
   */
  @Test
  public void testDeviceAndPaymentFeaturesNeverGranted() throws Exception {
    String input = "<iframe src=\"https://www.youtube.com/embed/x\" allow=\"camera; microphone; geolocation; display-capture; payment; clipboard-write; web-share; accelerometer; gyroscope; usb; midi; screen-wake-lock\"></iframe>";
    assertEquals("<iframe src=\"https://www.youtube.com/embed/x\"></iframe>", HTMLSanitizer.sanitize(input));
  }
}
