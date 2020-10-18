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
package org.exoplatform.ws.frameworks.cometd;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.installer.Installer;
import org.codehaus.cargo.container.installer.ZipURLInstaller;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.generic.DefaultContainerFactory;
import org.codehaus.cargo.generic.configuration.DefaultConfigurationFactory;
import org.codehaus.cargo.util.log.LogLevel;
import org.codehaus.cargo.util.log.SimpleLogger;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public class CargoContainer
{
  /**
   * Logger.
   */
  private static final Log LOG = ExoLogger.getLogger(CargoContainer.class);

   // private static InstalledLocalContainer container;
   protected static final String TEST_PATH =
            (System.getProperty("testPath") == null ? "." : System.getProperty("testPath"));

   protected static final String TEST_LIB_PATH = TEST_PATH + "/target/test";

   // public static InstalledLocalContainer cargoContainertStart() {
   // return cargoContainerStart(null, null);
   // }
   //  
   // public static InstalledLocalContainer cargoContainerStart(String port){
   // return cargoContainerStart(port, null);
   // }

   public static InstalledLocalContainer cargoContainerStart(String port, String home)
   {
      try
      {

         if (port == null || port == "")
         {
            // Default port
            port = "8080";
         }
         if (home == null || home == "")
         {
            // Default home
            home = System.getProperty("java.io.tmpdir");
         }

         Installer installer =
                  new ZipURLInstaller(new java.net.URL(
                           "http://www.apache.org/dist/tomcat/tomcat-6/v6.0.35/bin/apache-tomcat-6.0.35.zip"), home);
         installer.install();
         // Installer installer =
         // new ZipURLInstaller(new java.net.URL(
         // "http://www.apache.org/dist/tomcat/tomcat-5/v5.5.25/bin/apache-tomcat-5.5.25.zip"
         // ), home);
         // installer.install();

         LocalConfiguration configuration =
                  (LocalConfiguration) new DefaultConfigurationFactory().createConfiguration("tomcat5x",
                           ContainerType.INSTALLED, ConfigurationType.STANDALONE);// ,
         // "/home/vetal/eXo/"
         // +
         // id
         // +
         // "/conf"
         // )
         // ;

         configuration.setProperty(ServletPropertySet.PORT, port);
         // configuration.addDeployable(new WAR(TEST_PATH +
         // "/target/test/war/portal.war"));
//         configuration.addDeployable(new WAR(TEST_PATH + "/target/test/war/cometd.war"));
         configuration.addDeployable(new WAR(TEST_PATH + "/target/test/war/rest.war"));

         InstalledLocalContainer container =
                  (InstalledLocalContainer) new DefaultContainerFactory().createContainer("tomcat5x",
                           ContainerType.INSTALLED, configuration);

         container.setHome(installer.getHome());

         String[] arr;
         List<String> lst = new ArrayList<String>();
         File dir = new File(TEST_LIB_PATH);
         arr = dir.list(new FilenameFilter()
         {
            public boolean accept(File dir, String name)
            {
               return name.endsWith(".jar");
            }
         });
         for (String name : arr)
         {
            lst.add(TEST_LIB_PATH + "/" + name);
         }
         String[] arr2 = new String[lst.size()];
         lst.toArray(arr2);
         
         container.setExtraClasspath(arr2);
         container.setOutput("target/logs/tomcat.log");
         SimpleLogger logger = new SimpleLogger();
         LogLevel level = LogLevel.WARN;
         logger.setLevel(level);
         container.setLogger(logger);
         File inputFile = new File(TEST_PATH + "/src/test/resources/tomcat/exo-configuration.xml");
         File outputFile = new File(container.getHome() + "/exo-configuration.xml");

         LOG.info("CargoContainer.cargoContainerStart()" + container.getHome());
         
         FileReader in = new FileReader(inputFile);
         FileWriter out = new FileWriter(outputFile);
         int c;

         while ((c = in.read()) != -1)
            out.write(c);

         in.close();
         out.close();
         container.start();
         LOG.info("CargoContainer.containerStart() : " + container.getState().isStarted());
         return container;
      }
      catch (Exception e)
      {
         LOG.error(e);
         return null;
      }
   }

   public static void cargoContainerStop(InstalledLocalContainer container)
   {
      container.stop();
   }

}
