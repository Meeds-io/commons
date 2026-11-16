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
package org.mortbay.cometd.continuation;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import jakarta.servlet.ServletConfig;

import org.cometd.bayeux.ChannelId;
import org.cometd.bayeux.Promise;
import org.cometd.bayeux.server.*;
import org.cometd.oort.Oort;
import org.cometd.oort.Seti;
import org.cometd.server.*;
import org.picocontainer.Disposable;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserStatus;
import org.exoplatform.services.security.ConversationRegistry;

public class EXoContinuationBayeux extends BayeuxServerImpl implements Disposable {

  /**
   * The session attribute that holds the user of a client's session.
   */
  static final String                     USER_ATTRIBUTE       = "exo.cometd.userId";

  /**
   * The session attribute that holds the generation of the user, in the
   * conversation registry, at which the user's status was last read.
   */
  static final String                     GENERATION_ATTRIBUTE = "exo.cometd.userGeneration";

  /**
   * Map for userToken.
   */
  private static Map<String, String>      userToken         = new ConcurrentHashMap<>();

  /**
   * Stores the eXoID <=> clientID association
   */
  private static Map<String, Set<String>> clientIDs         = Collections.synchronizedMap(new LinkedHashMap<>());

  /**
   * Cometd webapp context name
   */
  private String                          cometdContextName = "cometd";

  private String                          cloudIDSeparator  = "/";

  /**
   * Used to send message to all client or a specific client that listen a
   * specific channel
   */
  private ServerSessionImpl               systemClient;

  private volatile Seti                   seti;

  private volatile Oort                   oort;

  /**
   * The channels the services asked Oort to observe, kept until Oort is set
   */
  private final Set<String>               observedChannels  = ConcurrentHashMap.newKeySet();

  private final List<Consumer<Oort>>      oortActions       = new CopyOnWriteArrayList<>();

  /**
   * Whether a handshake or a session of another node of the cluster is
   * accepted: false unless the node runs in a cluster with its own shared
   * secret.
   */
  private volatile boolean                oortTrusted;

  private ServletConfig                   servletConfig;

  private final ExoContainer              container;

  /**
   * The users of the store admitted on this node: one of them the store no
   * longer holds was deleted, and is refused.
   */
  private final Set<String>               storeUserIds      = ConcurrentHashMap.newKeySet();

  /**
   * Logger.
   */
  private static final Log                LOG               = ExoLogger.getLogger(EXoContinuationBayeux.class);

  /**
   * Default constructor.
   */
  public EXoContinuationBayeux() {
    super();
    this.container = ExoContainerContext.getCurrentContainerIfPresent();
    this.setSecurityPolicy(new EXoSecurityPolicy(this));
    this.addExtension(new GoneUserExtension(this));
    //
    EXoContinuationCometdServlet servlet = EXoContinuationCometdServlet.getInstance();
    if (servlet != null) {
      servlet.setContainer(ExoContainerContext.getCurrentContainer());
      servlet.reInit();
    }
  }

  /**
   * {@inheritDoc}
   */
  public ServerSession newRemoteClient() {
    ServerSessionImpl client = newServerSession();
    return client;
  }

  @Deprecated
  public void setTimeout(long timeout) {
  }

  public long getTimeout() {
    return getOption(AbstractServerTransport.TIMEOUT_OPTION, 30000);
  }

  /**
   * @return context name of cometd webapp
   */
  public String getCometdContextName() {
    return cometdContextName;
  }

  public void setCometdContextName(String context) {
    this.cometdContextName = context;
  }

  /**
   * {@inheritDoc}
   */
  long getRandom(long variation) {
    long l = randomLong() ^ variation;
    return l < 0 ? -l : l;
  }

  /**
   * @param eXoId the client id.
   * @return token for client
   */
  public String getUserToken(String eXoId) {
    if (eXoId == null) {
      // No handshake presents a null id: the token is never checked
      return newToken();
    }
    String token = userToken.get(eXoId);
    if (token != null) {
      return token;
    }
    rememberStoreUser(eXoId);
    return userToken.computeIfAbsent(eXoId, id -> newToken());
  }

  /**
   * A token issued for a user of the store marks the id as a store user of
   * this node, so that a handshake with that token is refused once the store
   * no longer holds the user. A token issued for an id the store does not
   * hold, such as a call guest's, marks nothing. It runs only when a token is
   * minted, so that a repeated call reads nothing, whatever the id.
   */
  private void rememberStoreUser(String eXoId) {
    try {
      if (findUser(eXoId) != null) {
        storeUserIds.add(eXoId);
      }
    } catch (Exception e) {
      LOG.warn("Cannot read user {}: the token is issued without marking it as a user of the store", eXoId, e);
    }
  }

