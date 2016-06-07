(function() {
// get plugin paths
var h = CKEDITOR.plugins.get('twitter');
var path = h.path;

// load css and javascript files
CKEDITOR.document.appendStyleSheet(CKEDITOR.getUrl(path + 'css/dialog.css'));

// find /common path, replacing last instance of the plugin name with 'common'
var pathCommon = (path + '~').replace('twitter/~', 'common/');

CKEDITOR.scriptLoader.load(path + 'js/get-dialog-html.js');
CKEDITOR.scriptLoader.load(pathCommon + 'js/embed-assets-in-editor.js');

// remove '#' from strings
var validateHashTagForUrl = function(string) {
  return string.replace(/\#/g, '');
};

// used to keep track of preview parameters in the dialog
var previewSettings = {};

// register dialog
CKEDITOR.dialog.add('twitterDialog', function(editor) {
  return {
    title: 'Embed Twitter Timeline',
    minWidth: 350,
    minHeight: 200,
    resizable: CKEDITOR.DIALOG_RESIZE_NONE,

    contents: [
      {
        id: 'settings',
        label: 'Configure Timeline',
        elements: [
          {
            type: 'html',
            id: 'setup',
            label: 'Setup',
            html: '',
            className: 'setup',
            onLoad: function() {
              $('#twitterDialog .setup').html(getTwitterDialogHtml(path + 'html/setup.html'));
            },
          },
          {
            type: 'hbox',
            widths: ['49%', '2%', '49%'],
            children: [
              {
                type: 'text',
                id: 'hashtag',
                label: 'Hashtag',
                setup: function(element) {
                  this.setValue(element.getAttribute('data-hashtag'));
                },
                commit: function(element) {
                  var hashtag = validateHashTagForUrl(this.getValue());
                  element.setAttribute('data-hashtag', hashtag);
                }
              },
              {
                type: 'html',
                id: 'or',
                className: 'or',
                html: 'or',
              },
              {
                type: 'text',
                id: 'username',
                label: 'Username',
                setup: function(element) {
                  this.setValue(element.getAttribute('data-username'));
                },
                commit: function(element) {
                  element.setAttribute('data-username', this.getValue());
                }
              }
            ]
          },
          {
            type: 'hbox',
            widths: ['34%', '33%', '33%'],
            children: [
              {
                type: 'text',
                id: 'height',
                label: 'Height',
                setup: function(element) {
                  this.setValue(element.getAttribute('data-height'));
                },
                commit: function(element) {
                  element.setAttribute('data-height', this.getValue());
                }
              },
              {
                type: 'text',
                id: 'width',
                label: 'Width',
                setup: function(element) {
                  this.setValue(element.getAttribute('data-width'));
                },
                commit: function(element) {
                  element.setAttribute('data-width', this.getValue());
                }
              },
              {
                type: 'select',
                id: 'max',
                label: '# of tweets',
                items: [
                  ['5', '5'], ['10', '10'], ['15', '15'], ['20', '20']
                ],
                setup: function(element) {
                  this.setValue(element.getAttribute('data-max'));
                },
                commit: function(element) {
                  element.setAttribute('data-max', this.getValue());
                }
              }
            ]
          }
        ]
      },
      {
        id: 'preview',
        label: 'Preview',
        elements: [
          {
            type: 'html',
            id: 'iframe',
            label: 'Preview',
            html: '',
            className: 'preview',
            onLoad: function() {
              $('#twitterDialog .preview').html(getTwitterDialogHtml(path + 'html/preview.html'));
            },
            onShow: function() {
              var dialog = this.getDialog();

              $('#twitterDialog').on('click', "[id*='cke_preview_']", function() {
                var currentPreviewSettings = {
                  hashtag: validateHashTagForUrl(dialog.getValueOf('settings', 'hashtag')),
                  username: dialog.getValueOf('settings', 'username'),
                  max: dialog.getValueOf('settings', 'max')
                };

                if (JSON.stringify(currentPreviewSettings) === JSON.stringify(previewSettings)) {
                  return;
                }

                previewSettings = currentPreviewSettings;

                url = 'https://www.edu-apps.org/twitter_lti/?embedded=1&';
                $.each(previewSettings, function(key, value) {
                  url += key + '=' + value + '&';
                });

                $('.twitter_preview').attr('src', url);
              });
            },
            setup: function(element) {
            },
            commit: function(element) {
            }
          },
        ]
      },
    ],

    onLoad: function() {
      $(this.getElement()).attr('id', 'twitterDialog');
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
      node.setAttribute('data-twitter-timeline', 'true');

      this.commitContent(node);
      var newFakeImage = editor.createFakeElement(node, 'cke_twitter_timeline', 'div', false);

      if (this.fakeImage) {
        newFakeImage.replace(this.fakeImage);
        editor.getSelection().selectElement(newFakeImage);
      } else {
        editor.insertElement(newFakeImage);
      }

      // embed the assets
      embedAssetsInCKEditor({
        editor: editor,
        id: 'ckeditor-twitter-assets',
        scripts: [
          path + 'js/embed-timeline.js',
        ],
      });
    }
  }
});
})();
