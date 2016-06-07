var getCreativeCommonsImagesDialogHtml = function(path, template) {
  var div = $('<div/>');

  if (!path) {
    var path = '';
  }

  $.ajax({
    url: path + 'html/' + template + '.html',
    dataType: 'html',
    async: false,
    success: function(html) {
      div.html(html);
    }
  });

  return div.html();
};
