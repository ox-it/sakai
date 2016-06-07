/**
  * Title:        SOLOSearch for VideoSearch
  * Description:  Companion object for ItemSearch that gets SOLO search results
  * Author:       Lawrence Okoth-Odida
  * Version:      0.1
  * Date:         31/07/2014
  */

var SOLOSearchService = function(params) {
  var url = 'https://api.m.ox.ac.uk/library/search';

  // fills in default settings for search query parameters
  var prepareQueryParams = function(settings) {
    var query = $.extend({
      count: '10'
    }, settings);

    query = $.extend(query, params);

    query.count = query.count || 10;
    delete query.form; // form was added in ItemSearch 0.1.2 (not needed in ajax call)

    return query;
  }

  // format an individual result's object and add it to the results array
  var formatResult = function(result, results) {
    results.push({
      url: null,
      title: result.title,
      description: result.description,
      meta: result
    });
  };

  var resetTokensIfNewSearchTerm = function(currentSearchTerm, searchTerm){
      if (currentSearchTerm != searchTerm){
          $.fn.itemSearch.currentPage = 1;
      }
      $.fn.itemSearch.currentSearchTerm = searchTerm;
  };

  // takes search term (string) and returns array of objects representing the
  // search results from SOLO
  this.performQuery = function(searchTerm) {
    var results = [];
      resetTokensIfNewSearchTerm($.fn.itemSearch.currentSearchTerm, searchTerm);
      var params = prepareQueryParams({title: searchTerm});
      params.start = ($.fn.itemSearch.currentPage-1) * params.count;
      var ajaxUrl = url;

    if (params.id) {
      ajaxUrl = 'https://api.m.ox.ac.uk/library/item:' + params.id + '/';
      params = {};
    }

    $.ajax({
      url: ajaxUrl,
      data: params,
      dataType: 'json',
      async: false,
      success: function(json) {
        var data = json['_embedded']['items'] || [json];

        if (data.length) {
          // go through each result, formatting them for ItemSearch
          $.each(data, function(key, item) {
            formatResult(item, results);
          });
        }
      },
    });

    return results;
  };
};
