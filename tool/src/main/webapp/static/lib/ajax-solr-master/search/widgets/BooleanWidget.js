(function ($) {

/**
 * This widget takes 2 filters to apply, one when checked and one when unchecked.
 * Both
 */
AjaxSolr.BooleanWidget = AjaxSolr.AbstractWidget.extend({
  init: function () {
    var self = this;
    self.manager.addHiddenField(self.field);
    var element = $(this.target).bind('change', function(e) {
        self.updateState(this);
        self.manager.doRequest();
    }).get();
    // Keep the store and ui in sync.
    this.updateState(element);
  },

  updateState: function(element) {
    // We only update if we have a value.
    var remove = (element.checked)?this.unchecked:this.checked;
    if (remove) {
      this.manager.store.removeByValue("fq", this.field+":"+remove);
    }
    var add = (element.checked)?this.checked:this.unchecked;
    if (add) {
      this.manager.store.addByValue("fq", this.field+":"+ add);
    }
  }
});

})(jQuery);
