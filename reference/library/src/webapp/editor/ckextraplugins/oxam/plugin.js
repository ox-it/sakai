CKEDITOR.plugins.add('oxam', {
  requires: 'dialog,fakeobjects',
  icons: 'oxam',

  init: function(editor) {
    editor.addCommand('oxam', new CKEDITOR.dialogCommand('oxamDialog'));
    editor.ui.addButton( 'oxam', {
      label: 'Embed OXAM Search Results',
      command: 'oxam',
      toolbar: 'insert'
    });

    if (editor.contextMenu) {
      editor.addMenuGroup('oxamGroup');

      editor.addMenuItem( 'oxamItem', {
        label: 'Change OXAM Query',
        icon: this.path + 'icons/oxam.png',
        command: 'oxam',
        group: 'oxamGroup'
      });

      editor.contextMenu.addListener(function(element) {
        if (element && element.is('img') && element.data('cke-real-element-type') == 'div' && element.hasClass('cke_oxam')) {
          return { oxamItem: CKEDITOR.TRISTATE_OFF };
        }
      });
    }

    // ensure the contents css is an array
    if (!Array.isArray(CKEDITOR.config.contentsCss)) {
      CKEDITOR.config.contentsCss = [CKEDITOR.config.contentsCss];
    }

    var css = [
      this.path + 'css/contents.css',
    ];

    // only load css if they aren't already in the array
    for (i in css) {
      if (CKEDITOR.config.contentsCss.indexOf(css[i]) < 0) {
        CKEDITOR.config.contentsCss.push(css[i]);
      }
    }

    CKEDITOR.dialog.add( 'oxamDialog', this.path + 'dialogs/oxam.js' );
  },

  afterInit: function(editor) {
    var dataProcessor = editor.dataProcessor;
    var dataFilter = dataProcessor && dataProcessor.dataFilter;

    if (dataFilter) {
      dataFilter.addRules({
        elements: {
          div: function(element) {
            var returnedElement = element;

            if (element.attributes['data-oxam-embed']) {
              returnedElement = editor.createFakeParserElement(element, 'cke_oxam', 'div', false);
            }
            return returnedElement;
          }
        }
      });
    }
  }
});
