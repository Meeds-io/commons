/* 
* Copyright (C) 2003-2015 eXo Platform SAS.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see http://www.gnu.org/licenses/ .
*/
package org.exoplatform.commons.search.es.client;

import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;

import org.exoplatform.commons.search.domain.Document;
import org.exoplatform.commons.search.index.impl.ElasticIndexingServiceConnector;
import org.exoplatform.commons.utils.IOUtil;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 9/3/15
 */
public class ElasticContentRequestBuilder {

  private static final Log LOG = ExoLogger.getExoLogger(ElasticContentRequestBuilder.class);

  private static final long    DEFAULT_MAX_REGEX_LENGTH         = 65536l;

  private static final String  DEFAULT_INDEX_SETTINGS_FILE_PATH = "jar:/es-default-index-settings.json";

  private static final String  INDEX_SETTINGS_FILE_PATH_PARAM   = "index.settings.path";

  private ConfigurationManager configurationManager;

  private String               defaultSettingsPath;

  private String               defaultSettings;

  public ElasticContentRequestBuilder(ConfigurationManager configurationManager, InitParams initParams) {
    this.configurationManager = configurationManager;
    if (initParams != null && initParams.getValueParam(INDEX_SETTINGS_FILE_PATH_PARAM) != null) {
      this.defaultSettingsPath = initParams.getValueParam(INDEX_SETTINGS_FILE_PATH_PARAM).getValue();
    }
    if (StringUtils.isBlank(this.defaultSettingsPath)) {
      this.defaultSettingsPath = DEFAULT_INDEX_SETTINGS_FILE_PATH;
    }
  }

  public ElasticContentRequestBuilder() {
    this(ExoContainerContext.getService(ConfigurationManager.class), null);
  }

  /**
   *
   * Get an ES create Index request content
   *
   * @param connector ES connector
   * @return JSON containing a create index request content
   *
   */
  public String getCreateIndexRequestContent(ElasticIndexingServiceConnector connector) {
    String settings = getDefaultSettings().replace("shard.number", String.valueOf(connector.getShards()))
                                          .replace("replica.number", String.valueOf(connector.getReplicas()))
                                          .replace("max_regex.length", String.valueOf(DEFAULT_MAX_REGEX_LENGTH));
    LOG.debug("Create index request to ES: \n {}", settings);
    return settings;
  }

  /**
   *
   * Get a deleteAll ES query
   *
   * @return JSON containing a delete request
   *
   */
  public String getDeleteAllDocumentsRequestContent() {

    JSONObject deleteAllRequest = new JSONObject();
    JSONObject deleteQueryRequest = new JSONObject();
    deleteQueryRequest.put("match_all", new JSONObject());
    deleteAllRequest.put("query", deleteQueryRequest);

    String request = deleteAllRequest.toJSONString();

    LOG.debug("Delete All request to ES: \n {}", request);
    return request;
  }

  /**
   *
   * Get an ES delete document content to insert in a bulk request
   * For instance:
   * { "delete" : { "_index" : "blog", "_type" : "post", "_id" : "blog_post_1" } }
   *
   * @return JSON containing a delete request
   *
   */
  public String getDeleteDocumentRequestContent(ElasticIndexingServiceConnector connector, String id) {

    JSONObject cudHeaderRequest = createCUDHeaderRequestContent(connector, id);

    String request = null;
    if (cudHeaderRequest != null) {
      JSONObject deleteRequest = new JSONObject();
      deleteRequest.put("delete", cudHeaderRequest);

      request =  deleteRequest.toJSONString()+"\n";
    }

    LOG.debug("Delete request to ES: \n {}", request);

    return request;
  }

  /**
   *
   * Get an ES create document content to insert in a bulk request
   * For instance:
   * { "create" : { "_index" : "blog", "_type" : "post", "_id" : "blog_post_1" } }
   * { "field1" : "value3" }
   *
   * @return JSON containing a create document request
   *
   */
  public String getCreateDocumentRequestContent(ElasticIndexingServiceConnector connector, String id) {

    JSONObject ElasticInformation = createCUDHeaderRequestContent(connector, id);

    Document document = connector.create(id);
    if (document==null) {
      LOG.debug("Can't find document with id '{}' using connector '{}'. Ignore it.", id, connector.getIndexAlias());
      return null;
    }

    JSONObject createRequest = new JSONObject();
    createRequest.put("create", ElasticInformation);

    String request = createRequest.toJSONString() + "\n" + document.toJSON() + "\n";

    LOG.debug("Create request to ES: \n {}", request);

    return request;
  }

  /**
   *
   * Get an ES create/update document content to put into a pipeline
   * The content of the request will update the full document (and not partially)
   * 
   * For instance:
   * 
   * { "field1" : "value3" }
   *
   * @return JSON containing a create/update Document to inject to a pipeline
   *
   */
  public String getCreatePipelineDocumentRequestContent(ElasticIndexingServiceConnector connector, String id) {
    Document document = connector.update(id);

    String request = null;
    if (document != null) {
      request = document.toJSON();
    }

    LOG.debug("Create Pipeline document request to ES: \n {}", request);

    return request;
  }

  /**
   *
   * Get an ES update document content to insert in a bulk request
   * We use the create api to reindex the full document (and not partially)
   * For instance:
   * { "create" : { "_index" : "blog", "_type" : "post", "_id" : "blog_post_1" } }
   * { "field1" : "value3" }
   *
   * @return JSON containing an update document request
   *
   */
  public String getUpdateDocumentRequestContent(ElasticIndexingServiceConnector connector, String id) {

    JSONObject ElasticInformation = createCUDHeaderRequestContent(connector, id);

    Document document = connector.update(id);

    String request = null;
    if (document != null) {
      JSONObject updateRequest = new JSONObject();
      updateRequest.put("index", ElasticInformation);

      request = updateRequest.toJSONString() + "\n" + document.toJSON() + "\n";
    }

    LOG.debug("Update request to ES: \n {}", request);

    return request;
  }

  public String getDefaultSettings() {
    if (this.defaultSettings == null) {
      try {
        this.defaultSettings = getFileContent(this.defaultSettingsPath);
      } catch (Exception e) {
        throw new IllegalStateException("Failed to retrieve default ES index settings from path: " + this.defaultSettingsPath, e);
      }
    }
    return this.defaultSettings;
  }

  /**
   *
   * Create an ES content containing information for bulk request
   * For instance:
   * { "_index" : "blog", "_type" : "post", "_id" : "blog_post_1" }
   *
   * @return JSON containing information for bulk request
   *
   */
  private JSONObject createCUDHeaderRequestContent(ElasticIndexingServiceConnector connector, String id) {
    JSONObject cudHeader = new JSONObject();
    cudHeader.put("_index", connector.getIndexAlias());
    cudHeader.put("_id", id);
    return cudHeader;
  }

  private String getFileContent(String filePath) throws Exception {
    InputStream mappingFileIS = configurationManager.getInputStream(filePath);
    return IOUtil.getStreamContentAsString(mappingFileIS);
  }

}