  /**
   * @param eXoID the id of client.
   * @return client with eXoID
   */
  @Deprecated
  public EXoContinuationClient getClientByEXoId(String eXoID) {
    List<ServerSession> ids = getSessions();
    for (ServerSession client : ids) {
      if (client instanceof EXoContinuationClient) {
        EXoContinuationClient exoClient = (EXoContinuationClient) client;
        if (exoClient.getEXoId() != null && exoClient.getEXoId().equals(eXoID))
          return exoClient;
      }
    }
    return null;
  }

  public boolean isSubscribed(String eXoID, String clientID) {
    return (clientIDs.get(eXoID) != null && clientIDs.get(eXoID).contains(clientID));
  }

  /**
   * @param  eXoID the id of the target client
   * @return       whether that client currently holds a CometD session.
   *               {@code false} when CometD is not initialized yet: {@code seti}
   *               is only assigned when the cometd servlet starts, so any caller
   *               reached before that — a listener firing during another addon's
   *               boot, for instance — must not blow up.
   */
  public boolean isPresent(String eXoID) {
    return seti != null && seti.isPresent(eXoID);
  }

  /**
   * Publishes a message on a channel, even when no session of this node is
   * subscribed to it: the subscribers of a pattern that matches the channel,
   * such as the other nodes of a cluster that observe it, receive it too.
   *
   * @param channel the id of channel.
   * @param data the message
   * @param msgId the message id
   */
  public void sendBroadcastMessage(String channel, Object data, String msgId) {
    ServerSessionImpl fromClient = getSystemClient();
    ServerChannel ch = createChannelIfAbsent(channel).getReference();
    ch.publish(fromClient, data, Promise.noop());
    if (LOG.isDebugEnabled()) {
      LOG.debug("Send broadcast message " + data + " on channel " + channel);
    }
  }

  /**
   * Send data to a individual client. The data passed is sent to the client as
   * the "data" member of a message with the given channel and id. The message
   * is not published on the channel and is thus not broadcast to all channel
   * subscribers. However to the target client, the message appears as if it was
   * broadcast.
   * <p>
   * Typcially this method is only required if a service method sends
   * response(s) to channels other than the subscribed channel. If the response
   * is to be sent to the subscribed channel, then the data can simply be
   * returned from the subscription method.
   * 
   * @param eXoID the id of target client
   * @param channel The id of channel the message is for
   * @param data The data of the message
   * @param id The id of the message (or null for a random id).
   */
  public void sendMessage(String eXoID, String channel, Object data, String id) {
    if (seti == null) {
      // CometD not initialized yet; nothing is connected, so there is no one to
      // deliver to. Same reasoning as isPresent(String).
      if (LOG.isDebugEnabled()) {
        LOG.debug("Dropping message on channel " + channel + " for " + eXoID + ": CometD isn't initialized yet");
      }
      return;
    }
    seti.sendMessage(eXoID, channel, data);
  }

  public void setSeti(Seti seti) {
    this.seti = seti;
  }

  public synchronized void setOort(Oort oort) {
    if (oort == this.oort) {
      return;
    }
    this.oort = oort;
    if (oort != null) {
      observedChannels.forEach(oort::observeChannel);
      oortActions.forEach(action -> run(action, oort));
    }
  }

  /**
   * Runs an action with this node's Oort once it is set, at once when it is
   * set already: a service that starts before the CometD servlet reaches the
   * other nodes of a cluster so, such as to learn when one leaves.
   *
   * @param action the action, given this node's Oort
   */
  public synchronized void onOort(Consumer<Oort> action) {
    oortActions.add(action);
    Oort currentOort = oort;
    if (currentOort != null) {
      run(action, currentOort);
    }
  }

  private void run(Consumer<Oort> action, Oort currentOort) {
    try {
      action.accept(currentOort);
    } catch (RuntimeException e) {
      LOG.warn("Error while running an action on the Oort of node {}", currentOort.getURL(), e);
    }
  }

