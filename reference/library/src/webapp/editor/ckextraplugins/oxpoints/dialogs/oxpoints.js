(function() {
// get plugin path
var h = CKEDITOR.plugins.get('oxpoints');
var path = h.path;

// find /common path, replacing last instance of the plugin name with 'common'
var pathCommon = (path + '~').replace('oxpoints/~', 'common/');
var pathCommonWl = (path + '~').replace('oxpoints/~', 'common-wl/');

// load css and javascript files
CKEDITOR.document.appendStyleSheet(CKEDITOR.getUrl(path + 'css/dialog.css'));
CKEDITOR.document.appendStyleSheet(CKEDITOR.getUrl(path + 'css/oxpoints.css'));
CKEDITOR.document.appendStyleSheet(CKEDITOR.getUrl('https://ajax.googleapis.com/ajax/libs/jqueryui/1.11.0/themes/smoothness/jquery-ui.css'));

CKEDITOR.scriptLoader.load(pathCommon + 'js/embed-assets-in-editor.js');
CKEDITOR.scriptLoader.load('https://static.data.ox.ac.uk/lib/jquery-ui/jquery-ui.min.js');
CKEDITOR.scriptLoader.load(pathCommonWl + 'js/oxpoints-autocomplete.js');
CKEDITOR.scriptLoader.load(path + 'js/get-dialog-html.js');

CKEDITOR.dialog.add('oxpointsDialog', function(editor) {
  return {
    title: 'Locate and embed OxPoint',
    minWidth: 350,
    minHeight: 200,
    resizable: CKEDITOR.DIALOG_RESIZE_NONE,

    contents: [
      {
        id: 'locator',
        label: 'OxPoint Locator',

        elements: [
          {
            type: 'html',
            id: 'description',
            html: '',
            className: 'description',
            onLoad: function() {
              $('#oxPointsDialog .description').html(getOxPointsDialogHtml(path, 'description.html'));
            },
          },
          {
            type: 'text',
            id: 'location',
            className: 'location',
            onLoad: function() {
              var $dialog = $('#oxPointsDialog');
              // bind autocomplete functionality to text input
              $dialog.find('.location input')
                .attr({
                  'placeholder': 'Search name of a department/college etc...',
                  'data-autocomplete-type': 'organization'
                })
                .oxPointsAutoComplete({
                  classes: 'oxpoints-autocomplete',
                  select: function(event, ui) {
                    var oxpoint = $dialog.find('.oxpoint');
                    if (ui.item.uri) {
                      oxpoint.attr('data-uri', ui.item.uri);
                    }
                  }
                });
            },
            setup: function(element) {
              var $dialog = $('#oxPointsDialog');
              var uri = element.getAttribute('data-uri');

              $dialog.find('.location input').attr('placeholder', element.getAttribute('data-searched'));
              $dialog.find('.oxpoint').attr('data-uri', uri);
            },
            commit: function(element) {
              var $dialog = $('#oxPointsDialog');
              var oxpoint = $dialog.find('.oxpoint');

              element.setAttribute('data-uri', oxpoint.data('uri'));
              element.setAttribute('data-searched', this.getValue());
            }
          },
          {
            type: 'hbox',
            widths: ['20%', '20%', '20%', '20%', '20%',],
            children: [
              {
                type: 'html',
                id: 'include',
                html: 'Include:',
              },
              {
                type: 'checkbox',
                id: 'inc-map',
                label: 'Map?',
                setup: function(element) {
                  this.setValue(element.getAttribute('data-inc-map') == 'true');
                },
                commit: function(element) {
                  element.setAttribute('data-inc-map', this.getValue());
                }
              },
              {
                type: 'checkbox',
                id: 'inc-add',
                label: 'Address?',
                setup: function(element) {
                  this.setValue(element.getAttribute('data-inc-add') == 'true');
                },
                commit: function(element) {
                  element.setAttribute('data-inc-add', this.getValue());
                }
              },
              {
                type: 'checkbox',
                id: 'inc-title',
                label: 'Title?',
                setup: function(element) {
                  this.setValue(element.getAttribute('data-inc-title') == 'true');
                },
                commit: function(element) {
                  element.setAttribute('data-inc-title', this.getValue());
                }
              },
              {
                type: 'checkbox',
                id: 'inc-home',
                label: 'Homepage?',
                setup: function(element) {
                  this.setValue(element.getAttribute('data-inc-home') == 'true');
                },
                commit: function(element) {
                  element.setAttribute('data-inc-home', this.getValue());
                }
              },
            ]
          },
          {
            type: 'hbox',
            widths: ['50%', '50%'],
            children: [
              {
                type: 'text',
                id: 'height',
                label: 'Map Height',
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
                label: 'Map Width',
                setup: function(element) {
                  this.setValue(element.getAttribute('data-width'));
                },
                commit: function(element) {
                  element.setAttribute('data-width', this.getValue());
                }
              },
            ]
          },
        ]
      },
    ],

    onLoad: function() {
      $(this.getElement()).attr('id', 'oxPointsDialog');
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
      node.setAttribute('data-oxpoint', 'true');

      this.commitContent(node);
      var newFakeImage = editor.createFakeElement(node, 'cke_oxpoint', 'div', false);

      if (this.fakeImage) {
        newFakeImage.replace(this.fakeImage);
        editor.getSelection().selectElement(newFakeImage);
      } else {
        editor.insertElement(newFakeImage);
      }

      // embed the assets
      embedAssetsInCKEditor({
        editor: editor,
        id: 'ckeditor-oxpoints-assets',
        scripts: [
          'http://maps.google.com/maps/api/js?sensor=false',
          path + 'js/gomap.js',
          path + 'js/oxpoint-map.js',
        ],
        stylesheets: [
          path + 'css/oxpoints.css',
        ]
      });
    }
  }
});
})();
