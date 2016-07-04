CKEDITOR.plugins.add('vimeo', {
  requires: 'dialog,fakeobjects',
  icons: 'vimeo',

  init: function(editor) {
    editor.addCommand('vimeo', new CKEDITOR.dialogCommand('vimeoDialog'));
    editor.ui.addButton( 'vimeo', {
      label: 'Embed Vimeo Video',
      command: 'vimeo',
      toolbar: 'insert'
    });

    if (editor.contextMenu) {
      editor.addMenuGroup('vimeoGroup');

      editor.addMenuItem( 'vimeoItem', {
        label: 'Edit Video',
        icon: this.path + 'icons/vimeo.png',
        command: 'vimeo',
        group: 'vimeoGroup'
      });

      editor.contextMenu.addListener(function(element) {
        if (element && element.is('img') && element.data('cke-real-element-type') == 'div' && element.hasClass('cke_vimeo')) {
          return { vimeoItem: CKEDITOR.TRISTATE_OFF };
        }
      });
    }

    // ensure the contents css is an array
    if (!Array.isArray(CKEDITOR.config.contentsCss)) {
      CKEDITOR.config.contentsCss = [CKEDITOR.config.contentsCss];
    }

    var css = [
      this.path + 'css/contents.css',
      '//maxcdn.bootstrapcdn.com/font-awesome/4.1.0/css/font-awesome.min.css',
    ];

    // only load css if they aren't already in the array
    for (i in css) {
      if (CKEDITOR.config.contentsCss.indexOf(css[i]) < 0) {
        CKEDITOR.config.contentsCss.push(css[i]);
      }
    }

    CKEDITOR.dialog.add( 'vimeoDialog', this.path + 'dialogs/vimeo.js' );
  },

  afterInit: function(editor) {
    var dataProcessor = editor.dataProcessor;
    var dataFilter = dataProcessor && dataProcessor.dataFilter;

    if (dataFilter) {
      dataFilter.addRules({
        elements: {
          div: function(element) {
            var returnedElement = element;

            if (element.attributes['data-vimeo-embed']) {
              returnedElement = editor.createFakeParserElement(element, 'cke_vimeo', 'div', false);
            }
            return returnedElement;
          }
        }
      });
    }
  }
});