  /**
   * Relays to this node the messages published on the given channels on the
   * other nodes of a cluster, as Oort does for the channels its configuration
   * names. A channel a service publishes on with no session of this node's
   * subscribed to it, such as a document's channel published on by the node
   * that receives a third party's callback, reaches the users of the other
   * nodes only so. May be called before Oort is set.
   *
   * @param channel a broadcast channel, or a pattern of them such as
   *          <code>/a/b/**</code>
   */
  public void observeChannel(String channel) {
    observedChannels.add(channel);
    Oort currentOort = oort;
    if (currentOort != null) {
      currentOort.observeChannel(channel);
    }
  }

  /**
   * @param sender the session that published a message
   * @return true when the message was published on another node and relayed
   *         to this one by Oort: a handler acting on a client's message acts
   *         only on the node the client published it to, or it acts once per
   *         node of a cluster
   */
  public boolean isRelayed(ServerSession sender) {
    Oort currentOort = oort;
    return currentOort != null && currentOort.isOort(sender);
  }

  /**
   * @param oortTrusted whether handshakes and sessions of the other nodes of
   *          the cluster are accepted
   */
  public void setOortTrusted(boolean oortTrusted) {
    this.oortTrusted = oortTrusted;
  }

  public boolean isOortTrusted() {
    return oortTrusted;
  }

  /**
   * Reads the user a client handshakes as. A user of the store is kept on the
   * session with the generation it was read at, so that the session's later
   * checks see a change of the user. An id the store does not hold, such as a
   * call guest's, is admitted with no user, unless a user of the store
   * connected to this node under it and was deleted since. The generation is
   * read after the user: a change of the user in between is caught by the
   * registry's second removal of the generation, a few seconds later.
   *
   * @param session the session of the handshake
   * @param userId the id it presents
   * @return false when the user is disabled, was deleted or cannot be read,
   *         the generation included
   */
  boolean admitUser(ServerSession session, String userId) {
    User user;
    String generation = null;
    try {
      user = findUser(userId);
      if (user != null && user.isEnabled()) {
        generation = userGeneration(userId);
      }
    } catch (Exception e) {
      LOG.warn("Cannot read user {}: its real-time session is refused", userId, e);
      return false;
    }
    if (user == null && !storeUserIds.contains(userId)) {
      return true;
    } else if (user == null || !user.isEnabled()) {
      userToken.remove(userId);
      return false;
    }
    storeUserIds.add(userId);
    session.setAttribute(USER_ATTRIBUTE, userId);
    session.setAttribute(GENERATION_ATTRIBUTE, generation);
    return true;
  }

  /**
   * A change of a user, made on any node, changes the user's generation in
   * the conversation registry: the session's user is read again then only, so
   * a session whose user is unchanged costs no read. The token of a user found
   * disabled or deleted is revoked, so that its pages cannot handshake again.
   *
   * @param session a session
   * @return false when the user of the session was disabled or deleted, or
   *         cannot be read; true for a session that holds no user of the
   *         store
   */
  boolean isSessionUserActive(ServerSession session) {
    Object userId = session.getAttribute(USER_ATTRIBUTE);
    if (userId == null) {
      return true;
    }
    String generation;
    User user;
    try {
      generation = userGeneration((String) userId);
      if (generation != null && generation.equals(session.getAttribute(GENERATION_ATTRIBUTE))) {
        return true;
      }
      user = findUser((String) userId);
    } catch (Exception e) {
      LOG.warn("Cannot read user {}: its real-time session is refused", userId, e);
      return false;
    }
    if (user == null || !user.isEnabled()) {
      userToken.remove(userId);
      return false;
    }
    session.setAttribute(GENERATION_ATTRIBUTE, generation);
    return true;
  }

  public void setCloudIDSeparator(String cloudIDSeparator) {
    if (cloudIDSeparator != null) {
      this.cloudIDSeparator = cloudIDSeparator;
    } else {
      LOG.warn("Can't set null for cloudIDSeparator");
    }
  }

  private String userGeneration(String userId) {
    ConversationRegistry conversationRegistry = service(ConversationRegistry.class);
    return conversationRegistry == null ? null : conversationRegistry.getUserGeneration(userId);
  }

  private User findUser(String userId) throws Exception {
    OrganizationService organizationService = service(OrganizationService.class);
    if (organizationService == null) {
      return null;
    }
    boolean begun = organizationService instanceof ComponentRequestLifecycle;
    if (begun) {
      RequestLifeCycle.begin((ComponentRequestLifecycle) organizationService);
    }
    try {
      return organizationService.getUserHandler().findUserByName(userId, UserStatus.ANY);
    } finally {
      if (begun) {
        RequestLifeCycle.end();
      }
    }
  }

