/**
  * Code adds auto-complete functionality to input fields using the api from
  * http://data.ox.ac.uk. This has been modified from existing code at dataox
  * and wrapped up as a jQuery plugin.

  * USAGE
      1. Have input field on your page:

        <input data-autocomplete-type="organization">

      2. In your JavaScript, call the input field:

        $('input').oxPointsAutoComplete({
          // ... parameters, explained below
        });

  * PARAMETERS
      @param classes {string}
        Any classes to add to the main ui container, separated by spaces

      @param select {function}
        Takes an event and the clicked  data node found from the autocomplete
        query. Dictates what occurs when the item is clicked. Data of interest
        is in ui.item.
  */
(function($){
$.fn.oxPointsAutoComplete = function(options) {
  var settings = options || {};
  var obj = this.get(0);    // the original DOM object
  var searchURL = (options.searchURL || $('body').attr('data-dataox-search-url') || "https://data.ox.ac.uk/search/") + '?callback=?'; 
  var h = $('<input type="hidden">').attr('name', this.attr('name')).val(this.val());
  this.attr('name', this.attr('name') + '-label').after(h);
  
  // build the default params for AJAX calls
  var buildDefaultParams = function(field) {
    var defaultParams = {format: 'js'};
    for (var i = 0; i < field.attributes.length; i++) {
      var attribute = field.attributes[i];
      if (attribute.name.slice(0, 18) == 'data-autocomplete-')
        defaultParams[attribute.name.slice(18)] = attribute.value;
    }
    return defaultParams;
  }
  
  var jsonCall = function(field) {
    if (field.val()) {
      var originalVal = field.val();
      field.val("looking up…");
      $.get(
        searchURL, 
        $.extend({}, 
        defaultParams, 
        {
          q: "uri:\""+originalVal+"\""
        }),
        function(data) {
          e.val(data.hits.total ? data.hits.hits[0].label : originalVal);
        }, 
        'json'
      );
    }
  }
  
  return this.each(function() {
    var $this = $(this);
    var params = buildDefaultParams(obj);
    var json = jsonCall($this);
    
    $this.autocomplete({
      source: function(request, respond) {
        $.getJSON(
          searchURL,                                       // url
          $.extend({}, params, { q: request.term + '*' }), // data
          function(data) {                                 // success
            for (var i=0; i<data.hits.hits.length; i++) {
              data.hits.hits[i] = data.hits.hits[i]._source;
              data.hits.hits[i].value = data.hits.hits[i].uri;
            }
            respond(data.hits.hits);
          }
        );
      },
      minLength: 2,
      focus: function(event, ui) {
        $this.val(ui.item.label);
        if (options.focus)
          options.focus(event, ui);
        return false;
      },
      open: function(event, ui) {
        // add classes to main ui container
        if (options.classes) {
          var $this = $(this);

          // for jQuery < 1.9
          $this.data('autocomplete').menu.element.addClass(options.classes);

          // for jQuery >= 1.9
          $this.data('uiAutocomplete').menu.element.addClass(options.classes);
        }
      },
      select: function(event, ui) {
        $this.val(ui.item.label);
        h.val(ui.item.value);

        if (options.select) {
          options.select(event, ui);
        }

        return false;
      }
    });
  });
}
}(jQuery));
