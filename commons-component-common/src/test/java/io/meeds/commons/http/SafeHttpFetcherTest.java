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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Runs the fetcher against a stub HTTP server in the test JVM — nothing leaves
 * the machine.
 * <p>
 * <b>How a loopback server stands in for a public one.</b> The server listens
 * on 127.0.0.1. Host names are resolved from a table, and the policy exempts
 * exactly 127.0.0.1 from the internal-address refusal; every other internal
 * address — 127.0.0.2, 10.0.0.5 — stays refused. A refused name therefore
 * resolves to such an address, and the stub's request log proves no request
 * reached it: on the machine 127.0.0.2 either has no listener at all or reaches
 * nothing but this stub, and the log is empty either way. One scenario exempts
 * by name instead, so that the reachable stub itself is refused under a name
 * that is not exempt: the refusal then happens on the connection's own lookup
 * and nothing else.
 */
class SafeHttpFetcherTest {

  private static final String              BODY     = "BEGIN:VCALENDAR\r\nVERSION:2.0\r\nEND:VCALENDAR\r\n";

  private final Map<String, InetAddress[]> dns      = new HashMap<>();

  private final List<String>               hits     = new CopyOnWriteArrayList<>();

  private final List<Headers>              requests = new CopyOnWriteArrayList<>();

  private volatile HttpHandler             handler;

  private HttpServer                       server;

  private ExecutorService                  executor;

  private InetAddress                      stub;

  private int                              port;

  private SafeHttpFetcher                  fetcher;

  /**
   * Starts the stub on loopback and maps the test names.
   *
   * @throws Exception when the server cannot start
   */
  @BeforeEach
  void startServer() throws Exception {
    stub = InetAddress.getByAddress("public.test", new byte[] { 127, 0, 0, 1 });
    server = HttpServer.create(new InetSocketAddress(stub, 0), 0);
    executor = Executors.newCachedThreadPool();
    server.setExecutor(executor);
    server.createContext("/", exchange -> {
      hits.add(exchange.getRequestURI().getPath());
      requests.add(exchange.getRequestHeaders());
      try {
        handler.handle(exchange);
      } catch (IOException e) {
        // the client went away, as a limit test intends
      } finally {
        exchange.close();
      }
    });
    server.start();
    port = server.getAddress().getPort();
    dns.put("public.test", new InetAddress[] { stub });
    dns.put("private.test", new InetAddress[] { InetAddress.getByAddress(new byte[] { 127, 0, 0, 2 }) });
    dns.put("internal.test", new InetAddress[] { InetAddress.getByAddress(new byte[] { 10, 0, 0, 5 }) });
    dns.put("metadata.test", new InetAddress[] { InetAddress.getByAddress(new byte[] { (byte) 169, (byte) 254, (byte) 169, (byte) 254 }) });
    fetcher = new SafeHttpFetcher(policy().build());
  }

  /**
   * Stops the stub and the fetcher.
   */
  @AfterEach
  void stopServer() {
    fetcher.close();
    server.stop(0);
    executor.shutdownNow();
  }

