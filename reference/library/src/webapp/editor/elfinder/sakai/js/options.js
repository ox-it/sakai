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
              CKEDITOR.replace( textarea.id, {
                startupFocus : true,
                fullPage: true,
                allowedContent: true
              });
            };

            return function(textarea) {
              ui.setSaveCloseButtons($(textarea).closest('.elfinder-dialog'));
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
            var cmloaded = false;
            var setup = function(textarea, mime) {
              var editor = CodeMirror.fromTextArea(textarea, {
                lineNumbers: true,
                mode: mime,
              });
              var $textarea = $(textarea).data('CodeMirrorInstance', editor);
              $textarea.parent().resize();
            };

            var getextension = function(filename) {
              return filename.split('.').pop().toLowerCase();
            };

            return function(textarea) {
              ui.setSaveCloseButtons($(textarea).closest('.elfinder-dialog'));
              var mime = this.file.mime;
              var run = function() {
                setup(textarea, mime);
              };

              if (!cmloaded) {
                $('head').append($('<link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/codemirror/5.5.0/codemirror.css">'));
                $.getScript('//cdnjs.cloudflare.com/ajax/libs/codemirror/5.5.0/codemirror.js', run);
                cmloaded = true;
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
