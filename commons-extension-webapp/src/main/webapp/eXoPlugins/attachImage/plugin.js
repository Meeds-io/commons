CKEDITOR.plugins.add( 'attachImage', {

  // Register the icons. They must match command names.
  icons: 'attachImage',
  lang : ['en','fr'],

  init: function( editor ) {
    editor.addCommand('attachImage', {
      exec: function() {
        document.dispatchEvent(new CustomEvent('open-file-explorer')); 
      }
    });

    // Create the toolbar button that executes the above command.
    editor.ui.addButton( 'attachImage', {
      label: editor.lang.formatOption.buttonTooltip,
      command: 'attachImage',
      toolbar: 'attach'
    });
  }
});