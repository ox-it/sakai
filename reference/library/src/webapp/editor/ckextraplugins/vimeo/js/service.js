/**
  * Title:        VideoSearch for Vimeo
  * Description:  Converts Vimeo Search results from EduApp's interface to be
                  compatible with VideoSearch.
  * Author:       Lawrence Okoth-Odida
  * Version:      0.1
  * Date:         30/07/2014

  * BASIC USAGE

  * ADVANCED USAGE

  */

var VimeoSearchService = function(options) {
  // ensure options is defined
  if (!options) {
    var options = {};
  }
  var key    = this.key;
  var params = options.params || {};
  var url    = 'https://www.edu-apps.org/lti_public_resources/api/search';

  // format an individual result's object and add it to the results array
  var formatResult = function(result, results) {
    results.push({
      url: result.embeded_url,
      title: result.title,
      description: result.description,
      meta: {
        id: result.id,
        thumbnails : {
          m: result.thumbnail_url
        },
        more: result
      }
    });
  }

  // takes search term (string) and returns array of objects representing the
  // search results from Vimeo
  this.performQuery = function(searchTerm) {

    var results = [];

    $.ajax({
      url: url,
      data: { 'tool_id': 'vimeo', query: searchTerm },
      type: 'POST',
      dataType: 'json',
      async: false,
      success: function(json) {
        var data = json['driver_response'] || { items: [] };

        if (data.items.length) {
          // go through each result, formatting them for VideoSearch
          $.each(data.items, function(key, item) {
            formatResult(item, results);
          });
        }
      }
    });

    return results;
  };
};
