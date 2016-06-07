(function() {
// get plugin paths
var h = CKEDITOR.plugins.get('oxitems');
var path = h.path;
var pathCommon   = (path + '~').replace('oxitems/~', 'common/');

// load css and javascript files
CKEDITOR.document.appendStyleSheet(CKEDITOR.getUrl(path + 'css/dialog.css'));
CKEDITOR.document.appendStyleSheet(CKEDITOR.getUrl(path + 'lib/chosen/chosen.css'));
CKEDITOR.document.appendStyleSheet(CKEDITOR.getUrl('https://ajax.googleapis.com/ajax/libs/jqueryui/1.11.0/themes/smoothness/jquery-ui.css'));

CKEDITOR.scriptLoader.load(pathCommon + 'js/embed-assets-in-editor.js');
CKEDITOR.scriptLoader.load('https://ajax.googleapis.com/ajax/libs/jqueryui/1.11.0/jquery-ui.min.js');
CKEDITOR.scriptLoader.load(path + 'lib/chosen/chosen.js');
CKEDITOR.scriptLoader.load(path + 'js/commit-setup-select-multiple-methods.js');
CKEDITOR.scriptLoader.load(path + 'js/bind-oxitems-autocomplete.js');

// register dialog
CKEDITOR.dialog.add('oxItemsDialog', function(editor) {
  return {
    title: 'Embed OxItems Feed',
    minWidth: 350,
    minHeight: 200,
    resizable: CKEDITOR.DIALOG_RESIZE_NONE,

    contents: [
      {
        id: 'settings',
        label: 'Settings',
        elements: [
          {
            type: 'text',
            id: 'channel-name',
            label: 'Channel *',
            className: 'channel_name',
            validate: CKEDITOR.dialog.validate.notEmpty('Channel name must be provided'),
            onLoad: function() {
              // add placeholder text
              var $input = $('#oxItemsDialog .channel_name input');

              $input.attr('placeholder', 'e.g. oucs/services');
              bindOxItemsAutoCompleteToInput($input);
            },
            setup: function(element) {
              this.setValue(element.getAttribute('data-channel_name'));
            },
            commit: function(element) {
              element.setAttribute('data-channel_name', this.getValue());
            }
          },
          {
            type: 'text',
            id: 'all-string',
            label: 'Number of values to output',
            className: 'all_string',
            onLoad: function() {
              // add placeholder text
              $('#oxItemsDialog .all_string input').attr('placeholder', 'e.g. all, unexpired, atleastone, random, today, 4, ...');
            },
            setup: function(element) {
              this.setValue(element.getAttribute('data-all_string'));
            },
            commit: function(element) {
              element.setAttribute('data-all_string', this.getValue());
            }
          },
          {
            type: 'hbox',
            widths: ['50%', '50%'],
            children: [
              {
                type: 'select',
                id: 'channel-format',
                label: 'Details for output',
                multiple: true,
                className: 'channel_format to_chosen',
                items: [
                  ['Link', 'l'],
                  ['Subtitle', 's'],
                  ['Title', 't'],
                  ['Updated elements', 'u'],
                  ['Special link', 'x'],
                ],
                setup: function(element) {
                  setupSelectMultipleChosen(element, 'channel_format');
                },
                commit: function(element) {
                  commitSelectMultipleChosen(element, 'channel_format');
                }
              },
              {
                type: 'select',
                id: 'dt-format',
                label: 'Details for title of list',
                multiple: true,
                className: 'dt_format to_chosen',
                items: [
                  ['Author', 'a'],
                  ['Contents', 'c'],
                  ['Link', 'l'],
                  ['Published', 'p'],
                  ['Subtitle', 's'],
                  ['Title', 't'],
                  ['Updated elements', 'u'],
                  ['Special link', 'x'],
                  ['Empty', 'e']
                ],
                setup: function(element) {
                  setupSelectMultipleChosen(element, 'dt_format');
                },
                commit: function(element) {
                  commitSelectMultipleChosen(element, 'dt_format');
                }
              },
            ]
          },
          {
            type: 'hbox',
            widths: ['50%', '50%'],
            children: [
              {
                type: 'select',
                id: 'dd-format',
                label: 'Details for body of list',
                multiple: true,
                className: 'dd_format to_chosen',
                items: [
                  ['Author', 'a'],
                  ['Contents', 'c'],
                  ['Link', 'l'],
                  ['Published', 'p'],
                  ['Subtitle', 's'],
                  ['Title', 't'],
                  ['Updated elements', 'u'],
                  ['Special link', 'x'],
                  ['Empty', 'e']
                ],
                setup: function(element) {
                  setupSelectMultipleChosen(element, 'dd_format');
                },
                commit: function(element) {
                  commitSelectMultipleChosen(element, 'dd_format');
                }
              },
              {
                type: 'select',
                id: 'sort-values',
                label: 'Order',
                multiple: true,
                className: 'sort_values to_chosen',
                items: [
                  ['^ Author', 'Aauthor'],        ['v Author', 'Dauthor'],
                  ['^ Content', 'Acontent'],      ['v Content', 'Dcontent'],
                  ['^ ID', 'Aid'],                ['v ID', 'Did'],
                  ['^ Published', 'Apublished'],  ['v Published', 'Dpublished'],
                  ['^ Summary', 'Asummary'],      ['v Summary', 'Dsummary'],
                  ['^ Title', 'Atitle'],          ['v Title', 'Dtitle'],
                  ['^ Update', 'Aupdate'],        ['v Update', 'Dauthor']
                ],
                onLoad: function() {
                  // ...
                },
                setup: function(element) {
                  setupSelectMultipleChosen(element, 'sort_values');
                },
                commit: function(element) {
                  commitSelectMultipleChosen(element, 'sort_values');
                }
              },
            ]
          },
          {
            type: 'hbox',
            widths: ['50%', '50%'],
            children: [
              {
                type: 'select',
                id: 'encoding',
                label: 'Encoding',
                items: [
                  ['UTF-8', 'utf-8'],
                  ['ISO-8859-1', 'iso-8859-1'],
                ],
                setup: function(element) {
                  this.setValue(element.getAttribute('data-encoding'));
                },
                commit: function(element) {
                  element.setAttribute('data-encoding', this.getValue());
                }
              },
              {
                type: 'text',
                id: 'empty-newsfeed-url',
                label: 'Empty news feed URL',
                className: 'empty_newsfeed_url',
                onLoad: function() {
                  // add placeholder text
                  $('#oxItemsDialog .empty_newsfeed_url input').attr('placeholder', 'http://');
                },
                setup: function(element) {
                  this.setValue(element.getAttribute('data-empty_newsfeed_url'));
                },
                commit: function(element) {
                  element.setAttribute('data-empty_newsfeed_url', this.getValue());
                }
              }
            ]
          }
        ]
      }
    ],

    onLoad: function() {
      // give dialog an id for easier styling
      $(this.getElement()).attr('id', 'oxItemsDialog');

      var $dialog = $('#oxItemsDialog');

      $dialog.find('.to_chosen select').chosen({
        width: '100%'
      });

      // fix ui to look consistent with other fields
      $dialog.find('.chosen-choices').click(function(){
        $dialog.find('.chosen-choices').removeClass('active');
        $(this).addClass('active');
      });
    },

    onShow: function() {
      // clear Chosen select field for the next dialog
      $('.to_chosen select').val([]).trigger('chosen:updated');

      this.fakeImage = this.node = null;
      var  fakeImage = this.getSelectedElement();

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
      node.setAttribute('data-oxitem', 'true');

      this.commitContent(node);
      var newFakeImage = editor.createFakeElement(node, 'cke_oxitem', 'div', false);

      if (this.fakeImage) {
        newFakeImage.replace(this.fakeImage);
        editor.getSelection().selectElement(newFakeImage);
      } else {
        editor.insertElement(newFakeImage);
      }

      // embed the assets
      embedAssetsInCKEditor({
        editor: editor,
        id: 'ckeditor-oxitems-assets',
        scripts: [
          path + 'js/oxitems.js',
        ],
      });
    }
  };
});
})();
