if (!window.OxfordCoursesWidget) {
  OxfordCoursesWidget = {};
}

// handles the data that is returned from data.ox.ac.uk
OxfordCoursesWidget.ResponseParser = function(results) {
  this.presentations = results.hits.hits;

  this.toRows = function(availableColumns) {
    return $.map(this.presentations, function(presentation, i) {
      var result = presentation._source;

      var row = new Row(availableColumns);
      row.setStart(result.start);
      row.setTitle(result.label, result.applyTo, result.homepage);
      row.setSubjects(result.subject);
      row.setVenue(result.venue);
      row.setProvider(result.offeredBy);
      row.setDescription(result.description);
      row.setEligibility(result.eligibility);

      return row;
    });
  }
};
