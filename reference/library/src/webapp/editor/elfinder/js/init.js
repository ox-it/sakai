// Closure to protect our initialization properties and safely get jQuery
(function($) {

var init = function() {
  // General properties
  var $window = $(window);
  var $document = $(document);
/*
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
  var langCode = query.langCode;
  var type = query.type;
*/
  var onlyMimes = [];
/*
  if (type == 'image') {
    onlyMimes.push('image');
  } else if (type == 'flash') {
    onlyMimes.push('application/x-shockwave-flash');
  }*/

  // Initialize elFinder
  var $elfinder = $('#elfinder').elfinder({
    // Connector script (replace with Java connector for Sakai)
    url : 'php/sakaiExampleConnector.php',

    // When a file is clicked, its data will be sent back to the editor that
    // instantiated it, and this window will close
    getFileCallback : function(file) {
      window.opener.CKEDITOR.tools.callFunction(funcNum, file);
      window.close();
    },

    // Restrict to certain file types
    onlyMimes: onlyMimes,

    // Dimensions
    height: $window.height(),

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
    },

    // Fullscreen editor, so no resizing
    resizable: false,
  });

  var $instance = $elfinder.elfinder('instance');

  // Auto resizing
  // Solution by @oyejorge on
  // https://github.com/Studio-42/elFinder/issues/84
  $window.resize(function() {
    var winHeight = $window.height() - 2; // Accomodate for top/bottom borders
    if ($elfinder.height() !== winHeight) {
      $elfinder.height(winHeight).resize();
    }
  });
};

// Run when the document is ready
$(init);

})(jQuery);
