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
 * Module responsible to initialise the application.
 */
require(['SHARED/jquery', 'statController', 'connectorController', 'operationController', 'appBroadcaster'],
    function($, statController, connectorController, operationController, appBroadcaster){

        //Controller
        var myStatController = new statController();
        var myConnectorController = new connectorController();
        var myOperationController = new operationController();
        //Event broadcaster
        var myAppBroadcaster = new appBroadcaster();

        /**
         * Initiliaze all the controller when the doc is ready
         *
         */
        $(document).ready(
            function($) {

                //Init Stats
                myStatController.init(myAppBroadcaster);

                //Init Connector list
                myConnectorController.init(myAppBroadcaster);

                //Init Operation list
                myOperationController.init(myAppBroadcaster);

            }
        );

    }
);