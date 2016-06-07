/**
  * Title:        VideoSearch for YouTube
  * Description:  Converts YouTube API V3 Search results to be compatible with
                  VideoSearch.
  * Author:       Lawrence Okoth-Odida
  * Notes:        Credit to Amit Agarwal for the only working code I could find
                  for using the v2 API to fetch search results
  * Version:      0.2
  * Date:         22/07/2014

  * BASIC USAGE

  * ADVANCED USAGE

  */

var YouTubeSearchService = function(options) {
  // ensure options is defined
  if (!options) {
    var options = {};
  }
  var key    = this.key;
  var params = options.params || {};
  var url    = 'https://www.googleapis.com/youtube/v3/search';

  // fills in default settings for search query parameters
  var prepareQueryParams = function(settings) {
    return $.extend({
      key: key,
      part: 'snippet',
      order: 'relevance',
      pageToken : settings.pageToken,
      maxResults: '25',
      type: 'video'
    }, settings)
  }

  // format an individual result's object and add it to the results array
  var formatResult = function(result, results) {
    results.push({
      url: 'http://youtu.be/' + result.id.videoId,
      title: result.snippet.title,
      description: result.snippet.description,
      meta: {
        id: result.id.videoId,
        thumbnails : {
          m: result.snippet.thumbnails.medium.url
        },
        more: result.snippet
      }
    });
  }

  var resetTokensIfNewSearchTerm = function(currentSearchTerm, searchTerm){
      if (currentSearchTerm != searchTerm){
          $.fn.itemSearch.pageTokens = new Array();
          $.fn.itemSearch.currentPage = 1;
          $.fn.itemSearch.pageTokens[1] = '';
      }
      $.fn.itemSearch.currentSearchTerm = searchTerm;
  };

  var setNextPageToken = function(url, searchTerm, pageTokens, i){
      if (pageTokens[i+1]==null) {
          $.ajax({
              url: url,
              data: prepareQueryParams({q: searchTerm, pageToken: pageTokens[i]}),
              dataType: 'json',
              async: false,
              success: function (json) {
                  if (json.items.length > 0) {
                      pageTokens[i + 1] = json.nextPageToken;
                  }
              }
          });
      }
  };

  // takes search term (string) and returns array of objects representing the
  // search results from YouTube
  this.performQuery = function(searchTerm) {
    if (!key) {
      throw 'NoYouTubeApiKeySpecified';
    }

    resetTokensIfNewSearchTerm($.fn.itemSearch.currentSearchTerm, searchTerm);
    var pageTokens = $.fn.itemSearch.pageTokens;
    var currentPage = $.fn.itemSearch.currentPage;
    var results = [];

    $.ajax({
      url: url,
      data: prepareQueryParams({q: searchTerm, pageToken: pageTokens[currentPage]}),
      dataType: 'json',
      async: false,
      success: function(json) {
        if (json.items.length > 0) {
            pageTokens[currentPage+1] = json.nextPageToken;

            // if they've clicked on page 1, then get the tokens for the pages up to 7
            if (currentPage == 1) {
                for (var i = 2; i <= 6; i++) {
                    setNextPageToken(url, searchTerm, pageTokens, i);
                }
            }
            // if they've clicked on page 5 or above, get the tokens for the previous 3 pages and the next 3 pages
            else if (currentPage >= 5){
                for (var i = currentPage-3; i <= currentPage+2; i++) {
                    setNextPageToken(url, searchTerm, pageTokens, i);
                }
            }

            // go through each result, formatting them for VideoSearch
            $.each(json.items, function(key, item) {
                formatResult(item, results);
          });
        }
      }
    });

    return results;
  };
}

// alias for prototype to make setting the key a bit shorter
YouTubeSearchService.pt = YouTubeSearchService.prototype;
