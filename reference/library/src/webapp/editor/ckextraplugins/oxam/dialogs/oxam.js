(function() {
// get absolute plugin path
var h = CKEDITOR.plugins.get('oxam');
var path = h.path;
var pathCommon   = (path + '~').replace('oxam/~', 'common/');
var pathCommonWl = (path + '~').replace('oxam/~', 'common-wl/');

// load css and javascript files
CKEDITOR.document.appendStyleSheet(CKEDITOR.getUrl(path + 'css/dialog.css'));
CKEDITOR.document.appendStyleSheet(CKEDITOR.getUrl(path + 'css/results.css'));

CKEDITOR.scriptLoader.load(pathCommon + 'js/embed-assets-in-editor.js');
CKEDITOR.scriptLoader.load(pathCommonWl + 'js/embed-jquery-assets-in-editor.js');
CKEDITOR.scriptLoader.load(path + 'js/oxam-embed.js');
CKEDITOR.scriptLoader.load(path + 'js/get-oxam-data.js');
CKEDITOR.scriptLoader.load(path + 'js/get-html.js');

CKEDITOR.dialog.add('oxamDialog', function(editor) {
  return {
    title:     'Embed Preconfigured OXAM Search',
    minWidth:  500,
    minHeight: 200,
    resizable: CKEDITOR.DIALOG_RESIZE_NONE,

    contents: [
      {
        id: 'tab-search',
        label: 'Search Oxam',
        elements : [
          {
            type: 'html',
            id: 'oxamdata',
            html: '',
            className: 'oxamdata',
            onLoad: function() {
              var oxamdata = $('#oxamDialog .oxamdata');
              oxamdata.closest('tr').hide();
              getOxamData(oxamdata);
            },
          },
          {
            type: 'html',
            id: 'description',
            html: '',
            className: 'description',
            onLoad: function() {
              getOxamDescription($('#oxamDialog .description'));
            },
          },
          {
            type: 'text',
            id: 'query',
            className: 'query',
            onLoad: function() {
              $('#oxamDialog .query input').attr('placeholder', 'Use a search query or select a paper below');
            },
            setup: function(element) {
              this.setValue(element.getAttribute('data-query'));
            },
            commit: function(element) {
              element.setAttribute('data-query', this.getValue());
            },
          },
          {
            type: 'select',
            id: 'exam',
            items: [],
            className: 'exam',
            onLoad: function() {
              getOxamExamListing($('#oxamDialog .exam select'));
            },
            setup: function(element) {
              this.setValue(element.getAttribute('data-exam'));
            },
            commit: function(element) {
              element.setAttribute('data-exam', this.getValue());
            },
          },
          {
            type: 'select',
            id: 'year',
            items: [],
            className: 'year',
            onLoad: function() {
              getOxamExamYears($('#oxamDialog .year select'));
            },
            setup: function(element) {
              this.setValue(element.getAttribute('data-year'));
            },
            commit: function(element) {
              element.setAttribute('data-year', this.getValue());
            },
          },
        ]
      },
      {
        id: 'preview',
        label: 'Preview',
        elements: [
          {
            type: 'html',
            id: 'preview',
            html: '<div data-oxam-embed>',
            onLoad: function() {
              bindOxamPreviewToTab(this.getDialog(), $('#oxamDialog [id*="cke_preview"]'));
            },
            onShow: function() {
              // ...
            }
          }
        ]
      }
    ],

    onLoad: function() {
      // give dialog a class for easier styling
      var dialog = $(this.getElement()).attr('id', 'oxamDialog');
    },

    onShow: function() {
      this.fakeImage = this.oxamNode = null;
      var  fakeImage = this.getSelectedElement();

      if (fakeImage && fakeImage.data('cke-real-element-type') && fakeImage.data('cke-real-element-type') == 'div') {
        this.fakeImage = fakeImage;
        this.oxamNode = editor.restoreRealElement(fakeImage);
        this.insertMode = false;
        this.setupContent(this.oxamNode);
      } else {
        this.insertMode = true;
      }
    },

    onOk: function() {
      var oxamNode = (!this.fakeImage)? new CKEDITOR.dom.element('div') : this.oxamNode;
      oxamNode.setAttribute('data-oxam-embed', 'true');

      // commit the content to the node
      this.commitContent(oxamNode);

      // embed assets into the node
      embedAssetsInCKEditorNode({
        node: oxamNode,
        js: [path + 'js/oxam-embed.js'],
        css: [path + 'css/results.css']
      });

      // create fake image instance
      var newFakeImage = editor.createFakeElement(oxamNode, 'cke_oxam', 'div', false);

      if (this.fakeImage) {
        newFakeImage.replace(this.fakeImage);
        editor.getSelection().selectElement(newFakeImage);
      } else {
        editor.insertElement(newFakeImage);
      }

      // embed jQuery
      embedjQueryAssetsInEditor(editor, pathCommon);
    }
  };
});
})();
