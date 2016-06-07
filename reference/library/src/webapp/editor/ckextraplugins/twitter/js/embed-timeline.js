(function($){
$.fn.twitterTimeline = function() {
  var getPropertiesFromElement = function($element) {
    var properties = {};

    properties.hashtag = $element.data('hashtag');
    properties.username = $element.data('username');
    properties.max = $element.data('max');
    properties.height = $element.data('height') || 500;
    properties.width = $element.data('width') || 500;

    return properties;
  };

  var getEmbedFromProperties = function(properties) {
    var iframe = $('<iframe/>');
    var src = 'https://www.edu-apps.org/twitter_lti/?embedded=1';
        src += '&hashtag=' + properties.hashtag;
        src += '&username=' + properties.username;
        src += '&max=' + properties.max;

    iframe.addClass('twitter_timeline');
    iframe.attr('src', src);
    iframe.attr('height', properties.height);
    iframe.attr('width', properties.width);

    return iframe;
  };

  return this.each(function(i, element) {
    var $element = $(element);
    var properties = getPropertiesFromElement($element);
    var embed = getEmbedFromProperties(properties);

    $element.append(embed);
  });
};

// automatically bind to data-twitter-timeline divs
$(document).ready(function() {
  $('[data-twitter-timeline]').twitterTimeline();
});
})(jQuery);
