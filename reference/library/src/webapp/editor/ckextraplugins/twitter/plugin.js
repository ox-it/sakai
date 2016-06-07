(function() {
// function to determine if selected element is of the type required for our dialog
var isCorrectElementType = function(element) {
  if (element && element.is) {
    // we are editing an existing element
    return element.is('img') &&
            element.data('cke-real-element-type') == 'div' &&
            element.hasClass('cke_twitter_timeline');
  } else if (element && element.attributes) {
    // we are converting source code div to fake item
    return element.attributes['data-twitter-timeline'];
  } else {
    return false;
  }
};

CKEDITOR.plugins.add('twitter', {
  requires: 'dialog,fakeobjects',
  icons: 'twitter',

  init: function(editor) {
    editor.addCommand('twitter', new CKEDITOR.dialogCommand('twitterDialog'));
    editor.ui.addButton('twitter', {
      label: 'Insert Twitter Timeline',
      command: 'twitter',
      toolbar: 'insert'
    });

    if (editor.contextMenu) {
      editor.addMenuGroup('twitterGroup');
      editor.addMenuItem('twitterItem', {
        label: 'Edit Timeline',
        icon: this.path + 'icons/twitter.png',
        command: 'twitter',
        group: 'twitterGroup'
      });

      editor.contextMenu.addListener(function(element) {
        if (isCorrectElementType(element)) {
          return { twitterItem: CKEDITOR.TRISTATE_OFF };
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

    CKEDITOR.dialog.add('twitterDialog', this.path + 'dialogs/twitter.js');
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
              returnedElement = editor.createFakeParserElement(element, 'cke_twitter_timeline', 'div', false);
            }

            return returnedElement;
          }
        }
      });
    }
  }
});
})();
