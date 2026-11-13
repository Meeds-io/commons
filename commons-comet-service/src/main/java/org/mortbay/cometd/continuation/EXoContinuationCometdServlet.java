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

import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.client.http.jetty.JettyHttpClientTransport;
import org.cometd.oort.Oort;
import org.cometd.oort.Seti;
import org.eclipse.jetty.client.HttpClient;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer.PortalContainerPostInitTask;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.security.codec.AbstractCodec;
import org.exoplatform.web.security.codec.CodecInitializer;
import org.exoplatform.web.security.security.TokenServiceInitializationException;
import org.exoplatform.ws.frameworks.cometd.ServletContextWrapper;
import org.cometd.oort.jakarta.OortConfigServlet;
import org.cometd.oort.jakarta.OortMulticastConfigServlet;
import org.cometd.oort.jakarta.OortStaticConfigServlet;
import org.cometd.oort.jakarta.SetiServlet;
import org.cometd.server.http.jakarta.CometDServlet;
import org.cometd.server.websocket.jakarta.WebSocketTransport;

public class EXoContinuationCometdServlet extends CometDServlet {

  private static final long     serialVersionUID   = 9204910608302112814L;
  
  private static final Log      LOG                = ExoLogger.getLogger(CometDServlet.class);

  private static final long     OORT_SECRET_SETTLE_MILLIS = 5000;

  private SetiServlet           setiConfig;
  
  private ExoContainer container;
  
  private boolean initialized;

  private boolean clusterEnabled = false;  
  
  private static EXoContinuationCometdServlet instance;
  
  private ServletConfig originConfig;

  public static final String    PREFIX             = "exo.cometd.";

  protected static final String CLOUD_ID_SEPARATOR = PREFIX + "cloudIDSeparator";
  
  public static String OORT_CONFIG_TYPE = "oort.configType";
  public static String OORT_CONFIG_CLOUD = "oort.cloud";
  public static String OORT_CONFIG_URL = "oort.url";

  public static String EXO_OORT_CONFIG_TYPE = PREFIX + OORT_CONFIG_TYPE;
  public static String EXO_OORT_CONFIG_CLOUD = PREFIX + OORT_CONFIG_CLOUD;
  public static String EXO_OORT_CONFIG_URL = PREFIX + OORT_CONFIG_URL;

  public static String OORT_MULTICAST = "multicast";
  public static String OORT_STATIC = "static";

  public static final Pattern URL_REGEX;
  static {
    String ip_regex = "(((((25[0-5])|(2[0-4][0-9])|([01]?[0-9]?[0-9]))\\.){3}((25[0-4])|(2[0-4][0-9])|((1?[1-9]?[1-9])|([1-9]0))))|(0\\.){3}0)";
    URL_REGEX = Pattern.compile("^((ht|f)tp(s?)://)" // protocol
                                + "(\\w+(:\\w+)?@)?" // username:password@
                                + "(" + ip_regex // ip
                                + "|([0-9a-z_!~*'()-]+\\.)*([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]\\.[a-z]{2,6}" // domain like www.exoplatform.org
                                + "|([a-zA-Z][-a-zA-Z0-9]+))" // domain like localhost
                                + "(:[0-9]{1,5})?" // port number :8080
                                + "((/?)|(/[0-9a-zA-Z_!~*'().;?:@&=+$,%#-]+)+/?)$"); // uri    
  }

  private ServletConfig servletConfig;

  public void init(ServletConfig config) throws ServletException {
    originConfig = config;

    PortalContainerPostInitTask task = new PortalContainerPostInitTask() {
      public void execute(ServletContext context, PortalContainer portalContainer) {
          setContainer(portalContainer);
          init();
          initialized = true;
      }
    };
    PortalContainer.addInitTask(config.getServletContext(), task);
    instance = this; // NOSONAR
  }
  
  public void reInit() {
    if (initialized) {
      init();
    }
  }