  private String newToken() {
    return Long.toString(this.getRandom(System.identityHashCode(this) ^ System.currentTimeMillis()), 36);
  }

  private <T> T service(Class<T> type) {
    return container == null ? null : container.getComponentInstanceOfType(type);
  }

  private ServerSessionImpl getSystemClient() {
    if (systemClient == null) {
      systemClient = newServerSession();
    }
    return systemClient;
  }

  public ServletConfig getServletConfig() {
    return servletConfig;
  }

  public void setServletConfig(ServletConfig config) {
    this.servletConfig = config;
  }

  @Override
  public void dispose() {
    for (ServerSession session : getSessions()) {
      ((ServerSessionImpl) session).destroyScheduler();
    }

    if (seti != null && oort != null) {
      try {
        seti.stop();
        oort.stop();
        super.stop();
      } catch (Exception e) {
        LOG.error(e.getMessage(), e);
      }
    }
  }

  public static class EXoSecurityPolicy implements SecurityPolicy, ServerSession.RemovedListener {

    private EXoContinuationBayeux bayeux;

    public EXoSecurityPolicy(EXoContinuationBayeux bayeux) {
      this.bayeux = bayeux;
    }

    /**
     * @param message the cometd message.
     * @return true if user valid else false.
     */
    private boolean checkUser(ServerMessage message) {
      String userId = (String) message.get("exoId");
      String eXoToken = (String) message.get("exoToken");
      String token = userId == null ? null : userToken.get(userId);
      return token != null && token.equals(eXoToken);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canCreate(BayeuxServer server, ServerSession client, String channelId, ServerMessage message) {
      //
      Boolean b = client != null && !ChannelId.isMeta(channelId);
      return b;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canHandshake(BayeuxServer server, ServerSession client, ServerMessage message) {
      if (client.isLocalSession()) {
        return true;
      } else if (isOortHandshakeAttempt(message)) {
        // Another node of the cluster proves itself with the cluster secret,
        // never with a user's token
        return isTrustedOortHandshake(message);
      } else if (checkUser(message) && bayeux.admitUser(client, (String) message.get("exoId"))) {
        client.addListener(this);

        String eXoID = (String) message.get("exoId");
        Set<String> cIds = clientIDs.get(eXoID);
        if (cIds == null) {
          cIds = ConcurrentHashMap.newKeySet();
          clientIDs.put(eXoID, cIds);
        }
        bayeux.seti.associate(eXoID, client);

        cIds.add(client.getId());
        return true;
      } else {
        return false;
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canPublish(BayeuxServer server, ServerSession client, ServerChannel channel, ServerMessage message) {
      return client != null && (client.isLocalSession() || bayeux.isSessionUserActive(client));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canSubscribe(BayeuxServer server, ServerSession client, ServerChannel channel, ServerMessage message) {
      return client != null && ((checkUser(message) && bayeux.isSessionUserActive(client)) || client.isLocalSession()
          || isTrustedOortSession(client));
    }

    private boolean isOortHandshakeAttempt(ServerMessage message) {
      Map<String, Object> ext = message.getExt();
      return ext != null && ext.get(Oort.EXT_OORT_FIELD) != null;
    }

    private boolean isTrustedOortHandshake(ServerMessage message) {
      return bayeux.oortTrusted && bayeux.oort != null && bayeux.oort.isOortHandshake(message);
    }

    private boolean isTrustedOortSession(ServerSession client) {
      return bayeux.oortTrusted && bayeux.oort != null && bayeux.oort.isOort(client);
    }

    @Override
    public void removed(ServerSession session, ServerMessage message, boolean timeout) {
      Iterator<Entry<String, Set<String>>> iter = clientIDs.entrySet().iterator();

      while (iter.hasNext()) {
        Entry<String, Set<String>> client = iter.next();
        Set<String> ids = client.getValue();
        ids.remove(session.getId());
        if (ids.isEmpty()) {
          iter.remove();
        }
      }
    }
  }

  /**
   * @return the users connected to this node and, in a cluster, to the other
   *         nodes, as Seti knows them: a message sent to one of them with
   *         {@link #sendMessage(String, String, Object, String)} reaches the
   *         node the user is connected to
   */
  public Set<String> getConnectedUserIds() {
    Seti currentSeti = seti;
    if (currentSeti == null) {
      return clientIDs.keySet();
    }
    Set<String> userIds = new HashSet<>(clientIDs.keySet());
    userIds.addAll(currentSeti.getUserIds());
    return userIds;
  }
}
