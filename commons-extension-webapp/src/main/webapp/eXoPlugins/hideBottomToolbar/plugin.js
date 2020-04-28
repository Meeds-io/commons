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
 * This is plugin to do show/hide the bottom toolbar when user focus/blur ckeditor.
 *
 * This plugin only works when the option config.toolbarLocation = 'bottom',
 * and it works tightly with skin moono-exo (which has css to force bottom toolbar hidden by default)
 */
require(['SHARED/jquery'],function($) {
  CKEDITOR.plugins.add( 'hideBottomToolbar', {
    init : function( editor ) {

      var toolbarHeight = 37;
      editor.on('instanceReady', function(evt) {
        var $bottom = $('#' + evt.editor.id + '_bottom');
        $bottom.removeClass('cke_bottom_visible').css('height', '0px');
      });
      editor.on('focus', function(evt) {
        //Fix the editor height via autogrow plugin
        evt.editor.execCommand('autogrow');

        var $ckeBottom = $('#' + evt.editor.id + '_bottom');
        var originalHeight = $ckeBottom.css('height', 'auto').outerHeight();
        toolbarHeight = $ckeBottom.addClass('cke_bottom_visible').outerHeight();
        var heightToAddVisibleClass = toolbarHeight - originalHeight;
        $ckeBottom.removeClass('cke_bottom_visible').css('height', '0px');

        var $content = $('#' + evt.editor.id + '_contents');
        var contentHeight = $content.height();

        $ckeBottom.animate({
          height: "" + toolbarHeight
        }, {
          step: function(number, tween) {
            $content.height(contentHeight - number);
            if (number >= heightToAddVisibleClass) {
              $ckeBottom.addClass('cke_bottom_visible');
            }
          }
        });
      });
      editor.on('blur', function(evt) {
        $('#' + evt.editor.id + '_contents').css('height', $('#' + evt.editor.id + '_contents').height() + toolbarHeight);
        $('#' + evt.editor.id + '_bottom').css('height', '0px');
        $('#' + evt.editor.id + '_bottom').removeClass('cke_bottom_visible');
      });
    }
  });
});