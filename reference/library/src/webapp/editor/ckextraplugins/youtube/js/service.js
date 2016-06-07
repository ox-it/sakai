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
      maxResults: '5',
      type: 'video',
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

  // takes search term (string) and returns array of objects representing the
  // search results from YouTube
  this.performQuery = function(searchTerm) {
    if (!key) {
      throw 'NoYouTubeApiKeySpecified';
    }

    var results = [];

    $.ajax({
      url: url,
      data: prepareQueryParams({q: searchTerm}),
      dataType: 'json',
      async: false,
      success: function(json) {
        if (json.items.length > 0) {
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
