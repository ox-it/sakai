var getFolderListingDialogHtml = function(path, file) {
  var div = $('<div/>');
  var url = path + 'html/' + file;

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
