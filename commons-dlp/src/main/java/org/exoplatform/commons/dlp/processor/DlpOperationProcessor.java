package org.exoplatform.commons.dlp.processor;

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.commons.dlp.connector.DlpServiceConnector;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public abstract class DlpOperationProcessor {

  private static final Log LOGGER = ExoLogger.getExoLogger(DlpOperationProcessor.class);
  
  public static final String DLP_FEATURE = "dlp";

  private Map<String, DlpServiceConnector> connectors = new HashMap<String, DlpServiceConnector>();

  /**
   * Add Dlp Connector to the service
   * @param dlpServiceConnector the dlp connector to add
   * @LevelAPI Experimental
   */
  public void addConnector (DlpServiceConnector dlpServiceConnector) {
    addConnector(dlpServiceConnector, false);
  }

  /**
   * Add Dlp Connector to the service
   * @param dlpServiceConnector the dlp connector to add
   * @param override equal true if we can override an existing connector, false otherwise
   * @LevelAPI Experimental
   */
  public void addConnector (DlpServiceConnector dlpServiceConnector, Boolean override) {
    if (connectors.containsKey(dlpServiceConnector.getType()) && override.equals(false)) {
      LOGGER.error("Impossible to add connector {}. A connector with the same name has already been registered.", dlpServiceConnector.getType());
    } else {
      connectors.put(dlpServiceConnector.getType(), dlpServiceConnector);
      LOGGER.info("A Dlp Connector has been added: {}", dlpServiceConnector.getType());
    }
  }

  /**
   * Gets all current connectors
   * @return Connectors
   * @LevelAPI Experimental
   */
  public Map<String, DlpServiceConnector> getConnectors() {
    return connectors;
  }
  
  /**
   * @return the Dlp Keywords
   */
  public abstract String getKeywords();

  /**
   * Add all documents in the dlp queue
   * @LevelAPI Experimental
   */
  public abstract void process();

  /**
   * Interrupt the dlp queue process
   */
  public abstract void interrupt();
}
