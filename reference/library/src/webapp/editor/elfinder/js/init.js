// Initialize elFinder
(function() {

// Documentation for client options:
// https://github.com/Studio-42/elFinder/wiki/Client-configuration-options
$(document).ready(function() {
  $('#elfinder').elfinder({
    url : 'php/connector.minimal.php',  // connector URL (REQUIRED)
  });
});

})();
