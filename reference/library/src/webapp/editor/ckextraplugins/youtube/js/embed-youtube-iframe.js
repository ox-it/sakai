var embedYouTubeIframe = function(node) {
  var iframe = $('<iframe width="640" height="360" frameborder="0" allowfullscreen></iframe>');
  var src = node.getAttribute('data-src');

  // set the iframe src
  iframe.attr('src', '//www.youtube.com/embed/' + src);

  // embed the iframe
  node.setHtml($('<div>').append(iframe).html());
};
