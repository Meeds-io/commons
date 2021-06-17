package org.exoplatform.commons.search.index;

import java.io.Serializable;
import java.util.List;

import org.exoplatform.commons.search.domain.Document;
import org.exoplatform.container.component.BaseComponentPlugin;

/**
 * Created by The eXo Platform SAS Author : Thibault Clement
 * tclement@exoplatform.com 7/22/15
 */
public abstract class IndexingServiceConnector extends BaseComponentPlugin {

  protected String          connectorName;

  protected boolean         enable           = true;

  /**
   * Transform an entity to Document in order to be indexed
   * 
   * @param id Id of entity to create
   * @return List of Document to create
   * @LevelAPI Experimental
   */
  public abstract Document create(String id);

  /**
   * Transform an entity to Document in order to be reindexed
   * 
   * @param id Id of entity to reindex
   * @return List of Document to reindex
   * @LevelAPI Experimental
   */
  public abstract Document update(String id);

  /**
   * Transform a list of entities to Document in order to be deleted from create
   * 
   * @param id Ids of entities to delete from
   * @return List of Ids to delete from create
   * @LevelAPI Experimental
   */
  public abstract String delete(String id);

  public abstract List<String> getAllIds(int offset, int limit);

  public abstract String getConnectorName();

  public boolean isEnable() {
    return enable;
  }

  public void setEnable(boolean enable) {
    this.enable = enable;
  }
}
