CKEDITOR.plugins.add( 'creative-commons-images', {
  requires: 'dialog,fakeobjects',
  icons: 'creative-commons-images',

  init: function(editor) {
    editor.addCommand('creative-commons-images', new CKEDITOR.dialogCommand('creativeCommonsImagesDialog'));
    editor.ui.addButton('creative-commons-images', {
      label: 'Insert Creative Commons Image',
      command: 'creative-commons-images',
      toolbar: 'insert'
    });

    if (editor.contextMenu) {
      editor.addMenuGroup('creativeCommonsImagesGroup');
      editor.addMenuItem( 'creativeCommonsImagesItem', {
        label: 'Edit CC Image Properties',
        icon: this.path + 'icons/creative-commons-images.png',
        command: 'creative-commons-images',
        group: 'creativeCommonsImagesGroup'
      });

      editor.contextMenu.addListener(function(element) {
        if (element.getAscendant('img', true) && element.hasClass('ccimage')) {
          return { creativeCommonsImagesItem: CKEDITOR.TRISTATE_OFF };
        }
      });
    }

    CKEDITOR.dialog.add( 'creativeCommonsImagesDialog', this.path + 'dialogs/creative-commons-images.js' );
  }
});
