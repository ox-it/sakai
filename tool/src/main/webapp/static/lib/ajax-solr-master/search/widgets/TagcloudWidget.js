(function ($) {

AjaxSolr.TagcloudWidget = AjaxSolr.AbstractFacetWidget.extend({
  afterRequest: function () {
    if (this.manager.response.facet_counts.facet_fields[this.field] === undefined) {
      $(this.target).html('no items found in current selection');
      return;
    }

    //var maxCount = 0;
    var objectedItems = [];
    for (var i = 0; i < this.manager.response.facet_counts.facet_fields[this.field].length; i=i+2) {
    	var facet = this.manager.response.facet_counts.facet_fields[this.field][i];
    	var count = parseInt(this.manager.response.facet_counts.facet_fields[this.field][i+1]);
    	/*
    	if (count > maxCount) {
            maxCount = count;
        }
        */
    	objectedItems.push({ facet: facet, count: count });
    }
    /*
    for (var facet in this.manager.response.facet_counts.facet_fields[this.field]) {
      var count = parseInt(this.manager.response.facet_counts.facet_fields[this.field][facet]);
      if (count > maxCount) {
        maxCount = count;
      }
      objectedItems.push({ facet: facet, count: count });
    }
    objectedItems.sort(function (a, b) {
      return a.facet < b.facet ? -1 : 1;
    });
	*/
    $(this.target).empty();
    for (var i = 0, l = objectedItems.length; i < l; i++) {
      var facet = objectedItems[i].facet;
      var count = objectedItems[i].count;
      $(this.target).append(
        $('<a href="#" class="tagcloud_item"></a>')
        .text(facet)
        /*.addClass('tagcloud_size_' + parseInt(count / maxCount * 10))*/
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
