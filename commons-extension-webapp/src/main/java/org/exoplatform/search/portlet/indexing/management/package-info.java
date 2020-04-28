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

@juzu.Application(defaultController = IndexingManagementApplication.class)
@Portlet
@Scripts({
    @Script(id = "apiClient", value = "scripts/rest/client/ApiClient.js"),
    @Script(id = "indexingOperationResource", value = "scripts/rest/model/AnIndexingOperationResource.js"),
    @Script(id = "indexingConnectorResources", value = "scripts/rest/model/AnIndexingConnectorResources.js"),
    @Script(id = "indexingManagementApi" , value = "scripts/rest/api/VindexingManagementApi.js"),
    @Script(id = "statController" , value = "scripts/app/controller/statController.js"),
    @Script(id = "connectorController" , value = "scripts/app/controller/connectorController.js"),
    @Script(id = "operationController" , value = "scripts/app/controller/operationController.js"),
    @Script(id = "appBroadcaster" , value = "scripts/app/broadcaster.js"),
    @Script(id = "main" , value = "scripts/app/main.js", depends = {"statController", "connectorController", "operationController", "appBroadcaster"})
})
@Less({
    @Stylesheet(id = "indexingManagement-less", value = "styles/indexingManagement.less")
})
@Assets("*")
package org.exoplatform.search.portlet.indexing.management;

import juzu.plugin.asset.Assets;
import juzu.plugin.asset.Script;
import juzu.plugin.asset.Scripts;
import juzu.plugin.asset.Stylesheet;
import juzu.plugin.less4j.Less;
import juzu.plugin.portlet.Portlet;