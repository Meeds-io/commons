/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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
package io.meeds.commons.http;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Modifier;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Pins what a fetched URL may point at: the URL shapes refused, every address
 * range refused in IPv4 and IPv6 — the IPv4 forms carried inside IPv6 included
 * — the resolution rules, the deployment's opt-out, the test seams, and what a
 * policy refuses to be built with.
 */
class SafeAddressGuardTest {

  private final Map<String, InetAddress[]> dns = new HashMap<>();

  /**
   * A guard over the table of names, internal addresses refused, the default
   * ports.
   *
   * @return the guard
   */
  private SafeAddressGuard guard() {
    return new SafeAddressGuard(policy().build());
  }

  /**
   * A policy over the table of names.
   *
   * @return the builder
   */
  private SafeFetchPolicyBuilder policy() {
    return SafeFetchPolicy.builder().resolver(this::resolve);
  }

  /**
   * Resolves a name from the table.
   *
   * @param host the name
   * @return its addresses
   * @throws UnknownHostException when the table does not know it
   */
  private InetAddress[] resolve(String host) throws UnknownHostException {
    InetAddress[] addresses = dns.get(host);
    if (addresses == null) {
      throw new UnknownHostException(host);
    }
    return addresses;
  }

  /**
   * An address from its bytes, without the JDK turning an IPv4-mapped IPv6
   * address into an IPv4 one.
   *
   * @param bytes four or sixteen bytes
   * @return the address
   * @throws UnknownHostException never for a valid length
   */
  private static InetAddress address(int... bytes) throws UnknownHostException {
    byte[] raw = new byte[bytes.length];
    for (int i = 0; i < bytes.length; i++) {
      raw[i] = (byte) bytes[i];
    }
    if (raw.length == 16) {
      return Inet6Address.getByAddress(null, raw, null);
    }
    return InetAddress.getByAddress(raw);
  }

  /**
   * The reason a URL is refused.
   *
   * @param url the URL
   * @return the reason
   */
  private SafeFetchFailure refusal(String url) {
    return assertThrows(SafeFetchException.class, () -> guard().normalize(url)).getFailure();
  }

  /**
   * The fragment is dropped, the scheme lower-cased, the rest kept as typed.
   *
   * @throws Exception never
   */
  @Test
  void normalizeDropsTheFragmentAndLowerCasesTheScheme() throws Exception {
    assertEquals("https://example.org/a.ics?k=v", guard().normalize("https://example.org/a.ics?k=v#frag").toString());
    assertEquals("http://example.org:8080/a", guard().normalize("  HTTP://example.org:8080/a  ").toString());
    assertEquals("https://example.org/A/b", guard().normalize("https://example.org/A/b").toString());
  }

  /**
   * Only the allowed schemes are read, and a policy may allow https alone.
   */
  @Test
  void schemesOutsideTheAllowedSetAreRefused() {
    assertEquals(SafeFetchFailure.SCHEME_NOT_ALLOWED, refusal("ftp://example.org/a"));
    assertEquals(SafeFetchFailure.SCHEME_NOT_ALLOWED, refusal("file:///etc/passwd"));
    assertEquals(SafeFetchFailure.SCHEME_NOT_ALLOWED, refusal("javascript:alert(1)"));
    assertEquals(SafeFetchFailure.SCHEME_NOT_ALLOWED, refusal("gopher://example.org/"));
    SafeAddressGuard httpsOnly = new SafeAddressGuard(policy().httpsOnly().build());
    assertEquals(SafeFetchFailure.SCHEME_NOT_ALLOWED,
                 assertThrows(SafeFetchException.class, () -> httpsOnly.normalize("http://example.org/a")).getFailure());
  }

  /**
   * No user name or password travels in a URL.
   */
  @Test
  void credentialsInTheUrlAreRefused() {
    assertEquals(SafeFetchFailure.CREDENTIALS_IN_URL, refusal("https://user:secret@example.org/a"));
    assertEquals(SafeFetchFailure.CREDENTIALS_IN_URL, refusal("https://user@example.org/a"));
  }

