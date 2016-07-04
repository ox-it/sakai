if (!window.OxfordCoursesWidget) {
  OxfordCoursesWidget = {};
}

// controls the interface of the widget
OxfordCoursesWidget.WidgetUI = function(element, dataTablesConfig) {
  this.e  = element;
  this.$e = $(element);
  this.dataTablesConfig = (typeof dataTablesConfig !== 'undefined') ? dataTablesConfig : {};

  this.addLoadingMessage = function() {
    $('<div/>', {'class': 'courses-widget-wait', 'text': 'Loading courses...'})
      .append(this.loadingImage())
      .appendTo(this.$e);

    this.showLoadingMessage();
  }

  this.showLoadingMessage = function() {
    this.$e.children(".courses-widget-wait").show();
  }

  this.hideLoadingMessage = function() {
    this.$e.children(".courses-widget-wait").hide();
  }

  this.loadingImage = function() {
    return $('<img/>', {'src': 'https://static.data.ox.ac.uk/loader.gif', 'alt': 'Please wait'})
  }

  this.addTitle = function(title) {
    $('<h2/>', {'class': 'courses-widget-title', 'text': title}).appendTo(this.$e);
  }

  this.addNoDatesLink = function(options, getData) {
    if (options.showWithoutDatesLink) {
      var linkTitle = (options.withoutDates)? "Show courses with specific dates" : "Show courses without specific dates";
      var ui = this;
      var $noDatesToggle = $('<a class="courses-widget-no-date-toggle-link" href="#">' + linkTitle + '</a>').click(function () {
        options.withoutDates = (options.withoutDates)? false : true;
        ui.$e.children('.course-results-table').remove();
        ui.$e.children('.dataTables_wrapper').remove();
        $(this).remove();
        getData(ui.e, options);

        return false;
      });

      this.$e.append($noDatesToggle);
    }
  }

  this.addTable = function(tableHtml) {
    this.$e.append(tableHtml);
  }

  this.configureDataTables = function(availableColumns) {

    var config = new Array();
    var i = 0;

    for (var c in availableColumns) {
      if (c == 'START') {
        config.push({ "sWidth": '8.1em', "aTargets":[i], 'sType':'date'});
      }
      i++;
    }

    var params = $.extend({
      aoColumnDefs: config,
      "iDisplayLength": 25,
      "oLanguage": {
        "sEmptyTable" : "No matching courses found.",
      }
    }, this.dataTablesConfig);

    dataTable = this.$e.children(".course-results-table").dataTable(params);
  }
};
