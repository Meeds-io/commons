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

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.util.Timeout;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Reads a URL the platform was given over HTTP, within the bounds of a
 * {@link SafeFetchPolicy}: the one place to fetch a user-, sender- or
 * agent-controlled URL from the server.
 * <p>
 * <b>Addresses</b> are the {@link SafeAddressGuard}'s: it is the resolver of
 * the connection manager ({@link GuardedDnsResolver}), so the address of every
 * connection is judged when the connection is opened — there is one lookup,
 * and it is the checked one; the guard also checks each URL's shape before it
 * is requested, and redirects are followed here, one hop at a time, each
 * target checked again. <b>Bounds</b>: a connect timeout, a read timeout, a
 * deadline over the whole read — redirects included — enforced by cancelling
 * the request, a body limit counted on the decoded bytes (a compressed body
 * cannot inflate past it), a redirect count, and the declared content type
 * checked before the body is read. A {@link SafeFetchRequest} narrows these
 * bounds, never widens them. <b>Nothing of the platform's goes out</b>: no
 * cookie store, no credentials, no proxy, no retry. The headers sent are the
 * HTTP client's own — {@code Host}, {@code Connection}, the policy's
 * {@code User-Agent} and the {@code Accept-Encoding} of the compressions it
 * decodes — and the ones the request names: {@code Accept},
 * {@code If-None-Match}, {@code If-Modified-Since}. <b>Nothing of the URL goes
 * into a log</b>: it may embed a secret.
 * <p>
 * Thread-safe; one instance per consumer, closed when the consumer goes. A
 * closed fetcher refuses every read with an {@link IllegalStateException}.
 */
public class SafeHttpFetcher implements Closeable {

  private static final Log                  LOG            = ExoLogger.getLogger(SafeHttpFetcher.class);

  private static final String               CLOSED_MESSAGE = "The fetcher is closed";

  private final SafeFetchPolicy             policy;

  private final SafeAddressGuard            guard;

  private final CloseableHttpClient         httpClient;

  private final ScheduledThreadPoolExecutor deadlines;

