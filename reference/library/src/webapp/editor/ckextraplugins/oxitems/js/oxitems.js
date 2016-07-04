/**
  * Title:        OxItems (for jQuery)
  * Description:  Displays content from embeded RSS feeds from OxItems
  * Author:       Lawrence Okoth-Odida
  * Version:      0.1
  * Date:         21/07/2014

  * USAGE
      1. Have a div with the correct 'data-' prefixed attributes for the
         ouput_newsfeed() function found at
         https://rss.oucs.ox.ac.uk/oxonly/oxitems/output_newsfeed.php
         e.g:

          <div class="oxitem" data-channel_name="oucs/services"></div>

      2. Call the method .oxItem on these divs:

          $('.oxitem').oxItems();

         oxItems takes a literal as its parameter(s). So far, the only parameter
         is @path, which dictates the path of the iframe.html necessary for
         loading the feed (by default the path is oxitems/html/iframe.html
  */
(function($){
// Credit to Crescent Fresh @ StackOverflow
// http://stackoverflow.com/questions/984510/what-is-my-script-src-url
// Answer modified for this plugin
var scriptSource = (function(scripts) {
  var scripts = $(document.getElementsByTagName('script'));

  // match the script by the name
  scripts = scripts.filter(function(i, script) {
    var src = $(script).attr('src');

    if (src) {
      var path = src.split('/');
      var filename = path[path.length-1];

      return filename == 'oxitems.js';
    } else {
      return false;
    }
  });

  script = $(scripts[0]);

  return script.attr('src');
}());

var iframePath = scriptSource.replace('js/oxitems.js', 'html/iframe.html');

$.fn.oxItems = function() {
  // get settings from a particular feed div
  var getNewsFeedSettings = function(div) {
    var params = [
      'channel_name',
      'all_string',
      'channel_format',
      'dt_format',
      'dd_format',
      'sort_values',
      'encoding',
      'template_url',
      'startdate_range',
      'empty_newsfeed_url',
    ];
    var feedSettings = {};

    for (i in params)
      feedSettings[params[i]] = div.data(params[i]) || '';

    return feedSettings;
  };

  // turn div into news feed
  var outputNewsFeed = function(div) {
    // build feed settings into a url string
    var feedSettings = getNewsFeedSettings(div);
    var url = '';

    for (setting in feedSettings)
      url += setting + '=' + feedSettings[setting] + '&';

    // isolate output in an iframe content and get its html
    var iframe = $('<iframe src="' + iframePath + '?' + url + '" />');
    div.append(iframe.hide());

    // when the frame is loaded, get its content and stick it after the div
    iframe.load(function() {
      div.after($(this).contents().find('body').html());
      div.remove();
    });
  };

  // return this for chaining
  return this.each(function(i, div) {
    outputNewsFeed($(div));
  });
};

// automatically bind to data-oxitem divs
$(document).ready(function() {
  $('[data-oxitem]').oxItems();
});
})(jQuery);
