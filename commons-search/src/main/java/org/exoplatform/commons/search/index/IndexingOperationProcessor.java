package org.exoplatform.commons.search.index;

import java.util.*;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com
 * 10/12/15
 */
public abstract class IndexingOperationProcessor {

  private static final Log                      LOG        = ExoLogger.getExoLogger(IndexingOperationProcessor.class);

  private Map<String, IndexingServiceConnector> connectors = new HashMap<>();

  /**
   * Add Indexing Connector to the service
   * 
   * @param indexingServiceConnector the indexing connector to add
   * @LevelAPI Experimental
   */
  public void addConnector(IndexingServiceConnector indexingServiceConnector) {
    addConnector(indexingServiceConnector, false);
  }

  /**
   * Add Indexing Connector to the service
   * 
   * @param indexingServiceConnector the indexing connector to add
   * @param override equal true if we can override an existing connector, false
   *          otherwise
   * @LevelAPI Experimental
   */
  public void addConnector(IndexingServiceConnector indexingServiceConnector, Boolean override) {
    if (connectors.containsKey(indexingServiceConnector.getConnectorName()) && override.equals(false)) {
      LOG.error("Impossible to add connector {}. A connector with the same name has already been registered.",
                indexingServiceConnector.getConnectorName());
    } else {
      connectors.put(indexingServiceConnector.getConnectorName(), indexingServiceConnector);
      LOG.info("An Indexing Connector has been added: {}", indexingServiceConnector.getConnectorName());
    }
  }

  /**
   * Gets all current connectors
   * 
   * @return Connectors
   * @LevelAPI Experimental
   */
  public Map<String, IndexingServiceConnector> getConnectors() {
    return connectors;
  }

  /**
   * Index all document in the indexing queue
   * 
   * @LevelAPI Experimental
   */
  public abstract void process();

  /**
   * Interrupt the indexing queue process
   */
  public abstract void interrupt();
}
