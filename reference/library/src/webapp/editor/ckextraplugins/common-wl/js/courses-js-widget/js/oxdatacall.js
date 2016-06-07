if (!window.OxfordCoursesWidget) {
  OxfordCoursesWidget = {};
}

// prepares the call to data.ox.ac.uk
OxfordCoursesWidget.OxDataCall = function() {

  this.url = 'https://data.ox.ac.uk/search/?callback=?';

  this.params = {
    'format'    : 'js',
    'type'      : 'presentation',
    'q'         : '*',
    'page_size' : 10000,
  }

  this.Params = {
    QUERY            : 'q',
    UNIT_ANCESTOR    : 'filter.offeredByAncestor.uri',
    WITHOUT_DATES    : 'filter.start.time',
    START_AFTER      : 'gte.start.time',
    START_BEFORE     : 'lt.start.time',
    SUBJECT_URI      : 'subject.uri',
    METHOD_URI       : 'filter.researchMethod.uri',
    ELIGIBILITY_URIS : 'filter.eligibility.uri'
  }

  this.prepare = function(options) {
    this.setQuery(options.includeTALLCourses)
    this.setUnits(options.units);

    if(options.withoutDates || (options.defaultViewWithoutDates() && options.withoutDates == undefined)) {
      options.withoutDates = true;
      this.setNoDates();
    } else {
      this.setDates(options.startingBefore, options.startingAfter);
    }

    this.setEligibility(options.eligibilities);
    this.setSkill(options.skill);
    this.setResearchMethod(options.researchMethod);

  }

  this.set = function(name, value) {
    this.params[name] = value;
  }

  this.setQuery = function(includeTALL) {
    this.set(this.Params.QUERY, includeTALL ? '*' : '* NOT catalog.uri:"http://course.data.ox.ac.uk/id/continuing-education/catalog"');
  }

  this.setUnits = function(units) {
    var uri = (units && units.length > 0) ? units : 'http://oxpoints.oucs.ox.ac.uk/id/00000000';
    this.set(this.Params.UNIT_ANCESTOR, uri);
  }

  this.setNoDates = function() {
    this.set(this.Params.WITHOUT_DATES, '-');
  }

  this.setDates = function(before, after) {
    if(before) this.set(this.Params.START_BEFORE, before);
    if(after)  this.set(this.Params.START_AFTER, after);
  }

  this.EligibilityIndex = {
    'PU': 'oxcap:eligibility-public',
    'OX': 'oxcap:eligibility-members',
    'ST': 'oxcap:eligibility-staff'
  }

  this.setEligibility = function(eligibilities) {
    var oxDataCall = this
    var list = $.map(eligibilities, function(val, i) {
        return oxDataCall.EligibilityIndex[val] || null;
      });
    this.set(this.Params.ELIGIBILITY_URIS, list);
  }

  this.setSkill = function(skill) {
    if (skill) this.set(this.Params.SUBJECT_URI, skill);
  }

  this.setResearchMethod = function(method) {
    if (method) this.set(this.Params.METHOD_URI, method);
  }

  this.perform = function(callback) {
    $.ajaxSettings.traditional = true;
    $.getJSON(this.url, this.params, callback);
  }

};
