if (!window.OxfordCoursesWidget) {
  OxfordCoursesWidget = {};
}

if (!OxfordCoursesWidget.Column) {
  OxfordCoursesWidget.Column = function() {
  };
}

OxfordCoursesWidget.paddedValue = function(v) {
  if (v < 10 ) {
    v = "0"+v;
  }
  return v;
};

OxfordCoursesWidget.now = function() {
  d = new Date();
  return d.getFullYear() + "-" + paddedValue(d.getMonth()+1) + "-" + paddedValue(d.getDate()) + "T" + paddedValue(d.getHours()) + ":" + paddedValue(d.getMinutes()) + ":" + paddedValue(d.getSeconds());
};

OxfordCoursesWidget.add_css = function(url) {
  if (document.createStyleSheet) {
    document.createStyleSheet(url);
  } else {
    $('<link rel="stylesheet" type="text/css" href="' + url + '" />').appendTo('head');
  }
};

OxfordCoursesWidget.mixedContentSafeLink = function(text, url) {
  if(url.indexOf('http://') == 0) {
    return $('<a>', {title: text, href: url, target: "_blank"}).text(text);
  } else {
    return $('<a>', {title: text, href: url}).text(text);
  }
};

OxfordCoursesWidget.setUp = function(e, dataTablesConfig) {

  var reader  = new ParametersReader(new Options(), e);
  var options = reader.read();
  getData(e, options, dataTablesConfig);

  var ui = new WidgetUI(e, dataTablesConfig);
  ui.addTitle(options.title);
  ui.addLoadingMessage();
};

// this can be called from `setUp` or from clicking on the show without dates link
OxfordCoursesWidget.getData = function(e, options, dataTablesConfig) {
  var ui = new WidgetUI(e, dataTablesConfig);
  ui.showLoadingMessage();

  call = new OxDataCall();
  call.prepare(options);
  callback = function(json) { handleData(e, options, json, dataTablesConfig); };
  call.perform(callback);

};

// handles the query results 
OxfordCoursesWidget.handleData = function(e, options, results, dataTablesConfig) {

  var parser  = new ResponseParser(results);
  var tabler  = new TableBuilder(options.displayColumns, !options.withoutDates, OxfordCoursesWidget.Fields);

  var availableColumns = tabler.availableColumns();

  tabler.addRows(parser.toRows(availableColumns));

  var ui = new WidgetUI(e, dataTablesConfig);
  ui.addNoDatesLink(options, getData);
  ui.addTable(tabler.build());
  ui.configureDataTables(availableColumns);
  ui.hideLoadingMessage();
};

OxfordCoursesWidget.Fields = {
  START       : new OxfordCoursesWidget.Column('start',       'Start date',  'course-presentation-start'),
  TITLE       : new OxfordCoursesWidget.Column('title',       'Title',       'course-title'),
  SUBJECT     : new OxfordCoursesWidget.Column('subject',     'Subject(s)',  'course-subject'),
  VENUE       : new OxfordCoursesWidget.Column('venue',       'Venue',       'course-presentation-venue'),
  PROVIDER    : new OxfordCoursesWidget.Column('provider',    'Provider',    'course-provider'),
  DESCRIPTION : new OxfordCoursesWidget.Column('description', 'Description', 'course-description'),
  ELIGIBILITY : new OxfordCoursesWidget.Column('eligibility', 'Eligibility', 'course-eligibility')
};
