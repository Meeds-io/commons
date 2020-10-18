/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.commons.testing;

import junit.framework.AssertionFailedError;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;


/**
 * <p>An helper for building a root container and a portal container. I have done several attempt to make easily
 * and safe root/portal container boot for unit test. This one is my best attempt so far.</p>
 *
 * <p>Note that the portal container are booted in the order they are declared first.</p>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ContainerBuilder
{
  private static final Log LOG = ExoLogger.getLogger(ContainerBuilder.class);

   /** A hack used during the boot of a portal container. */
   private final ThreadLocal<String> bootedPortalName = new ThreadLocal<String>();

   /** . */
   private ClassLoader loader;

   /** . */
   private List<URL> rootConfigURLs;

   /** . */
   private LinkedHashMap<String, List<URL>> portalConfigURLs;

   public ContainerBuilder()
   {
      this.loader = Thread.currentThread().getContextClassLoader();
      this.rootConfigURLs = new ArrayList<URL>();
      this.portalConfigURLs = new LinkedHashMap<String, List<URL>>();
   }

   public ContainerBuilder withRoot(String configPath)
   {
      rootConfigURLs.addAll(urls(configPath));
      return this;
   }

   public ContainerBuilder withRoot(URL configURL)
   {
      rootConfigURLs.add(configURL);
      return this;
   }

   public ContainerBuilder withPortal(String configPath)
   {
      return withPortal("portal", configPath);
   }

   public ContainerBuilder withPortal(String portalName, String configPath)
   {
      for (URL configURL : urls(configPath))
      {
         withPortal(portalName, configURL);
      }
      return this;
   }

   public ContainerBuilder withPortal(URL configURL)
   {
      return withPortal("portal", configURL);
   }

   public ContainerBuilder withPortal(String portalName, URL configURL)
   {
      List<URL> urls = portalConfigURLs.get(portalName);
      if (urls == null)
      {
         urls = new ArrayList<URL>();
         portalConfigURLs.put(portalName, urls);
      }
      urls.add(configURL);
      return this;
   }

   public ContainerBuilder withLoader(ClassLoader loader)
   {
      this.loader = loader;
      return this;
   }

   private List<URL> urls(String path)
   {
      try
      {
         return Collections.list(loader.getResources(path));
      }
      catch (IOException e)
      {
         AssertionFailedError err = new AssertionFailedError();
         err.initCause(e);
         throw err;
      }
   }

   public RootContainer build()
   {
      try
      {
         return _build();
      }
      catch (SecurityException e) 
      {
         AssertionFailedError err = new AssertionFailedError();
         err.initCause(e);
         throw err;
      } 
      catch (IllegalArgumentException e) 
      {
         AssertionFailedError err = new AssertionFailedError();
         err.initCause(e);
         throw err;
      } 
      catch (NoSuchFieldException e) 
      {
         AssertionFailedError err = new AssertionFailedError();
         err.initCause(e);
         throw err;
      } 
      catch (IllegalAccessException e) 
      {
         AssertionFailedError err = new AssertionFailedError();
         err.initCause(e);
         throw err;
      }
   }

  private RootContainer _build() throws SecurityException,
                                NoSuchFieldException,
                                IllegalArgumentException,
                                IllegalAccessException
   {
      //
      if (rootConfigURLs.size() == 0)
      {
         throw new IllegalStateException("Must provide at least one URL for building the root container");
      }

      // Must clear the top container first otherwise it's not going to work well
      // it's a big ugly but I don't want to change anything in the ExoContainerContext class for now
      // and this is for unit testing
      Field topContainerField = ExoContainerContext.class.getDeclaredField("topContainer");
      topContainerField.setAccessible(true);
      topContainerField.set(null, null);

      // Same remark than above
      Field singletonField = RootContainer.class.getDeclaredField("singleton_");
      singletonField.setAccessible(true);
      singletonField.set(null, null);
      
//      // needed otherwise, we cannot call this method twice in the same thread
//      // since version kernel 2.4.x, org.exoplatform.container.RootContainer.booting field is static final
//      Field bootingField = RootContainer.class.getDeclaredField("booting");
//      LOG.info("-----------------" + bootingField.getClass());
//      bootingField.setAccessible(true);
//      
//      Field value = bootingField.getType().getDeclaredField("value");
//      value.setAccessible(true);
//      
//      value.set(bootingField.getType(), 0);
//      
      RootContainer.setInstance(null);
      
      
      if (ExoContainerContext.getCurrentContainerIfPresent() != null) {
        PortalContainer.setInstance(null);
      }

      
      
      
      ClassLoader rootCL = new ClassLoader(loader)
      {
         @Override
         public Enumeration<URL> getResources(String name) throws IOException
         {
           Enumeration<URL> resources;
           
            if ("conf/configuration.xml".equals(name))
            {
              resources =  Collections.enumeration(rootConfigURLs);
            }
            else if ("conf/portal/configuration.xml".equals(name))
            {
               String portalName = bootedPortalName.get();
               resources =  Collections.enumeration(portalConfigURLs.get(portalName));
            }
            else if ("conf/portal/test-configuration.xml".equals(name))
            {
              resources =  new Vector<URL>().elements();
            }
            else
            {
              resources =  super.getResources(name);
            }
            
            if (resources!=null && resources.hasMoreElements()) {
              if (LOG.isInfoEnabled()) {
                LOG.info("Loaded " + name);
              }
            } else {
              if (LOG.isInfoEnabled()) {
                LOG.info("No resource found " + name);
              }
            }
            return resources;
            
         }
      };
      

      
      //
      ClassLoader oldCL = Thread.currentThread().getContextClassLoader();


      
      // Boot root container
      RootContainer root;
      try
      {
         Thread.currentThread().setContextClassLoader(rootCL);

         //
         root = RootContainer.getInstance();
         
         //
         for (String portalName : portalConfigURLs.keySet())
         {
            try
            {
               bootedPortalName.set(portalName);
               root.getPortalContainer(portalName);
            }
            finally
            {
               bootedPortalName.set(null);
            }
         }
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldCL);
      }

      //
      return root;
   }

   public static RootContainer bootstrap(URL configurationURL, String... profiles)
   {
      ContainerBuilder builder = new ContainerBuilder();
      builder.withRoot(configurationURL);
      return builder.build();
   }
}
