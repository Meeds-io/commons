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
require(['SHARED/jquery', 'SHARED/suggester'],function($) {
  var defaultOptions = {
    type: 'mix'
  };
  var suggesterShowing = false;

  var fillingCharSequence = CKEDITOR.tools.repeat( '\u200b', 7 );
  function initSuggester(editor, config) {
    var $inputor = false;

    if (editor.mode != 'source') {
      editor.document.getBody().$.contentEditable = true;
      config = $.extend(true, {}, config, {iframe: editor.window.getFrame().$});
      $inputor = $(editor.document.getBody().$);
      $inputor.suggester(config);
    } else {
      $inputor = $(editor.container.$).find(".cke_source");
      $inputor.suggester(config);
    }

    if ($inputor) {
      var alias = config.alias ? '-' + config.alias + '.atwho' : '.atwho';
      $inputor.on('shown' + alias, function() {
        suggesterShowing = true;
      });
      $inputor.on('hidden' + alias, function() {
        suggesterShowing = false;
      });
    }
  }

  function getContent(editor, textData) {
    var val = textData;
    if (editor.mode != 'source') {
      val = $(editor.document.getBody().$).suggester("replaceMentions", textData);
    } else {
      val = $(editor.container.$).find(".cke_source").suggester("replaceMentions", textData);
    }
    val = val.replace( fillingCharSequence, '');
    val = val.replace(/ &nbsp;/ig, ' ');

    return val;
  }

  CKEDITOR.plugins.add( 'suggester', {
    init : function( editor ) {
      var config = editor.config.suggester;
      if (config == undefined) config = {};
      config = $.extend(true, {}, defaultOptions, config);
      
      editor.addContentsCss( $('#ckeditor-suggester').attr('href') );

      editor.on('mode', function(e) {
        initSuggester(this, config);
      });
      editor.on('instanceReady', function() {
        //initSuggester(editor, config);
      });
      editor.on('getData', function(evt) {
        var data = evt.data;
        data.dataValue = getContent(evt.editor, data.dataValue);
      });
      editor.on( 'key', function( event ) {
        if (suggesterShowing && (event.data.keyCode == 13 || event.data.keyCode == 10)) {
          event.cancel();
        }
      });
    }
  });
});