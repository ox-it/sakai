(function($) {

var ui = $.sakai.elfinder.ui;

$.sakai.elfinder.options.commandsOptions.edit.editors = [
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
];

})(jQuery);
