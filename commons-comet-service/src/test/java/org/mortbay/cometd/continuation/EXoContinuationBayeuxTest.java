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
package org.mortbay.cometd.continuation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import org.cometd.bayeux.Channel;
import org.cometd.bayeux.Promise;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.LocalSession;
import org.cometd.bayeux.server.SecurityPolicy;
import org.cometd.bayeux.server.ServerMessage;
import org.cometd.bayeux.server.ServerSession;
import org.cometd.oort.Oort;
import org.cometd.oort.Seti;
import org.cometd.server.BayeuxServerImpl;
import org.cometd.server.ServerSessionImpl;
import org.cometd.server.http.JSONHttpTransport;
import org.junit.Before;
import org.junit.Test;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.UserStatus;
import org.exoplatform.services.security.ConversationRegistry;

public class EXoContinuationBayeuxTest {

  private static final String       CHANNEL     = "/eXo/Application/Test/document/**";

  private final Map<String, String> generations = new HashMap<>();

  private ExoContainer              container;

  private UserHandler               userHandler;

  private ConversationRegistry      conversationRegistry;

  @Before
  public void setUp() {
    container = mock(ExoContainer.class);
    OrganizationService organizationService = mock(OrganizationService.class);
    userHandler = mock(UserHandler.class);
    when(organizationService.getUserHandler()).thenReturn(userHandler);
    conversationRegistry = mock(ConversationRegistry.class);
    when(conversationRegistry.getUserGeneration(anyString())).thenAnswer(invocation -> generations.computeIfAbsent(invocation.getArgument(0),
                                                                                                                  id -> "1"));
    when(container.getComponentInstanceOfType(OrganizationService.class)).thenReturn(organizationService);
    when(container.getComponentInstanceOfType(ConversationRegistry.class)).thenReturn(conversationRegistry);
  }

  @Test
  public void testAChannelObservedBeforeOortIsSetIsObservedOnceItIs() {
    EXoContinuationBayeux bayeux = new EXoContinuationBayeux();
    Oort oort = mock(Oort.class);

    // A service starts before the CometD servlet sets Oort
    bayeux.observeChannel(CHANNEL);
    bayeux.setOort(oort);

    verify(oort).observeChannel(CHANNEL);
  }

  @Test
  public void testAChannelObservedOnceOortIsSetIsObservedAtOnce() {
    EXoContinuationBayeux bayeux = new EXoContinuationBayeux();
    Oort oort = mock(Oort.class);
    bayeux.setOort(oort);

    bayeux.observeChannel(CHANNEL);

    verify(oort).observeChannel(CHANNEL);
  }

  @Test
  public void testTheUsersConnectedToAnotherNodeAreConnectedUsers() {
    EXoContinuationBayeux bayeux = new EXoContinuationBayeux();
    Seti seti = mock(Seti.class);
    when(seti.getUserIds()).thenReturn(Set.of("john"));
    bayeux.setSeti(seti);

    assertTrue(bayeux.getConnectedUserIds().contains("john"));
  }

  @Test
  public void testAMessageOortRepublishedIsRelayedAndAClientsIsNot() {
    EXoContinuationBayeux bayeux = new EXoContinuationBayeux();
    ServerSession oortSession = mock(ServerSession.class);
    ServerSession clientSession = mock(ServerSession.class);
    Oort oort = mock(Oort.class);
    when(oort.isOort(oortSession)).thenReturn(true);
    assertFalse(bayeux.isRelayed(oortSession));

    bayeux.setOort(oort);

    assertTrue(bayeux.isRelayed(oortSession));
    assertFalse(bayeux.isRelayed(clientSession));
  }

  @Test
  public void testABroadcastReachesTheSubscribersOfAPatternThatMatchesItsChannel() throws Exception {
    EXoContinuationBayeux bayeux = new EXoContinuationBayeux();
    bayeux.setOption(BayeuxServerImpl.TRANSPORTS_OPTION, JSONHttpTransport.class.getName());
    bayeux.start();
    try {
      // As another node's Oort client, subscribed to the pattern only
      List<Object> received = new CopyOnWriteArrayList<>();
      LocalSession otherNode = bayeux.newLocalSession("otherNode");
      otherNode.handshake();
      otherNode.getChannel(CHANNEL).subscribe((channel, message) -> received.add(message.getData()));

      bayeux.sendBroadcastMessage("/eXo/Application/Test/document/42", "saved", null);

      assertEquals(List.of("saved"), received);
    } finally {
      bayeux.stop();
    }
  }

