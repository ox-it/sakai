// Need to fix css class references
(function($) {

$.sakai.elfinder.options.handlers = {
  // Folder Open
  'open' : function(event, instance) {
    // Update breadcrumb information
    // Update footer information
    //var folder = event.data.files[0];
    var data = event.data;
    var path = data.options.path;
    var files = data.files;
    var hash = data.cwd.hash;
    console.log(event);
    //console.log(event);
    $('#sakai-ui-footer').data('files', files);
    $('#sakai-ui-footer').data('currentDirectory', path);
  },

  // File select
  'select' : function(event, instance) {
    var $footer = $('#sakai-ui-footer');
    var files = $footer.data('files');
    var selected = event.data.selected;

    // Single select
    //console.log(selected);
    var hash = selected[0] || false;
    if (hash) {
      var file;
      $.each(files, function(i, f) {
        if (f.hash == hash) {
          file = f;
        }
      });
      //console.log(file);
      $footer.data('currentFile', file.path);
    } else {
      $footer.data('currentFile', false);
    }

    // Multiple select
    //console.log(event.data);
  },
};

})(jQuery);