  /**
   * The fetcher of a policy.
   *
   * @param policy what may be read and how far the fetcher goes
   */
  public SafeHttpFetcher(SafeFetchPolicy policy) {
    this.policy = policy;
    this.guard = new SafeAddressGuard(policy);
    this.httpClient = HttpClients.custom()
                                 .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
                                                                                                .setDnsResolver(new GuardedDnsResolver(guard))
                                                                                                .setDefaultConnectionConfig(ConnectionConfig.custom()
                                                                                                                                            .setConnectTimeout(Timeout.of(policy.getConnectTimeout()))
                                                                                                                                            .setSocketTimeout(Timeout.of(policy.getReadTimeout()))
                                                                                                                                            .build())
                                                                                                .setMaxConnTotal(policy.getMaxConnectionsTotal())
                                                                                                .setMaxConnPerRoute(policy.getMaxConnectionsPerRoute())
                                                                                                .build())
                                 .setDefaultRequestConfig(RequestConfig.custom()
                                                                       .setRedirectsEnabled(false)
                                                                       .setResponseTimeout(Timeout.of(policy.getReadTimeout()))
                                                                       .setConnectionRequestTimeout(Timeout.of(policy.getConnectTimeout()))
                                                                       .build())
                                 .disableRedirectHandling()
                                 .disableCookieManagement()
                                 .disableAuthCaching()
                                 .disableAutomaticRetries()
                                 .setUserAgent(policy.getUserAgent())
                                 .build();
    this.deadlines = new ScheduledThreadPoolExecutor(1, runnable -> {
      Thread thread = new Thread(runnable, policy.getName() + "-deadline");
      thread.setDaemon(true);
      return thread;
    });
    // a read ends long before its deadline: its cancelled timer leaves the
    // queue at once instead of waiting there for the deadline to pass
    this.deadlines.setRemoveOnCancelPolicy(true);
  }

  /**
   * @return the policy this fetcher reads under
   */
  public SafeFetchPolicy getPolicy() {
    return policy;
  }

  /**
   * @return the guard, whose URL rules a caller may apply before a read
   */
  public SafeAddressGuard getGuard() {
    return guard;
  }

  /**
   * Reads a URL with a plain GET under the policy.
   *
   * @param uri the URL
   * @return what the server answered
   * @throws SafeFetchException with the reason nothing usable was read
   * @throws IllegalStateException when the fetcher is closed
   */
  public SafeFetchResponse fetch(URI uri) throws SafeFetchException {
    return fetch(SafeFetchRequest.get(uri));
  }

  /**
   * Reads a URL, following redirects within the policy's count, each target
   * checked by the guard before it is requested and judged again by the
   * resolver when its connection opens.
   *
   * @param request the read
   * @return what the server answered
   * @throws SafeFetchException with the reason nothing usable was read
   * @throws IllegalStateException when the fetcher is closed
   */
  public SafeFetchResponse fetch(SafeFetchRequest request) throws SafeFetchException {
    if (deadlines.isShutdown()) {
      throw new IllegalStateException(CLOSED_MESSAGE);
    }
    long deadline = System.nanoTime() + policy.getTotalTimeout().toNanos();
    URI current = guard.checkTarget(request.uri());
    for (int hop = 0;; hop++) {
      SafeFetchHop answer = request(current, request, deadline);
      if (answer.redirect() == null) {
        return answer.response();
      }
      if (hop >= policy.getMaxRedirects()) {
        throw new SafeFetchException(SafeFetchFailure.TOO_MANY_REDIRECTS);
      }
      current = guard.checkTarget(redirectTarget(current, answer.redirect()));
    }
  }

  /**
   * Stops the deadline thread and closes the connections; a read started
   * afterwards is refused.
   */
  @Override
  public void close() {
    deadlines.shutdownNow();
    try {
      httpClient.close();
    } catch (IOException e) {
      LOG.debug("The {} HTTP client did not close cleanly", policy.getName(), e);
    }
  }

  /**
   * One request of a read: the deadline armed, the answer handled. The address
   * is judged by the resolver when the connection opens, not here, so that the
   * address checked is the address dialled.
   *
   * @param uri the URL of this hop
   * @param request the read
   * @param deadline the read's deadline, as {@link System#nanoTime()}
   * @return the answer: a response, or a redirect to follow
   * @throws SafeFetchException with the reason nothing usable was read
   * @throws IllegalStateException when the fetcher is closed
   */
  private SafeFetchHop request(URI uri, SafeFetchRequest request, long deadline) throws SafeFetchException {
    long remaining = deadline - System.nanoTime();
    if (remaining <= 0) {
      throw new SafeFetchException(SafeFetchFailure.TIMEOUT);
    }
    HttpGet get = new HttpGet(uri);
    if (StringUtils.isNotBlank(request.accept())) {
      get.setHeader(HttpHeaders.ACCEPT, request.accept());
    }
    if (StringUtils.isNotBlank(request.ifNoneMatch())) {
      get.setHeader(HttpHeaders.IF_NONE_MATCH, request.ifNoneMatch());
    }
    if (StringUtils.isNotBlank(request.ifModifiedSince())) {
      get.setHeader(HttpHeaders.IF_MODIFIED_SINCE, request.ifModifiedSince());
    }
    ScheduledFuture<?> timer = null;
    try {
      timer = deadlines.schedule(get::cancel, remaining, TimeUnit.NANOSECONDS);
      return httpClient.execute(get, response -> handle(get, uri, response, request, deadline));
    } catch (SafeFetchException e) {
      throw e;
    } catch (RejectedExecutionException e) {
      // closed while this read started: the deadline thread stops first
      throw new IllegalStateException(CLOSED_MESSAGE, e);
    } catch (IOException | RuntimeException e) {
      SafeFetchFailure failure = System.nanoTime() >= deadline || get.isCancelled() ? SafeFetchFailure.TIMEOUT : failureOf(e);
      LOG.debug("A URL could not be read by {}: {} ({})", policy.getName(), failure, e.getClass().getSimpleName());
      throw new SafeFetchException(failure, e);
    } finally {
      if (timer != null) {
        timer.cancel(false);
      }
    }
  }

  /**
   * Reads an answer: nothing modified, a redirect to follow, a body of an
   * accepted type within the limit, or the failure it is. A 304 is read as
   * nothing modified only when the request sent a validator; answering an
   * unconditional read, it is an error status.
   * <p>
   * An answer not read to its end is aborted before the client closes it:
   * closing a response otherwise drains its body to the declared length, so a
   * body refused as too large, of a refused type, an error page or a redirect's
   * body would still be downloaded.
   *
   * @param get the request, cancelled when its answer is not read to its end
   * @param uri the URL that answered
   * @param response the answer
   * @param request the read
   * @param deadline the read's deadline
   * @return the answer read
   * @throws IOException carrying the reason of a failure
   */
  private SafeFetchHop handle(HttpGet get,
                              URI uri,
                              ClassicHttpResponse response,
                              SafeFetchRequest request,
                              long deadline) throws IOException {
    int status = response.getCode();
    Map<String, String> headers = headersOf(response);
    String contentType = headers.get(HttpHeaders.CONTENT_TYPE);
    if (status == HttpStatus.SC_NOT_MODIFIED && isConditional(request)) {
      return SafeFetchHop.answered(new SafeFetchResponse(status, true, false, null, contentType, headers, uri));
    }
    if (isRedirect(status)) {
      get.cancel();
      String location = headers.get(HttpHeaders.LOCATION);
      if (StringUtils.isBlank(location)) {
        throw new SafeFetchException(SafeFetchFailure.HTTP_ERROR, status);
      }
      return SafeFetchHop.redirected(location);
    }
    if (status < 200 || status >= 300) {
      get.cancel();
      throw new SafeFetchException(SafeFetchFailure.HTTP_ERROR, status);
    }
    Set<String> accepted = acceptedContentTypes(request);
    if (accepted != null) {
      String mediaType = SafeFetchPolicyBuilder.mediaTypeOf(contentType);
      if (mediaType == null || !accepted.contains(mediaType)) {
        get.cancel();
        throw new SafeFetchException(SafeFetchFailure.CONTENT_TYPE_NOT_ALLOWED);
      }
    }
    HttpEntity entity = response.getEntity();
    if (entity == null) {
      return SafeFetchHop.answered(new SafeFetchResponse(status, false, false, new byte[0], contentType, headers, uri));
    }
    long limit = maxBytes(request);
    if (entity.getContentLength() > limit && !request.truncateAtLimit()) {
      get.cancel();
      throw new SafeFetchException(SafeFetchFailure.TOO_LARGE);
    }
    ByteArrayOutputStream body = new ByteArrayOutputStream();
    boolean truncated = readBody(get, entity, body, limit, request.truncateAtLimit(), deadline);
    return SafeFetchHop.answered(new SafeFetchResponse(status, false, truncated, body.toByteArray(), contentType, headers, uri));
  }

  /**
   * Reads a body up to a limit, counted on the decoded bytes, and within the
   * read's deadline; past the limit, the body is cut there when the request
   * allows it, refused otherwise, and the rest is never downloaded.
   *
   * @param get the request, cancelled when the body is not read to its end
   * @param entity the body answered
   * @param body where the bytes read go
   * @param limit the most bytes read
   * @param truncateAtLimit whether a longer body is cut rather than refused
   * @param deadline the read's deadline
   * @return whether the body was cut at the limit
   * @throws IOException carrying the reason of a failure
   */
  private static boolean readBody(HttpGet get,
                                  HttpEntity entity,
                                  ByteArrayOutputStream body,
                                  long limit,
                                  boolean truncateAtLimit,
                                  long deadline) throws IOException {
    try (InputStream input = entity.getContent()) {
      byte[] buffer = new byte[8192];
      long total = 0;
      int read;
      while ((read = input.read(buffer)) != -1) {
        if (System.nanoTime() >= deadline) {
          get.cancel();
          throw new SafeFetchException(SafeFetchFailure.TIMEOUT);
        }
        if (total + read > limit) {
          get.cancel();
          if (!truncateAtLimit) {
            throw new SafeFetchException(SafeFetchFailure.TOO_LARGE);
          }
          body.write(buffer, 0, (int) (limit - total));
          return true;
        }
        total += read;
        body.write(buffer, 0, read);
      }
    }
    return false;
  }

  /**
   * The media types a read accepts: the request's within the policy's, the
   * policy's alone when the request names none.
   *
   * @param request the read
   * @return the media types, null when any is accepted; empty when the request
   *         names none the policy accepts, so that every answer is refused
   */
  private Set<String> acceptedContentTypes(SafeFetchRequest request) {
    Set<String> policyTypes = policy.getAcceptedContentTypes();
    Set<String> requestTypes = request.acceptedContentTypes();
    if (requestTypes == null) {
      return policyTypes.isEmpty() ? null : policyTypes;
    }
    if (policyTypes.isEmpty()) {
      return requestTypes;
    }
    Set<String> narrowed = new HashSet<>(requestTypes);
    narrowed.retainAll(policyTypes);
    return narrowed;
  }

  /**
   * The most bytes a read takes: the request's limit, capped at the policy's.
   *
   * @param request the read
   * @return the limit
   */
  private long maxBytes(SafeFetchRequest request) {
    return request.maxBytes() > 0 ? Math.min(request.maxBytes(), policy.getMaxBytes()) : policy.getMaxBytes();
  }

  /**
   * The number of deadline timers still queued: none once every read ended.
   *
   * @return the timers queued
   */
  int pendingDeadlines() {
    return deadlines.getQueue().size();
  }

  /**
   * Whether a read sent a validator, the only case where a 304 means nothing
   * changed.
   *
   * @param request the read
   * @return true when it is conditional
   */
  private static boolean isConditional(SafeFetchRequest request) {
    return StringUtils.isNotBlank(request.ifNoneMatch()) || StringUtils.isNotBlank(request.ifModifiedSince());
  }

  /**
   * Whether a status is a redirect the fetcher follows.
   *
   * @param status the HTTP status
   * @return true for 301, 302, 303, 307 and 308
   */
  private static boolean isRedirect(int status) {
    return status == HttpStatus.SC_MOVED_PERMANENTLY
        || status == HttpStatus.SC_MOVED_TEMPORARILY
        || status == HttpStatus.SC_SEE_OTHER
        || status == HttpStatus.SC_TEMPORARY_REDIRECT
        || status == HttpStatus.SC_PERMANENT_REDIRECT;
  }

  /**
   * The URL a redirect points at, a relative {@code Location} resolved against
   * the URL that answered.
   *
   * @param current the URL that answered
   * @param location the {@code Location} header
   * @return the URL to read next
   * @throws SafeFetchException when the {@code Location} is not a URL
   */
  private static URI redirectTarget(URI current, String location) throws SafeFetchException {
    try {
      return current.resolve(new URI(location.trim()));
    } catch (URISyntaxException | IllegalArgumentException e) {
      throw new SafeFetchException(SafeFetchFailure.INVALID_URL);
    }
  }

  /**
   * The first value of every header answered, by name.
   *
   * @param response the answer
   * @return the headers
   */
  private static Map<String, String> headersOf(ClassicHttpResponse response) {
    Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    for (Header header : response.getHeaders()) {
      if (header.getName() != null && StringUtils.isNotBlank(header.getValue())) {
        headers.putIfAbsent(header.getName(), header.getValue());
      }
    }
    return headers;
  }

  /**
   * The reason of a failure the HTTP client threw, from the exception or its
   * causes: a refusal by the guard, a name resolving to nothing, a timeout, or
   * no answer at all.
   *
   * @param failure what was thrown
   * @return the reason
   */
  private static SafeFetchFailure failureOf(Throwable failure) {
    for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
      if (cause instanceof RefusedAddressException) {
        return SafeFetchFailure.REFUSED_ADDRESS;
      }
      if (cause instanceof UnknownHostException) {
        return SafeFetchFailure.UNRESOLVABLE;
      }
      if (cause instanceof InterruptedIOException) {
        return SafeFetchFailure.TIMEOUT;
      }
      if (cause.getCause() == cause) {
        break;
      }
    }
    return SafeFetchFailure.UNREACHABLE;
  }

}
