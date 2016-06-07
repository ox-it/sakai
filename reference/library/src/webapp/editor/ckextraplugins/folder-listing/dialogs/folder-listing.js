(function() {
// get plugin paths
var h = CKEDITOR.plugins.get('folder-listing');
var path = h.path;
var pathCommon   = (path + '~').replace('folder-listing/~', 'common/');
var pathCommonWl = (path + '~').replace('folder-listing/~', 'common-wl/');

// load css and javascript files
CKEDITOR.document.appendStyleSheet(CKEDITOR.getUrl(path + 'css/dialog.css'));
CKEDITOR.document.appendStyleSheet(CKEDITOR.getUrl(pathCommonWl + 'css/file-tree.css'));

CKEDITOR.scriptLoader.load(pathCommon + 'js/get-plugin-dialog-html.js');
CKEDITOR.scriptLoader.load(pathCommon + 'js/embed-assets-in-editor.js');
CKEDITOR.scriptLoader.load(pathCommonWl + 'js/embed-jquery-assets-in-editor.js');
CKEDITOR.scriptLoader.load(pathCommonWl + 'js/file-tree.js');
CKEDITOR.scriptLoader.load(pathCommonWl + 'js/folder-listing.js');
CKEDITOR.scriptLoader.load(pathCommonWl + 'js/get-user-data.js');
CKEDITOR.scriptLoader.load(pathCommonWl + 'js/get-available-sites.js');
CKEDITOR.scriptLoader.load(pathCommonWl + 'js/bind-folder-listing-to-preview.js');

// register dialog
CKEDITOR.dialog.add('folderListingDialog', function(editor) {
  return {
    title: 'Embed Folder Listing',
    minWidth: 350,
    minHeight: 200,
    resizable: CKEDITOR.DIALOG_RESIZE_NONE,

    contents: [
      {
        id: 'settings',
        label: 'Settings',
        elements: [
          {
            type: 'html',
            id: 'description',
            className: 'description',
            html: '',
            onLoad: function() {
              $('#folderListingDialog .description').html(getPluginDialogHtml(path, 'description.html'));
            },
          },
          {
            type: 'select',
            id: 'available-sites',
            label: 'Available Sites',
            items: [],
            className: 'available_sites',
            onLoad: function() {
              var dialog = this.getDialog();
              var field = this;

              // populate the select field
              var availableSites = getAvailableSites();
              var select = $('#folderListingDialog .available_sites select');

              $.each(availableSites, function(i, site) {
                var option = $('<option/>');
                    option.attr('value', site[1])
                          .html(site[0]);
                select.append(option);
              });

              $('#folderListingDialog').on('change', '.available_sites select', function() {
                var directory = field.getValue();
                var preview = $('#folderListingDialog .folder_listing_preview');

                dialog.setValueOf('settings', 'directory', '');

                bindFolderListingToPreview(preview, dialog, directory, false, path);
              });
            },
            setup: function(element) {
              var fullPath = element.getAttribute('data-directory');
              var site = getSiteFromRelativePath(fullPath);

              this.setValue(site);
              $('.available_sites select').trigger('change');

              var preview = $('#folderListingDialog .folder_listing_preview');
              var dialog = this.getDialog();

              bindFolderListingToPreview(preview, dialog, fullPath, fullPath);
            },
          },
          {
            type: 'html',
            id: 'select-directory',
            className: 'preview',
            html: '',
            onLoad: function() {
              $('#folderListingDialog .preview').html(getPluginDialogHtml(path, 'preview.html'));
            },
          },
          {
            type: 'text',
            id: 'directory',
            label: 'Active directory',
            setup: function(element) {
              var path = getDirectoryFromPath(element.getAttribute('data-directory'));
              this.setValue(path);
            },
            commit: function(element) {
              var dialog = this.getDialog();
              var fullPath = dialog.getValueOf('settings', 'available-sites') + this.getValue();

              element.setAttribute('data-directory', fullPath);
            },
          },
          {
            type: 'hbox',
            widths: ['10%', '30%', '30%', '30%'],
            children: [
              {
                type: 'html',
                id: 'show',
                html: 'Show:',
              },
              {
                type: 'checkbox',
                id: 'copyright',
                label: 'Copyright?',
                default: 'checked',
                setup: function(element) {
                  this.setValue(element.getAttribute('data-copyright') == 'true');
                },
                commit: function(element) {
                  element.setAttribute('data-copyright', this.getValue());
                }
              },
              {
                type: 'checkbox',
                id: 'resource',
                label: 'Descriptions?',
                default: 'checked',
                setup: function(element) {
                  this.setValue(element.getAttribute('data-description') == 'true');
                },
                commit: function(element) {
                  element.setAttribute('data-description', this.getValue());
                }
              },
              {
                type: 'checkbox',
                id: 'files',
                label: '# files in folder?',
                default: 'checked',
                setup: function(element) {
                  this.setValue(element.getAttribute('data-files') == 'true');
                },
                commit: function(element) {
                  element.setAttribute('data-files', this.getValue());
                }
              },
            ]
          },
        ]
      },
    ],

    onLoad: function() {
      // add id for easier css targeting
      $(this.getElement()).attr('id', 'folderListingDialog');
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
      node.setAttribute('data-folder-listing', 'true');

      // commit the content to the node
      this.commitContent(node);

      // embed assets into the node
      embedAssetsInCKEditorNode({
        node: node,
        js: [pathCommonWl + 'js/file-tree.js', pathCommonWl + 'js/folder-listing.js'],
        css: [pathCommonWl + 'css/file-tree.css']
      });

      // create fake image instance
      var newFakeImage = editor.createFakeElement(node, 'cke_folder_listing', 'div', false);

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
