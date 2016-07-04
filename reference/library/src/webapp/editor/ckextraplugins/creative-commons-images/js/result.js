// object for handling the html display of search results
var CreativeCommonsImageSearchResult = function(path) {
  var resultTemplate = $('<div/>').load(path + 'html/result.html');

  this.display = function(result) {

    var template = resultTemplate.clone();

    template.find('img').attr('data-src', result.thumbnails.square)
                        .attr('src', result.thumbnails.square);
    template.find('.sizes').attr('data-title', result.description);
    template.find('.small').attr('href', result.thumbnails.small);
    template.find('.medium').attr('href', result.thumbnails.medium);
    template.find('.large').attr('href', result.thumbnails.large);
    template.find('.square').attr('href', result.thumbnails.square);

    return template.html();
  };
};
