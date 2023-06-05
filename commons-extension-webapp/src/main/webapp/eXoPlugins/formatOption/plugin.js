CKEDITOR.plugins.add( 'formatOption', {

  // Register the icons. They must match command names.
  icons: 'formatOption',
  lang : ['en','fr'],

  init: function( editor ) {
    editor.addCommand('formatOption', {
      exec: function() {
        const toolbarWrapper = document.querySelector(`.${editor.id} .cke_toolgroup`);
        toolbarWrapper.classList.toggle("fullToolbar");
        if (toolbarWrapper.classList.contains('fullToolbar')) {
          document.dispatchEvent(new CustomEvent('editors-options-opened', {detail: 'displayFormatOptions'} ));
        } else {
          document.dispatchEvent(new CustomEvent('editors-options-opened', {detail: 'displayRichOptions'} ));
        }
      }
    });

    // Create the toolbar button that executes the above command.
    editor.ui.addButton( 'formatOption', {
      label: editor.lang.formatOption.buttonTooltip,
      command: 'formatOption',
      toolbar: 'insert'
    });
  }
});