  @Test
  public void testAnActionOnOortRunsOnceOortIsSetAndAtOnceAfter() {
    EXoContinuationBayeux bayeux = new EXoContinuationBayeux();
    Oort oort = mock(Oort.class);
    List<Oort> before = new CopyOnWriteArrayList<>();
    List<Oort> after = new CopyOnWriteArrayList<>();

    bayeux.onOort(before::add);
    assertEquals(List.of(), before);
    bayeux.setOort(oort);
    bayeux.onOort(after::add);

    assertEquals(List.of(oort), before);
    assertEquals(List.of(oort), after);
  }

  /**
   * The servlet sets the same Oort before and after starting it: its channels
   * are observed, and its actions run, once.
   */
  @Test
  public void testTheSameOortSetTwiceRunsItsActionsOnce() {
    EXoContinuationBayeux bayeux = new EXoContinuationBayeux();
    Oort oort = mock(Oort.class);
    List<Oort> ran = new CopyOnWriteArrayList<>();
    bayeux.observeChannel(CHANNEL);
    bayeux.onOort(ran::add);

    bayeux.setOort(oort);
    bayeux.setOort(oort);

    assertEquals(List.of(oort), ran);
    verify(oort, times(1)).observeChannel(CHANNEL);
  }

  @Test
  public void testAFailedActionOnOortLeavesTheOthersRun() {
    EXoContinuationBayeux bayeux = new EXoContinuationBayeux();
    Oort oort = mock(Oort.class);
    List<Oort> ran = new CopyOnWriteArrayList<>();
    bayeux.onOort(o -> {
      throw new IllegalStateException("failed");
    });
    bayeux.onOort(ran::add);

    bayeux.setOort(oort);

    assertEquals(List.of(oort), ran);
  }

  /**
   * A user of the store is admitted while enabled, and refused once disabled,
   * whatever token it presents.
   */
  @Test
  public void testADisabledUserIsRefusedAtTheHandshake() throws Exception {
    EXoContinuationBayeux bayeux = newBayeux();
    User john = storeUser("john", true);
    ServerMessage handshake = userMessage(bayeux, "john");
    SecurityPolicy policy = bayeux.getSecurityPolicy();

    assertTrue(policy.canHandshake(bayeux, session(), handshake));
    when(john.isEnabled()).thenReturn(false);
    assertFalse(policy.canHandshake(bayeux, session(), handshake));
  }

  /**
   * A call guest presents an id the store does not hold: it is admitted with
   * no user of the store, and its session is not checked again.
   */
  @Test
  public void testAnIdTheStoreDoesNotHoldIsAdmitted() throws Exception {
    EXoContinuationBayeux bayeux = newBayeux();
    ServerSession session = session();
    ServerMessage handshake = userMessage(bayeux, "guest-ann-1");
    SecurityPolicy policy = bayeux.getSecurityPolicy();

    assertTrue(policy.canHandshake(bayeux, session, handshake));
    assertTrue(policy.canSubscribe(bayeux, session, null, handshake));
    assertTrue(new GoneUserExtension(bayeux).rcvMeta(session, connect()));
  }

  /**
   * The store is read when a token is minted only: a repeated call for an id
   * the store does not hold reads nothing.
   */
  @Test
  public void testATokenIsReadFromTheStoreOnlyWhenMinted() throws Exception {
    EXoContinuationBayeux bayeux = newBayeux();

    String token = bayeux.getUserToken("guest-bob-7");
    assertEquals(token, bayeux.getUserToken("guest-bob-7"));
    assertEquals(token, bayeux.getUserToken("guest-bob-7"));

    verify(userHandler, times(1)).findUserByName("guest-bob-7", UserStatus.ANY);
  }

