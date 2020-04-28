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
(function($) {
    var obj = {};
    var url = eXo.env.server.context + "/" + eXo.env.portal.rest + "/state/ping";
    obj.sendPing = function(frequency) {
        obj.sendSinglePing();
        setInterval(function() {
            if(!obj.error) {
                obj.sendSinglePing();
            }}, frequency);
    }
    obj.sendSinglePing = function() {
        $.ajax({
            url: url,
            context: this,
            error: function(xhr){
                if (xhr && xhr.status != 200) {
                    console.log("Last ping returns a status code " + xhr.status + ", stopping");
                    this.error = true;
                }
            }
        });
    };
    return obj;
})($);
