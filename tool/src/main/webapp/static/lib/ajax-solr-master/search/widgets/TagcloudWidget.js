(function ($) {

AjaxSolr.TagcloudWidget = AjaxSolr.AbstractFacetWidget.extend({
  afterRequest: function () {
    if (this.manager.response.facet_counts.facet_fields[this.field] === undefined ||
        this.manager.response.facet_counts.facet_fields[this.field].length == 0) {
      $(this.target).html('<span class="tagcloud_empty">no items found in current selection</a>');
      document.getElementById(this.id).style.height = "1.5em";
      return;
    }

    var objectedItems = [];
    for (var i = 0; i < this.manager.response.facet_counts.facet_fields[this.field].length; i=i+2) {
    	var facet = this.manager.response.facet_counts.facet_fields[this.field][i];
    	var count = parseInt(this.manager.response.facet_counts.facet_fields[this.field][i+1]);
    	objectedItems.push({ facet: facet, count: count });
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