  /**
   * A user of the store that connected to this node and was deleted since is
   * refused, and its token revoked: its pages cannot handshake again.
   */
  @Test
  public void testAUserDeletedAfterConnectingIsRefusedAndItsTokenRevoked() throws Exception {
    EXoContinuationBayeux bayeux = newBayeux();
    storeUser("john", true);
    ServerMessage handshake = userMessage(bayeux, "john");
    SecurityPolicy policy = bayeux.getSecurityPolicy();
    assertTrue(policy.canHandshake(bayeux, session(), handshake));

    when(userHandler.findUserByName("john", UserStatus.ANY)).thenReturn(null);

    assertFalse(policy.canHandshake(bayeux, session(), handshake));
    assertFalse(handshake.get("exoToken").equals(bayeux.getUserToken("john")));
  }

  /**
   * A user disabled after the handshake, through any node, changes the user's
   * generation: the session's next connect ends it, and it can no longer
   * subscribe or publish.
   */
  @Test
  public void testTheSessionOfAUserGoneAfterItsHandshakeEndsAtItsNextConnect() throws Exception {
    EXoContinuationBayeux bayeux = newBayeux();
    User john = storeUser("john", true);
    ServerSession session = session();
    ServerMessage handshake = userMessage(bayeux, "john");
    SecurityPolicy policy = bayeux.getSecurityPolicy();
    assertTrue(policy.canHandshake(bayeux, session, handshake));
    BayeuxServer.Extension extension = new GoneUserExtension(bayeux);
    assertTrue(extension.rcvMeta(session, connect()));

    when(john.isEnabled()).thenReturn(false);
    generations.put("john", "2");

    assertFalse(policy.canSubscribe(bayeux, session, null, handshake));
    assertFalse(policy.canPublish(bayeux, session, null, handshake));
    assertFalse(extension.rcvMeta(session, connect()));
    verify(session).disconnect();
  }

  /**
   * The bayeux runs the connect check on every connect of its sessions.
   */
  @Test
  public void testTheBayeuxRunsTheGoneUserCheckOnEveryConnect() {
    EXoContinuationBayeux bayeux = newBayeux();

    assertEquals(1, bayeux.getExtensions().stream().filter(GoneUserExtension.class::isInstance).count());
  }

  /**
   * A user deleted after the handshake loses its session at its next connect,
   * and its token: its pages cannot handshake again.
   */
  @Test
  public void testTheSessionOfAUserDeletedAfterItsHandshakeEndsAndItsTokenIsRevoked() throws Exception {
    EXoContinuationBayeux bayeux = newBayeux();
    storeUser("john", true);
    ServerSession session = session();
    ServerMessage handshake = userMessage(bayeux, "john");
    assertTrue(bayeux.getSecurityPolicy().canHandshake(bayeux, session, handshake));

    when(userHandler.findUserByName("john", UserStatus.ANY)).thenReturn(null);
    generations.put("john", "2");

    assertFalse(new GoneUserExtension(bayeux).rcvMeta(session, connect()));
    verify(session).disconnect();
    assertFalse(handshake.get("exoToken").equals(bayeux.getUserToken("john")));
  }

  /**
   * A user that cannot be read at a connect, after a change of the user, is
   * refused.
   */
  @Test
  public void testAUserThatCannotBeReadAtAConnectIsRefused() throws Exception {
    EXoContinuationBayeux bayeux = newBayeux();
    storeUser("john", true);
    ServerSession session = session();
    assertTrue(bayeux.getSecurityPolicy().canHandshake(bayeux, session, userMessage(bayeux, "john")));

    when(userHandler.findUserByName("john", UserStatus.ANY)).thenThrow(new IllegalStateException("store down"));
    generations.put("john", "2");

    assertFalse(new GoneUserExtension(bayeux).rcvMeta(session, connect()));
  }

