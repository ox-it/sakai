if (!window.OxfordCoursesWidget) {
  OxfordCoursesWidget = {};
}

OxfordCoursesWidget.Row = function(availableColumns) {
  this.cells = {}
  this.columns = availableColumns;

  this.addCell = function(field, html) {
    var found = false;
    for (var i in this.columns) {
      if (this.columns[i] == field) {
        found = true;
      }
    }

    if (found) {
      this.cells[field.name] = html;
    }

  }

  this.toHtml = function() {
    var row = this;
    var tds = $.map(this.columns, function(column, i) {
        return $('<td/>').append(row.cells[column.name])[0].outerHTML;
      });
    return $('<tr/>').append(tds.join(''));
  };

  this.setStart = function(start) {
    if(start) {
      this.addCell(Fields.START, moment(start.time).format("ddd D MMM YYYY")); // Mon 1 Oct 2012
    }
  }

  this.setTitle = function(label, apply, homepage) {
    title = label ? label.valueOf() : '-';

    if (apply) {
      this.addCell(Fields.TITLE, mixedContentSafeLink(label, apply.uri));
    } else if (homepage) {
      this.addCell(Fields.TITLE, mixedContentSafeLink(label, homepage.uri));
    } else {
      this.addCell(Fields.TITLE, $('<span/>', {'title': label, 'text': label}));
    }
  }

  this.setSubjects = function(subjects) {
    if (subjects) {
      var notJACS = new Array();
      for (j in subjects) {
        if (!this.isJacsCode(subjects[j].uri)) {
          notJACS.push(subjects[j].label);
        }
      }
      this.addCell(Fields.SUBJECT, $('<span>').text(notJACS.join(', ')));
    }
  }

  this.isJacsCode = function(code) {
    return code.indexOf('http://jacs.dataincubator.org/') == 0;
  }

  this.setVenue = function(venue) {
    if (venue) {
      var label = venue.label || '-';
      this.addCell(Fields.VENUE, label);
    }
  }

  this.setProvider = function(provider) {
    if (provider) {
      var label = provider.label || '-';
      this.addCell(Fields.PROVIDER, label);
    }
  }

  this.setDescription = function(description) {
    if (description) {
      this.addCell(Fields.DESCRIPTION, description);
    }
  }

  this.setEligibility = function(eligibility) {
    if (eligibility) {
      this.addCell(Fields.ELIGIBILITY, this.capitalise(eligibility.label));
    }
  }

  this.capitalise = function(word) {
    return capitalised = word.charAt(0).toUpperCase() + word.slice(1)
  }
};
