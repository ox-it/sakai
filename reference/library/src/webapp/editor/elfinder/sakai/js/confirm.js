// Handles logic for actually embedding the correct file/folder link
// TODO: Push getFileCallback logic into this file as well
// This will involve delegating the events from the $elfinder container
(function ($) {

$.sakai.elfinder.options.getFileCallback = function(file) {
  console.log(file);
};

$.sakai.elfinder.confirm = function($elfinder) {
  var ui = $.sakai.elfinder.ui;
  var elements = ui.elements;
  var $footer = $elfinder.find(elements.footer);

  console.log($footer);

  // When 'OK' is clicked, check the current file/directory and send that info
  // back to the editor (and close the window)
  $footer.on('click', '.button-ok', function(event) {
    // Check the current file
    var path = $footer.data('currentFile');

    // If no file has been selected, use the current directory
    path = path || $footer.data('currentDirectory');

    console.log(path);
    //sendToEditor(path);
  });

  // When 'Cancel' is clicked, close the window
  $footer.on('click', '.button-cancel', function(event) {
    window.close();
  });
};

})(jQuery);
