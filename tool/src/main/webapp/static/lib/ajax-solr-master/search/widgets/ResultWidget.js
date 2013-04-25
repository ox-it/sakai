(function ($) {

AjaxSolr.ResultWidget = AjaxSolr.AbstractWidget.extend({
	
  start: 0,
  
  beforeRequest: function () {
    $(this.target).html($('<img>').attr('src', 'images/loader.gif'));
  },

  facetLinks: function (facet_field, facet_values) {
    var links = [];
    if (facet_values) {
      for (var i = 0, l = facet_values.length; i < l; i++) {
        if (facet_values[i] !== undefined) {
          links.push(
            $('<a href="#"></a>')
            .text(facet_values[i])
            .click(this.facetHandler(facet_field, facet_values[i]))
          );
        }
        else {
          links.push('no items found in current selection');
        }
      }
    }
    return links;
  },

  facetHandler: function (facet_field, facet_value) {
    var self = this;
    return function () {
      self.manager.store.remove('fq');
      self.manager.store.addByValue('fq', facet_field + ':' + AjaxSolr.Parameter.escapeValue(facet_value));
      self.doRequest();
      return false;
    };
  },

  afterRequest: function () {
    $(this.target).empty();
    //for (var i = 0, l = this.manager.response.response.docs.length; i < l; i++) {
    //  var doc = this.manager.response.response.docs[i];
      
    for (var i = 0, l = this.manager.response.grouped.course_identifier.groups.length; i < l; i++) {
      for (var j = 0, m = this.manager.response.grouped.course_identifier.groups[i].doclist.docs.length; j < m; j++) {
        var doc = this.manager.response.grouped.course_identifier.groups[i].doclist.docs[0];

        $(this.target).append(this.template(doc));

        var items = [];
        items = items.concat(this.facetLinks('departments', doc.provider_title));
        items = items.concat(this.facetLinks('skills', doc.course_subject_rdf));
        items = items.concat(this.facetLinks('research methods', doc.course_subject_rm));

        var $links = $('#links_' + doc.id);
        $links.empty();
        for (var j = 0, m = items.length; j < m; j++) {
          $links.append($('<li></li>').append(items[j]));
        }
      }
    }
  },

  template: function (doc) {
    var output = '<div id="doc"><form><p><strong>' + doc.course_title + ',</strong>&nbsp;&nbsp;'+doc.provider_title+',';
    output += '<p id="links_' + doc.course_identifier + '" class="links"></p>';
    output += '<p id="description"><a href="javascript:{}" class="more">Show descrption</a>&nbsp;&nbsp;';
    output += '<span style="display:none;"><br />' + doc.course_description+'<br /></span>';
    output += '<input type="hidden" name="id" value="' + doc.course_identifier + '">';
    output += '<input type="hidden" name="previous" value="Current Courses">';
    output += '<input type="submit" value="More details">';
    output += '</p>';
    output += '</p></form></div>';
    return output;
  },
  
  init: function () {
    $(document).on('click', 'a.more', function () {
      var $this = $(this),
          span = $this.parent().find('span');

      if (span.is(':visible')) {
        span.hide();
        $this.text('Show descrption');
      }
      else {
    	$this.text('Hide descrption');
        span.show();
      }
      return false;
    });
  }
});

})(jQuery);