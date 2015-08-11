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
      editors : [
        // CKEditor (for html files)
        {
          mimes : ['text/html'],
          exts  : ['htm', 'html', 'xhtml'],
          load : (function() {
            // Closure to create local variables
            // Ensure CKEditor is only loaded once
            var ckloaded = false;
            var setup = function(textarea) {
              var editor = CKEDITOR.replace(textarea.id, {
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

            return function(textarea) {
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
            };
          })(),

          close : function(textarea) {
          },

          save : function(textarea) {
            var instance = CKEDITOR.instances[textarea.id];
            textarea.value = instance.getData();
          },

          focus : function(textarea) {
          }
        },

        // CodeMirror
        {
          load : (function() {
            // Closure to create local variables
            // Ensure CodeMirror is only loaded once
            var url = 'codemirror/';
            var cmloaded = false;
            var setup = function(textarea, mime) {
              var $textarea = $(textarea);
              var $dialog = $textarea.closest('.elfinder-dialog');
              var config = {};
              config.lineNumbers = true;
              if (mime) config.mode = mime;

              var editor = CodeMirror.fromTextArea(textarea, config);

              // Set data instance for use later
              $textarea.data('CodeMirrorInstance', editor);

              // Set current dimensions
              var $content = $dialog.find('.ui-dialog-content');
              var setDimensions = function() {
                var height = $content.height() - 1; // -1 is for bottom border fix
                var width = $content.width();

                editor.setSize(width, height);
              };

              // Force CodeMirror to resize with the dialog
              $dialog.on('resize', setDimensions);

              // Force resizing immediately
              $dialog.trigger('resize');
            };

            return function(textarea) {
              var $dialog = $(textarea).closest('.elfinder-dialog');
              ui.setSaveCloseButtons($dialog);

              var mime = this.file.mime;
              var run = function() {
                var mode = CodeMirror.findModeByMIME(mime).mode;
                console.log(mode);

                var script = url + '/mode/' + mode + '/' + mode + '.js';

                $.getScript(url + '/mode/' + mode + '/' + mode + '.js')
                .done(function() {
                  setup(textarea, mime);
                }).fail(function() {
                  console.log('Failed to load mode for ' + mode);
                  setup(textarea);
                });
              };

              if (!cmloaded) {
                $('head').append($('<link rel="stylesheet" href="' + url + 'lib/codemirror.css">'));
                $.getScript(url + 'lib/codemirror.js', function() {
                  cmloaded = true;
                  $.getScript(url + 'mode/meta.js', run);
                });
              } else {
                run();
              }
            };
          })(),

          close : function(textarea) {
          },

          save : function(textarea) {
            var instance = $(textarea).data('CodeMirrorInstance');
            textarea.value = instance.getValue();
          },

          focus : function(textarea) {
          }
        },
      ]
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
