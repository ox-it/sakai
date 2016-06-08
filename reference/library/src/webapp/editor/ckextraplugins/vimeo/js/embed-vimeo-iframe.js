var embedVimeoIframe = function(node) {
  var iframe = $('<iframe width="500" height="281" frameborder="0" webkitallowfullscreen mozallowfullscreen allowfullscreen></iframe>');
  var src = node.getAttribute('data-src');

  // set the iframe src
  iframe.attr('src', 'https://player.vimeo.com/video/' + src + '?badge=0');

  // embed the iframe
  node.setHtml($('<div>').append(iframe).html());
};
