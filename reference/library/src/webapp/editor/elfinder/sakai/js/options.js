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
