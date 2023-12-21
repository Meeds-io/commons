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
package org.exoplatform.commons.search.index.impl;

import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;

import org.exoplatform.commons.search.index.IndexingServiceConnector;
import org.exoplatform.commons.utils.IOUtil;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 7/22/15
 */
public abstract class ElasticIndexingServiceConnector extends IndexingServiceConnector {

  private static final Log     LOG = ExoLogger.getLogger(ElasticIndexingServiceConnector.class);

  private static final Integer REPLICAS_NUMBER_DEFAULT = 0;
  private static final String REPLICAS_NUMBER_PROPERTY_NAME = "exo.es.indexing.replica.number.default";
  private static final Integer SHARDS_NUMBER_DEFAULT = 5;
  private static final String SHARDS_PROPERTY_NAME = "exo.es.indexing.shard.number.default";

  private static final String  MAPPING_FILE_PATH_PARAM       = "mapping.file.path";

  protected String indexAlias;
  protected String currentIndex;
  protected String mapping;
  protected Integer shards = SHARDS_NUMBER_DEFAULT;
  protected Integer replicas = REPLICAS_NUMBER_DEFAULT;

  protected ElasticIndexingServiceConnector(InitParams initParams) {
    PropertiesParam param = initParams.getPropertiesParam("constructor.params");

    this.currentIndex = param.getProperty("index_current");
    if (StringUtils.isBlank(this.currentIndex)) {
      throw new IllegalStateException("Connector ES index name is mandatory.");
    }
    this.indexAlias = param.getProperty("index_alias");
    if (StringUtils.isBlank(indexAlias)) {
      this.indexAlias = this.currentIndex;
    }

    //Get number of replicas in connector declaration or exo properties
    if (StringUtils.isNotBlank(param.getProperty("replica.number"))) {
      this.replicas = Integer.valueOf(param.getProperty("replica.number"));
    }
    else if (StringUtils.isNotBlank(PropertyManager.getProperty(REPLICAS_NUMBER_PROPERTY_NAME))) {
      this.replicas = Integer.valueOf(PropertyManager.getProperty(REPLICAS_NUMBER_PROPERTY_NAME));
    }
    //Get number of shards in connector declaration or exo properties
    if (StringUtils.isNotBlank(param.getProperty("shard.number"))) {
      this.shards = Integer.valueOf(param.getProperty("shard.number"));
    }
    else if (StringUtils.isNotBlank(PropertyManager.getProperty(SHARDS_PROPERTY_NAME))) {
      this.shards = Integer.valueOf(PropertyManager.getProperty(SHARDS_PROPERTY_NAME));
    }

    if (initParams.containsKey(MAPPING_FILE_PATH_PARAM)) {
      String mappingFilePath = initParams.getValueParam(MAPPING_FILE_PATH_PARAM).getValue();
      try {
        PortalContainer container = PortalContainer.getInstance();
        ConfigurationManager configurationManager = container.getComponentInstanceOfType(ConfigurationManager.class);
        InputStream mappingFileIS = configurationManager.getInputStream(mappingFilePath);
        this.mapping = IOUtil.getStreamContentAsString(mappingFileIS);
      } catch (Exception e) {
        LOG.error("Can't read elasticsearch index mapping from path {}", mappingFilePath, e);
      }
    }
  }

  /**
   *
   * Default mapping rules for ES index
   *
   * This method must be overridden by your specific connector if you want to define special mapping
   *
   * @return JSON containing a mapping to create new index
   *
   */
  @SuppressWarnings("unchecked")
  public String getMapping() {
      if (StringUtils.isNotBlank(this.mapping)) {
        return this.mapping;
      }

      JSONObject notAnalyzedField = new JSONObject();
      notAnalyzedField.put("type", "text");
      notAnalyzedField.put("index", false);

      JSONObject keywordMapping = new JSONObject();
      keywordMapping.put("type", "keyword");

      JSONObject properties = new JSONObject();
      properties.put("permissions", keywordMapping);
      properties.put("sites", keywordMapping);
      properties.put("url", notAnalyzedField);

      JSONObject mappingProperties = new JSONObject();
      mappingProperties.put("properties",properties);

      return mappingProperties.toJSONString();
  }

  public String getCurrentIndex() {
    return currentIndex;
  }

  public String getIndexAlias() {
    return indexAlias;
  }

  public Integer getShards() {
    return shards;
  }

  public void setShards(Integer shards) {
    this.shards = shards;
  }

  public Integer getReplicas() {
    return replicas;
  }

  public void setReplicas(Integer replicas) {
    this.replicas = replicas;
  }

  @Override
  public String delete(String id) {
    return id;
  }

  public boolean isNeedIngestPipeline() {
    return false;
  }

  public String getPipelineName() {
    return null;
  }

  public String getAttachmentProcessor() {
    return null;
  }
}

