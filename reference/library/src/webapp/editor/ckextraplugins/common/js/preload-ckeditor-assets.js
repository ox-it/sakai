/**
  file that preloads javascript and css
  for ckeditor plugins
*/

(function($){
  $(document).ready(function() {
    /** initializaton */
    var head = $('head');
    var scripts = $('.ckeditorPluginAsset.javascript');
    var stylesheets = $('.ckeditorPluginAsset.stylesheet');

    /** functions */
    // push unique scripts into the head
    var uniqueSrcs = [];
    var loadScripts = function($container, $scripts) {
      $scripts.each(function(i) {
        var src = $(this).attr('data-src');
        if ($.inArray(src, uniqueSrcs) === -1){
            uniqueSrcs.push(src);
            $container.append($('<script>').attr({'type' : 'text/javascript', 'src' : src}));
        }
      });
    };

    // push unique css into the head
      var uniqueHrefs= [];
      var loadStyleSheets = function($container, $stylesheets) {
      $stylesheets.each(function(i) {
        var href = $(this).attr('data-href');
        if ($.inArray(href, uniqueHrefs) === -1) {
            uniqueHrefs.push(href);
            $container.append($('<link>').attr({'type': 'text/css', 'rel': 'stylesheet', 'href': href}));
        }
      });
    };

    /** procedure */
    loadScripts(head, scripts);
    loadStyleSheets(head, stylesheets);
  });
})(jQuery);
