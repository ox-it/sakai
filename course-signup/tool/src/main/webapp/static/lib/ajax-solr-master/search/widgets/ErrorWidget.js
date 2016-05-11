(function ($) {

AjaxSolr.ErrorWidget = AjaxSolr.AbstractWidget.extend({
	
	onError: function (message) {
		$("#rightError").html("<span>"+message+"</span>");
		$("#leftError").html("<span>"+message+"</span>");
		$("#docs").empty();
	},

	afterRequest: function () {
		$("#rightError").empty();
		$("#leftError").empty();
	}

});

})(jQuery);