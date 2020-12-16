package org.exoplatform.commons.dlp.queue;

public interface QueueDlpService {

  /**
   * Add a dlp operation to the dlp queue
   * @param connectorName Name of the connector
   * @param id id of the entity
   */
  void addToQueue(String connectorName, String id);

}