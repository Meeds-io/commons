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
 * Module representing an Indexing Connector Resource.
 * @exports indexingConnectorResource
 */
define('indexingConnectorResource', ['SHARED/jquery'],
    function($) {

        var AnIndexingConnectorResources = function AnIndexingConnectorResources() {
            var self = this;

            /**
             * The Connector Type
             * datatype: String
             **/
            self.type = null;

            /**
             * Does the Connector is enable or not
             * datatype: Boolean
             **/
            self.enable = null;


            self.constructFromObject = function(data) {

                self.type = data.type;

                self.enable = data.enable;

            }


            /**
             * get The Connector Type
             * @return {String}
             **/
            self.getType = function() {
                return self.type;
            }

            /**
             * set The Connector Type
             * @param {String} type
             **/
            self.setType = function (type) {
                self.type = type;
            }

            /**
             * get Does the Connector is enable or not
             * @return {Boolean}
             **/
            self.getEnable = function() {
                return self.enable;
            }

            /**
             * set Does the Connector is enable or not
             * @param {Boolean} enable
             **/
            self.setEnable = function (enable) {
                self.enable = enable;
            }


            self.toJson = function () {
                return JSON.stringify(self);
            }
        }

        return AnIndexingConnectorResources;
    }
);