  /**
   * An id the store does not hold, and a refused user, take no generation in
   * the registry: its entries are the store users' only.
   */
  @Test
  public void testOnlyAnAdmittedUserOfTheStoreTakesAGeneration() throws Exception {
    EXoContinuationBayeux bayeux = newBayeux();
    storeUser("mary", false);
    SecurityPolicy policy = bayeux.getSecurityPolicy();

    assertTrue(policy.canHandshake(bayeux, session(), userMessage(bayeux, "guest-ann-1")));
    assertFalse(policy.canHandshake(bayeux, session(), userMessage(bayeux, "mary")));

    assertEquals(Map.of(), generations);
  }

  /**
   * The user is read again only when its generation changed: an unchanged
   * user costs no read at each connect, and a change that leaves the user
   * enabled keeps the session.
   */
  @Test
  public void testTheUserIsReadAgainOnlyWhenItsGenerationChanged() throws Exception {
    EXoContinuationBayeux bayeux = newBayeux();
    storeUser("john", true);
    ServerSession session = session();
    assertTrue(bayeux.getSecurityPolicy().canHandshake(bayeux, session, userMessage(bayeux, "john")));
    BayeuxServer.Extension extension = new GoneUserExtension(bayeux);

    // One read when the token was issued (a repeated call reads nothing), one at the handshake
    bayeux.getUserToken("john");
    assertTrue(extension.rcvMeta(session, connect()));
    assertTrue(extension.rcvMeta(session, connect()));
    verify(userHandler, times(2)).findUserByName("john", UserStatus.ANY);

    generations.put("john", "2");
    assertTrue(extension.rcvMeta(session, connect()));
    assertTrue(extension.rcvMeta(session, connect()));
    verify(userHandler, times(3)).findUserByName("john", UserStatus.ANY);
    verify(session, never()).disconnect();
  }

  /**
   * A user that cannot be read is refused.
   */
  @Test
  public void testAUserThatCannotBeReadIsRefused() throws Exception {
    EXoContinuationBayeux bayeux = newBayeux();
    when(userHandler.findUserByName("john", UserStatus.ANY)).thenThrow(new IllegalStateException("store down"));

    assertFalse(bayeux.getSecurityPolicy().canHandshake(bayeux, session(), userMessage(bayeux, "john")));
  }

  /**
   * A generation that cannot be read at a handshake refuses it, as a user that
   * cannot be read does.
   */
  @Test
  public void testAGenerationThatCannotBeReadAtAHandshakeIsRefused() throws Exception {
    EXoContinuationBayeux bayeux = newBayeux();
    storeUser("john", true);
    when(conversationRegistry.getUserGeneration("john")).thenThrow(new IllegalStateException("registry down"));

    assertFalse(bayeux.getSecurityPolicy().canHandshake(bayeux, session(), userMessage(bayeux, "john")));
  }

  /**
   * A generation that cannot be read at a connect refuses it: the session ends.
   */
  @Test
  public void testAGenerationThatCannotBeReadAtAConnectEndsTheSession() throws Exception {
    EXoContinuationBayeux bayeux = newBayeux();
    storeUser("john", true);
    ServerSession session = session();
    assertTrue(bayeux.getSecurityPolicy().canHandshake(bayeux, session, userMessage(bayeux, "john")));

    when(conversationRegistry.getUserGeneration("john")).thenThrow(new IllegalStateException("registry down"));

    assertFalse(new GoneUserExtension(bayeux).rcvMeta(session, connect()));
    verify(session).disconnect();
  }

  /**
   * Through the started bayeux, a user disabled after its handshake loses its
   * session at the next connect, and its pages cannot handshake again.
   */
  @Test
  public void testTheStartedBayeuxEndsTheSessionOfAUserDisabledAfterItsHandshake() throws Exception {
    EXoContinuationBayeux bayeux = newBayeux();
    bayeux.setOption(BayeuxServerImpl.TRANSPORTS_OPTION, JSONHttpTransport.class.getName());
    bayeux.start();
    try {
      User john = storeUser("john", true);
      String token = bayeux.getUserToken("john");

      ServerMessage.Mutable reply = handle(bayeux, bayeux.newServerSession(), handshake("john", token, null));
      assertTrue(reply.isSuccessful());
      String id = reply.getClientId();
      ServerSessionImpl session = (ServerSessionImpl) bayeux.getSession(id);
      assertNotNull(session);
      assertTrue(handle(bayeux, session, engineConnect(id)).isSuccessful());

      when(john.isEnabled()).thenReturn(false);
      generations.put("john", "2");
      reply = handle(bayeux, session, engineConnect(id));

      assertFalse(reply.isSuccessful());
      assertNull(bayeux.getSession(id));
      reply = handle(bayeux, bayeux.newServerSession(), handshake("john", token, null));
      assertFalse(reply.isSuccessful());
    } finally {
      bayeux.stop();
    }
  }

