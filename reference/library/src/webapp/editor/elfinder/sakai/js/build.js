(function($) {

$.sakai = {};
$.sakai.elfinder = {};

})(jQuery);
(function($) {

$.sakai.elfinder.query = function() {
  var query = {};
  var string = window.location.search.slice(1);
  var params = string.split('&');
  $.each(params, function(idx, param) {
    var keyValue = param.split('=');
    query[keyValue[0]] = keyValue[1];
  });

  return query;
};

})(jQuery);
// List of tools available in Sakai
;(function($) {

$.sakai.elfinder.tools = [
  'Announcements',
  'Attachments',
  'Forums',
  'Polls',
];

})(jQuery);
// User-Interface changes to elFinder 2.1 for Sakai 11
(function ($) {

var ui = $.sakai.elfinder.ui = function($elfinder) {
  // Footer
  // Adds OK and cancel buttons, so the current folder can be embeded
  this.footer = function() {
    var $footer = $('<div id="sakai-ui-footer"></div>').addClass(toClass(elements.confirmbar));
    var $OK = $('<div>OK</div>').addClass(toClass(elements.button, elements.ok));
    var $Cancel = $('<div>Cancel</div>').addClass(toClass(elements.button, elements.cancel));
    var $buttons = $('<div><div/>').append($OK, $Cancel).addClass(toClass(elements.buttons));

    return $footer.append($buttons);
  };

  // Bind the new UI elements
  $elfinder.append(this.footer());

  // Move breadcrumb trail from the footer to below the toolbar
  $elfinder.find(elements.statusbar).insertAfter('.elfinder-toolbar');

  // Move 'selected' and 'size' information from the statusbar to the footer
  $elfinder.find('.elfinder-stat-size, .elfinder-stat-selected').appendTo(elements.confirmbar);
};

var toClass = ui.toClass = function() {
  return $.makeArray(arguments).join(' ').replace(/\./g, '');
};

var elements = ui.elements = {};

/* add classes to buttons on dialog (file editing) */
ui.setSaveCloseButtons = function($dialog) {
  var $buttons = $dialog.find('.ui-button').addClass(toClass(elements.button));
  var $cancel = $($buttons[0]).addClass(toClass(elements.cancel));
  var $saveandclose = $($buttons[1]).addClass(toClass(elements.ok));
  var $save = $($buttons[2]).addClass(toClass(elements.ok));
  $saveandclose.insertAfter($save);
  $cancel.insertAfter($saveandclose);
};

elements.footer = '#sakai-ui-footer';
elements.confirmbar = 'elfinder-confirm-bar';
elements.buttons = '.buttons';
elements.button = '.button';
elements.ok = '.button-ok';
elements.cancel = '.button-cancel';
elements.statusbar = '.elfinder-statusbar';
elements.confirmbar = '.elfinder-confirm-bar';

})(jQuery);
(function ($) {

var border = 2;

// Auto resizing
// Solution by @oyejorge on
// https://github.com/Studio-42/elFinder/issues/84
$.sakai.elfinder.resizer = function($elfinder, $window) {
  $window.resize(function() {
    var winHeight = $window.height() - border;
    if ($elfinder.height() !== winHeight) {
      $elfinder.height(winHeight).resize();
    }
  });
};

})(jQuery);
(function($) {

var $window = $(window);
var query = $.sakai.elfinder.query();
var url = '/' + (query.connector || '');
var startDir = query.startdir;
var lang = query.langCode;

if (startDir) {
  // Remove first /prefix/
  startDir = startDir.split('/');
  startDir = startDir.filter(function(elem) { return elem; });
  startDir = startDir.slice(1);
  startDir = '/' + startDir.join('/') + '/content/';

  // Hashing
  startDir = btoa(startDir);
  startDir = startDir.replace('+', '-')
                     .replace('/', '_')
                     .replace('=', '.');
}

var type = query.type;
var onlyMimes = [];

if (type == 'image') {
  onlyMimes = ['image'];
} else if (type == 'flash') {
  onlyMimes = ['application/x-shockwave-flash'];
}


var testHelp = {
  type: 'dialog',
  title: 'Help',
  content: 'example',
};

var ui = $.sakai.elfinder.ui;

$.sakai.elfinder.options = {
  // Connector script
  url : url,

  // Starting directory
  startPathHash : startDir,

  // Language
  lang: lang,

  // Default view
  defaultView: 'list',

  // When a file is clicked, its data will be sent back to the editor that
  // instantiated it, and this window will close
  getFileCallback : function(file) {
    //setFileData(file);
    //$elfinder.find('#sakai-ui-footer .button-ok').click();
  },

  // Restrict to certain file types
  onlyMimes: onlyMimes,

  // Dimensions
  height: $window.height(),

  // Buttons available on the toolbar
  uiOptions: {
    toolbar : [
      ['help', testHelp],
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
      // Styles tool icons in the navbar
      getClass: function(directory) {
        /*
        var classes = '';
        //console.log(directory);
        if ($.inArray(directory.name, tools)) {
          classes = 'sakai-' + directory.name.toLowerCase();
        }
        return classes;
        */
      }
    }
  },

  // Command-specific options
  commandsOptions : {
    info: {
      // Custom properties for the info dialog
      custom: {
        // ...
      }
    },

    edit : {
      // File editors
      editors : [],
    }
  },

  // Fullscreen editor, so no resizing
  resizable: false,

  // Custom dialogs
  dialogs: {
    testHelp: {
      title: 'Help',
      content: 'Some help stuff here',
      height: '500px',
      width: 600,
    },
    anotherHelp: {
      title: 'More help',
      tabs: {
        tab1: {
          title: 'Tab3',
          content: 'Here',
        },
        tab2: {
          title: 'Tab2',
          content: 'Here',
        }
      }
    },
  }
};

})(jQuery);
(function($) {

var ui = $.sakai.elfinder.ui;

// CKEditor (html editor)
var ckeditor = (function() {
  var ckloaded = false; // ensures CKEditor is only loaded once
  var instance;         // one reference to the editor instance

  // Sets up the textarea
  var setup = function(textarea) {
    // Set the editor instance
    var editor = instance = CKEDITOR.replace(textarea.id, {
      startupFocus : true,
      fullPage: true,
      allowedContent: true,
      removePlugins: 'resize',
    });

    // Force CKEditor to resize with the dialog
    var $dialog = $(textarea).closest('.elfinder-dialog');
    $dialog.on('resize', function(event, ui) {
      var $content = $dialog.find('.ui-dialog-content');
      var height = $content.height() - 1;
      var width = $content.width();

      editor.resize(width, height);
    });
  };

  // Exposed methods
  return {
    mimes : ['text/html'],
    exts  : ['htm', 'html', 'xhtml'],
    load : function(textarea) {
      var $dialog = $(textarea).closest('.elfinder-dialog');
      ui.setSaveCloseButtons($dialog);

      if (!ckloaded) {
        $.getScript('//cdn.ckeditor.com/4.5.2/standard/ckeditor.js', function() {
          setup(textarea);
        });
        ckloaded = true;
      } else {
        setup(textarea);
      }
    },

    close : function(textarea) {
      // ...
    },

    save : function(textarea) {
      if (instance) {
        textarea.value = instance.getData();
      }
    },

    focus : function(textarea) {
      // ...
    }
  };
})();

// Codemirror (code editor)
var codemirror = (function() {
  var url = 'codemirror/';
  var codemirrorjs = url + 'lib/codemirror.js';
  var scripts = []; // keeps track of loaded codemirror js files
  var instance;     // one reference to the editor instance

  // Sets up the textarea
  var setup = function(textarea, mime) {
    var $textarea = $(textarea);
    var $dialog = $textarea.closest('.elfinder-dialog');
    var config = { lineNumbers : true };
    if (mime) config.mode = mime;

    // Set the editor instance
    var editor = instance = CodeMirror.fromTextArea(textarea, config);

    // Set current dimensions
    var $content = $dialog.find('.ui-dialog-content').addClass('elfinder-codemirror');
    var setDimensions = function() {
      var width = $content.width();
      var height = $content.height();

      editor.setSize(width, height);
    };

    // Force CodeMirror to resize with the dialog
    $dialog.on('resize', setDimensions);

    // Force resizing immediately
    setDimensions();
  };

  // Checks if a codemirror script has already been loaded
  var loaded = function(url) {
    return scripts.indexOf(url) !== -1;
  };

  // Exposed methods
  return {
    load : function(textarea) {
      var $dialog = $(textarea).closest('.elfinder-dialog');
      ui.setSaveCloseButtons($dialog);

      var mime = this.file.mime;
      var run = function() {
        var mode = CodeMirror.findModeByMIME(mime).mode;
        var script = url + '/mode/' + mode + '/' + mode + '.js';

        // Do not load the mode script if the type is null
        if (mode === 'null') {
          scripts.push(script);
        }

        // Do not load the mode script if it has already been loaded before
        if (loaded(script)) {
          setup(textarea, mime);
          return;
        }

        $.getScript(url + '/mode/' + mode + '/' + mode + '.js')
        .done(function() {
          scripts.push(script);
          setup(textarea, mime);
        }).fail(function() {
          console.log('Failed to load mode for ' + mode);
          setup(textarea);
        });
      };


      if (!loaded(codemirrorjs)) {
        $('head').append($('<link rel="stylesheet" href="' + url + 'lib/codemirror.css">'));
        $.getScript(codemirrorjs, function() {
          scripts.push(codemirrorjs);
          $.getScript(url + 'mode/meta.js', run);
        });
      } else {
        run();
      }
    },

    close : function(textarea) {
      // ...
    },

    save : function(textarea) {
      if (instance) {
        textarea.value = instance.getValue();
      }
    },

    focus : function(textarea) {
      // ...
    }
  };
})();

$.sakai.elfinder.options.commandsOptions.edit.editors = [
  ckeditor,
  codemirror,
];

})(jQuery);
(function ($) {

var ui = $.sakai.elfinder.ui;
var elements = ui.elements;

// Set handlers to deal with extra data
$.sakai.elfinder.options.handlers = {
  // When a directory opens set the @cwd to that directory
  open : function(event, instance) {
    var $footer = $(elements.footer);
    $footer.data('cwd', event.data.options.path);
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
(function($, window) {

// When the document is ready, launch elfinder
$(function() {
  var options = $.sakai.elfinder.options;
  var ui = $.sakai.elfinder.ui;
  var resizer = $.sakai.elfinder.resizer;
  var confirm = $.sakai.elfinder.confirm;
  var $elfinder = $('#elfinder');
  var $window = $(window);

  // Launch elfinder
  $elfinder.elfinder(options);

  // Bind resizing functionality
  resizer($elfinder, $window);

  // Add extra UI
  ui($elfinder);

  // Confirmation logic (when OK is clicked)
  confirm($elfinder);
});

})(jQuery, window);