  /**
   * Only the allowed ports, the implicit ones included; any port when the
   * policy says so, and only then.
   *
   * @throws Exception never
   */
  @Test
  void portsOutsideTheAllowedSetAreRefused() throws Exception {
    assertEquals(SafeFetchFailure.PORT_NOT_ALLOWED, refusal("https://example.org:22/a"));
    assertEquals(SafeFetchFailure.PORT_NOT_ALLOWED, refusal("http://example.org:6379/"));
    guard().normalize("https://example.org:8443/a");
    SafeAddressGuard narrow = new SafeAddressGuard(policy().allowedPorts(Set.of(8443)).build());
    assertEquals(SafeFetchFailure.PORT_NOT_ALLOWED,
                 assertThrows(SafeFetchException.class, () -> narrow.normalize("https://example.org/a")).getFailure());
    SafeAddressGuard any = new SafeAddressGuard(policy().anyPort().build());
    assertEquals(6379, any.normalize("http://example.org:6379/").getPort());
    SafeAddressGuard fromProperty = new SafeAddressGuard(policy().allowedPorts("80, 9443,x", Set.of(443)).build());
    fromProperty.normalize("https://example.org:9443/a");
    assertEquals(SafeFetchFailure.PORT_NOT_ALLOWED,
                 assertThrows(SafeFetchException.class, () -> fromProperty.normalize("https://example.org/a")).getFailure());
    SafeAddressGuard fallback = new SafeAddressGuard(policy().allowedPorts("none", Set.of(443)).build());
    fallback.normalize("https://example.org/a");
  }

  /**
   * What is not a usable URL is refused as such, the redirect target included.
   */
  @Test
  void unusableUrlsAreRefused() {
    assertEquals(SafeFetchFailure.INVALID_URL, refusal(null));
    assertEquals(SafeFetchFailure.INVALID_URL, refusal("   "));
    assertEquals(SafeFetchFailure.INVALID_URL, refusal("not a url"));
    assertEquals(SafeFetchFailure.INVALID_URL, refusal("/relative/a"));
    assertEquals(SafeFetchFailure.INVALID_URL, refusal("https:///a"));
    assertEquals(SafeFetchFailure.INVALID_URL, refusal("https://example.org/a\nb"));
    assertEquals(SafeFetchFailure.INVALID_URL, refusal("https://example.org/" + "a".repeat(SafeAddressGuard.MAX_URL_LENGTH)));
    assertEquals(SafeFetchFailure.INVALID_URL,
                 assertThrows(SafeFetchException.class, () -> guard().checkTarget(URI.create("/relative"))).getFailure());
    assertEquals(SafeFetchFailure.INVALID_URL,
                 assertThrows(SafeFetchException.class, () -> guard().checkTarget(null)).getFailure());
  }

  /**
   * Every IPv4 range a URL must not reach is refused: this network, RFC 1918,
   * carrier-grade NAT, loopback, link-local with the metadata address, IETF
   * assignments, benchmarking, multicast, reserved and broadcast.
   *
   * @throws Exception never
   */
  @Test
  void everyInternalIpv4RangeIsRefused() throws Exception {
    int[][] refused = { { 0, 0, 0, 0 }, { 0, 1, 2, 3 }, { 10, 1, 2, 3 }, { 100, 64, 0, 1 }, { 100, 127, 255, 255 },
        { 127, 0, 0, 1 }, { 127, 5, 5, 5 }, { 169, 254, 169, 254 }, { 169, 254, 0, 1 }, { 172, 16, 0, 1 },
        { 172, 31, 255, 255 }, { 192, 168, 1, 1 }, { 192, 0, 0, 8 }, { 198, 18, 0, 1 }, { 198, 19, 255, 255 },
        { 224, 0, 0, 1 }, { 239, 1, 1, 1 }, { 240, 0, 0, 1 }, { 255, 255, 255, 255 } };
    for (int[] bytes : refused) {
      assertTrue(SafeAddressGuard.isBlocked(address(bytes)), "refused: " + address(bytes));
    }
  }

