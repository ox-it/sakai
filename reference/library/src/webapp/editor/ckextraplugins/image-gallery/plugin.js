(function() {
// function to determine if selected element is of the type required for our dialog
var isCorrectElementType = function(element) {
  if (element && element.is) {
    // we are editing an existing element
    return element.is('img') &&
            element.data('cke-real-element-type') == 'div' &&
            element.hasClass('cke_image_gallery');
  } else if (element && element.attributes) {
    // we are converting source code div to fake item
    return element.attributes['data-image-gallery'];
  } else {
    return false;
  }
};

CKEDITOR.plugins.add('image-gallery', {
  icons: 'image-gallery',
  init: function(editor) {
    editor.addCommand('image-gallery', new CKEDITOR.dialogCommand('imageGalleryDialog'));
    editor.ui.addButton('image-gallery', {
      label: 'Embed Image Gallery',
      command: 'image-gallery',
      toolbar: 'insert'
    });

    if (editor.contextMenu) {
      editor.addMenuGroup('imageGalleryGroup');
      editor.addMenuItem('imageGalleryItem', {
        label: 'Change Directory',
        icon: this.path + 'icons/image-gallery.png',
        command: 'image-gallery',
        group: 'imageGalleryGroup'
      });

      editor.contextMenu.addListener(function(element) {
        if (isCorrectElementType(element)) {
          return { imageGalleryItem: CKEDITOR.TRISTATE_OFF };
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

    CKEDITOR.dialog.add('imageGalleryDialog', this.path + 'dialogs/image-gallery.js');
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
              returnedElement = editor.createFakeParserElement(element, 'cke_image_gallery', 'div', false);
            }

            return returnedElement;
          }
        }
      });
    }
  }
});
})();