  public void init() {
    ServletConfig servletConfig = getServletConfig();

    try {
      super.init();

      String profiles = PropertyManager.getProperty("exo.profiles");
      if (profiles != null) {
        clusterEnabled = profiles.contains("cluster");
        if (clusterEnabled) {
          warnInvalidUrl(getInitParameter(OortConfigServlet.OORT_URL_PARAM));
        }
      }

      String configType = getInitParameter(OORT_CONFIG_TYPE);
      LOG.info("Cometd configured with type {}", configType);

      if (clusterEnabled) {
        if (OORT_MULTICAST.equals(configType)) {
          OortConfigServlet oConfig = new OortMulticastConfig();
          oConfig.init(servletConfig);
        } else if (OORT_STATIC.equals(configType)) {
          LOG.info("Cometd configured with cloud {}", getInitParameter(OORT_CONFIG_CLOUD));

          OortConfigServlet oConfig = new OortStaticConfig();
          oConfig.init(servletConfig);
        } else {
          LOG.warn("No valid parameter {} value was set ({}), cometd initialization will fail", EXO_OORT_CONFIG_TYPE, configType);
        }
      } else {
        PropertyManager.setProperty(EXO_OORT_CONFIG_CLOUD, "");
        OortConfigServlet oConfig = new OortStaticConfig();
        oConfig.init(servletConfig);
      }

      setiConfig = new SetiServlet();
      setiConfig.init(servletConfig);

      ServletContext cometdContext = servletConfig.getServletContext();
      Seti seti = (Seti) cometdContext.getAttribute(Seti.SETI_ATTRIBUTE);
      Oort oort = (Oort) cometdContext.getAttribute(Oort.OORT_ATTRIBUTE);

      EXoContinuationBayeux bayeux = (EXoContinuationBayeux) getBayeux();
      bayeux.setSeti(seti);
      applyOortTrust(bayeux, oort, clusterEnabled);

      String separator = getInitParameter(CLOUD_ID_SEPARATOR);
      if (separator != null) {
        bayeux.setCloudIDSeparator(separator);
      }
    } catch (Exception e) {
      LOG.error("Cannot initialize Bayeux", e);
    }
  }

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
                                                                                  IOException {
    if (getContainer() == null || getBayeux() == null) {
      final AsyncContext ac = request.startAsync(request, response);
      ac.start(() -> {
        while (getContainer() == null || getBayeux() == null) {
          try {
            Thread.sleep(5000);
          } catch (InterruptedException e) {
            LOG.error(e);
          }
        }

        try {
          EXoContinuationCometdServlet.this.service(ac.getRequest(), ac.getResponse());
        } catch (Exception e) {
          LOG.error(e);
        }
      });
    } else {
      super.service(request, response);      
    }
  }

  @Override
  public String getInitParameter(String name) {
    return getServletConfig().getInitParameter(name);
  }

  @Override
  public ServletConfig getServletConfig() {
    EXoContinuationBayeux bayeux = getBayeux();
    if (servletConfig == null) {
      servletConfig = new ServletConfigWrapper(originConfig);
      bayeux.setServletConfig(servletConfig);
    }
    return servletConfig;
  }

  public void setContainer(ExoContainer container) {
    this.container = container;
  }

  private ExoContainer getContainer() {
    return container;
  }
  
  /**
   * {@inheritDoc}
   */
  protected EXoContinuationBayeux newBayeuxServer() {
    return getBayeux();
  }

  public EXoContinuationBayeux getBayeux() {
    try {
      if (LOG.isDebugEnabled())
        LOG.debug("EXoContinuationCometdServlet - Current Container-ExoContainer: " + getContainer());
      EXoContinuationBayeux bayeux = (EXoContinuationBayeux) getContainer().getComponentInstanceOfType(BayeuxServer.class);
      if (LOG.isDebugEnabled())
        LOG.debug("EXoContinuationCometdServlet - -->AbstractBayeux=" + bayeux);
      return bayeux;
    } catch (Exception e) {
      LOG.error("Error new Bayeux creation ", e);
      return null;
    }
  }
  
  public static EXoContinuationCometdServlet getInstance() {
    return instance;
  }

  @Override
  public void destroy() {
    //destroy procedure should be done in BayeuxServer eXo kernel disposable lifecycle method
  }
  
  private Oort configTransports(Oort oort) {
    ServletConfig config = getServletConfig();
    String transport = config.getInitParameter("transports");
    if (transport == null || !transport.contains(WebSocketTransport.class.getName())) {
      oort.getClientTransportFactories().add(new JettyHttpClientTransport.Factory(new HttpClient()));
    }
    return oort;
  }
  
  /**
   * @param clusterEnabled whether the node runs with the cluster profile
   * @param secret the secret the nodes of the cluster share
   * @return true when the other nodes of the cluster may be trusted: in a
   *         cluster, with a secret that is neither blank nor the public
   *         default one
   */
  static boolean isOortTrusted(boolean clusterEnabled, String secret) {
    return clusterEnabled && OortSecretResolver.isOwnSecret(secret);
  }

  /**
   * Sets the Oort on the bayeux, and whether the other nodes of the cluster are
   * trusted, from the secret the Oort itself holds.
   *
   * @param bayeux the bayeux of this node
   * @param oort the Oort of this node
   * @param clusterEnabled whether the node runs with the cluster profile
   * @return whether the other nodes of the cluster are trusted
   */
  static boolean applyOortTrust(EXoContinuationBayeux bayeux, Oort oort, boolean clusterEnabled) {
    boolean oortTrusted = oort != null && isOortTrusted(clusterEnabled, oort.getSecret());
    bayeux.setOort(oort);
    bayeux.setOortTrusted(oortTrusted);
    return oortTrusted;
  }

