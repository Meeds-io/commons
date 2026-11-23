/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.commons.search.es.client;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

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

  public static final String  BULK_REQUEST        = "bulk_request";

  private static final Log    AUDIT_TRAIL         = ExoLogger.getExoLogger("org.exoplatform.es.audittrail");

  private static final Pattern VALUE_PREVIEW      = Pattern.compile("(Preview of field's value: )'.*'", Pattern.DOTALL);

  private static final Pattern PARSE_ERROR        = Pattern.compile("(failed to parse: ).*", Pattern.DOTALL);

  private static final String  VALUE_NOT_LOGGED   = "[not logged]";

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

  /**
   * @return the message as logged: with no separator, and without the value
   *         preview of the reason the search engine gives when it rejects a
   *         document
   */
  static String escape(String message) {
    String loggedMessage = hideValues(message);
    return loggedMessage == null ? null : loggedMessage.replace(SEPARATOR, ',');
  }

  /**
   * @return the search engine's whole answer without the reasons it gives,
   *         which quote the indexed values of a rejected document
   */
  static String hideAnswerValues(String answer) {
    return answer == null ? null : hideValues(hideReasons(answer));
  }

  private static String hideReasons(String answer) {
    StringBuilder hidden = new StringBuilder(answer.length());
    int index = 0;
    boolean reasonKey = false;
    while (index < answer.length()) {
      char character = answer.charAt(index);
      if (character != '"') {
        hidden.append(character);
        if (!Character.isWhitespace(character) && character != ':') {
          reasonKey = false;
        }
        index++;
        continue;
      }
      int end = endOfString(answer, index);
      if (reasonKey) {
        hidden.append('"').append(VALUE_NOT_LOGGED).append('"');
        reasonKey = false;
      } else {
        String text = answer.substring(index, end);
        hidden.append(text);
        // a key is followed by a colon, a value by a comma or a closing bracket
        reasonKey = "\"reason\"".equals(text);
      }
      index = end;
    }
    return hidden.toString();
  }

  /**
   * @return the index after the string that starts at the given quote, or the
   *         answer's length when the string is not closed
   */
  private static int endOfString(String answer, int quote) {
    int index = quote + 1;
    while (index < answer.length()) {
      char character = answer.charAt(index);
      if (character == '\\') {
        index += 2;
      } else if (character == '"') {
        return index + 1;
      } else {
        index++;
      }
    }
    return answer.length();
  }

  /**
   * @return the message without the value preview or the parse details of the
   *         reason the search engine gives when it rejects a document
   */
  static String hideValues(String message) {
    if (message == null) {
      return null;
    }
    String loggedMessage = VALUE_PREVIEW.matcher(message).replaceAll("$1" + VALUE_NOT_LOGGED);
    return PARSE_ERROR.matcher(loggedMessage).replaceAll("$1" + VALUE_NOT_LOGGED);
  }
}
