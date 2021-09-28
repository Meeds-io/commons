require(['SHARED/jquery', 'SHARED/tagSuggester'],function($) {
  const fillingCharSequence = CKEDITOR.tools.repeat( '\u200b', 7 );

  let suggesterShowing = false;

  function initTagSuggester(editor, config) {
    var $inputor = false;

    if (editor.mode != 'source') {
      editor.document.getBody().$.contentEditable = true;
      config = $.extend(true, {}, config, {iframe: editor.window.getFrame().$});
      $inputor = $(editor.document.getBody().$);
      $inputor.tagSuggester(config);
    } else {
      $inputor = $(editor.container.$).find(".cke_source");
      $inputor.tagSuggester(config);
    }

    if ($inputor) {
      var alias = config.alias ? '-' + config.alias + '.atwho' : '.atwho';
      $inputor.on('shown' + alias, function() {
        suggesterShowing = true;
      });
      $inputor.on('hidden' + alias, function() {
        suggesterShowing = false;
        editor.fire('change');
      });
    }
  }

  CKEDITOR.plugins.add( 'tagSuggester', {
    init : function( editor ) {
      var config = editor.config.tagSuggester;
      if (config == undefined) config = {};
      config = $.extend(true, {
        avoidReset: true,
      }, config);
      editor.on('mode', function(e) {
        initTagSuggester(this, config);
      });
      editor.on('dataReady', function(e) {
        if (editor.instanceReady) {
          initTagSuggester(this, config);
        }
      });
      editor.on('instanceReady', function() {
        initTagSuggester(editor, config);
      });
      editor.on('getData', function(evt) {
        const val = evt.data && evt.data.dataValue || '';
        evt.data.dataValue = val.replace(fillingCharSequence, '').replace(/ &nbsp;/ig, ' ');
      });
      editor.on( 'key', function( event ) {
        if (suggesterShowing && (event.data.keyCode == 13 || event.data.keyCode == 10)) {
          event.cancel();
        }
      });
    }
  });
});