  private ServerMessage.Mutable handshake(String userId, String token, String clientId) {
    ServerMessage.Mutable message = new org.cometd.server.ServerMessageImpl();
    message.setChannel(Channel.META_HANDSHAKE);
    message.put("exoId", userId);
    message.put("exoToken", token);
    message.put("version", "1.0");
    message.put("supportedConnectionTypes", List.of("long-polling"));
    return message;
  }

  private ServerMessage.Mutable engineConnect(String clientId) {
    ServerMessage.Mutable message = new org.cometd.server.ServerMessageImpl();
    message.setChannel(Channel.META_CONNECT);
    message.setClientId(clientId);
    message.put("connectionType", "long-polling");
    return message;
  }

  private ServerMessage.Mutable handle(EXoContinuationBayeux bayeux, ServerSessionImpl session, ServerMessage.Mutable message) {
    AtomicReference<ServerMessage.Mutable> reply = new AtomicReference<>();
    bayeux.handle(session, message, Promise.from(reply::set, failure -> {
      throw new IllegalStateException(failure);
    }));
    return reply.get();
  }

  /**
   * A token issued for a user of the store, which the store no longer holds at
   * the handshake: the handshake is refused and the token is gone. The user
   * never handshook on this node.
   */
  @Test
  public void testATokenIssuedForAUserDeletedSinceIsRefusedAtTheHandshakeAndRevoked() throws Exception {
    EXoContinuationBayeux bayeux = newBayeux();
    storeUser("john", true);
    ServerMessage handshake = userMessage(bayeux, "john");
    String token = (String) handshake.get("exoToken");

    when(userHandler.findUserByName("john", UserStatus.ANY)).thenReturn(null);

    assertFalse(bayeux.getSecurityPolicy().canHandshake(bayeux, session(), handshake));
    assertFalse(token.equals(bayeux.getUserToken("john")));
  }

  private EXoContinuationBayeux newBayeux() {
    ExoContainerContext.setCurrentContainer(container);
    try {
      EXoContinuationBayeux bayeux = new EXoContinuationBayeux();
      bayeux.setSeti(mock(Seti.class));
      return bayeux;
    } finally {
      ExoContainerContext.setCurrentContainer(null);
    }
  }

  private User storeUser(String userId, boolean enabled) throws Exception {
    User user = mock(User.class);
    when(user.isEnabled()).thenReturn(enabled);
    when(userHandler.findUserByName(userId, UserStatus.ANY)).thenReturn(user);
    return user;
  }

  private ServerMessage userMessage(EXoContinuationBayeux bayeux, String userId) {
    ServerMessage message = mock(ServerMessage.class);
    String token = bayeux.getUserToken(userId);
    when(message.get("exoId")).thenReturn(userId);
    when(message.get("exoToken")).thenReturn(token);
    return message;
  }

  private ServerMessage.Mutable connect() {
    ServerMessage.Mutable message = mock(ServerMessage.Mutable.class);
    when(message.getChannel()).thenReturn(Channel.META_CONNECT);
    return message;
  }

  private ServerSession session() {
    ServerSession session = mock(ServerSession.class);
    Map<String, Object> attributes = new HashMap<>();
    when(session.getId()).thenReturn("session-" + System.identityHashCode(attributes));
    doAnswer(invocation -> attributes.put(invocation.getArgument(0), invocation.getArgument(1))).when(session)
                                                                                                  .setAttribute(anyString(), any());
    when(session.getAttribute(anyString())).thenAnswer(invocation -> attributes.get(invocation.<String> getArgument(0)));
    return session;
  }
}
