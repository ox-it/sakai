// object for handling the html display of search results
var SOLOSearchResult = function(path) {
  var resultTemplate = $('<div/>').load(path + 'html/result.html');

  this.display = function(result) {
    var template = resultTemplate.clone();

    template.find('.result').attr('data-id', result.meta.id);
    template.find('h2 a').html(result.title);
    template.find('.description').html(result.description);
    template.find('.author span').html(result.meta.author);
    template.find('.publisher span').html(result.meta.publisher);
    template.find('.libraries span').html(result.meta.libraries);
    template.find('.copies span').html(result.meta.copies);

    return template.html();
  };
};

