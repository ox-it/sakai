/**
  * Title:        SoloCitation
  * Description:  Replaces placeholder divs with citation references to Oxford's
                  SOLO library system
  * Author:       Lawrence Okoth-Odida
  * Version:      0.1
  * Date:         01/08/2014

  * BASIC USAGE
      1. Create a div with a data-solo-citation attribute and a data-id attribute
         whose value is the ID of an item in SOLO. E.g.

         <div data-solo-citation data-id="010000555" />

      2. Target the div with soloCitation():

         $('[data-solo-citation]').soloCitation();

         At the point in the page where the reference was made, a block of data
         with a link to the item's Mobile Oxford page will be embedded.

  * ADVANCED USAGE
      soloCitation takes a literal as its parameters.

      @param citationsContainer {jQuery object}
        If you specify a container for the citations, the block of data will be
        pushed into this container instead, and the place in the page where the
        reference was made will instead have a numerically indexed link to the
        reference in the container (e.g. instead it will have [1], and clicking
        that link will move the page to the reference block in the citations
        container).
  */
(function($) {
$.fn.soloCitation = function(options) {
  var settings = $.extend({
    citationsContainer: false,
  }, options);

  var url = 'https://api.m.ox.ac.uk/library/item:';
  var citations = {};
  var citationsCounter = 1;

  var getSoloData = function($div) {
    var id = $div.data('id');
    var data = {};

    $.ajax({
      url: url + id + '/',
      dataType: 'json',
      async: false,
      success: function(json) {
        data = json;
      }
    });

    return data;
  };

  var getSoloDataHtml = function(data) {
    var html = $('<div/>').addClass('citation');

    var a = $('<a/>').html(data.title)
                     .attr({
                       target: '_blank',
                       href: 'http://m.ox.ac.uk/#/library/item/' + data.id
                     });
    var h2 = $('<h2/>').append(a);
    var author = $('<p/>').html('Author: ' + data.author);
    var showIsbn = data.isbns.length ? data.isbns[0] : '';
    var isbn = $('<p/>').html('ISBN: ' + showIsbn);

    html.append(h2).append(author).append(isbn);

    return $('<div/>').append(html).html();
  };

  return this.each(function(i, div) {
    var $div = $(div);

    if (settings.citationsContainer) {
      if (!citations[$div.data('id')]) {
        var data = getSoloData($div);
        var html = getSoloDataHtml(data);

        citations[data.id] = [citationsCounter, data];
        var citationLink = $('<a/>').html('[' + citationsCounter + ']').attr('href', '#cit' + citationsCounter);
        var citationAnchor = $('<a/>').attr('name', 'cit' + citationsCounter);
        settings.citationsContainer.append(citationAnchor).append(html);

        citationsCounter++;
      } else {
        var data = citations[$div.data('id')][1];
        var html = getSoloDataHtml(data);
        var cit = citations[data.id][0];
        var citationLink = $('<a/>').html('[' + cit + ']').attr('href', '#cit' + cit);
        var citationAnchor = $('<a/>').attr('name', 'cit' + cit);
      }

      $div.append(citationLink);
    } else {
      var data = getSoloData($div);
      var html = getSoloDataHtml(data);
      $div.append(html);
    }
  });
};

// automatically bind to solo-citation divs
$(document).ready(function() {
  var citationsContainer = $('.citationsContainer');

  // binds soloCitation to citations; if a citationsContainer exists,
  // the citations will be pushed into it
  $('[data-solo-citation]').soloCitation({
    citationsContainer: citationsContainer.length? citationsContainer : false,
  });
});
})(jQuery);
