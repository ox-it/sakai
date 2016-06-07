(function() {
// get plugin path
var h = CKEDITOR.plugins.get('creative-commons-images');
var path = h.path;

// find /common path, replacing last instance of the plugin name with 'common'
var pathCommon = (path + '~').replace('creative-commons-images/~', 'common/');

// load css and javascript files
CKEDITOR.document.appendStyleSheet(CKEDITOR.getUrl(pathCommon+ 'css/dialog.css'));
CKEDITOR.document.appendStyleSheet(CKEDITOR.getUrl(path + 'css/dialog.css'));

CKEDITOR.scriptLoader.load(pathCommon + 'js/itemsearch.js');
CKEDITOR.scriptLoader.load(path + 'js/service.js');
CKEDITOR.scriptLoader.load(path + 'js/result.js');
CKEDITOR.scriptLoader.load(path + 'js/get-dialog-html.js');
CKEDITOR.scriptLoader.load(path + 'js/bind-creative-commons-image-search-to-dialog.js');
CKEDITOR.scriptLoader.load(path + 'js/key.js');

CKEDITOR.dialog.add('creativeCommonsImagesDialog', function(editor) {
  return {
    title: 'Embed Creative Commons Images',
    width: 500,
    minHeight: 300,
    resizable: CKEDITOR.DIALOG_RESIZE_NONE,

    contents: [
      {
        id: 'search-cc',
        label: 'Search CC Images',
        elements: [
          {
            type: 'html',
            id: 'search-cc-form',
            html: '',
            className: 'search-form',
            onLoad: function() {
              // set the html contents then bind the search functionality
              $('#creativeCommonsImagesDialog .search-form').html(getCreativeCommonsImagesDialogHtml(path, 'search-form'));
              BindCreativeCommonsImageSearchToDialog(path, this.getDialog());
            }
          }
        ],
      },
      {
        id: 'settings',
        label: 'Settings',

        elements: [
          {
            type: 'text',
            id: 'src',
            label: 'Source',
            setup: function(element) {
              this.setValue(element.getAttribute('src'));
            },
            commit: function (element) {
              var src = this.getValue();
              if (src) {
                  element.setAttribute('src', src);
                  element.setAttribute('data-cke-saved-src', src);
                  element.setAttribute('title', 'Credit: ' + src);
              }
              else if (!this.insertMode){
                  element.removeAttribute('src');
                  element.removeAttribute('title');
              }
            }
          },
          {
            type: 'text',
            id: 'credit',
            label: 'Description/Credit',
            setup: function(element) {
              this.setValue(element.getAttribute('alt'));
            },
            commit: function (element) {
              var credit = this.getValue();
              if (credit)
                element.setAttribute('alt', credit);
              else if (!this.insertMode)
                element.removeAttribute('alt');
            }
          },
          {
            type: 'select',
            id: 'alignment',
            label: 'Alignment',
            items: [['Not Set', 'none'], ['Left', 'left'], ['Right', 'right']],
            setup: function(element) {
              var align = element.getAttribute('style');
              if (align) {
                align = align.replace(['float', ';'], '').trim();
                this.setValue(align)
              }
            },
            commit: function (element) {
              var align = this.getValue();
              if (align != 'none')
                  element.setAttribute('style', 'float: ' + align);
              else
                element.removeAttribute('style');
            }
          }
        ]
      }
    ],

    onLoad: function() {
      // set id for easier CSS selection
      $(this.getElement()).attr('id', 'creativeCommonsImagesDialog');
    },

    onShow: function() {
      var selection = editor.getSelection();
      var element = selection.getStartElement();
      if (element)
        element = element.getAscendant('img', true);

      if (!element || element.getName() != 'img' || !element.hasClass('ccimage')) {
        element = editor.document.createElement('img');
        element.addClass('ccimage');

        // Flag the insertion mode for later use.
        this.insertMode = true;
      }
      else
        this.insertMode = false;

      this.element = element;

      // Invoke the setup methods of all dialog elements, so they can load the element attributes.
      if (!this.insertMode) {
        this.setupContent(this.element);
        this.selectPage('settings');
      }
    },

    // This method is invoked once a user clicks the OK button, confirming the dialog.
    onOk: function() {
      var dialog = this;
      var img = this.element;
      this.commitContent(img);
      if (this.insertMode)
      editor.insertElement(img);
    }
  };
});
})();
