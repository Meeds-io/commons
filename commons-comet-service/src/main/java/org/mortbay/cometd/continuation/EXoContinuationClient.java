package org.mortbay.cometd.continuation;

import org.cometd.server.ServerSessionImpl;

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

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */

@Deprecated
public class EXoContinuationClient extends ServerSessionImpl {

  /**
    * 
    */
  protected String eXoId;

  /**
   * @param bayeux the bayeux.
   */
  protected EXoContinuationClient(EXoContinuationBayeux bayeux) {
    super(bayeux);
  }

  /**
   * @return exoId of client.
   */
  public String getEXoId() {
    return eXoId;
  }

  /**
   * @param eXoId the client id.
   */
  public void setEXoId(String eXoId) {
    this.eXoId = eXoId;
  }
}
