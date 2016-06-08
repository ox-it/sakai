// function to determine if selected element is of the type required for our dialog
var isCorrectElementType = function(element) {
  if (element && element.is) {
    // we are editing an existing element
    return  element.is('img') &&
            element.data('cke-real-element-type') == 'div' &&
            element.hasClass('cke_oxitem');
  } else if (element && element.attributes) {
    // we are converting source code div to fake item
    return element.attributes['data-oxitem'];
  } else {
    return false;
  }
};

CKEDITOR.plugins.add('oxitems', {
  icons: 'oxitems',
  init: function(editor) {
    editor.addCommand('oxitems', new CKEDITOR.dialogCommand('oxItemsDialog'));
    editor.ui.addButton('oxitems', {
      label: 'Insert OxItems Feed',
      command: 'oxitems',
      toolbar: 'insert'
    });

    if (editor.contextMenu) {
      editor.addMenuGroup('oxItemsGroup');
      editor.addMenuItem('oxItemsItem', {
        label: 'Configure OxItems Feed',
        icon: this.path + 'icons/oxitems.png',
        command: 'oxitems',
        group: 'oxItemsGroup'
      });

      editor.contextMenu.addListener(function(element) {
        if (isCorrectElementType(element)) {
          return { oxItemsItem: CKEDITOR.TRISTATE_OFF };
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

    CKEDITOR.dialog.add('oxItemsDialog', this.path + 'dialogs/oxitems.js');
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
              returnedElement = editor.createFakeParserElement(element, 'cke_oxitem', 'div', false);
            }

            return returnedElement;
          }
        }
      });
    }
  }
});
