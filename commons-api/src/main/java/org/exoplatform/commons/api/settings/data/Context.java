/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.api.settings.data;

import org.exoplatform.services.security.ConversationState;

/**
 * This class allow a user to be able to save some settings linked to a Context.
 * Created by The eXo Platform SAS Author : eXoPlatform bangnv@exoplatform.com
 * Nov 9, 2012
 * @LevelAPI Platform
 */

public enum Context {

  /**
   * USER : Each user should be able to save his own settings. GLOBAL : Settings
   * should impact all users in the underlying scope.
   * @LevelAPI Platform
   */
  GLOBAL, USER;

  private String id;

  /**
   * Create a context with an specify id
   * @param id id represent as a username.
   * @LevelAPI Platform
   */
  public Context id(String id) {
    this.id = id;
    return this;
  }

  /**
   * Get context id
   * @return return null if context is GLOBAL and return user's id if context is USER
   * @LevelAPI Platform
   */
  public String getId() {
    ConversationState state = ConversationState.getCurrent();
    String userId = (state != null) ? state.getIdentity().getUserId() : null;
    id = (userId != null) ? userId : id;
    //TODO: throws NullPointException if id is null
    return id;
  }

}
