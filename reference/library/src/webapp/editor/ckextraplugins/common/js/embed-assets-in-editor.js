// embed extra ckeditor assets
var embedExtraCkeditorAssets = function(editor, jQueryPath, preloaderPath) {
  var data = editor.getData();
  var $data = $('<div>').append($(data));
  var jQ = jQueryPath ? jQueryPath : 'https://code.jquery.com/jquery-1.11.1.js';
  var scripts = $data.find("script[src*='" + jQ + "']").remove();
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

// embed the assets into the node
embedAssetsInNode = function(node, assets) {
  var outputHtml = $('<div></div>');

  // set defaults
  if (!assets.scripts) {
    assets.scripts = [];
  }
  if (!assets.stylesheets) {
    assets.stylesheets = [];
  }

  // scripts
  for (i = 0; i < assets.scripts.length; i++) {
    outputHtml.append($('<div>').attr({ 'class': 'ckeditorPluginAsset javascript', 'data-src': assets.scripts[i]}));
  }

  // stylesheets
  for (i = 0; i < assets.stylesheets.length; i++) {
    outputHtml.append($('<div>').attr({ 'class': 'ckeditorPluginAsset stylesheet', 'data-href': assets.stylesheets[i]}));
  }

  node.setHtml(outputHtml.html());
};
