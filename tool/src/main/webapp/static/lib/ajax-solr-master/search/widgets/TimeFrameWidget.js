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
		objectedItems.push({ count: before, query: "[* TO NOW]", field: "course_basedate" });
	}
	var after = parseInt(this.manager.response.facet_counts.facet_ranges.course_basedate.after);
	if (after > 0) {
		objectedItems.push({ count: after, query: "[NOW TO *]", field: "course_basedate" });
	}
	var count = parseInt(this.manager.response.facet_counts.facet_ranges.course_created.counts[1]);
	if (count > 0) {
		objectedItems.push({ count: count, query: "[NOW-14DAY TO NOW]", field: "course_created" });
	}
	
	$(this.target).empty();
	for (var i = 0, l = objectedItems.length; i < l; i++) {
		var count = objectedItems[i].count;
		var query = objectedItems[i].query;
		var field = objectedItems[i].field;
		var facet = this.manager.getQueryDisplay(this.fq(field, query));
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
		if (self[meth].call(self, field, query)) {
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
  set: function (field, query) {
    return this.changeSelection(function () {
      var a = this.manager.store.removeByValue('fq', new RegExp('^-?' + this.field + ':')),
          b = this.manager.store.addByValue('fq', this.fq(field, query));
      return a || b;
    });
  },

  /**
   * Adds a filter query.
   *
   * @returns {Boolean} Whether a filter query was added.
   */
  add: function (field, query) {
    return this.changeSelection(function () {
      return this.manager.store.addByValue('fq', this.fq(field, query));
    });
  },
  
  /**
   * @param {String} value The facet value.
   * @param {Boolean} exclude Whether to exclude this fq parameter value.
   * @returns {String} An fq parameter value.
   */
  fq: function (field, query, exclude) {
    return (exclude ? '-' : '') + field + ':' + AjaxSolr.Parameter.escapeValue(query);
  }
  
})
})(jQuery);
