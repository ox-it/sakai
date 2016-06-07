(function() {
// get plugin paths
var h = CKEDITOR.plugins.get('image-gallery');
var path = h.path;
var pathCommon   = (path + '~').replace('image-gallery/~', 'common/');
var pathCommonWl = (path + '~').replace('image-gallery/~', 'common-wl/');

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
CKEDITOR.dialog.add('imageGalleryDialog', function(editor) {
  return {
    title: 'Embed Image Gallery',
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
              $('#imageGalleryDialog .description').html(getPluginDialogHtml(path, 'description.html'));
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
              var select = $('#imageGalleryDialog .available_sites select');

              $.each(availableSites, function(i, site) {
                var option = $('<option/>');
                    option.attr('value', site[1])
                          .html(site[0]);
                select.append(option);
              });

              $('#imageGalleryDialog').on('change', '.available_sites select', function() {
                var directory = field.getValue();
                var preview = $('#imageGalleryDialog .folder_listing_preview');

                bindFolderListingToPreview(preview, dialog, directory, false, path);
              });
            },
            setup: function(element) {
              var fullPath = element.getAttribute('data-directory');
              var site = getSiteFromRelativePath(fullPath);

              this.setValue(site);
              $('#imageGalleryDialog .available_sites select').trigger('change');

              var preview = $('#imageGalleryDialog .folder_listing_preview');
              var dialog = this.getDialog();

              bindFolderListingToPreview(preview, dialog, fullPath, fullPath, path);
            },
          },
          {
            type: 'html',
            id: 'select-directory',
            className: 'preview',
            html: '',
            onLoad: function() {
              $('#imageGalleryDialog .preview').html(getPluginDialogHtml(path, 'preview.html'));
            },
          },
          {
            type: 'text',
            id: 'directory',
            label: 'Folder containing images',
            setup: function(element) {
              var dirPath = getDirectoryFromPath(element.getAttribute('data-directory'));
              this.setValue(dirPath);
            },
            commit: function(element) {
              var dialog = this.getDialog();
              var fullPath = dialog.getValueOf('settings', 'available-sites') + this.getValue();

              element.setAttribute('data-directory', fullPath);
            }
          },
          {
              type: 'text',
              id: 'description',
              label: 'Description',
              className: 'hide',
              setup: function(element) {
                  this.setValue(element.getAttribute('data-description'));
              },
              commit: function(element) {
                  element.setAttribute('data-description', this.getValue());
              }
          },
          {
            type: 'text',
            id: 'rel',
            label: 'Slideshow ID (optional)',
            className: 'rel',
            onLoad: function() {
              $('#imageGalleryDialog .rel input').attr('placeholder', 'Short identifier for images in this gallery');
            },
            setup: function(element) {
              this.setValue(element.getAttribute('data-rel'));
            },
            commit: function(element) {
              element.setAttribute('data-rel', this.getValue());
            }
          },
          {
            type: 'hbox',
            widths: ['30%', '30%', '40%'],
            children: [
              {
                type: 'text',
                id: 'max-width',
                label: 'Max Width',
                setup: function(element) {
                  this.setValue(element.getAttribute('data-maxWidth'));
                },
                commit: function(element) {
                  element.setAttribute('data-maxWidth', this.getValue());
                }
              },
              {
                type: 'text',
                id: 'max-height',
                label: 'Max Height',
                setup: function(element) {
                  this.setValue(element.getAttribute('data-maxHeight'));
                },
                commit: function(element) {
                  element.setAttribute('data-maxHeight', this.getValue());
                }
              },
              {
                type: 'text',
                id: 'slideshow-speed',
                label: 'Slideshow Speed (secs)',
                className: 'slideshowSpeed',
                onLoad: function() {
                  $('#imageGalleryDialog .slideshowSpeed input').attr('placeholder', '0 to disable slideshow');
                },
                setup: function(element) {
                  this.setValue(element.getAttribute('data-slideshowSpeed'));
                },
                commit: function(element) {
                  element.setAttribute('data-slideshowSpeed', this.getValue());
                }
              },
            ]
          },
        ]
      },
    ],

    onLoad: function() {
      $(this.getElement()).attr('id', 'imageGalleryDialog');
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
      $('.available_sites select').trigger('change');
    },

    onOk: function() {
      var node = (!this.fakeImage)? new CKEDITOR.dom.element('div') : this.node;
      node.setAttribute('data-image-gallery', 'true');

      // commit the content to the node
      this.commitContent(node);

      // embed assets into the node
      embedAssetsInCKEditorNode({
        node: node,
        js: [path + 'lib/colorbox/colorbox.js', path + 'js/image-gallery.js'],
        css: [path + 'lib/colorbox/colorbox.css', path + 'css/image-gallery.css']
      });

      // create fake image instance
      var newFakeImage = editor.createFakeElement(node, 'cke_image_gallery', 'div', false);

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
