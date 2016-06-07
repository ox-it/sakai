/**
  * Title:        CreativeCommonsImagesSearch for ItemSearch
  * Description:  Companion object for ItemSearch that gets CC Images
  * Author:       Lawrence Okoth-Odida
  * Version:      0.1
  * Date:         04/08/2014
  */

var CreativeCommonsImageSearchService = function(params) {
  var url = 'https://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=' + FlickrApiKey;

  // fills in default settings for search query parameters
  var prepareQueryParams = function(settings) {
    var query = $.extend({
      'sort': 'relevance',
      'license': 1,
      'per_page': 25
    }, settings);

    // check if we are searching via text or tags
    if (params.type == 'text') {
      delete query.tags;
    } else {
      delete query.text;
    }

    return query;
  }

  var getImgLink = function(item, size) {
    return 'https://farm' + item.farm + '.staticflickr.com/' + item.server + '/' + item.id + '_' + item.secret + size + '.jpg';
  }

  // format an individual result's object and add it to the results array
  var formatResult = function(result, results) {
    results.push({
      url: getImgLink(result, '_q'),
      title: result.title,
      description: result.title + ' found at http://flickr.com/photos/' + result.owner,
      thumbnails: {
          small:  getImgLink(result, '_m'),
          medium: getImgLink(result, '_z'),
          large:  getImgLink(result, '_b'),
          square: getImgLink(result, '_q'),
      }
    });
  };

  var resetTokensIfNewSearchTerm = function(currentSearchTerm, searchTerm){
      if (currentSearchTerm != searchTerm){
          $.fn.itemSearch.currentPage = 1;
      }
      $.fn.itemSearch.currentSearchTerm = searchTerm;
  };

  // takes search term (string) and returns array of objects representing the
  // search results from YouTube
  this.performQuery = function(searchTerm) {
    var results = [];
    resetTokensIfNewSearchTerm($.fn.itemSearch.currentSearchTerm, searchTerm);
    var currentPage = $.fn.itemSearch.currentPage;
    var params = prepareQueryParams({
      text: searchTerm,
      tags: searchTerm,
      format: 'json',
      page : currentPage,
      nojsoncallback: 1
    });

    $.ajax({
      url: url,
      data: params,
      dataType: 'json',
      async: false,
      success: function(json) {
        var data = json.photos.photo;

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

