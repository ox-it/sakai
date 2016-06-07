/**
  * Title:        OXAMEmbed
  * Description:  Turn a div into an oxam search result listing
  * Author:       Lawrence Okoth-Odida
  * Version:      0.1
  * Date:         04/08/2014
  */
(function($){
$.fn.oxamEmbed = function(options) {
  var ajaxUrls = [
    '/portal/hierarchytool/8a98905b-a664-4618-9200-4ceb2118b0d6/advanced',
    '/portal/hierarchytool/2e4b8eb6-8bfb-45bf-aae8-44d69a6f7880/search',
  ];

  var getParams = function($div) {
    if (options) {
      return options;
    }

    var params = {};

    if ($div.data('query')) {
      params.query = $div.data('query');
    } else {
      params.year = $div.data('year');
      params.exam = $div.data('exam');
    }

    return params;
  };

  var getParamsFromString = function(string) {
    var tmpParams = string.split('?');
        tmpParams = tmpParams[1] || tmpParams[0];
        tmpParams = tmpParams.split('&');
    var params = {};

    for (i in tmpParams) {
      var param = tmpParams[i].split('=');
      params[param[0]] = param[1];
    }

    return params;
  };

  var getStringFromParams = function(params) {
    var string = '';

    for (param in params) {
      string += param + '=' + params[param] + '&';
    }

    return string;
  };

  var getAjaxUrl = function(params, includeQuery) {
    var url = '';
    var query = getStringFromParams(params);

    // if we have no 'query' param, pick the first ajax url
    if (!params.query) {
      url = ajaxUrls[0];
    } else {
      url = ajaxUrls[1];
    }

    if (includeQuery) {
      url += '?' + query;
    }

    return url;
  };

  var formatPagination = function($pagination, $div) {
    // fix to tidy up span and div soup
    $pagination.children().each(function(i, child) {
      var node = $('<span/>');
      var $child = $(child);
      var text = $child.text().trim();
      var link = $child.is('a') ? $child : $child.find('a');

      // if we have an anchor, ensure we keep the link
      if (link.length) {
        var a = $('<a/>').attr('href', decodeURIComponent(link.attr('href'))).html(text);
        node.append(a);
      } else {
        node.html(text).addClass('current');
      }

      node.addClass('page page' + text);

      $child.replaceWith(node);
    });

    // now bind search functionality to the anchors
    $pagination.addClass('pagination').removeAttr('id');
    $pagination.find('a').on('click', function(e) {
      var $this = $(this);
      var params = getParamsFromString($this.attr('href'));

      displayResultsFromOxam($div, params);
      e.preventDefault();
    });
  };

  var formatResults = function($html, $div, params) {
    /* assumed node structure of $html is:
      <h3/>   <--- simply 'Results'
      <ul/>   <--- the results
      <div/>  <--- 'Showing x to y of y'
      <div/>  <--- pagination of results
    */

    // when clicking a paginated link, show results from OXAM but in the same
    // div rather than redirecting to OXAM
    $html.find('h3').remove();
    var pagination = $html.find('div').last();
    var results = $html.find('ul');
        //results.unwrap();
        results.addClass('results exams')
               .find('li').addClass('result exam')
               .each(function(i, li) {
                 var $li = $(li);
                 var classes = ['examId', 'title', 'year', 'term', 'schoolCode', 'school'];
                 var span = $li.find('span');
                 $li.find('a').addClass('paper');

                 span.each(function(j, sp) {
                   $(sp).addClass(classes[j]);
                 });
               });
    var showing = $html.find('div:eq(-2)');
    var goToOxam = $('<a/>').html('Go to OXAM')
                            .attr({
                              href:   getAjaxUrl(params, true),
                              target: '_blank',
                              class: 'goto',
                            });
    var anchors = pagination.find('a');

    if (pagination.find('a').length) {
      formatPagination(pagination, $div);
    } else {
      // no extra pages, so the last div is just the 'Showing x to y of y' content
      var showing = pagination;
    }

    showing.addClass('showing');
    showing.html('<span>' + showing.html() + '</span>');
    showing.append($('<div/>').append(goToOxam).html());

    return $html;
  };

  var displayResultsFromOxam = function($div, params) {
    $div.removeClass('success error').addClass('waiting');

    $.ajax({
      dataType: 'html',
      url: getAjaxUrl(params, false),
      data: params,
      success: function(html) {
        var $html = $(html);
        var results = $(html).find('.content div');
        var formattedResults = formatResults(results, $div, params);
        $div.addClass('success').html(formattedResults);
      },
      error: function() {
        $div.addClass('error').html('Error fetching results from OXAM. Try again later.');
      },
      complete: function() {
        $div.removeClass('waiting');
      }
    });
  };

  return this.each(function(i, div){
    var $div = $(div);
    var params = getParams($div);

    displayResultsFromOxam($div, params);
  });
};

// automatically bind to data-oxam-embed divs
$(document).ready(function() {
  $('[data-oxam-embed]').oxamEmbed();
});
})(jQuery);
