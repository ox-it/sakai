(function ($) {

/**
 * This widget works with facets that only have one range.
 * This needs HTML of <div id="myid"><input type=checkbox><label>MyLabel</label></div>.
 */
AjaxSolr.BooleanFacetWidget = AjaxSolr.AbstractWidget.extend({
  /**
   * The label to display for this facet.
   */
  label: "Label",

  /**
   * This filter we should apply, this is just used internally.
   */
  filter: "",

  init: function () {
    var self = this;
    // Don't allow normal clearing of this field thanks.
    self.manager.addHiddenField(self.field);
    var element = $(this.target).find("input[type=checkbox]").bind('change', function(e) {
        self.updateState(this);
        self.manager.doRequest();
    }).get();
    // Keep the store and ui in sync.
    this.updateState(element);
  },

  updateState: function(element) {
    // We only update if we have a value.
    var remove = (element.checked)?this.unchecked:this.checked;
    if (element.checked) {
      this.manager.store.addByValue("fq", this.field+":"+ this.filter);
    } else {
     this.manager.store.removeByValue("fq", new RegExp("^"+this.field+":.*"));
    }
  },

  afterRequest: function() {
    var response = this.manager.response;
    if (response.facet_counts.facet_ranges[this.field] == undefined ||
            response.facet_counts.facet_ranges[this.field].counts.length == 0) {
        // If we can't find our field or there aren't any ranges.
        $(this.target).find("input[type=checkbox]").attr("disabled", true);
        $(this.target).find("label").html(this.label+ " (0)");
    } else if (response.facet_counts.facet_ranges[this.field].counts.length == 2) {
        // If it all looks good.
        $(this.target).find("input[type=checkbox]").removeAttr("disabled");
        this.filter = "["+ response.facet_counts.facet_ranges[this.field].counts[0] + " TO "+
             response.facet_counts.facet_ranges[this.field].counts[0] +
             response.facet_counts.facet_ranges[this.field].gap + "]";
        $(this.target).find("label").html(this.label+ " ("+ response.facet_counts.facet_ranges[this.field].counts[1] + ")");
    } else {
       // Too many ranges.
       $(this.target).find("input[type=checkbox]").removeAttr("disabled");
       $(this.target).find("label").html(this.label+ " (disabled)");
    }
  }

});

})(jQuery);
