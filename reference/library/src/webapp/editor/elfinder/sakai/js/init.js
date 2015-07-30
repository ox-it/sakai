// Closure to protect our initialization properties and safely get jQuery
(function($) {

var init = function() {
  // General properties
  var $window = $(window);
  var $document = $(document);

  // Get parameters from window's URL string
  var parseGetQuery = function() {
    var query = {};
    var string = window.location.search.slice(1);
    var params = string.split('&');
    $.each(params, function(idx, param) {
      var keyValue = param.split('=');
      query[keyValue[0]] = keyValue[1];
    });

    return query;
  };

  var query = parseGetQuery();
  var funcNum = query.CKEditorFuncNum;
  var lang = query.langCode;
  var type = query.type;
  var url = '/' + (query.connector || '');
  var border = 2; // border padding (for calculating height)
  var sendToEditor = function(data) {
    window.opener.CKEDITOR.tools.callFunction(funcNum, data);
    window.close();
  };

  // Restricted Mimes
  var onlyMimes = [];

  if (type == 'images') {
    onlyMimes.push('images');
  } else if (type == 'flash') {
    onlyMimes.push('application/x-shockwave-flash');
  }

  // Initial directory
  var startDir;

  if (query.startdir) {
    var startDir = query.startdir;
    startDir = btoa(startDir);
    startDir = startDir.replace('+', '-')
                       .replace('/', '_')
                       .replace('=', '.');
  }

  // Initialize elFinder
  var $elfinder = $('#elfinder');

  $elfinder.elfinder({
    // Connector script
    url : url,

    // Starting directory
    startDir : startDir,

    // Language
    lang: lang,

    // When a file is clicked, its data will be sent back to the editor that
    // instantiated it, and this window will close
    getFileCallback : function(file) {
      setFileData(file);
      $elfinder.find('#sakai-ui-footer .button-ok').click();
    },

    // Restrict to certain file types
    onlyMimes: onlyMimes,

    // Dimensions
    height: $window.height() - border,

    // Buttons available on the toolbar
    uiOptions: {
      toolbar : [
        ['help'],
        ['back', 'forward'],
        ['reload'],
        ['home', 'up'],
        ['mkdir', 'upload'],
        ['open', 'download', 'getfile'],
        ['info'],
        ['quicklook'],
        ['copy', 'cut', 'paste'],
        ['search'],
        ['view'],
      ],

      tree : {
        getClass: function(directory) {
          var classes = '';
          //console.log(directory);
          if ($.inArray(directory.name, tools)) {
            classes = 'sakai-' + directory.name.toLowerCase();
          }
          return classes;
        }
      }
    },

    // Custom properties for the info dialog
    commandsOptions : {
      info: {
        custom: {
          // Description
          desc : {
            label: 'Description',

            // Template
            tpl: '<div id="elfinder-fm-file-desc">somedesc</div>',

            // Only for html files
            mimes: ['text/html'],

            // Data requests
            action: function(file, fm, dialog, msg) {
              /*
              // Request for data
              fm.request({
                data : {
                  cmd : 'desc',
                  target: file.hash,
                },
                preventDefault: true,
              })
              .done(function(data) {
                var $desc = dialog.find('#elfinder-fm-file-desc');
                $desc.html(data.desc);
              });
              */
            },
          },
        }
      },
    },

    // Fullscreen editor, so no resizing
    resizable: false,
  });

  var $instance = $elfinder.elfinder('instance');

  // Add extra UI elements
  var $footer = UI.footer();
  $elfinder.append($footer);

  // Move breadcrumb trail
  $elfinder.find('.elfinder-statusbar').insertAfter('.elfinder-toolbar');

  // Move 'selected' and sizeinformation to the footer
  $elfinder.find('.elfinder-stat-size, .elfinder-stat-selected').appendTo('.elfinder-confirm-bar');

  // Bind callbacks
  $.each(callbacks, function(name, callback) {
    $instance.bind(name, callback);
  });

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

  // Auto resizing
  // Solution by @oyejorge on
  // https://github.com/Studio-42/elFinder/issues/84
  $window.resize(function() {
    var winHeight = $window.height() - border;
    if ($elfinder.height() !== winHeight) {
      $elfinder.height(winHeight).resize();
    }
  });
};

// Extra UI functionality
var UI = {};

// Footer
// Adds OK and cancel buttons, so the current folder can be embeded
UI.footer = function() {
  var $footer = $('<div id="sakai-ui-footer" class="elfinder-confirm-bar"></div>');
  var $OK = $('<div class="button button-ok">OK</div>');
  var $Cancel = $('<div class="button button-cancel">Cancel</div>');
  var $buttons = $('<div class="buttons"><div/>').append($OK, $Cancel);

  return $footer.append($buttons);
};

// Tool names
var tools = [
  'Announcements',
  'Attachments',
  'Forums',
  'Polls',
  'Resources',
];

var setFileData = function(file) {
  $('#sakai-ui-footer').data('currentFile', file.path);
};

// Callbacks
var callbacks = {
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

// Run when the document is ready
$(init);

})(jQuery);
