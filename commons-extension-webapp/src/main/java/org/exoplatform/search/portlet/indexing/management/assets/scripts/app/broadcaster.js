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
/**
 * Created by TClement on 12/17/15.
 */
/**
 * Module responsible to broadcast events to the controllers.
 * @exports appBroadcaster
 */
define('appBroadcaster', ['SHARED/jquery', 'operationController', 'statController', 'connectorController'],
    function($, operationController, statController, connectorController) {


        //Controller
        var myStatController = new statController();
        var myOperationController = new operationController();
        var myConnectorController = new connectorController();

        var appBroadcaster = function appBroadcaster() {
            var self = this;

            self.onReindexConnector = function() {
                myConnectorController.refreshConnectorList();
                myStatController.refreshStatNbOperation();
                myOperationController.refreshOperationList();
            }

            self.onDeleteOperation = function() {
                myOperationController.refreshOperationList();
                myStatController.refreshStatNbOperation();
            }

        }

        return appBroadcaster;
    }
);