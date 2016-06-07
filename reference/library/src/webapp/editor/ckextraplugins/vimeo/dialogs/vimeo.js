(function() {
// get absolute plugin path
var h = CKEDITOR.plugins.get('vimeo');
var path = h.path;

// find /common path, replacing last instance of the plugin name with 'common'
var pathCommon = (path + '~').replace('vimeo/~', 'common/');

// load css and javascript files
CKEDITOR.document.appendStyleSheet(CKEDITOR.getUrl(path + 'css/dialog.css'));

CKEDITOR.scriptLoader.load(pathCommon + 'js/itemsearch.js');
CKEDITOR.scriptLoader.load(path + 'js/service.js');
CKEDITOR.scriptLoader.load(path + 'js/result.js');
CKEDITOR.scriptLoader.load(path + 'js/bind-itemsearch-to-container.js');
CKEDITOR.scriptLoader.load(path + 'js/display-vimeo-search-page.js');
CKEDITOR.scriptLoader.load(path + 'js/embed-vimeo-iframe.js');

CKEDITOR.dialog.add('vimeoDialog', function(editor) {
  return {
    title:     'Vimeo Video Search',
    minWidth:  500,
    minHeight: 150,
    resizable: CKEDITOR.DIALOG_RESIZE_NONE,

    contents: [
      {
        id: 'tab-search',
        label: 'Search Vimeo',
        elements : [
          {
            type: 'html',
            id: 'searchpage',
            className: 'searchpage',
            html: '',
            onLoad: function() {
              $('#vimeoDialog .searchpage').html(displayVimeoSearchPage(path));
              var container = $('#vimeoSearchForm');
              var searchResults = $('#vimeoSearchResults');
              var result = new VimeoSearchResult(path);

              BindVimeoSearchToContainer(container, searchResults, result);
            },
            setup: function(element) {
              var $frame = $('#vimeoSearchIframe');
              var setUpElement = function() {
                var contents = $frame.contents();
                var value = element.getAttribute('data-title') || $('#vimeoDialog .searchResultId').val();
                contents.find('input').val(value);
                contents.find('form').submit();
              };

              setUpElement();
              $frame.load(setUpElement);
            },
            commit: function (element) {
              // use the search result id if it has been set
              var value = $('#vimeoDialog .searchResultId').val();

              if (!value) {
                value = $('#vimeoSearchIframe').contents().find('input').val();
              }

              if (value) {
                element.setAttribute('data-src', value);
              } else if (!this.insertMode) {
                element.removeAttribute('data-src');
              }

              // for finding the item again easily
              element.setAttribute('data-title', $('#vimeoDialog .videoTitle').val());
            }
          },
        ]
      }
    ],

    onLoad: function() {
      // give dialog a class for easier styling
      $(this.getElement()).attr('id', 'vimeoDialog');
    },

    onShow: function() {
      this.fakeImage = this.vimeoNode = null;
      var  fakeImage = this.getSelectedElement();

      if (fakeImage && fakeImage.data('cke-real-element-type') && fakeImage.data('cke-real-element-type') == 'div') {
        this.fakeImage = fakeImage;
        this.vimeoNode = editor.restoreRealElement(fakeImage);
        this.insertMode = false;
        this.setupContent(this.vimeoNode);
      } else {
        this.insertMode = true;
      }
    },

    onOk: function() {
      // create node to store the information
      var vimeoNode = (!this.fakeImage)? new CKEDITOR.dom.element('div') : this.vimeoNode;
      vimeoNode.setAttribute('data-vimeo-embed', 'true');

      // commit the content to the div
      this.commitContent(vimeoNode);

      // embed the iframe contents into the div
      embedVimeoIframe(vimeoNode);

      // create fake image for the editor
      var newFakeImage = editor.createFakeElement(vimeoNode, 'cke_vimeo', 'div', false);

      if (this.fakeImage) {
        newFakeImage.replace(this.fakeImage);
        editor.getSelection().selectElement(newFakeImage);
      } else {
        editor.insertElement(newFakeImage);
      }
    }
  };
});
})();

