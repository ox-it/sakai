(function ($) {

AjaxSolr.TimeFrameWidget = AjaxSolr.AbstractFacetWidget.extend({
  afterRequest: function () {
	
	if (this.manager.response.facet_counts.facet_ranges.course_created === undefined &&
		this.manager.response.facet_counts.facet_ranges.course_basedate === undefined) {
		$(this.target).html('no items found in current selection');
		return;
	}
	
	var objectedItems = [];
	var before = parseInt(this.manager.response.facet_counts.facet_ranges.course_basedate.before);
	if (before > 0) {
		objectedItems.push({ facet: "Old Courses", count: before });
	}
	var after = parseInt(this.manager.response.facet_counts.facet_ranges.course_basedate.after);
	if (after > 0) {
		objectedItems.push({ facet: "Current Courses", count: after });
	}
	var count = parseInt(this.manager.response.facet_counts.facet_ranges.course_created.counts[1]);
	if (count > 0) {
		objectedItems.push({ facet: "New Courses", count: count });
	}
	
	$(this.target).empty();
	for (var i = 0, l = objectedItems.length; i < l; i++) {
		var facet = objectedItems[i].facet;
		var count = objectedItems[i].count;
		$(this.target).append(
				$('<a href="#" class="tagcloud_item"></a>')
				.text(facet+" ("+count+")")
				.click(this.clickHandler(facet))
		);
	}

	var myHeight = objectedItems.length*1.5;
	if (myHeight > 10) {
		myHeight = 10;
	}
	var thisHeight = myHeight+'em';
	document.getElementById(this.id).style.height = thisHeight;
  }
});

})(jQuery);
