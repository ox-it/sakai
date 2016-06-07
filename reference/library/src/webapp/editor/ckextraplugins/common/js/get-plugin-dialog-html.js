var getPluginDialogHtml = function(pluginPath, htmlFile) {
  var div = $('<div/>');
  var url = pluginPath + 'html/' + htmlFile;

  $.ajax({
    url: url,
    dataType: 'html',
    async: false,
    success: function(html) {
      div.html(html);
    }
  });

  return div.html();
};

