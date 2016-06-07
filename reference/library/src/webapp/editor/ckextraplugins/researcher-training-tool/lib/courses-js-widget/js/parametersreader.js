if (!window.OxfordCoursesWidget) {
  OxfordCoursesWidget = {};
}

// Loads the parameters from div attributes and passses them to an Options instance
OxfordCoursesWidget.ParametersReader = function(options, element) {
  this.options = options;
  this.e = element;
  this.$e = $(element);

  this.fetch = function(param) {
    return this.$e.attr(param)
  }

  this.read = function() {
    this.options.setTitle(this.fetch("data-title"));
    this.options.setDisplayColumns(this.fetch("data-displayColumns"));
    this.options.setUnits(this.fetch("data-providedBy"));
    this.options.setEligibilities(this.fetch("data-eligibility"));
    this.options.setResearchMethod(this.fetch("data-researchMethod"));
    this.options.setSkill(this.fetch("data-skill"));
    this.options.setShowWithoutDatesLink(this.fetch("data-showWithoutDatesLink"));
    this.options.setDefaultDatesView(this.fetch("data-defaultDatesView"));

    this.options.setStartingFilters(
      this.fetch("data-startingBefore"),
      this.fetch("data-startingAfter")
    );

    return options;
  }
};