  /**
   * Prepares the Oort of this node before it starts and connects to the other
   * nodes: the secret they share, then the Oort and the trust on the bayeux.
   */
  private void prepareOort(Oort oort) {
    applyClusterSecret(oort);
    if (!applyOortTrust(getBayeux(), oort, clusterEnabled) && clusterEnabled) {
      LOG.error("No secret shared by the nodes of the cluster could be resolved: this node refuses the other nodes and live updates are not relayed between nodes. Set {}oort.secret to the same random value on every node.",
                PREFIX);
    }
  }

  /**
   * Sets on the Oort of a cluster node the secret its nodes share, before it
   * connects to them: the configured one when it is the deployment's own,
   * otherwise the one stored in the global settings.
   */
  private void applyClusterSecret(Oort oort) {
    if (!clusterEnabled) {
      return;
    }
    try {
      SettingService settingService = container.getComponentInstanceOfType(SettingService.class);
      CacheService cacheService = container.getComponentInstanceOfType(CacheService.class);
      AbstractCodec codec = container.getComponentInstanceOfType(CodecInitializer.class).getCodec();
      String secret = new OortSecretResolver(container, settingService, cacheService, codec, OORT_SECRET_SETTLE_MILLIS)
                                                                                                .resolve(getInitParameter(OortConfigServlet.OORT_SECRET_PARAM));
      if (OortSecretResolver.isOwnSecret(secret)) {
        oort.setSecret(secret);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      LOG.error("Interrupted while resolving the secret shared by the nodes of the cluster", e);
    } catch (TokenServiceInitializationException | RuntimeException e) {
      LOG.error("Cannot resolve the secret shared by the nodes of the cluster", e);
    }
  }

  private void warnInvalidUrl(String url) {
    if (url == null || url.isEmpty()) {
      LOG.warn("You didn’t set exo.cometd.oort.url, cometd cannot work in cluster mode without this property, please set it.");
    } else if (!URL_REGEX.matcher(url).matches()) {
      LOG.warn("exo.cometd.oort.url is invalid {}, cometd cannot work in cluster mode without this property, please set it.", url);
    }
  }

  /**
   * This class help to workaround issue with eap 6.2 that has not support
   * Websocket transport yet
   */
  public class OortStaticConfig extends OortStaticConfigServlet {
    private static final long serialVersionUID = 1054209695244836363L;

    
    @Override
    protected void configureOort(ServletConfig config, Oort oort) throws Exception {
      super.configureOort(config, oort);
      prepareOort(oort);
    }

    @Override
    protected void configureCloud(ServletConfig config, Oort oort) {
      if (clusterEnabled) {
        super.configureCloud(config, oort);
      }
    }

    @Override
    protected Oort newOort(BayeuxServer bayeux, String url) {
      Oort oort = super.newOort(bayeux, url);
      return configTransports(oort);
    }
  }
  
  public class OortMulticastConfig extends OortMulticastConfigServlet {
    private static final long serialVersionUID = 6836833932474627776L;
    
    @Override
    protected void configureOort(ServletConfig config, Oort oort) throws Exception {
      super.configureOort(config, oort);
      prepareOort(oort);
    }

    @Override
    protected void configureCloud(ServletConfig config, Oort oort) throws Exception {
      if (clusterEnabled) {
        super.configureCloud(config, oort);        
      }
    }
    
    @Override
    protected Oort newOort(BayeuxServer bayeux, String url) {
      Oort oort = super.newOort(bayeux, url);
      return configTransports(oort);
    }
  }
  
  private class ServletConfigWrapper implements ServletConfig {
    private ServletConfig delegate;
    private ServletContext contextWrapper;
    private String[] configs;

    public ServletConfigWrapper(ServletConfig config) {
      this.delegate = config;
      contextWrapper = new ServletContextWrapper(delegate.getServletContext());
    }

    @Override
    public String getInitParameter(String name) {
      String propertyName = PREFIX + name;
      String value = PropertyManager.getProperty(propertyName);

      LOG.debug("Reading cometd property {} from JVM properties, value = {}", propertyName, value);
      if (value == null) {
        value = delegate.getInitParameter(name);
        LOG.debug("Reading cometd property {} from Servlet init params, value = {}", propertyName, value);
      }
      return value;
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
      if (configs == null) {
        List<String> keys = new LinkedList<String>();
        Properties props = System.getProperties();
        int len = PREFIX.length();

        for (Object key : props.keySet()) {
          String k = key.toString().trim();
          if (k.startsWith(PREFIX) && k.length() > len) {
            keys.add(k.substring(len));
          }
        }

        configs = keys.toArray(new String[keys.size()]);
      }
      Set<String> names = new HashSet<>();
      names.addAll(Collections.list(delegate.getInitParameterNames()));
      names.addAll(Arrays.asList(configs));

      return Collections.enumeration(names);
    }

    @Override
    public ServletContext getServletContext() {
      return contextWrapper;
    }

    @Override
    public String getServletName() {
      return delegate.getServletName();
    }    
  } 
}
