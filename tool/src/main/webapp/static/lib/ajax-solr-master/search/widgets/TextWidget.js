(function ($) {

AjaxSolr.TextWidget = AjaxSolr.AbstractTextWidget.extend({
  init: function () {
    var self = this;
    $(this.target).find('input[type=text]').bind('keydown', function(e) {
    	var value = $(this).val();
        value && self.set(value);
        if (e.which == 13 && value) {
          self.doRequest();
        }
    });
    
    $("#search form").submit(function(e) {
        self.doRequest();
        $('div.simple_search').toggle();
		$('div.advanced_search').toggle();
      });
  },

  afterRequest: function () {
    $(this.target).find('input[type=text]').val('');
  }
});

})(jQuery);
