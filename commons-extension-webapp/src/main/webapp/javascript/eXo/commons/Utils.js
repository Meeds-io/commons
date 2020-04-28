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
 * Created by The eXo Platform SEA
 * Author : eXoPlatform
 * toannh@exoplatform.com
 * On 15/09/15
 * Utils commons
 *
 */
(function(gj) {

  var Utils = function() {}

  Utils.prototype.checkDevice = function(){
    var body = gj('body:first').removeClass('phoneDisplay').removeClass('phoneSmallDisplay').removeClass('tabletDisplay').removeClass('tabletLDisplay');
    var isMobile = body.find('.visible-phone:first').css('display') !== 'none';
    var isSmallMobile = body.find('.visible-phone-small:first').css('display') !== 'none';
    var isTablet = body.find('.visible-tablet:first').css('display') !== 'none';
    var isTabletL = body.find('.visible-tabletL:first').css('display') !== 'none';
    if (isMobile) {
      body.addClass('phoneDisplay');
    }
    if (isSmallMobile) {
      body.addClass('phoneSmallDisplay');
    }
    if (isTablet) {
      body.addClass('tabletDisplay');
    }
    if (isTabletL) {
      body.addClass('tabletLDisplay');
    }
    return {'isMobile' : isMobile,'isSmallMobile' : isSmallMobile, 'isTablet' : isTablet, 'isTabletL' : isTabletL};
  };
  Utils.prototype.urlify = function(text){
    return text.replace(/((((https?|ftp|file):\/\/)|www\.)[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])/ig, function(url){
      var value = url;
      if(url.indexOf('www.') == 0) {
        url = 'http://' + url;
      }
      return '<a href="' + url + '" target="_blank">' + value + '</a>';
    })
  };
  Utils.prototype.fetchElementByConfig = function(params){
    Utils.activityId = params.activityId || null;
    Utils.targetDiv = params.targetDiv || "";
    Utils.targetElement = params.targetElement || "";
    if (Utils.activityId == null) {
      return;
    }
    Utils.wrapDiv = Utils.targetDiv + Utils.activityId;
    var descriptionValue = gj('div#'+Utils.wrapDiv).find('.'+Utils.targetElement).first('p').text();
    //--- Urlify description
    descriptionValue = this.urlify(descriptionValue);
    //--- Update description output
    gj('div#'+Utils.wrapDiv).find('.'+Utils.targetElement).first('p').html( descriptionValue );
  };
  if(!eXo.commons) eXo.commons={};
  eXo.commons.Utils = new Utils();
  return {
    Utils : eXo.commons.Utils
  };

})(jQuery);