/**
  * Given an .oxpoint div with suitable 'data-' tags, it produces the
  * corresponding Google Map using the jQuery plugin goMap.
  */
(function($){
$.fn.oxPointMap = function(options) {
  // pull settings from oxpoint div (data attributes)
  var getDisplaySettings = function(oxpoint) {
    return {
      uri: oxpoint.data('uri'),
      showMap: oxpoint.data('inc-map'),
      showAddress: oxpoint.data('inc-add'),
      showHome: oxpoint.data('inc-home'),
      showTitle: oxpoint.data('inc-title'),
      height: (oxpoint.data('height') ? oxpoint.data('height') : '400') + 'px',
      width: (oxpoint.data('width') ? oxpoint.data('width') : '700') + 'px',
    };
  }

  // given an oxpoint and its settings, embed the map on the page where it is
  var displayMap = function(oxpoint, settings) {
    var wrapper = $('<div class="oxpointmap"/>');
    var explodedurl = settings.uri.split('/');
    var id = explodedurl[explodedurl.length-1];
    var url = 'https://api.m.ox.ac.uk/places/oxpoints:' + id;

    // perform ajax call and display the map if successful
    $.ajax({
      url: url,
      dataType: 'json',
      success: function(json) {
        // map
        if (settings.showMap && $.fn.goMap) {
          var map = $('<div class="map"/>').css({ width: settings.width, height: settings.height });
          wrapper.append(map);
          map.goMap({
            latitude: json.lat,
            longitude: json.lon,
            zoom: 16,
            markers: [{
              title: json.name,
              latitude: json.lat,
              longitude: json.lon
            }]
          });
        }
        // title
        if (settings.showTitle) {
          var h2 = $('<h2 class="title"/>');
          h2.html(json.name);
          wrapper.append(h2);
        }
        // address
        if (settings.showAddress && json.address) {
          var address = $('<div class="address"/>');
          address.html(json.address);
          wrapper.append(address);
        }
        // homepage
        if (settings.showHome) {
          var homepage = $('<div class="homepage"/>');
          var a = $('<a/>');

          a.attr('href', json.website)
           .attr('target', '_blank')
           .html(json.website);

          homepage.append(a);
          wrapper.append(homepage);
        }
      },
      error: function(e, s, t) {
        wrapper.append('<p>Error in fetching information.</p>');
      },
      complete: function() {
        oxpoint.after(wrapper);
        oxpoint.remove();
      }
    });
  };

  return this.each(function() {
    var $this = $(this);
    displayMap($this, getDisplaySettings($this));
  });
};

// automatically bind to data-oxpoint divs
$(document).ready(function() {
  $('[data-oxpoint]').oxPointMap();
});
}(jQuery));
