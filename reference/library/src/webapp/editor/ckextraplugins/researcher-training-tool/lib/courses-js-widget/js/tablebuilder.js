if (!window.OxfordCoursesWidget) {
  OxfordCoursesWidget = {};
}

// responsible for putting the results table together
//   @param chosenColumns the columns that were specified in the div on initialisation
//   @param showDates boolean flag indicating whether dates should be shown
OxfordCoursesWidget.TableBuilder = function(chosenColumns, showDates, Fields) {

  this.rows = [];
  this.columns = {};

  // called at the end to make sure the namespace is all there
  this.init = function() {
    // let's initialise these columns based on the what was chosen in the options
    if (chosenColumns && chosenColumns.length > 0) {
      for (var i in chosenColumns) {
        var columnName = chosenColumns[i];
        var foundIndex = this.getColumnIndex(columnName, Fields);
        if (foundIndex && this.canDisplayColumn(columnName, showDates)) {
          this.columns[foundIndex] = Fields[foundIndex];
        }
      }
    } else {
      this.columns = Fields;
    }
  }

  // public
  this.availableColumns = function() {
    return this.columns;
  }

  this.addRows = function(rows) {
    for (var i in rows) {
      this.rows.push(rows[i]);
    }
  }

  this.build = function() {
    var table = $('<table/>', {'class': 'course-results-table'});

    var head = $('<thead/>');
    var headRow = $('<tr/>');
    for (var i in this.columns) {
      headRow.append(this.columns[i].toHtml());
    }
    table.append(head.append(headRow));

    var body = $('<tbody/>');
    for (var i in this.rows) {
      body.append(this.rows[i].toHtml());
    }
    table.append(body);

    return table;
  }

  // private
  this.getColumnIndex = function(columnName, fields) {
    for (var i in fields) {
      if(fields[i].name == columnName) {
        return i;
      }
    }
    return false;
  }

  this.canDisplayColumn = function(columnName, showDates) {
    return showDates || columnName != 'start';
  }

  this.init();
};
