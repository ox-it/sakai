(function ($) {

AjaxSolr.TextWidget = AjaxSolr.AbstractTextWidget.extend({
  init: function () {
    var self = this;
    $(this.target).find('input[type=text]').bind('keyup', function(e) {
    	var value = $(this).val();
        value && self.set(value);
        if (e.which == 13 && value) {
          self.doRequest();
        }
    });
    
    $("#search form").submit(function(e) {
    	e.preventDefault();
        self.doRequest();
      });
  },

  afterRequest: function () {
    $(this.target).find('input[type=text]').val('');
  }
});

})(jQuery);
