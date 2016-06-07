(function() {
// function to determine if selected element is of the type required for our dialog
var isCorrectElementType = function(element) {
  if (element && element.is) {
    // we are editing an existing element
    return element.is('img') &&
            element.data('cke-real-element-type') == 'div' &&
            element.hasClass('cke_solo_citation');
  } else if (element && element.attributes) {
    // we are converting source code div to fake item
    return element.attributes['data-solo-citation'];
  } else {
    return false;
  }
};

CKEDITOR.plugins.add('solo-citation', {
  icons: 'solo-citation',
  init: function(editor) {
    editor.addCommand('solo-citation', new CKEDITOR.dialogCommand('soloCitationDialog'));
    editor.ui.addButton('solo-citation', {
      label: 'Insert SOLO Citation',
      command: 'solo-citation',
      toolbar: 'insert'
    });

    if (editor.contextMenu) {
      editor.addMenuGroup('soloCitationGroup');
      editor.addMenuItem('soloCitationItem', {
        label: 'Edit Citation',
        icon: this.path + 'icons/solo-citation.png',
        command: 'solo-citation',
        group: 'soloCitationGroup'
      });

      editor.contextMenu.addListener(function(element) {
        if (isCorrectElementType(element)) {
          return { soloCitationItem: CKEDITOR.TRISTATE_OFF };
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

    CKEDITOR.dialog.add('soloCitationDialog', this.path + 'dialogs/solo-citation.js');
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
              returnedElement = editor.createFakeParserElement(element, 'cke_solo_citation', 'div', false);
            }

            return returnedElement;
          }
        }
      });
    }
  }
});
})();

