(function() {
// function to determine if selected element is of the type required for our dialog
var isCorrectElementType = function(element) {
  if (element && element.is) {
    // we are editing an existing element
    return element.is('img') &&
            element.data('cke-real-element-type') == 'div' &&
            element.hasClass('cke_folder_listing');
  } else if (element && element.attributes) {
    // we are converting source code div to fake item
    return element.attributes['data-folder-listing'];
  } else {
    return false;
  }
};

CKEDITOR.plugins.add('folder-listing', {
  icons: 'folder-listing',
  init: function(editor) {
    editor.addCommand('folder-listing', new CKEDITOR.dialogCommand('folderListingDialog'));
    editor.ui.addButton('folder-listing', {
      label: 'Embed Folder Listing',
      command: 'folder-listing',
      toolbar: 'insert'
    });

    if (editor.contextMenu) {
      editor.addMenuGroup('folderListingGroup');
      editor.addMenuItem('folderListingItem', {
        label: 'Change folder',
        icon: this.path + 'icons/folder-listing.png',
        command: 'folder-listing',
        group: 'folderListingGroup'
      });

      editor.contextMenu.addListener(function(element) {
        if (isCorrectElementType(element)) {
          return { folderListingItem: CKEDITOR.TRISTATE_OFF };
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

    CKEDITOR.dialog.add('folderListingDialog', this.path + 'dialogs/folder-listing.js');
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
              returnedElement = editor.createFakeParserElement(element, 'cke_folder_listing', 'div', false);
            }

            return returnedElement;
          }
        }
      });
    }
  }
});
})();
