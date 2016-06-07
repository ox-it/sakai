(function() {
// get plugin paths
var h = CKEDITOR.plugins.get('researcher-training-tool');
var path = h.path;

// find /common path, replacing last instance of the plugin name with 'common'
var pathCommon = (path + '~').replace('researcher-training-tool/~', 'common/');
var pathCommonWl = (path + '~').replace('researcher-training-tool/~', 'common-wl/');

// load css and javascript files
CKEDITOR.document.appendStyleSheet(CKEDITOR.getUrl(path + 'css/dialog.css'));
CKEDITOR.document.appendStyleSheet(CKEDITOR.getUrl(pathCommon + 'css/jquery-ui.css'));

// fix for $.browser being undefined in jQuery 1.9+ for datepicker
if (!$.browser) {
  CKEDITOR.scriptLoader.load(pathCommon + 'js/jquery-migrate-1.2.1.js');
}

// fix in case the ui library hasn't loaded yet
if (!$.fn.autocomplete) {
  CKEDITOR.scriptLoader.load(pathCommon + 'js/jquery-ui.min.js');
}

CKEDITOR.scriptLoader.load(pathCommon + 'js/embed-assets-in-editor.js');
CKEDITOR.scriptLoader.load(pathCommonWl + 'js/embed-jquery-assets-in-editor.js');
CKEDITOR.scriptLoader.load(pathCommonWl + '/js/courses-js-widget/oxford-courses-widget.js');
CKEDITOR.scriptLoader.load(pathCommonWl + 'js/oxpoints-autocomplete.js');
CKEDITOR.scriptLoader.load(path + 'js/skills.js');
CKEDITOR.scriptLoader.load(path + 'js/select-multiple-values.js');
CKEDITOR.scriptLoader.load(path + 'js/bind-researcher-training-preview-to-dialog.js');

// method for getting starting before/after date from string
var getCourseDate = function(date) {
  return (date && date.split('T')[0]) ? date.split('T')[0] : null;
};

// keeps track of attributes in preview (so preview doesn't refresh if attributes stay the same)
var previewAttributes = {};

// register dialog
CKEDITOR.dialog.add('researcherTrainingToolDialog', function(editor) {
  return {
    title: 'Embed Researcher Training Tool Listing',
    minWidth: 500,
    minHeight: 200,
    resizable: CKEDITOR.DIALOG_RESIZE_NONE,

    contents: [
      {
        // selection criteria
        id: 'selection-criteria',
        label: 'Selection Criteria',
        elements: [
          {
            type: 'text',
            id: 'provided-by',
            label: 'Provided By',
            className: 'oxpoint_autocomplete provided_by',
            onLoad: function() {
              var input = $('#researcherTrainingToolDialog .oxpoint_autocomplete input');
              input
              .attr({
                'placeholder': 'Search name of a department/college etc...',
                'data-autocomplete-type': 'organization'
              })
              .oxPointsAutoComplete({
                classes: 'researcher-training-tool-autocomplete',
                select: function(event, ui) {
                  input.attr('data-uri', ui.item.uri);
                  input.attr('data-name', input.val());
                }
              });
            },
            setup: function(element) {
              var uri = element.getAttribute('data-providedBy');
              this.setValue(uri);
              $('#researcherTrainingToolDialog .oxpoint_autocomplete input').attr('data-uri', uri);
            },
            commit: function(element) {
              var value = '';
              var uri = $('#researcherTrainingToolDialog .oxpoint_autocomplete input').data('uri');

              if (uri)
                value = uri;
              else
                value = this.getValue();

              if (value)
                element.setAttribute('data-providedBy', value);
              else if (!this.insertMode)
                element.removeAttribute('data-providedBy');
            }
          },
          {
            type: 'hbox',
            widths: ['50%', '50%'],
            children: [
              {
                type: 'text',
                id: 'starting-after',
                label: 'Starting After',
                className: 'cke_datepicker',
                setup: function(element) {
                  // parse date from the full string (everything prior to the T)
                  var date = getCourseDate(element.getAttribute('data-startingAfter'));
                  this.setValue(date);
                },
                commit: function(element) {
                  var date = this.getValue();

                  if (date)
                    element.setAttribute('data-startingAfter', date + 'T00:00:00');
                  else if (!this.insertMode)
                    element.setAttribute('data-startingAfter', '')
                }
              },
              {
                type: 'text',
                id: 'starting-before',
                label: 'Starting Before',
                className: 'cke_datepicker',
                setup: function(element) {
                  var date = getCourseDate(element.getAttribute('data-startingBefore'));
                  this.setValue(date);
                },
                commit: function(element) {
                  var date = this.getValue();

                  if (date)
                    element.setAttribute('data-startingBefore', date + 'T00:00:00');
                  else if (!this.insertMode)
                    element.removeAttribute('data-startingBefore');
                }
              }
            ]
          },
          {
            type: 'hbox',
            widths: ['40%', '40%', '20%'],
            children: [
              {
                type: 'select',
                id: 'eligibility',
                label: 'Eligibility',
                className: 'select_multiple',
                multiple: true,
                items: [
                  ['Staff', 'ST'],
                  ['Members of the University', 'OX'],
                  ['Public', 'PU'],
                ],
                setup: function(element) {
                  var values = element.getAttribute('data-eligibility');
                  if (values)
                    this.setValues(values.trim().split(' '));
                },
                commit: function(element) {
                  var values = this.getValues();
                  if (values.length)
                    element.setAttribute('data-eligibility', values.join(' '));
                  else if (!this.insertMode)
                    element.removeAttribute('data-eligibility');
                }
              },
              {
                type: 'select',
                id: 'skill',
                label: 'Skills',
                className: 'select_multiple skills',
                multiple: true,
                items: [],
                onLoad: function() {
                  // load skills into select field
                  var skills = getOxfordSkillCodes();
                  var select = $('#researcherTrainingToolDialog .skills select');

                  for (i in skills) {
                    var skill = skills[i];
                    var option = $('<option/>').html(skill[0]).val(skill[1]);

                    select.append(option);
                  }
                },
                setup: function(element) {
                  var skills = element.getAttribute('data-skill');
                  if (skills)
                    this.setValues(values.trim().split(' '));
                },
                commit: function(element) {
                  var skills = this.getValues();
                  if (skills.length)
                    element.setAttribute('data-skill', skills.join(' '));
                  else if (!this.insertMode)
                    element.removeAttribute('data-skill');
                }
              },
              {
                type: 'select',
                id: 'research-method',
                label: 'Research Method',
                items: [
                  ['', ''],
                  ['Qualitative', 'qualitative'],
                  ['Quantitative', 'quantitative'],
                ],
                setup: function(element) {
                  this.setValue(element.getAttribute('data-researchMethod'));
                },
                commit: function(element) {
                  var method = this.getValue();
                  if (method)
                    element.setAttribute('data-researchMethod', this.getValue());
                  else if (!this.insertMode)
                    element.removeAttribute('data-researchMethod');
                }
              }
            ]
          }
        ]
      },
      {
        // display settings
        id: 'display-settings',
        label: 'Display Settings',
        elements: [
          {
            type: 'text',
            id: 'title',
            label: 'Title',
            setup: function(element) {
              this.setValue(element.getAttribute('data-title'));
            },
            commit: function(element) {
              element.setAttribute('data-title', this.getValue());
            }
          },
          {
            type: 'hbox',
            widths: ['40%', '30%', '30%'],
            children: [
              {
                type: 'select',
                id: 'display-columns',
                label: 'Columns to display',
                className: 'select_multiple',
                multiple: true,
                items: [
                  ['Start', 'start'],
                  ['Title', 'title'],
                  ['Subject', 'subject'],
                  ['Provider', 'provider'],
                  ['Description', 'description'],
                  ['Venue', 'venue'],
                  ['Eligibility', 'eligibility'],
                ],
                'default': 'title',
                setup: function(element) {
                  this.setValues(element.getAttribute('data-displayColumns').trim().split(' '));
                },
                commit: function(element) {
                  var values = this.getValues();

                  if (values.length)
                    element.setAttribute('data-displayColumns', values.join(' '));
                  else if (!this.insertMode)
                    element.removeAttribute('data-displayColumns');
                }
              },
              {
                type: 'select',
                id: 'default-dates-view',
                label: 'Default dates view',
                items: [
                  ['With Dates', 'withDates'],
                  ['Wihout Dates', 'withoutDates'],
                ],
                setup: function(element) {
                  this.setValue(element.getAttribute('data-defaultDatesView'));
                },
                commit: function(element) {
                  element.setAttribute('data-defaultDatesView', this.getValue());
                }
              },
              {
                type: 'checkbox',
                id: 'show-without-dates-link',
                label: 'Show without dates link?',
                setup: function(element) {
                  this.setValue(element.getAttribute('data-showWithoutDatesLink') == 'true');
                },
                commit: function(element) {
                  element.setAttribute('data-showWithoutDatesLink', this.getValue());
                }
              }
            ]
          }
        ]
      }
    ],

    onLoad: function() {
      // give dialog a class for easier styling
      $(this.getElement()).attr('id', 'researcherTrainingToolDialog');

      var bindDatePickerToFields = function() {
        $('#researcherTrainingToolDialog .cke_datepicker input').datepicker({ dateFormat: 'yy-mm-dd' });
      };

      // load datepicker dependencies
      if (!$.browser) {
        CKEDITOR.scriptLoader.load(pathCommon + 'js/jquery-migrate-1.2.1.js', function() {
          bindDatePickerToFields();
        });
      } else {
        bindDatePickerToFields();
      }
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
      node.setAttribute('data-researcher-training-tool', 'true');
      node.setAttribute('class', 'courses-widget-container');

      // commit the content to the node
      this.commitContent(node);

      // embed assets into the node
      embedAssetsInCKEditorNode({
        node: node,
        js: [pathCommonWl + '/js/courses-js-widget/oxford-courses-widget.js'],
      });

      // create fake image instance
      var newFakeImage = editor.createFakeElement(node, 'cke_researcher_training_tool', 'div', false);

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
