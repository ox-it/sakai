(function ($) {

var ui = $.sakai.elfinder.ui;
var elements = ui.elements;

// Set handlers to deal with extra data
$.sakai.elfinder.options.handlers = {
  // When a directory opens set the @cwd to that directory
  open : function(event, instance) {
    var $footer = $(elements.footer);
    $footer.data('cwd', event.data.options.url);
  },

  // When a file is selected, trigger options.getFileCallback
  // Otherwise, set the embed link to the cwd
  select : function(event, instance) {
    var selected = event.data.selected;
    var $footer = $(elements.footer);

    if (!selected.length) {
      // The folder url
      var cwd = $footer.data('cwd');
      $footer.data('embed', cwd);
    } else {
      instance.exec('getfile', selected);
    }
  },
};

// When a file has been dblclicked, set the embed link to the file's path
$.sakai.elfinder.options.getFileCallback = function(file) {
  var $footer = $(elements.footer);
  $footer.data('embed', file.url);
  $footer.data('file', file);
  console.log(file);
};

// Binding embedding functionality to the OK and Cancel buttons
$.sakai.elfinder.confirm = function($elfinder) {
  var $footer = $elfinder.find(elements.footer);
  var funcNum = $.sakai.elfinder.query().CKEditorFuncNum;

  // When OK is clicked, embed the embed link
  $footer.on('click', '.button-ok', function(event) {
    var embed = $footer.data('embed');
    window.opener.CKEDITOR.tools.callFunction(funcNum, embed);
    window.close();
  });

  // When 'Cancel' is clicked, close the window
  $footer.on('click', '.button-cancel', function(event) {
    window.close();
  });
};

})(jQuery);
