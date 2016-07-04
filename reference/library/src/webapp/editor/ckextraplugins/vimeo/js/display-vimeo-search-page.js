var displayVimeoSearchPage = function(path) {
  var div = $('<div/>');

  $.ajax({
    url: path + 'html/search-form.html',
    dataType: 'html',
    async: false,
    success: function(html) {
      div.html(html);
    }
  });

  return div.html();
};
