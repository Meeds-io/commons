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
package org.exoplatform.ws.frameworks.cometd.loadbalancer;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.ws.frameworks.cometd.loadbalancer.LoadBalancerImpl.LoadBalancerConf;

/**
 * Created by The eXo Platform SAS.
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
*/
public class LoadBalancerConfigPlugin extends BaseComponentPlugin
{
   /**
    * 
    */
   private LoadBalancerConf balancerConf;
   
   public LoadBalancerConfigPlugin(InitParams params)
   {
      if (params != null)
      {
         ObjectParameter parameter = params.getObjectParam("cometd.lb.configuration");
         balancerConf = (LoadBalancerConf) parameter.getObject();
      }
   }
   
   
   public LoadBalancerConf getBalancerConf()
   {
      return balancerConf;
   }
   
}
