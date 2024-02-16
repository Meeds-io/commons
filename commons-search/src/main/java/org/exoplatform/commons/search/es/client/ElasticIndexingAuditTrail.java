package org.exoplatform.commons.search.es.client;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com
 * 10/28/15
 */
public class ElasticIndexingAuditTrail {
  public static final String  REINDEX_ALL         = "reindex_all";

  public static final String  DELETE_ALL          = "delete_all";

  public static final String  CREATE_INDEX        = "create_index";

  public static final String  DELETE_INDEX        = "delete_index";

  public static final String  CREATE_INDEX_ALIAS  = "create_index_alias";

  public static final String  REINDEX_TYPE        = "reindex_type";

  public static final String  SEARCH_INDEX        = "search_type";

  public static final String  CREATE_PIPELINE     = "create_pipeline";

  public static final String  CREATE_DOC_PIPELINE = "create_doc_pipeline";

  private static final Log    AUDIT_TRAIL         = ExoLogger.getExoLogger("org.exoplatform.es.audittrail");

  private static final char   SEPARATOR           = ';';

  private static final String LOG_PATTERN         = "{}" + StringUtils.repeat(SEPARATOR + "{}", 6);

  public static boolean isError(Integer httpStatusCode) {
    return (httpStatusCode != null) && ((httpStatusCode < 200) || (httpStatusCode > 299));
  }

  public void audit(String action,
                    String entityId,
                    String index,
                    Integer httpStatusCode,
                    String message,
                    long executionTime) {
    if (isError(httpStatusCode)) {
      logError(action, entityId, index, httpStatusCode, message, executionTime);
    } else {
      logInfo(action, entityId, index, httpStatusCode, message, executionTime);
    }
  }

  public void logRejectedDocumentBulkOperation(String action,
                                               String entityId,
                                               String index,
                                               Integer httpStatusCode,
                                               String message,
                                               long executionTime) {
    logError(action, entityId, index, httpStatusCode, message, executionTime);
  }

  public boolean isFullLogEnabled() {
    return AUDIT_TRAIL.isDebugEnabled();
  }

  public void logAcceptedBulkOperation(String action,
                                       String entityId,
                                       String index,
                                       Integer httpStatusCode,
                                       String message,
                                       long executionTime) {
    logDebug(action, entityId, index, httpStatusCode, message, executionTime);
  }

  public void logRejectedSearchOperation(String action,
                                            String index,
                                            Integer httpStatusCode,
                                            String message,
                                            long executionTime) {
    logError(action, null, index, httpStatusCode, message, executionTime);
  }

  public void logAcceptedSearchOperation(String action,
                                       String index,
                                       Integer httpStatusCode,
                                       String message,
                                       long executionTime) {
    logDebug(action, null, index, httpStatusCode, message, executionTime);
  }

  private void logInfo(String action,
                       String entityId,
                       String index,
                       Integer httpStatusCode,
                       String message,
                       long executionTime) {
    AUDIT_TRAIL.info(LOG_PATTERN,
        action,
        StringUtils.isBlank(entityId) ? "" : escape(entityId),
        StringUtils.isBlank(index) ? "" : escape(index),
        httpStatusCode == null ? "" : httpStatusCode,
        StringUtils.isBlank(message) ? "" : escape(message),
        executionTime);
  }

  private void logError(String action,
                        String entityId,
                        String index,
                        Integer httpStatusCode,
                        String message,
                        long executionTime) {
    if (AUDIT_TRAIL.isDebugEnabled() || PropertyManager.isDevelopping()) {
      // display stack Trace for Debugging
      AUDIT_TRAIL.error(LOG_PATTERN,
                        action,
                        StringUtils.isBlank(entityId) ? "" : escape(entityId),
                        StringUtils.isBlank(index) ? "" : escape(index),
                        httpStatusCode == null ? "" : httpStatusCode,
                        StringUtils.isBlank(message) ? "" : escape(message),
                        executionTime,
                        new IllegalStateException());
    } else {
      AUDIT_TRAIL.error(LOG_PATTERN,
                        action,
                        StringUtils.isBlank(entityId) ? "" : escape(entityId),
                        StringUtils.isBlank(index) ? "" : escape(index),
                        httpStatusCode == null ? "" : httpStatusCode,
                        StringUtils.isBlank(message) ? "" : escape(message),
                        executionTime);
    }
  }

  private void logDebug(String action,
                        String entityId,
                        String index,
                        Integer httpStatusCode,
                        String message,
                        long executionTime) {
    AUDIT_TRAIL.debug(LOG_PATTERN,
        action,
        StringUtils.isBlank(entityId) ? "" : escape(entityId),
        StringUtils.isBlank(index) ? "" : escape(index),
        httpStatusCode == null ? "" : httpStatusCode,
        StringUtils.isBlank(message) ? "" : escape(message),
        executionTime);
  }

  private String escape(String message) {
    if (message == null) {
      return null;
    }
    return message.replace(SEPARATOR, ',');
  }
}
