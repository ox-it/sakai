/**
* Title          jQuery ItemSearch
* Description    A jQuery plugin for adding generic item-searching functionality to a search form.
* Author         Lawrence Okoth-Odida
* Version        0.1.2
* Date           19/08/2014
* Documentation  https://github.com/lokothodida/jquery-itemsearch/wiki/
*/
(function($) {
$.fn.itemSearch = function(options) {
  // initialze settings
  var settings = $.extend({
    resultsContainer: $('#results'),
    displayResult: function (result) {
      return '<li><a href="' + result.url + '">' + result.title + '</li>';
    },
    waiting: 'Loading...',
    registerElements: [],
    noResult: 'Sorry, no results',
    pagination: 5,
    params: {},
  }, options);

  // form element classes (used when the form is built)
  var selectors = {
    form  : 'item-search-form',
    query : 'item-search-query',
    button: 'item-search-button'
  };

  // builds search form (just a form with an input and anchor for a button)
  var buildSearchForm = function() {
    var form = $('<form/>').addClass(selectors.form);
    var input = $('<input/>').addClass(selectors.query);
    var button = $('<a/>').addClass(selectors.button).html('Search');

    form.append(input);
    addRegisteredElementsToForm(form);
    form.append(button);

    return form;
  };

  var addRegisteredElementsToForm = function(form) {
    for (i = 0; i < settings.registerElements.length; i++) {
      var element = settings.registerElements[i];

      form.append(element);
    }
  };

  var getParamsFromRegisteredElements = function() {
    var params = {};

    for (i = 0; i < settings.registerElements.length; i++) {
      var $element = settings.registerElements[i];
      params[$element.attr('name')] = $element.val();
    }

    return params;
  };

  // constructs the output html for the formatted results as a string
  var buildResultsPage = function(results) {
    var html = '';

    if (results.length) {
      $.each(results, function(key, result)  { 
        html += settings.displayResult(result);
      });
    } else {
      html = $('<div/>').append($('<p/>').append(settings.noResult)).html();
    }

    if (settings.pagination && results.length) {
      html = buildPaginatedResults(html);
    }

    return html;
  };
  
  var buildPaginatedResults = function(resultsHtml) {
    var results = $(resultsHtml);
        results = results.filter(function(result) {
          // remove empty text nodes
          return this.nodeType != 3;
        });
    var nav = $('<div/>').addClass('pagination');
    var pages = Math.ceil(results.length / settings.pagination);
    var container = $('<div/>').addClass('paginated-results');
    var pagesContainer = $('<div/>').addClass('pages');

    // build page numbers and containers
    for (i = 1; i <= pages; i++) {
      nav.append($('<a/>').html(i).attr({'class': 'pageNum', 'href': '#', 'data-page': i}));
      pagesContainer.append($('<div/>').attr({'class': 'page', 'data-page': i}));
    };

    container.append(pagesContainer);

    // now move results into the correct containers
    for (i = 0; i < results.length; i++) {
      var page = Math.ceil((i+1) / settings.pagination);
      container.find('.page[data-page="' + page + '"]').append(results[i]);
    }

    $(document).on('click', '.pageNum', function() {
      // find closest results container
      var $this = $(this);
      var localContainer = $this.closest('.paginated-results');
      var page = $this.data('page');

      localContainer.find('.page').hide();
      localContainer.find('.page[data-page="' + page + '"]').show();

      return false;
    });

    // add navigational elements
    container.prepend(nav);
    container.append(nav.clone());

    // show the first page
    container.find('.page').hide();
    container.find('.page[data-page="1"]').show();

    return $('<div/>').append(container).html();
  };

  // return this, binding the functionality accordingly
  return this.each(function() {
    var $form = buildSearchForm();
    
    var bindToSubmit = function(eventParams) {
      if (settings.waiting) {
        settings.resultsContainer.html(settings.waiting);
      }

      if (eventParams && eventParams.results) {
        // someone is triggering setResults()
        var html    = buildResultsPage(eventParams.results);
        settings.resultsContainer.html(html);
      } else {
        var params  = $.extend(getParamsFromRegisteredElements(), settings.params);
        var service = new settings.service($.extend({ form: $form }, (params || {})));
        var query   = $form.find('.' + selectors.query).val();
        var results = service.performQuery(query);
        var html    = buildResultsPage(results);

        if (results !== false) {
          // if results is a boolean, we are waiting for asynchronous data
          settings.resultsContainer.html(html);
        }
      }

      return false;
    };

    // now bind search functionality to the form elements
    $form.on('click', '.' + selectors.button, bindToSubmit);

    // bind functionality to hitting ENTER (keyCode #13 is the ENTER button)
    $form.on('keydown', 'input', function(e) {
      if (e.keyCode === 13) {
        // done so that users can simply use on('submitItemSearchForm' ....) for
        // their elements defined in registeredElements literal
        $(this).trigger('submitItemSearchForm');

        return false;
      }
    });

    $form.on('submitItemSearchForm', 'input', function() {
      bindToSubmit();
    });

    $form.setResults = function(results) {
      bindToSubmit({ results: results });
    };

    $form.on('submit', bindToSubmit);

    $(this).prepend($form);
  });
};
})(jQuery);
