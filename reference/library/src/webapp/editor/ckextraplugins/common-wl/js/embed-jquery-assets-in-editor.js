// embed jquery and asset preloader into the editor
var embedjQueryAssetsInEditor = function(editor, pathCommon) {
  var jQueryPath = 'https://weblearn.ox.ac.uk/library/js/jquery/jquery-1.9.1.min.js';
  var preloaderPath = pathCommon + 'js/preload-ckeditor-assets.js';
  var data = editor.getData();
  var $data = $('<div>').append($(data));
  var jQ = jQueryPath ? jQueryPath : 'https://code.jquery.com/jquery-1.11.1.js';

  // remove existing jQuery instances
  var scripts = $data.find("script[src*='" + jQ + "']").remove();

  // remove existing preloader instances
  var preloader = $data.find("script[src*='" + preloaderPath + "']").remove();

  var script = $('<script/>').attr({
    type : 'text/javascript',
    src: jQ
  });

  // insert script that preloads assets
  $data.prepend($('<script/>').attr({
    type : 'text/javascript',
    src: preloaderPath
  }));

  // insert jquery
  $data.prepend(script);

  var instance = editor.name;
  CKEDITOR.instances[instance].setData($data.html());
};

// embed assets into a ckeditor node (div)
var embedAssetsInCKEditorNode = function(params) {
  // params presets
  params = $.extend({
    js: [], css: []
  }, params);

  // output buffer
  var outputHtml = $('<div></div>');

  console.log(params);

  // scripts
  for (i = 0; i < params.js.length; i++) {
    outputHtml.append($('<div>').attr({ 'class': 'ckeditorPluginAsset javascript', 'data-src': params.js[i]}));
  }

  // stylesheets
  for (i = 0; i < params.css.length; i++) {
    outputHtml.append($('<div>').attr({ 'class': 'ckeditorPluginAsset stylesheet', 'data-href': params.css[i]}));
  }

  // set output
  params.node.setHtml(outputHtml.html());
};
