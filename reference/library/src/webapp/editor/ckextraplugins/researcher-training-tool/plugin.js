(function() {
// function to determine if selected element is of the type required for our dialog
var isCorrectElementType = function(element) {
  if (element && element.is) {
    // we are editing an existing element
    return element.is('img') &&
            element.data('cke-real-element-type') == 'div' &&
            element.hasClass('cke_researcher_training_tool');
  } else if (element && element.attributes) {
    // we are converting source code div to fake item
    return element.attributes['data-researcher-training-tool'];
  } else {
    return false;
  }
};

CKEDITOR.plugins.add('researcher-training-tool', {
  requires: 'dialog,fakeobjects',
  icons: 'researcher-training-tool',

  init: function(editor) {
    editor.addCommand('researcher-training-tool', new CKEDITOR.dialogCommand('researcherTrainingToolDialog'));
    editor.ui.addButton('researcher-training-tool', {
      label: 'Embed List of Researcher Training',
      command: 'researcher-training-tool',
      toolbar: 'insert'
    });

    if (editor.contextMenu) {
      editor.addMenuGroup('researcherTrainingToolGroup');
      editor.addMenuItem('researcherTrainingToolItem', {
        label: 'Configure Courses',
        icon: this.path + 'icons/researcher-training-tool.png',
        command: 'researcher-training-tool',
        group: 'researcherTrainingToolGroup'
      });

      editor.contextMenu.addListener(function(element) {
        if (isCorrectElementType(element)) {
          return { researcherTrainingToolItem: CKEDITOR.TRISTATE_OFF };
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

    CKEDITOR.dialog.add('researcherTrainingToolDialog', this.path + 'dialogs/researcher-training-tool.js');
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
              returnedElement = editor.createFakeParserElement(element, 'cke_researcher_training_tool', 'div', false);
            }

            return returnedElement;
          }
        }
      });
    }
  }
});
})();
