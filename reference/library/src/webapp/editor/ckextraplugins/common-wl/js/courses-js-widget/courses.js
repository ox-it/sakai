/**

Copyright (c) 2013 University of Oxford

All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
* Neither the name of the University of Oxford nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/

define(['jquery', 'jquery.dataTables', 'moment'], function($) {

	var moment = require('moment');

	function paddedValue(v) {
		if (v < 10 ) {
			v = "0"+v;
		}
		return v;
	}

	function now() {
		d = new Date(); 
		return d.getFullYear() + "-" + paddedValue(d.getMonth()+1) + "-" + paddedValue(d.getDate()) + "T" + paddedValue(d.getHours()) + ":" + paddedValue(d.getMinutes()) + ":" + paddedValue(d.getSeconds());
	}

	function add_css(url) {
		if (document.createStyleSheet) {
			document.createStyleSheet(url);
		} else {
			$('<link rel="stylesheet" type="text/css" href="' + url + '" />').appendTo('head');
		}
	}

	function mixedContentSafeLink(text, url) {
		if(url.indexOf('http://') == 0) {
			return $('<a>', {title: text, href: url, target: "_blank"}).text(text);
		} else {
			return $('<a>', {title: text, href: url}).text(text);
		}
	}

	// Loads the parameters from div attributes and passses them to an Options instance
	function ParametersReader(options, element) {

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

	}

	// Holding the parameters for the widget
	function Options() {

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
	}

	// controls the interface of the widget
	function WidgetUI(element, dataTablesConfig) {
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
	}

	// prepares the call to data.ox.ac.uk
	function OxDataCall() {

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

	}

	// responsible for putting the results table together
	//   @param chosenColumns the columns that were specified in the div on initialisation
	//   @param showDates boolean flag indicating whether dates should be shown
	function TableBuilder(chosenColumns, showDates) {

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
	}

	function Column(name, text, classname) {
		this.name       = name
		this.classname  = classname
		this.text       = text

		this.toHtml = function() {
			return $('<th/>', {'text': text, 'class': classname});
		}
	}

	var Fields = {
		START       : new Column('start',       'Start date',  'course-presentation-start'),
		TITLE       : new Column('title',       'Title',       'course-title'),
		SUBJECT     : new Column('subject',     'Subject(s)',  'course-subject'),
		VENUE       : new Column('venue',       'Venue',       'course-presentation-venue'),
		PROVIDER    : new Column('provider',    'Provider',    'course-provider'),
		DESCRIPTION : new Column('description', 'Description', 'course-description'),
		ELIGIBILITY : new Column('eligibility', 'Eligibility', 'course-eligibility')
	};

	// handles the data that is returned from data.ox.ac.uk
	function ResponseParser(results) {
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
	}

	function Row(availableColumns) {
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
	}

/* Our main function 
*/
	$(function() {

		add_css("//static.data.ox.ac.uk/lib/DataTables/media/css/jquery.dataTables.css");
		add_css("//static.data.ox.ac.uk/courses-js-widget/courses.css");

		var setUp = function(e, dataTablesConfig) {

			var reader  = new ParametersReader(new Options(), e);
			var options = reader.read();
			getData(e, options, dataTablesConfig);

			var ui = new WidgetUI(e, dataTablesConfig);
			ui.addTitle(options.title);
			ui.addLoadingMessage();
		}

		// this can be called from `setUp` or from clicking on the show without dates link
		var getData = function(e, options, dataTablesConfig) {
			var ui = new WidgetUI(e, dataTablesConfig);
			ui.showLoadingMessage();

			call = new OxDataCall();
			call.prepare(options);
			callback = function(json) { handleData(e, options, json, dataTablesConfig); };
			call.perform(callback);

		};

		// handles the query results 
		var handleData = function(e, options, results, dataTablesConfig) {

			var parser  = new ResponseParser(results);
			var tabler  = new TableBuilder(options.displayColumns, !options.withoutDates);

			var availableColumns = tabler.availableColumns();

			tabler.addRows(parser.toRows(availableColumns));

			var ui = new WidgetUI(e, dataTablesConfig);
			ui.addNoDatesLink(options, getData);
			ui.addTable(tabler.build());
			ui.configureDataTables(availableColumns);
			ui.hideLoadingMessage();
		};


		/*	create a jQuery plugin/wrapper for binding this functionality on the fly.
				'options' is a literal that takes the following parameter(s):

				@param dataTablesConfig {object}
					Literal of dataTables API calls. For example:

						dataTablesConfig: {
							fnInitComplete: function(settings, json) {
								alert('Table has initialized!');
							},
							fnInfoCallback : function(oSettings, iStart, iEnd, iMax, iTotal, sPre) {
								alert('Table has ben modified!');
							}
						}

					Will give an alert box when the table is first drawn and whenever its state
					changes (e.g. the page changes).

					(Full API @ http://www.datatables.net/examples/api/index.html)

				(more parameters can be added in the future)
		*/
		$.fn.oxfordCoursesWidget = function(options) {
			var settings = $.extend({
				dataTablesConfig: {}
			}, options);

			return this.each(function(i, e) {
				setUp(e, settings.dataTablesConfig);
			});
		};

		$('.courses-widget-container').oxfordCoursesWidget();
	});
});
