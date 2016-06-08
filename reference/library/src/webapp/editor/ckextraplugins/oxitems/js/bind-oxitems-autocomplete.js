var bindOxItemsAutoCompleteToInput = function($input) {
  var codes = [];

  var init = function() {
    getCodes();
    bindToInput();
  };

  var getCodes = function() {
    /**
    * when the api exists, this function will fill the codes variable
    * with all of the oxitems codes
    */
  };

  var bindToInput = function() {
    $input.autocomplete({
      source: codes,
    }).autocomplete('widget').addClass('oxitems-autocomplete');
  };

  init();
};