  /**
   * The public neighbours of every refused IPv4 range stay reachable.
   *
   * @throws Exception never
   */
  @Test
  void publicIpv4NeighboursAreAllowed() throws Exception {
    int[][] allowed = { { 8, 8, 8, 8 }, { 1, 1, 1, 1 }, { 100, 63, 255, 255 }, { 100, 128, 0, 1 }, { 172, 15, 255, 255 },
        { 172, 32, 0, 1 }, { 192, 169, 0, 1 }, { 169, 253, 1, 1 }, { 192, 0, 1, 1 }, { 198, 20, 0, 1 },
        { 223, 255, 255, 254 }, { 93, 184, 216, 34 } };
    for (int[] bytes : allowed) {
      assertFalse(SafeAddressGuard.isBlocked(address(bytes)), "allowed: " + address(bytes));
    }
  }

  /**
   * Every IPv6 range a URL must not reach is refused, and so is every refused
   * IPv4 address carried inside an IPv6 one: mapped, compatible, NAT64 and
   * 6to4.
   *
   * @throws Exception never
   */
  @Test
  void everyInternalIpv6RangeAndEmbeddedIpv4IsRefused() throws Exception {
    InetAddress[] refused = { InetAddress.getByName("::"), InetAddress.getByName("::1"), InetAddress.getByName("fe80::1"),
        InetAddress.getByName("fec0::1"), InetAddress.getByName("fc00::1"), InetAddress.getByName("fd12:3456::1"),
        InetAddress.getByName("ff02::1"),
        // ::ffff:127.0.0.1 and ::ffff:10.0.0.1, IPv4-mapped
        address(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0xff, 0xff, 127, 0, 0, 1),
        address(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0xff, 0xff, 10, 0, 0, 1),
        // ::169.254.169.254, IPv4-compatible
        address(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 169, 254, 169, 254),
        // 64:ff9b::a9fe:a9fe, NAT64 of the metadata address
        address(0, 0x64, 0xff, 0x9b, 0, 0, 0, 0, 0, 0, 0, 0, 169, 254, 169, 254),
        // 2002:c0a8:0101::, 6to4 of 192.168.1.1
        address(0x20, 0x02, 192, 168, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0) };
    for (InetAddress address : refused) {
      assertTrue(SafeAddressGuard.isBlocked(address), "refused: " + address);
    }
  }

  /**
   * Public IPv6 addresses, and the IPv6 forms of public IPv4 ones, stay
   * reachable.
   *
   * @throws Exception never
   */
  @Test
  void publicIpv6AddressesAreAllowed() throws Exception {
    InetAddress[] allowed = { InetAddress.getByName("2001:4860:4860::8888"), InetAddress.getByName("2606:4700::1111"),
        address(0, 0x64, 0xff, 0x9b, 0, 0, 0, 0, 0, 0, 0, 0, 8, 8, 8, 8),
        address(0x20, 0x02, 8, 8, 8, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0) };
    for (InetAddress address : allowed) {
      assertFalse(SafeAddressGuard.isBlocked(address), "allowed: " + address);
    }
  }

  /**
   * A name is judged on every address it resolves to: one internal address among
   * public ones refuses it, and a refusal is told apart from a name resolving to
   * nothing.
   *
   * @throws Exception never
   */
  @Test
  void aNameIsRefusedWhenAnyAddressIsInternal() throws Exception {
    dns.put("public.test", new InetAddress[] { address(93, 184, 216, 34) });
    dns.put("private.test", new InetAddress[] { address(10, 0, 0, 5) });
    dns.put("mixed.test", new InetAddress[] { address(93, 184, 216, 34), address(127, 0, 0, 1) });
    dns.put("empty.test", new InetAddress[0]);

    assertArrayEquals(dns.get("public.test"), guard().resolveAllowed("public.test"));
    assertThrows(RefusedAddressException.class, () -> guard().resolveAllowed("private.test"));
    assertThrows(RefusedAddressException.class, () -> guard().resolveAllowed("mixed.test"));
    UnknownHostException unknown = assertThrows(UnknownHostException.class, () -> guard().resolveAllowed("unknown.test"));
    assertFalse(unknown instanceof RefusedAddressException, "a name resolving to nothing is not a refusal");
    assertThrows(UnknownHostException.class, () -> guard().resolveAllowed("empty.test"));
    assertThrows(UnknownHostException.class, () -> guard().resolveAllowed("  "));
  }

