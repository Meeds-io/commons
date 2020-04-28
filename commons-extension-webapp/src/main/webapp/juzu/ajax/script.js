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
  $.fn.jz = function() {
    return this.closest(".jz");
  };
  $.fn.jzURL = function(mid) {
    return this.
      jz().
      children().
      filter(function() { return $(this).data("method-id") == mid; }).
      map(function() { return $(this).data("url"); })[0];
  };
  var re = /^(.*)\(\)$/;
  $.fn.jzAjax = function(url, options) {
    if (typeof url === "object") {
      options = url;
      url = options.url;
    }
    var match = re.exec(url);
    if (match != null) {
      url = this.jzURL(match[1]);
      if (url != null) {
        options = $.extend({}, options || {});
        options.url = url;
        return $.ajax(options);
      }
    }
  };
  $.fn.jzLoad = function(url, data, complete) {
    var match = re.exec(url);
    if (match != null) {
      var repl = this.jzURL(match[1]);
      url = repl || url;
    }
    if (typeof data === "function") {
      complete = data;
      data = null;
    }
    return this.load(url, data, complete);
  };
  $.fn.jzFind = function(arg) {
    return this.jz().find(arg);
  }
})(jQuery);
