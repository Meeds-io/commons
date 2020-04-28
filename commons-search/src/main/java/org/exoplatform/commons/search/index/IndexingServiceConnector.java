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
package org.exoplatform.commons.search.index;

import org.exoplatform.commons.search.domain.Document;
import org.exoplatform.container.component.BaseComponentPlugin;

import java.io.Serializable;
import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 7/22/15
 */
public abstract class IndexingServiceConnector extends BaseComponentPlugin implements Serializable {

  private String type;
  private boolean enable = true;

  /**
   * Transform an entity to Document in order to be indexed
   * @param id Id of entity to create
   * @return List of Document to create
   * @LevelAPI Experimental
   */
  public abstract Document create(String id);

  /**
   * Transform an entity to Document in order to be reindexed
   * @param id Id of entity to reindex
   * @return List of Document to reindex
   * @LevelAPI Experimental
   */
  public abstract Document update(String id);

  /**
   * Transform a list of entities to Document in order to be deleted from create
   * @param id Ids of entities to delete from
   * @return List of Ids to delete from create
   * @LevelAPI Experimental
   */
  public abstract String delete (String id);

  public abstract List<String> getAllIds(int offset, int limit);

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public boolean isEnable() {
    return enable;
  }

  public void setEnable(boolean enable) {
    this.enable = enable;
  }

  public boolean canReindex() {
    return true;
  }
}
