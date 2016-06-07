(function() {
// get plugin paths
var h = CKEDITOR.plugins.get('solo-citation');
var path = h.path;

// find /common path, replacing last instance of the plugin name with 'common'
var pathCommon = (path + '~').replace('solo-citation/~', 'common/');
var pathCommonWl = (path + '~').replace('solo-citation/~', 'common-wl/');

// load css and javascript files
CKEDITOR.document.appendStyleSheet(CKEDITOR.getUrl(path + 'css/dialog.css'));

CKEDITOR.scriptLoader.load(pathCommon + 'js/itemsearch.js');
CKEDITOR.scriptLoader.load(pathCommon + 'js/embed-assets-in-editor.js');
CKEDITOR.scriptLoader.load(pathCommonWl + 'js/embed-jquery-assets-in-editor.js');
CKEDITOR.scriptLoader.load(path + 'js/service.js');
CKEDITOR.scriptLoader.load(path + 'js/result.js');
CKEDITOR.scriptLoader.load(path + 'js/get-html.js');
CKEDITOR.scriptLoader.load(path + 'js/bind-solo-search-to-dialog.js');

// register dialog
CKEDITOR.dialog.add('soloCitationDialog', function(editor) {
  return {
    title: 'Embed SOLO Citation',
    minWidth: 350,
    minHeight: 200,
    resizable: CKEDITOR.DIALOG_RESIZE_NONE,

    contents: [
      {
        id: 'settings',
        label: 'Embed SOLO Citation',
        elements: [
          {
            type: 'html',
            id: 'search-form',
            html: '',
            className: 'search-form',
            onLoad: function() {
              // set the html contents then bind the search functionality
              $('#soloCitationDialog .search-form').html(getSoloCitationDialogHtml(path, 'search-form'));
              BindSoloSearchToDialog(path);
            },
            onShow: function() {
              // reset the search fields
              var soloSearchIframe = $('#soloSearchIframe').contents();
              soloSearchIframe.find('input').val('');
            },
            setup: function(element) {
              // when the frame loads, set the id to the element's data-id field
              // and run the search
              $('#soloSearchIframe').load(function() {
                var soloSearchIframe = $(this).contents();
                var soloSearchResultId = soloSearchIframe.find('#soloSearchResultId');
                var id = element.getAttribute('data-id');

                soloSearchResultId.val(id);

                if (id) {
                  soloSearchResultId.trigger('submitItemSearchForm');
                }
              });
            },
            commit: function(element) {
              var soloSearchResultId = $('#soloSearchIframe').contents().find('#soloSearchResultId');
              element.setAttribute('data-id', soloSearchResultId.val());
            },
          },
        ]
      }
    ],

    onLoad: function() {
      // set id for easier CSS selection
      $(this.getElement()).attr('id', 'soloCitationDialog');
    },

    onShow: function() {
      this.fakeImage = this.node = null;
      var fakeImage = this.getSelectedElement();

      if (fakeImage && fakeImage.data('cke-real-element-type') && fakeImage.data('cke-real-element-type') == 'div') {
        this.fakeImage = fakeImage;
        this.node = editor.restoreRealElement(fakeImage);
        this.insertMode = false;
        this.setupContent(this.node);
      } else {
        this.insertMode = true;
      }
    },

    onOk: function() {
      var node = (!this.fakeImage)? new CKEDITOR.dom.element('div') : this.node;
      node.setAttribute('data-solo-citation', 'true');

      // commit the content to the node
      this.commitContent(node);

      // embed assets into the node
      embedAssetsInCKEditorNode({
        node: node,
        js: [path + 'js/solo-citation.js'],
        css: [path + 'css/solo-citation.css']
      });

      // create fake image instance
      var newFakeImage = editor.createFakeElement(node, 'cke_solo_citation', 'div', false);

      if (this.fakeImage) {
        newFakeImage.replace(this.fakeImage);
        editor.getSelection().selectElement(newFakeImage);
      } else {
        editor.insertElement(newFakeImage);
      }

      // embed jQuery
      embedjQueryAssetsInEditor(editor, pathCommon);
    }
  }
});
})();