  /**
   * An IP literal, IPv6 in brackets included, is judged like any address through
   * the JDK's resolver.
   */
  @Test
  void anIpLiteralIsJudged() {
    SafeAddressGuard jdk = new SafeAddressGuard(SafeFetchPolicy.builder().build());
    assertThrows(RefusedAddressException.class, () -> jdk.resolveAllowed("[::1]"));
    assertThrows(RefusedAddressException.class, () -> jdk.resolveAllowed("127.0.0.1"));
    assertThrows(RefusedAddressException.class, () -> jdk.resolveAllowed("169.254.169.254"));
    assertThrows(RefusedAddressException.class, () -> jdk.resolveAllowed("[::ffff:10.0.0.1]"));
  }

  /**
   * The deployment's opt-out lets internal addresses through, and each test seam
   * exempts exactly what it names: an address whatever its name, a name whatever
   * its addresses.
   *
   * @throws Exception never
   */
  @Test
  void internalAddressesPassOnlyWhenTheDeploymentAllowsThemOrASeamExemptsThem() throws Exception {
    dns.put("private.test", new InetAddress[] { address(10, 0, 0, 5) });
    dns.put("loopback.test", new InetAddress[] { address(127, 0, 0, 1) });
    dns.put("other-loopback.test", new InetAddress[] { address(127, 0, 0, 2) });

    SafeAddressGuard allowing = new SafeAddressGuard(policy().internalAddressesAllowed(true).build());
    assertArrayEquals(dns.get("private.test"), allowing.resolveAllowed("private.test"));

    SafeAddressGuard byAddress = new SafeAddressGuard(policy().exemptAddresses(Set.of(address(127, 0, 0, 1))).build());
    assertArrayEquals(dns.get("loopback.test"), byAddress.resolveAllowed("loopback.test"));
    assertThrows(RefusedAddressException.class, () -> byAddress.resolveAllowed("other-loopback.test"));
    assertThrows(RefusedAddressException.class, () -> byAddress.resolveAllowed("private.test"));

    SafeAddressGuard byName = new SafeAddressGuard(policy().exemptHosts(Set.of("private.test")).build());
    assertArrayEquals(dns.get("private.test"), byName.resolveAllowed("private.test"));
    assertThrows(RefusedAddressException.class, () -> byName.resolveAllowed("loopback.test"));
  }

  /**
   * The exemption seams are out of reach of a production policy: only code of
   * this package can set them, while the resolver seam, whose every answer
   * the guard still judges, stays public.
   *
   * @throws Exception never
   */
  @Test
  void onlyTheResolverSeamIsPublic() throws Exception {
    assertFalse(Modifier.isPublic(SafeFetchPolicyBuilder.class.getDeclaredMethod("exemptHosts", Collection.class).getModifiers()));
    assertFalse(Modifier.isPublic(SafeFetchPolicyBuilder.class.getDeclaredMethod("exemptAddresses", Collection.class).getModifiers()));
    assertTrue(Modifier.isPublic(SafeFetchPolicyBuilder.class.getDeclaredMethod("resolver", HostResolver.class).getModifiers()));
  }

  /**
   * A port list read from a deployment property ignores what is not a port —
   * a word, zero, a number above 65535 however many digits it has — rather
   * than failing, and falls back when it names none.
   */
  @Test
  void aPortListIgnoresWhatIsNotAPort() {
    Set<Integer> fallback = Set.of(443);
    assertEquals(Set.of(443, 8443),
                 SafeFetchPolicy.builder().allowedPorts("443, 99999999999,abc,0,70000,8443", fallback).build().getAllowedPorts());
    assertEquals(fallback, SafeFetchPolicy.builder().allowedPorts("99999999999", Set.of(443)).build().getAllowedPorts());
    assertEquals(Set.of(8080), SafeFetchPolicy.builder().allowedPorts("08080", fallback).build().getAllowedPorts());
    assertEquals(fallback, SafeFetchPolicy.builder().allowedPorts(null, Set.of(443)).build().getAllowedPorts());
  }

