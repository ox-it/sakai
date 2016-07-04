var BindResearcherTrainingPreviewToDialog = function($dialog, dialog, previewAttributes) {
  // make clicking the tab trigger the preview
  $dialog.on('click', "[id*='cke_preview_']", function() {

    var previewWindow = $('.rttpreview');
    var div = $('<div class="courses-widget-container-preview courses-widget-container"/>');
    var dateSuffix = 'T00:00:00';

    // get data from the fields
    var attributes = {
      // selection criteria
      'providedBy':     $('.oxpoint_autocomplete input').data('uri'),
      'startingBefore': dialog.getValueOf('selection-criteria', 'starting-before') + dateSuffix,
      'startingAfter':  dialog.getValueOf('selection-criteria', 'starting-after') + dateSuffix,
      'skill':          dialog.getContentElement('selection-criteria', 'skill').getValues().join(' '),
      'eligibility':    dialog.getContentElement('selection-criteria', 'eligibility').getValues().join(' '),
      'researchMethod': dialog.getValueOf('selection-criteria', 'research-method'),

      // display settings
      'title':                dialog.getValueOf('display-settings', 'title'),
      'displayColumns':       dialog.getContentElement('display-settings', 'display-columns').getValues().join(' '),
      'defaultDatesView':     dialog.getValueOf('display-settings', 'default-dates-view'),
      'showWithoutDatesLink': dialog.getValueOf('display-settings', 'show-without-dates-link')
    };

    // go through the attributes, putting the data into the div
    for (attr in attributes) {
      if (attributes[attr] == '' || attributes[attr] === "undefined" || attributes[attr] == dateSuffix)
        delete attributes[attr];

      if (attributes[attr])
        div.attr('data-' + attr, attributes[attr]);
    }

    // fix to provide a correct frame of reference for the date
    if (!attributes.startingBefore && !attributes.startingAfter)
      div.attr('data-startingAfter' , '');

    if (JSON.stringify(previewAttributes) !== JSON.stringify(attributes)) {
      // empty the preview window and put the new div in
      previewWindow.empty().append(div);

      var anchorToCKButton = function(i, button) {
        $(this).addClass('cke_dialog_ui_button cke_dialog_ui_button_ok');
      };

      var table = previewWindow.find('.courses-widget-container-preview');

      // bind functionality to the container
      table.each(function() {
        var $this = $(this);
        if (typeof(this.isWidget) !== "undefined" && this.isWidget) {
          this.isWidget = false; // ensures the widget will be built
        }

        $this.oxfordCoursesWidget({
          dataTablesConfig: {
            fnInitComplete: function(settings, json) {
              // add CKEditor classes for uniform styling
              table.find('input').addClass('cke_dialog_ui_input_text');
              table.find('select').addClass('cke_dialog_ui_input_select');
            },
          }
        });
      });

      // add classes for easier styling of the table
      previewAttributes = attributes;
    }
  });
};