  /**
   * A policy over the table of names, exempting the stub's address, with short
   * bounds: 1 MB, 2 s to connect and read, 5 s in all, 3 redirects.
   *
   * @return the builder, for a scenario to tighten
   */
  private SafeFetchPolicyBuilder policy() {
    return SafeFetchPolicy.builder()
                          .name("test-fetcher")
                          .allowedPorts(Set.of(port))
                          .resolver(this::resolve)
                          .exemptAddresses(Set.of(stub))
                          .maxBytes(1024L * 1024)
                          .connectTimeout(Duration.ofSeconds(2))
                          .readTimeout(Duration.ofSeconds(2))
                          .totalTimeout(Duration.ofSeconds(5))
                          .maxRedirects(3);
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
   * A URL on a test name, at the stub's port.
   *
   * @param host the test name
   * @param path the path
   * @return the URL
   */
  private URI url(String host, String path) {
    return URI.create("http://" + host + ":" + port + path);
  }

  /**
   * Answers a body.
   *
   * @param exchange the exchange
   * @param status the status
   * @param body the body
   * @throws IOException when the client went away
   */
  private static void answer(HttpExchange exchange, int status, String body) throws IOException {
    byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
    exchange.sendResponseHeaders(status, bytes.length == 0 ? -1 : bytes.length);
    if (bytes.length > 0) {
      try (OutputStream output = exchange.getResponseBody()) {
        output.write(bytes);
      }
    }
  }

  /**
   * The reason a read fails.
   *
   * @param fetcherUsed the fetcher
   * @param request the read
   * @return the reason
   */
  private static SafeFetchException failure(SafeHttpFetcher fetcherUsed, SafeFetchRequest request) {
    return assertThrows(SafeFetchException.class, () -> fetcherUsed.fetch(request));
  }

  /**
   * The reason a plain read fails.
   *
   * @param fetcherUsed the fetcher
   * @param uri the URL
   * @return the reason
   */
  private static SafeFetchFailure failure(SafeHttpFetcher fetcherUsed, URI uri) {
    return failure(fetcherUsed, SafeFetchRequest.get(uri)).getFailure();
  }

  /**
   * A URL answering a body gives it with its status, headers — looked up
   * whatever their case — content type and the URL it came from.
   *
   * @throws Exception when the read fails
   */
  @Test
  void aBodyIsReadWithItsHeaders() throws Exception {
    handler = exchange -> {
      exchange.getResponseHeaders().add("ETag", "\"v1\"");
      exchange.getResponseHeaders().add("Content-Type", "text/calendar; charset=utf-8");
      answer(exchange, 200, BODY);
    };

    SafeFetchResponse response = fetcher.fetch(url("public.test", "/cal.ics"));

    assertEquals(200, response.status());
    assertFalse(response.notModified());
    assertFalse(response.truncated());
    assertArrayEquals(BODY.getBytes(StandardCharsets.UTF_8), response.body());
    assertEquals("\"v1\"", response.header("etag"));
    assertEquals("text/calendar; charset=utf-8", response.contentType());
    assertEquals("text/calendar", response.mediaType());
    assertEquals(url("public.test", "/cal.ics"), response.uri());
    assertNull(response.header("X-Missing"));
  }

  /**
   * The validators of the previous read are sent, and a 304 is read as nothing
   * changed, with the headers it carries.
   *
   * @throws Exception when the read fails
   */
  @Test
  void aConditionalReadSendsTheValidatorsAndReadsNotModified() throws Exception {
    handler = exchange -> {
      Headers headers = exchange.getRequestHeaders();
      if ("\"v1\"".equals(headers.getFirst("If-None-Match"))
          && "Mon, 14 Sep 2026 10:00:00 GMT".equals(headers.getFirst("If-Modified-Since"))) {
        exchange.getResponseHeaders().add("ETag", "\"v1\"");
        answer(exchange, 304, "");
      } else {
        answer(exchange, 200, BODY);
      }
    };

    SafeFetchResponse response = fetcher.fetch(SafeFetchRequest.get(url("public.test", "/cal.ics"))
                                                               .withValidators("\"v1\"", "Mon, 14 Sep 2026 10:00:00 GMT"));

    assertTrue(response.notModified(), "the server said nothing changed");
    assertEquals(304, response.status());
    assertNull(response.body());
    assertEquals("\"v1\"", response.header("ETag"));
  }

  /**
   * Nothing of the platform's goes out: a cookie set by a server is never sent
   * back, no credentials are sent, and only the headers the request names are
   * added, with the policy's User-Agent.
   *
   * @throws Exception when a read fails
   */
  @Test
  void noCookieAndNoCredentialsAreSent() throws Exception {
    handler = exchange -> {
      exchange.getResponseHeaders().add("Set-Cookie", "session=secret; Path=/");
      answer(exchange, 200, BODY);
    };

    fetcher.fetch(url("public.test", "/cal.ics"));
    fetcher.fetch(SafeFetchRequest.get(url("public.test", "/cal.ics")).withAccept("text/calendar"));

    assertEquals(2, requests.size());
    for (Headers headers : requests) {
      assertNull(headers.getFirst("Cookie"), "no cookie is sent back");
      assertNull(headers.getFirst("Authorization"), "no credentials are sent");
      assertNull(headers.getFirst("Proxy-Authorization"), "no proxy credentials are sent");
      assertEquals(SafeFetchPolicy.DEFAULT_USER_AGENT, headers.getFirst("User-Agent"));
    }
    assertNull(requests.get(0).getFirst("Accept"));
    assertEquals("text/calendar", requests.get(1).getFirst("Accept"));
  }

  /**
   * A redirect to an internal address is refused, and the request never reaches
   * it: the stub saw the first request only. The metadata address and a private
   * address alike.
   */
  @Test
  void aRedirectToAnInternalAddressIsRefusedAndNeverReached() {
    for (String target : new String[] { "private.test", "internal.test", "metadata.test" }) {
      hits.clear();
      handler = exchange -> {
        if ("/start".equals(exchange.getRequestURI().getPath())) {
          exchange.getResponseHeaders().add("Location", "http://" + target + ":" + port + "/secret");
          answer(exchange, 302, "");
        } else {
          answer(exchange, 200, BODY);
        }
      };

      assertEquals(SafeFetchFailure.REFUSED_ADDRESS, failure(fetcher, url("public.test", "/start")), target);
      assertEquals(List.of("/start"), hits, "the redirect target must never be requested: " + target);
    }
  }

  /**
   * A redirect whose target breaks the URL rules — a port outside the allowed
   * set, a scheme other than http, credentials — is refused like a typed URL
   * would be, and never requested; a redirect without a Location is an error.
   */
  @Test
  void aRedirectOutsideTheRulesIsRefusedAndNeverReached() {
    handler = exchange -> {
      String requested = exchange.getRequestURI().getPath();
      if ("/port".equals(requested)) {
        exchange.getResponseHeaders().add("Location", "http://public.test:22/secret");
        answer(exchange, 302, "");
      } else if ("/scheme".equals(requested)) {
        exchange.getResponseHeaders().add("Location", "file:///etc/passwd");
        answer(exchange, 302, "");
      } else if ("/credentials".equals(requested)) {
        exchange.getResponseHeaders().add("Location", "http://user:secret@public.test:" + port + "/secret");
        answer(exchange, 302, "");
      } else if ("/nowhere".equals(requested)) {
        answer(exchange, 302, "");
      } else {
        answer(exchange, 200, BODY);
      }
    };

    assertEquals(SafeFetchFailure.PORT_NOT_ALLOWED, failure(fetcher, url("public.test", "/port")));
    assertEquals(SafeFetchFailure.SCHEME_NOT_ALLOWED, failure(fetcher, url("public.test", "/scheme")));
    assertEquals(SafeFetchFailure.CREDENTIALS_IN_URL, failure(fetcher, url("public.test", "/credentials")));
    SafeFetchException nowhere = failure(fetcher, SafeFetchRequest.get(url("public.test", "/nowhere")));
    assertEquals(SafeFetchFailure.HTTP_ERROR, nowhere.getFailure());
    assertEquals(302, nowhere.getStatus());
    assertEquals(List.of("/port", "/scheme", "/credentials", "/nowhere"), hits, "no redirect target may be requested");
  }

  /**
   * The control of the refusals above: the same redirect, to an address that is
   * allowed, is followed, a relative Location resolved against the URL that
   * answered, and the answer names the URL it came from.
   *
   * @throws Exception when the read fails
   */
  @Test
  void aRedirectToAnAllowedAddressIsFollowed() throws Exception {
    dns.put("private.test", new InetAddress[] { stub });
    handler = exchange -> {
      if ("/start".equals(exchange.getRequestURI().getPath())) {
        exchange.getResponseHeaders().add("Location", "http://private.test:" + port + "/secret");
        answer(exchange, 302, "");
      } else if ("/secret".equals(exchange.getRequestURI().getPath())) {
        exchange.getResponseHeaders().add("Location", "/landing");
        answer(exchange, 301, "");
      } else {
        answer(exchange, 200, BODY);
      }
    };

    SafeFetchResponse response = fetcher.fetch(url("public.test", "/start"));

    assertEquals(List.of("/start", "/secret", "/landing"), hits);
    assertEquals(url("private.test", "/landing"), response.uri());
    assertArrayEquals(BODY.getBytes(StandardCharsets.UTF_8), response.body());
  }

  /**
   * A name that resolved to an allowed address for one connection and to an
   * internal one for the next — DNS rebinding — is refused at that next
   * connection: the address the client dials is the one judged, every time, and
   * the first answer is never trusted for the second. The stub closes each
   * connection so that the second read must dial again.
   *
   * @throws Exception when the first read fails
   */
  @Test
  void aNameRebindingToAnInternalAddressIsRefusedAtConnection() throws Exception {
    InetAddress internal = InetAddress.getByAddress(new byte[] { 127, 0, 0, 2 });
    AtomicInteger lookups = new AtomicInteger();
    handler = exchange -> {
      exchange.getResponseHeaders().add("Connection", "close");
      answer(exchange, 200, BODY);
    };
    try (SafeHttpFetcher rebinding = new SafeHttpFetcher(policy().resolver(host -> {
      if (!"rebind.test".equals(host)) {
        throw new UnknownHostException(host);
      }
      return lookups.incrementAndGet() == 1 ? new InetAddress[] { stub } : new InetAddress[] { internal };
    }).build())) {
      assertArrayEquals(BODY.getBytes(StandardCharsets.UTF_8), rebinding.fetch(url("rebind.test", "/cal.ics")).body());
      assertEquals(1, lookups.get(), "one connection, one lookup");
      assertEquals(SafeFetchFailure.REFUSED_ADDRESS, failure(rebinding, url("rebind.test", "/cal.ics")));
    }
    assertEquals(2, lookups.get(), "the second connection must resolve the name again rather than trust the first answer");
    assertEquals(List.of("/cal.ics"), hits, "no request may reach the stub through a name judged internal");
  }

  /**
   * The reachable stub itself, under a name that is not exempt, is refused by
   * the lookup the connection makes: nothing but the guard inside the client
   * stands between the request and an answer, and the stub never sees it.
   */
  @Test
  void aReachableLoopbackUnderANonExemptNameIsRefusedAtConnection() {
    dns.put("loopback.test", new InetAddress[] { stub });
    handler = exchange -> answer(exchange, 200, BODY);
    try (SafeHttpFetcher byName = new SafeHttpFetcher(policy().exemptAddresses(Set.of()).exemptHosts(Set.of("public.test")).build())) {
      assertEquals(SafeFetchFailure.REFUSED_ADDRESS, failure(byName, url("loopback.test", "/cal.ics")));
      assertTrue(hits.isEmpty(), "the stub must never see the request");
      assertDoesNotThrowFetch(byName, url("public.test", "/cal.ics"));
    }
    assertEquals(List.of("/cal.ics"), hits);
  }

  /**
   * Reads a URL that must succeed.
   *
   * @param fetcherUsed the fetcher
   * @param uri the URL
   */
  private static void assertDoesNotThrowFetch(SafeHttpFetcher fetcherUsed, URI uri) {
    try {
      fetcherUsed.fetch(uri);
    } catch (SafeFetchException e) {
      throw new AssertionError("the read must succeed: " + e.getFailure(), e);
    }
  }

  /**
   * A name resolving to a private address is refused before any request, and
   * without waiting for a connection.
   */
  @Test
  void aNameResolvingToAPrivateAddressIsRefusedBeforeAnyRequest() {
    handler = exchange -> answer(exchange, 200, BODY);
    long start = System.nanoTime();

    assertEquals(SafeFetchFailure.REFUSED_ADDRESS, failure(fetcher, url("internal.test", "/cal.ics")));

    assertTrue(Duration.ofNanos(System.nanoTime() - start).toMillis() < 1500, "a refusal must not wait for a connection");
    assertTrue(hits.isEmpty());
  }

  /**
   * The deployment's opt-out lets an internal address be read.
   *
   * @throws Exception when the read fails
   */
  @Test
  void internalAddressesAreReadWhenTheDeploymentAllowsThem() throws Exception {
    dns.put("loopback.test", new InetAddress[] { InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 }) });
    handler = exchange -> answer(exchange, 200, BODY);
    try (SafeHttpFetcher closed = new SafeHttpFetcher(policy().exemptAddresses(Set.of()).build());
         SafeHttpFetcher open = new SafeHttpFetcher(policy().exemptAddresses(Set.of()).internalAddressesAllowed(true).build())) {
      assertEquals(SafeFetchFailure.REFUSED_ADDRESS, failure(closed, url("loopback.test", "/cal.ics")));
      assertTrue(hits.isEmpty());
      assertArrayEquals(BODY.getBytes(StandardCharsets.UTF_8), open.fetch(url("loopback.test", "/cal.ics")).body());
    }
  }

  /**
   * A name resolving to nothing is reported as such, apart from a refusal.
   */
  @Test
  void anUnknownNameIsUnresolvable() {
    assertEquals(SafeFetchFailure.UNRESOLVABLE, failure(fetcher, url("nowhere.test", "/cal.ics")));
    assertTrue(hits.isEmpty());
  }

  /**
   * A redirect loop stops at the limit, and a policy following no redirect
   * stops at the first.
   */
  @Test
  void redirectsStopAtTheLimit() {
    handler = exchange -> {
      exchange.getResponseHeaders().add("Location", "/loop");
      answer(exchange, 302, "");
    };

    assertEquals(SafeFetchFailure.TOO_MANY_REDIRECTS, failure(fetcher, url("public.test", "/loop")));
    assertEquals(4, hits.size(), "the first request and three redirects, then nothing");

    hits.clear();
    try (SafeHttpFetcher none = new SafeHttpFetcher(policy().maxRedirects(0).build())) {
      assertEquals(SafeFetchFailure.TOO_MANY_REDIRECTS, failure(none, url("public.test", "/loop")));
    }
    assertEquals(1, hits.size(), "no redirect is followed");
  }

  /**
   * A body over the limit is refused, announced or streamed, under the policy's
   * limit or the request's own; a request asking for more than the policy
   * allows is held to the policy's.
   */
  @Test
  void aBodyOverTheLimitIsRefused() {
    try (SafeHttpFetcher small = new SafeHttpFetcher(policy().maxBytes(1024).build())) {
      handler = exchange -> answer(exchange, 200, "X".repeat(2048));
      assertEquals(SafeFetchFailure.TOO_LARGE, failure(small, url("public.test", "/announced")));
      assertEquals(SafeFetchFailure.TOO_LARGE, failure(fetcher, SafeFetchRequest.get(url("public.test", "/announced")).withMaxBytes(1024)).getFailure());

      handler = exchange -> {
        exchange.sendResponseHeaders(200, 0);
        try (OutputStream output = exchange.getResponseBody()) {
          for (int i = 0; i < 8; i++) {
            output.write("Y".repeat(512).getBytes(StandardCharsets.UTF_8));
            output.flush();
          }
        }
      };
      assertEquals(SafeFetchFailure.TOO_LARGE, failure(small, url("public.test", "/streamed")));
      assertEquals(SafeFetchFailure.TOO_LARGE, failure(fetcher, SafeFetchRequest.get(url("public.test", "/streamed")).withMaxBytes(1024)).getFailure());

      SafeFetchRequest wider = SafeFetchRequest.get(url("public.test", "/streamed")).withMaxBytes(1024L * 1024);
      assertEquals(SafeFetchFailure.TOO_LARGE, failure(small, wider).getFailure(), "a request cannot raise the policy's limit");

      handler = exchange -> answer(exchange, 200, "X".repeat(2048));
      SafeFetchRequest widerAnnounced = SafeFetchRequest.get(url("public.test", "/announced")).withMaxBytes(1024L * 1024);
      assertEquals(SafeFetchFailure.TOO_LARGE, failure(small, widerAnnounced).getFailure(), "a request cannot raise the policy's limit");

      handler = exchange -> answer(exchange, 200, "Z".repeat(1024));
      assertDoesNotThrowFetch(small, url("public.test", "/exact"));
    }
  }

  /**
   * A body over the limit is cut at it, and returned as such, when the request
   * allows it: for a page whose head comes first. Announced or streamed, and
   * the rest is never downloaded.
   *
   * @throws Exception when a read fails
   */
  @Test
  void aBodyOverTheLimitIsCutWhenTheRequestAllowsIt() throws Exception {
    AtomicLong written = new AtomicLong();
    handler = exchange -> {
      exchange.sendResponseHeaders(200, 1L << 30);
      byte[] chunk = new byte[64 * 1024];
      try (OutputStream output = exchange.getResponseBody()) {
        while (written.get() < (1L << 30)) {
          output.write(chunk);
          written.addAndGet(chunk.length);
        }
      }
    };

    SafeFetchResponse response = fetcher.fetch(SafeFetchRequest.get(url("public.test", "/page")).withMaxBytes(1500).truncatedAtLimit());

    assertTrue(response.truncated());
    assertEquals(1500, response.body().length);
    sleep(300);
    assertTrue(written.get() < 16L * 1024 * 1024, "the rest of the body was downloaded: " + written.get() + " bytes");

    handler = exchange -> answer(exchange, 200, "P".repeat(100));
    response = fetcher.fetch(SafeFetchRequest.get(url("public.test", "/short")).withMaxBytes(1500).truncatedAtLimit());
    assertFalse(response.truncated());
    assertEquals(100, response.body().length);
  }

  /**
   * A body refused as too large by its declared length, an error page, or a body
   * of a refused type is not downloaded: the exchange is aborted before the
   * client closes the response, which would otherwise drain the body to its
   * declared end.
   */
  @Test
  void aRefusedAnswerIsNotDownloaded() {
    AtomicLong written = new AtomicLong();
    AtomicInteger status = new AtomicInteger(200);
    handler = exchange -> {
      exchange.getResponseHeaders().add("Content-Type", "text/html");
      exchange.sendResponseHeaders(status.get(), 1L << 30);
      byte[] chunk = new byte[64 * 1024];
      try (OutputStream output = exchange.getResponseBody()) {
        while (written.get() < (1L << 30)) {
          output.write(chunk);
          written.addAndGet(chunk.length);
        }
      }
    };

    assertEquals(SafeFetchFailure.TOO_LARGE, failure(fetcher, url("public.test", "/huge")));
    sleep(300);
    assertTrue(written.get() < 16L * 1024 * 1024, "the refused body was downloaded: " + written.get() + " bytes");

    written.set(0);
    status.set(500);
    assertEquals(SafeFetchFailure.HTTP_ERROR, failure(fetcher, url("public.test", "/error")));
    sleep(300);
    assertTrue(written.get() < 16L * 1024 * 1024, "the error page was downloaded: " + written.get() + " bytes");

    written.set(0);
    status.set(200);
    assertEquals(SafeFetchFailure.CONTENT_TYPE_NOT_ALLOWED,
                 failure(fetcher, SafeFetchRequest.get(url("public.test", "/typed")).withAcceptedContentTypes(Set.of("image/png"))).getFailure());
    sleep(300);
    assertTrue(written.get() < 16L * 1024 * 1024, "the refused type was downloaded: " + written.get() + " bytes");
  }

  /**
   * The declared type must be in the accepted set when there is one — the
   * policy's, or the request's within it — parameters and case ignored; an
   * answer declaring none is refused then; without a set, anything is read.
   *
   * @throws Exception when a read fails
   */
  @Test
  void aDeclaredTypeOutsideTheAcceptedSetIsRefused() throws Exception {
    AtomicInteger variant = new AtomicInteger();
    handler = exchange -> {
      switch (variant.get()) {
      case 0 -> exchange.getResponseHeaders().add("Content-Type", "Image/PNG; charset=binary");
      case 1 -> exchange.getResponseHeaders().add("Content-Type", "text/html");
      default -> {
        // no Content-Type at all
      }
      }
      answer(exchange, 200, BODY);
    };
    try (SafeHttpFetcher images = new SafeHttpFetcher(policy().acceptedContentTypes(Set.of("image/png", "image/svg+xml")).build())) {
      assertDoesNotThrowFetch(images, url("public.test", "/png"));
      assertEquals(SafeFetchFailure.CONTENT_TYPE_NOT_ALLOWED,
                   failure(images, SafeFetchRequest.get(url("public.test", "/png")).withAcceptedContentTypes(Set.of("image/svg+xml"))).getFailure(),
                   "a request narrows the policy's types");
      variant.set(1);
      assertEquals(SafeFetchFailure.CONTENT_TYPE_NOT_ALLOWED, failure(images, url("public.test", "/html")));
      assertDoesNotThrowFetch(fetcher, url("public.test", "/html"));
      fetcher.fetch(SafeFetchRequest.get(url("public.test", "/html")).withAcceptedContentTypes(Set.of("text/html")));
      SafeFetchRequest wider = SafeFetchRequest.get(url("public.test", "/html")).withAcceptedContentTypes(Set.of("text/html", "image/png"));
      assertEquals(SafeFetchFailure.CONTENT_TYPE_NOT_ALLOWED, failure(images, wider).getFailure(), "a request cannot widen the policy's types");
      variant.set(2);
      assertEquals(SafeFetchFailure.CONTENT_TYPE_NOT_ALLOWED, failure(images, url("public.test", "/untyped")));
      assertNull(fetcher.fetch(url("public.test", "/untyped")).mediaType());
    }
  }

  /**
   * An error status is reported as such, with the status.
   */
  @Test
  void anErrorStatusIsReportedWithItsStatus() {
    handler = exchange -> answer(exchange, 500, "oops");
    SafeFetchException error = failure(fetcher, SafeFetchRequest.get(url("public.test", "/error")));
    assertEquals(SafeFetchFailure.HTTP_ERROR, error.getFailure());
    assertEquals(500, error.getStatus());
    assertTrue(error.getMessage().contains("500"));
    handler = exchange -> answer(exchange, 404, "");
    assertEquals(404, failure(fetcher, SafeFetchRequest.get(url("public.test", "/missing"))).getStatus());
    assertEquals(-1, failure(fetcher, SafeFetchRequest.get(url("nowhere.test", "/"))).getStatus());
  }

  /**
   * A 2xx answer without a body is read as an empty body, not as a failure.
   *
   * @throws Exception when the read fails
   */
  @Test
  void anAnswerWithoutABodyIsEmpty() throws Exception {
    handler = exchange -> answer(exchange, 204, "");
    SafeFetchResponse response = fetcher.fetch(url("public.test", "/empty"));
    assertEquals(204, response.status());
    assertEquals(0, response.body().length);
  }

  /**
   * A server that does not answer in time gives a timeout.
   */
  @Test
  void aSilentServerTimesOut() {
    handler = exchange -> {
      sleep(2000);
      answer(exchange, 200, BODY);
    };
    try (SafeHttpFetcher impatient = new SafeHttpFetcher(policy().readTimeout(Duration.ofMillis(300)).build())) {
      assertEquals(SafeFetchFailure.TIMEOUT, failure(impatient, url("public.test", "/slow")));
    }
  }

  /**
   * A server trickling bytes, each within the read timeout, is stopped by the
   * deadline over the whole read.
   */
  @Test
  void aTricklingServerIsStoppedByTheDeadline() {
    handler = exchange -> {
      exchange.sendResponseHeaders(200, 0);
      try (OutputStream output = exchange.getResponseBody()) {
        for (int i = 0; i < 30; i++) {
          output.write('B');
          output.flush();
          sleep(100);
        }
      }
    };
    long start = System.nanoTime();
    try (SafeHttpFetcher bounded = new SafeHttpFetcher(policy().readTimeout(Duration.ofSeconds(1)).totalTimeout(Duration.ofMillis(700)).build())) {
      assertEquals(SafeFetchFailure.TIMEOUT, failure(bounded, url("public.test", "/trickle")));
    }
    assertTrue(Duration.ofNanos(System.nanoTime() - start).toMillis() < 2500, "the deadline must cut the read short");
  }

  /**
   * A server silent past the deadline, yet within the read timeout, is cut off
   * at the deadline: the deadline is armed as a timer that cancels the request,
   * it does not wait for the next byte or the read timeout to notice. The
   * read timeout is three times the deadline and the server answers after
   * twice the deadline, so only the timer can end the wait in time.
   */
  @Test
  void aServerSilentPastTheDeadlineIsCutOffByTheTimer() {
    handler = exchange -> {
      sleep(1000);
      answer(exchange, 200, BODY);
    };
    long start = System.nanoTime();
    try (SafeHttpFetcher bounded = new SafeHttpFetcher(policy().readTimeout(Duration.ofSeconds(3)).totalTimeout(Duration.ofMillis(500)).build())) {
      assertEquals(SafeFetchFailure.TIMEOUT, failure(bounded, url("public.test", "/silent")));
    }
    long elapsed = Duration.ofNanos(System.nanoTime() - start).toMillis();
    assertTrue(elapsed < 900, "the timer must cancel the request at the deadline, not wait for the answer: " + elapsed + " ms");
  }

  /**
   * A URL outside the rules is refused before any request, by the same guard a
   * caller may apply beforehand.
   */
  @Test
  void aUrlOutsideTheRulesIsRefusedBeforeAnyRequest() {
    handler = exchange -> answer(exchange, 200, BODY);
    assertEquals(SafeFetchFailure.PORT_NOT_ALLOWED, failure(fetcher, URI.create("http://public.test:22/")));
    assertEquals(SafeFetchFailure.SCHEME_NOT_ALLOWED, failure(fetcher, URI.create("ftp://public.test:" + port + "/")));
    assertEquals(SafeFetchFailure.CREDENTIALS_IN_URL, failure(fetcher, URI.create("http://u:p@public.test:" + port + "/")));
    assertEquals(SafeFetchFailure.INVALID_URL, failure(fetcher, URI.create("relative")));
    assertTrue(hits.isEmpty());
    assertEquals(fetcher.getPolicy().getAllowedPorts(), Set.of(port));
    assertThrows(SafeFetchException.class, () -> fetcher.getGuard().normalize("http://public.test:22/"));
  }

  /**
   * A 304 answering a read that sent no validator is an error status, not a
   * not-modified answer without a body: only a conditional read can be told
   * that nothing changed.
   */
  @Test
  void aNotModifiedAnswerToAnUnconditionalReadIsAnError() {
    handler = exchange -> answer(exchange, 304, "");

    SafeFetchException unconditional = failure(fetcher, SafeFetchRequest.get(url("public.test", "/cal.ics")));

    assertEquals(SafeFetchFailure.HTTP_ERROR, unconditional.getFailure());
    assertEquals(304, unconditional.getStatus());
  }

  /**
   * Either validator alone makes a read conditional, so that a 304 then means
   * nothing changed.
   *
   * @throws Exception when a read fails
   */
  @Test
  void eitherValidatorAloneMakesAReadConditional() throws Exception {
    handler = exchange -> answer(exchange, 304, "");

    assertTrue(fetcher.fetch(SafeFetchRequest.get(url("public.test", "/etag")).withValidators("\"v1\"", null)).notModified());
    assertTrue(fetcher.fetch(SafeFetchRequest.get(url("public.test", "/date")).withValidators(null, "Mon, 14 Sep 2026 10:00:00 GMT"))
                      .notModified());
  }

  /**
   * The headers sent are the HTTP client's own and the ones the request names,
   * nothing else.
   *
   * @throws Exception when a read fails
   */
  @Test
  void onlyTheClientsAndTheRequestsHeadersAreSent() throws Exception {
    handler = exchange -> answer(exchange, 200, BODY);

    fetcher.fetch(url("public.test", "/plain"));
    fetcher.fetch(SafeFetchRequest.get(url("public.test", "/named")).withAccept("text/calendar").withValidators("\"v1\"", "Mon, 14 Sep 2026 10:00:00 GMT"));

    Set<String> clientOwn = Set.of("host", "connection", "user-agent", "accept-encoding");
    assertEquals(clientOwn, headerNames(requests.get(0)));
    Set<String> named = new HashSet<>(clientOwn);
    named.addAll(Set.of("accept", "if-none-match", "if-modified-since"));
    assertEquals(named, headerNames(requests.get(1)));
  }

  /**
   * The names of the headers a request carried, lower-cased.
   *
   * @param headers the headers
   * @return the names
   */
  private static Set<String> headerNames(Headers headers) {
    Set<String> names = new HashSet<>();
    headers.keySet().forEach(name -> names.add(name.toLowerCase(Locale.ROOT)));
    return names;
  }

  /**
   * The deadline timer of a read leaves the queue when the read ends, rather
   * than staying queued until its deadline passes.
   *
   * @throws Exception when a read fails
   */
  @Test
  void aReadLeavesNoDeadlineQueued() throws Exception {
    handler = exchange -> answer(exchange, 200, BODY);

    for (int i = 0; i < 3; i++) {
      fetcher.fetch(url("public.test", "/cal.ics"));
    }
    assertThrows(SafeFetchException.class, () -> fetcher.fetch(url("internal.test", "/cal.ics")));

    assertEquals(0, fetcher.pendingDeadlines(), "every timer is removed once its read ended");
  }

  /**
   * A closed fetcher refuses every read with the exception its Javadoc names,
   * a URL the guard would refuse included, and reaches nothing.
   */
  @Test
  void aClosedFetcherRefusesARead() {
    handler = exchange -> answer(exchange, 200, BODY);
    SafeHttpFetcher closing = new SafeHttpFetcher(policy().build());
    closing.close();
    URI uri = url("public.test", "/cal.ics");

    URI refused = url("internal.test", "/cal.ics");
    URI outsideTheRules = URI.create("ftp://public.test/cal.ics");

    assertThrows(IllegalStateException.class, () -> closing.fetch(uri));
    assertThrows(IllegalStateException.class, () -> closing.fetch(refused));
    assertThrows(IllegalStateException.class, () -> closing.fetch(outsideTheRules));
    assertTrue(hits.isEmpty());
  }

  /**
   * An IP literal naming an internal address is refused through the fetcher
   * under the production resolver, the stub's own loopback address included,
   * and nothing reaches the stub: the refusal does not rest on the table of
   * names, which answers a literal with nothing.
   */
  @Test
  void anInternalIpLiteralIsRefusedUnderTheProductionResolver() {
    handler = exchange -> answer(exchange, 200, BODY);
    try (SafeHttpFetcher production = new SafeHttpFetcher(policy().exemptAddresses(Set.of()).resolver(InetAddress::getAllByName).build())) {
      for (String host : new String[] { "127.0.0.1", "[::1]", "169.254.169.254", "[::ffff:127.0.0.1]", "10.0.0.5" }) {
        URI literal = URI.create("http://" + host + ":" + port + "/cal.ics");
        assertEquals(SafeFetchFailure.REFUSED_ADDRESS, failure(production, literal), host);
      }
    }
    assertTrue(hits.isEmpty(), "no request may reach the stub through an internal literal");
  }

  /**
   * The HTTP client hands an IP-literal host to the guarded resolver when it
   * opens the connection, so the literal is judged at the connection as well
   * as before the request. Pins the client's behaviour: a client version
   * resolving literals on its own would make the resolver see nothing here.
   *
   * @throws Exception when the read fails
   */
  @Test
  void anIpLiteralHostIsResolvedThroughTheGuardAtConnection() throws Exception {
    List<String> lookups = new CopyOnWriteArrayList<>();
    handler = exchange -> answer(exchange, 200, BODY);
    try (SafeHttpFetcher counting = new SafeHttpFetcher(policy().resolver(host -> {
      lookups.add(host);
      return InetAddress.getAllByName(host);
    }).build())) {
      counting.fetch(URI.create("http://127.0.0.1:" + port + "/cal.ics"));
    }
    assertEquals(List.of("127.0.0.1"), lookups);
    assertEquals(List.of("/cal.ics"), hits);
  }

  /**
   * The validators a read gives can always be sent back on the next read: an
   * {@code ETag} holding a tab — white space a field value may carry — is
   * handed out and sent back, and a value carrying another control character
   * is not handed out at all, the {@code Last-Modified} beside it kept.
   *
   * @throws Exception when a read fails
   */
  @Test
  void theValidatorsAReadGivesCanBeSentBack() throws Exception {
    handler = exchange -> {
      exchange.getResponseHeaders().add("ETag", "\"a\tb\"");
      exchange.getResponseHeaders().add("X-Control", "a\u0001b");
      exchange.getResponseHeaders().add("Last-Modified", "Mon, 14 Sep 2026 10:00:00 GMT");
      answer(exchange, 200, BODY);
    };

    SafeFetchResponse first = fetcher.fetch(url("public.test", "/cal.ics"));
    assertEquals("\"a\tb\"", first.header("ETag"));
    assertNull(first.header("X-Control"));
    assertEquals("Mon, 14 Sep 2026 10:00:00 GMT", first.header("Last-Modified"));

    fetcher.fetch(SafeFetchRequest.get(url("public.test", "/cal.ics")).withValidators(first.header("ETag"), first.header("Last-Modified")));
    assertEquals(2, requests.size());
    // the tab is white space, which either side of the wire may fold to a space
    assertTrue(requests.get(1).getFirst("If-None-Match").matches("\"a[\t ]b\""));
    assertEquals("Mon, 14 Sep 2026 10:00:00 GMT", requests.get(1).getFirst("If-Modified-Since"));
  }

  /**
   * A {@code Content-Type} holding a tab before its parameters is read as its
   * media type, and the read is accepted.
   *
   * @throws Exception when the read fails
   */
  @Test
  void aContentTypeHoldingATabIsAccepted() throws Exception {
    handler = exchange -> {
      exchange.getResponseHeaders().add("Content-Type", "text/calendar;\tcharset=UTF-8");
      answer(exchange, 200, BODY);
    };

    SafeFetchResponse response = fetcher.fetch(SafeFetchRequest.get(url("public.test", "/cal.ics"))
                                                               .withAcceptedContentTypes(Set.of("text/calendar")));

    assertEquals(200, response.status());
    assertEquals("text/calendar", response.mediaType());
  }

  /**
   * A fetcher closed while a read is under way refuses the read's next hop with
   * the exception and message its Javadoc names, and the redirect is never
   * requested: the policy's resolver closes the fetcher while the first hop
   * opens its connection, after that hop's deadline was armed, so the refusal
   * comes from arming the next hop's deadline rather than from the check a
   * read makes before it starts.
   */
  @Test
  void aFetcherClosedDuringAReadRefusesItsNextHop() {
    handler = exchange -> {
      exchange.getResponseHeaders().add("Location", "/landing");
      answer(exchange, 302, "");
    };
    AtomicReference<SafeHttpFetcher> closedByItsResolver = new AtomicReference<>();
    SafeHttpFetcher closing = new SafeHttpFetcher(policy().resolver(host -> {
      closedByItsResolver.get().close();
      return resolve(host);
    }).build());
    closedByItsResolver.set(closing);

    URI uri = url("public.test", "/start");
    IllegalStateException refusal = assertThrows(IllegalStateException.class, () -> closing.fetch(uri));
    assertEquals("The fetcher is closed", refusal.getMessage());
    assertEquals(List.of("/start"), hits);
  }

  /**
   * A host of digits and dots that is not a canonical IPv4 literal is refused
   * as an invalid URL through the fetcher under the production resolver, the
   * forms naming the stub's own loopback address included, and nothing reaches
   * the stub: the refusal does not wait for the connection's lookup, which
   * would read {@code 2130706433} as 127.0.0.1.
   */
  @Test
  void aNonCanonicalNumericHostIsRefusedBeforeAnyConnection() {
    handler = exchange -> answer(exchange, 200, BODY);
    try (SafeHttpFetcher production = new SafeHttpFetcher(policy().exemptAddresses(Set.of()).resolver(InetAddress::getAllByName).build())) {
      for (String host : new String[] { "010.0.0.1", "127.000.000.001", "2130706433" }) {
        URI numeric = URI.create("http://" + host + ":" + port + "/cal.ics");
        assertEquals(SafeFetchFailure.INVALID_URL, failure(production, numeric), host);
      }
    }
    assertTrue(hits.isEmpty(), "no request may reach the stub through a numeric host");
  }

  /**
   * Sleeps, interrupted or not.
   *
   * @param millis how long
   */
  private static void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

}
