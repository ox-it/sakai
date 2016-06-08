if (!window.OxfordCoursesWidget) {
  OxfordCoursesWidget = {};
}

// Holding the parameters for the widget
OxfordCoursesWidget.Options = function() {

  this.includeTALLCourses = false;

  this.setTitle = function(title) {
    this.title = title || "Courses";
  }

  this.setDisplayColumns = function(columns) {
    this.displayColumns = columns ? this.trimWhitespace(columns).split(' ') : [];
  }

  this.trimWhitespace = function(string) {
    return string.replace(/^\s+|\s+$/g, '');
  }

  // assumes a space separated list
  this.setUnits = function(units) {
    this.units = (units || "").split(' ');
  }

  // assumes a space separated list
  this.setEligibilities = function(eligibilities) {
    this.eligibilities = (eligibilities || "OX PU").split(' ');
  }

  this.setResearchMethod = function(method) {
    this.researchMethod = method ? "https://data.ox.ac.uk/id/ox-rm/" + method : "";
  }

  this.setSkill = function(skill) {
    this.skill = skill ? "https://data.ox.ac.uk/id/ox-rdf/descriptor/" + skill : "";
  }

  this.setStartingFilters = function(before, after) {
    if (before == undefined) {
      if (after == undefined) {
        after = "now"; // set default to courses in the future
      }
    }

    // set to either now(), the current time, or failing these an empty string
    this.startingBefore = this.readNowAsCurrentTime(before || "");
    this.startingAfter  = this.readNowAsCurrentTime(after || "");
  }

  // helper function for setting dates
  this.readNowAsCurrentTime = function(param) {
    return param == "now" ? now() : param;
  }

  this.setShowWithoutDatesLink = function(param) {
    this.showWithoutDatesLink = param === 'true';
  }

  this.setDefaultDatesView = function(param) {
    this.defaultDatesView = param;
  }

  this.defaultViewWithDates = function() {
    return this.defaultDatesView == 'withDates';
  }

  this.defaultViewWithoutDates = function() {
    return this.defaultDatesView == 'withoutDates';
  }
};
