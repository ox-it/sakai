var embedTwitterTimelineIframe = function(node) {
  // load iframe properties
  var properties = {};
  properties.hashtag = node.getAttribute('data-hashtag');
  properties.username = node.getAttribute('data-username');
  properties.max = node.getAttribute('data-max');
  properties.height = node.getAttribute('data-height') || 500;
  properties.width = node.getAttribute('data-width') || 500;

  // set up the iframe
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

  // perform the embed (into the node)
  var embedCode = $('<div>').append(getEmbedFromProperties(properties));
  node.setHtml(embedCode.html());
};
