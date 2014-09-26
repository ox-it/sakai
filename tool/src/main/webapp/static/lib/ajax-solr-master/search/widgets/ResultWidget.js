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
    for (var i = 0, l = this.manager.response.response.docs.length; i < l; i++) {
      var doc = this.manager.response.response.docs[i];

      $(this.target).append(this.template(doc));

      var items = [];
      items = items.concat(this.facetLinks('departments', doc.provider_title));
      items = items.concat(this.facetLinks('skills', doc.course_subject_rdf));
      items = items.concat(this.facetLinks('research methods', doc.course_subject_rm));
      items = items.concat(this.facetLinks('classes', doc.course_class));
      items = items.concat(this.facetLinks('delivery', doc.course_delivery));

      var $links = $('#links_' + doc.id);
      $links.empty();
      for (var j = 0, m = items.length; j < m; j++) {
        $links.append($('<li></li>').append(items[j]));
      }
    }

    $("#result form").submit(function(e) {
        e.preventDefault();
        try {
            var form = this;
            var id = $("input[name=id]", form).val();
            var range = $("input[name=previous]", form).val();
            var workingWindow = parent.window || window;
            var position = Signup.util.dialogPosition();
            var height = Math.round($(workingWindow).height() * 0.9);
            var width = Math.round($(window).width() * 0.9);

            var courseDetails = $("<div></div>").dialog({
                autoOpen: false,
                stack: true,
                position: position,
                width: width,
                height: height,
                modal: true,
                close: function(event, ui){
                    courseDetails.remove(); /* Tidy up the DOM. */
                }
            });

            Signup.course.show(courseDetails, id, range, externalUser, "../rest", function(){
                courseDetails.dialog("open");
            });
        } catch (e) {
            console.log(e);
        }
    });
  },

  template: function (doc) {
    var now = new Date();
    var range = "ALL";
    var signup_message = "";
    var booking_message = "";
    var close = Signup.util.parseDate(doc.course_signup_close);
    var open = Signup.util.parseDate(doc.course_signup_open);

    if (isNaN(open.getDate()) || isNaN(close.getDate())) {
        if (doc.course_signup_opentext) {
            signup_message = doc.course_signup_opentext;
        }
    } else {

        if (close < now) {
            signup_message = "closed";
            range = "PREVIOUS";
        } else {
            range = "UPCOMING";
            if (open > now) {
                signup_message = "opens in " + Signup.util.formatDuration(open -now);
            } else {
                signup_message = "closes in " + Signup.util.formatDuration(close -now);
            }
        }
    }

    var output = '<div id="doc"><form class="details"><strong>' + doc.course_title + '</strong>';
    output += ',&nbsp;&nbsp;'+doc.provider_title;
    if (signup_message) {
            output += '&nbsp;(signup '+signup_message+ ')';
    }
    output += '<p id="links_' + doc.course_identifier + '" class="links"></p>';
    output += '<div id="description"><a href="javascript:{}" class="more">Show description</a>&nbsp;&nbsp;';
    output += '<div class="toggle" style="display:none;">' + doc.course_description+'<br /></div>';
    output += '<input type="hidden" name="id" value="' + doc.course_identifier + '">';
    output += '<input type="hidden" name="previous" value="'+range+'">';
    output += '<input type="submit" value="More details">';
    output += '</div>';
    output += '</form></div>';
    return output;
  },

  init: function () {
    $(document).on('click', 'a.more', function () {
      var $this = $(this),
          toggle = $this.parent().find('div.toggle');

      if (toggle.is(':visible')) {
          toggle.hide();
        $this.text('Show description');
      }
      else {
        $this.text('Hide description');
        toggle.show();
      }
      return false;
    });
  }


});

})(jQuery);
