(function() {
// function to determine if selected element is of the type required for our dialog
var isCorrectElementType = function(element) {
  if (element && element.is) {
    // we are editing an existing element
    return element.is('img') &&
            element.data('cke-real-element-type') == 'div' &&
            element.hasClass('cke_oxpoint');
  } else if (element && element.attributes) {
    // we are converting source code div to fake item
    return element.attributes['data-oxpoint'];
  } else {
    return false;
  }
};

CKEDITOR.plugins.add('oxpoints', {
  requires: 'dialog,fakeobjects',
  icons: 'oxpoints', // icon from http://simpleicon.com/map-marker-8.html

  init: function(editor) {
    editor.addCommand( 'oxpoints', new CKEDITOR.dialogCommand('oxpointsDialog'));
    editor.ui.addButton( 'oxpoints', {
      label: 'Insert Oxpoints Map',
      command: 'oxpoints',
      toolbar: 'insert'
    });

    if (editor.contextMenu) {
      editor.addMenuGroup( 'oxpointsGroup' );
      editor.addMenuItem( 'oxpointsItem', {
        label: 'Edit OxPoints Map',
        icon: this.path + 'icons/oxpoints.png',  // icon from http://simpleicon.com/map-marker-8.html
        command: 'oxpoints',
        group: 'oxpointsGroup'
      });

      editor.contextMenu.addListener( function( element ) {
        if (isCorrectElementType(element)) {
          return { oxpointsItem: CKEDITOR.TRISTATE_OFF };
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

    CKEDITOR.dialog.add( 'oxpointsDialog', this.path + 'dialogs/oxpoints.js' );
  },

  afterInit: function(editor) {
    var dataProcessor = editor.dataProcessor;
    var dataFilter = dataProcessor && dataProcessor.dataFilter;

    if (dataFilter) {
      dataFilter.addRules({
        elements: {
          div: function(element) {
            var returnedElement = element;

            if (isCorrectElementType(element)) {
              returnedElement = editor.createFakeParserElement(element, 'cke_oxpoint', 'div', false);
            }

            return returnedElement;
          }
        }
      });
    }
  }
});
})();
