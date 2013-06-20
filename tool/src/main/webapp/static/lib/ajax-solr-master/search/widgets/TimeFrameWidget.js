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
		objectedItems.push({ facet: "Old Courses", count: before, query: "[* TO NOW]", field: "course_basedate" });
	}
	var after = parseInt(this.manager.response.facet_counts.facet_ranges.course_basedate.after);
	if (after > 0) {
		objectedItems.push({ facet: "Current Courses", count: after, query: "[NOW TO *]", field: "course_basedate" });
	}
	var count = parseInt(this.manager.response.facet_counts.facet_ranges.course_created.counts[1]);
	if (count > 0) {
		objectedItems.push({ facet: "New Courses", count: count, query: "[NOW-14DAY TO NOW]", field: "course_created" });
	}
	
	$(this.target).empty();
	for (var i = 0, l = objectedItems.length; i < l; i++) {
		var facet = objectedItems[i].facet;
		var count = objectedItems[i].count;
		var query = objectedItems[i].query;
		var field = objectedItems[i].field;
		$(this.target).append(
				$('<a href="#" class="tagcloud_item"></a>')
				.text(facet+" ("+count+")")
				.click(this.clickHandler(field, query))
		);
	}

	var myHeight = objectedItems.length*1.5;
	if (myHeight > 10) {
		myHeight = 10;
	}
	var thisHeight = myHeight+'em';
	document.getElementById(this.id).style.height = thisHeight;
  },


/**
 * @param {String} value The value.
 * @returns {Function} Sends a request to Solr if it successfully adds a
 *   filter query with the given value.
 */

  clickHandler: function (field, query) {
	var self = this, meth = this.multivalue ? 'add' : 'set';
	return function () {
		if (self[meth].call(self, value)) {
			self.doRequest();
		}
		return false;
	}
  },
  
  /**
   * Sets the filter query.
   *
   * @returns {Boolean} Whether the selection changed.
   */
  set: function (value) {
    return this.changeSelection(function () {
      var a = this.manager.store.removeByValue('fq', new RegExp('^-?' + this.field + ':')),
          b = this.manager.store.addByValue('fq', this.fq(value));
      return a || b;
    });
  },

  /**
   * Adds a filter query.
   *
   * @returns {Boolean} Whether a filter query was added.
   */
  add: function (value) {
    return this.changeSelection(function () {
      return this.manager.store.addByValue('fq', this.fq(value));
    });
  },
  
  /**
   * @param {String} value The facet value.
   * @param {Boolean} exclude Whether to exclude this fq parameter value.
   * @returns {String} An fq parameter value.
   */
  fq: function (value, exclude) {
    return (exclude ? '-' : '') + this.field + ':' + AjaxSolr.Parameter.escapeValue(value);
  }
  
})
})(jQuery);
