var getTwitterDialogHtml = function(filename) {
  var div = $('<div/>');

  $.ajax({
    url: filename,
    dataType: 'html',
    async: false,
    success: function(html) {
      div.html(html);
    }
  });

  return div.html();
};
