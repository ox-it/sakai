$(function() {
	$("input.uppercase").css("text-transform", "uppercase").blur(function() {
		this.value = this.value.toUpperCase();
	});

	// Used to show the extra items on the factets
	$(".facet .control").click(function(e) {
		$(this).parent().children().toggle('fast');
		e.preventDefault();
	});

});