  /**
   * An IP literal naming an internal address is refused by the URL check
   * itself, before any resolution — the resolver here knows no name at all —
   * and a public literal passes; the opt-out and the address seam apply as
   * they do at resolution.
   *
   * @throws Exception never
   */
  @Test
  void anInternalIpLiteralIsRefusedByTheUrlCheck() throws Exception {
    for (String host : new String[] { "127.0.0.1", "[::1]", "169.254.169.254", "[::ffff:127.0.0.1]", "10.0.0.5", "[fd00:ec2::254]",
        "100.100.100.200", "0.0.0.0" }) {
      assertEquals(SafeFetchFailure.REFUSED_ADDRESS, refusal("http://" + host + "/"), host);
    }
    assertEquals("http://93.184.216.34/", guard().normalize("http://93.184.216.34/").toString());
    assertEquals("http://[2606:2800:220:1:248:1893:25c8:1946]/", guard().normalize("http://[2606:2800:220:1:248:1893:25c8:1946]/").toString());
    assertEquals("http://localhost/", guard().normalize("http://localhost/").toString(), "a name is left to the resolver");

    SafeAddressGuard allowing = new SafeAddressGuard(policy().internalAddressesAllowed(true).build());
    assertEquals("http://127.0.0.1/", allowing.normalize("http://127.0.0.1/").toString());
    SafeAddressGuard byAddress = new SafeAddressGuard(policy().exemptAddresses(Set.of(address(127, 0, 0, 1))).build());
    assertEquals("http://127.0.0.1/", byAddress.normalize("http://127.0.0.1/").toString());
    URI otherLoopback = URI.create("http://127.0.0.2/");
    assertEquals(SafeFetchFailure.REFUSED_ADDRESS,
                 assertThrows(SafeFetchException.class, () -> byAddress.checkTarget(otherLoopback)).getFailure());
  }

  /**
   * A policy refuses what no fetch may run under: a scheme the client does not
   * speak, an empty scheme or port set, a port outside 1-65535, a non-positive
   * limit, count or timeout, a per-route bound above the total.
   */
  @Test
  void aPolicyRefusesWhatNoFetchMayRunUnder() {
    SafeFetchPolicyBuilder builder = SafeFetchPolicy.builder();
    Set<String> ftp = Set.of("ftp");
    Set<String> noScheme = Set.of();
    Set<Integer> noPort = Set.of();
    Set<Integer> portZero = Set.of(0);
    Set<Integer> portAbove = Set.of(65536);
    Duration negative = Duration.ofSeconds(-1);
    assertThrows(IllegalArgumentException.class, () -> builder.allowedSchemes(ftp));
    assertThrows(IllegalArgumentException.class, () -> builder.allowedSchemes(noScheme));
    assertThrows(IllegalArgumentException.class, () -> builder.allowedPorts(noPort));
    assertThrows(IllegalArgumentException.class, () -> builder.allowedPorts(portZero));
    assertThrows(IllegalArgumentException.class, () -> builder.allowedPorts(portAbove));
    assertThrows(IllegalArgumentException.class, () -> builder.maxBytes(0));
    assertThrows(IllegalArgumentException.class, () -> builder.maxRedirects(-1));
    assertThrows(IllegalArgumentException.class, () -> builder.connectTimeout(Duration.ZERO));
    assertThrows(IllegalArgumentException.class, () -> builder.readTimeout(negative));
    assertThrows(IllegalArgumentException.class, () -> builder.totalTimeout(null));
    assertThrows(IllegalArgumentException.class, () -> builder.maxConnections(2, 3));
    assertThrows(IllegalArgumentException.class, () -> builder.name(" "));
    assertThrows(IllegalArgumentException.class, () -> builder.userAgent(""));
    SafeFetchPolicy policy = builder.allowedSchemes(Set.of("HTTPS", "http")).acceptedContentTypes(Set.of("Image/PNG; q=1", " ")).build();
    assertEquals(Set.of("https", "http"), policy.getAllowedSchemes());
    assertEquals(Set.of("image/png"), policy.getAcceptedContentTypes());
    assertEquals(SafeFetchPolicy.DEFAULT_PORTS, policy.getAllowedPorts());
    assertFalse(policy.isInternalAddressesAllowed());
    assertTrue(policy.getExemptHosts().isEmpty());
    assertTrue(policy.getExemptAddresses().isEmpty());
  }

}
