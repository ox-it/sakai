/*
 * #%L
 * Course Signup Webapp
 * %%
 * Copyright (C) 2010 - 2013 University of Oxford
 * %%
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *             http://opensource.org/licenses/ecl2
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
var Manager;

(function ($) {

  $(function () {
	AjaxSolr.MyManager = AjaxSolr.Manager.extend({
		
			/** 
			 * A collection of all registered widgets. For internal use only.
			 *
			 * @field
			 * @private
			 * @type Object
			 * @default {}
			 */
			errorwidgets: {},
			
			/**
			 * A collection of pretty display values
			 */
			valueNames: {},

			/**
			 * A collection of display names for fields
			 */
			fieldNames: {},
			
			/** 
			 * Adds a widget to the manager.
			 *
			 * @param {AjaxSolr.AbstractWidget} widget
			 */
			addErrorWidget: function (widget) { 
				widget.manager = this;
				this.errorwidgets[widget.id] = widget;
			},
			
			handleError: function (jqXHR) {
				for (var widgetId in this.errorwidgets) {
					this.errorwidgets[widgetId].onError(jqXHR.responseText);
				}
			},

			/**
			 * Adds a value mapping for displaying query values.
			 *
			 * @param key The field and value.
			 * @param value The pretty value to display to the user.
			 * @return this to allow chaining.
			 */
			addValueName: function (key, value) {
				this.valueNames[key]=value;
				return this;
			},

			/**
			 * Looks up a nice display value. It also strips surrounding double quotes from
			 * any returned values.
			 *
			 * @param key The field and value to lookup a display for.
			 * @return The pretty value to display to the user or the value from the supplied key.
			 */
			getValueName: function (key) {
				if (key in this.valueNames) {
					return this.valueNames[key];
				}
				var value = key.split(":")[1];
				// Strip leading/trailing double quotes
				return value.match(/^("?)(.+)\1$/)[2];
			},

			/**
			 * Add a nice alias for a field name.
			 *
			 * @param field The field name.
			 * @param name The pretty name for the field.
			 * @return this to allow chaining.
			 */
			addFieldName: function (field, name) {
				this.fieldNames[field] = name;
				return this;
			},

			/**
			 * Lookup a nice alias for a field name.
			 *
			 * @param field The field name.
			 * @return The pretty name for the field.
			 */
			getFieldName: function(field) {
				if (field in this.fieldNames) {
					return this.fieldNames[field];
				}
				return field;
			}
	});

	Manager = new AjaxSolr.MyManager({
		//solrUrl: 'http://localhost:8983/solr/ses/'
		solrUrl: '../rest/course/solr/'
	});
	
	Manager.addValueName("course_basedate:[* TO NOW]", "Previous Courses")
		.addValueName("course_basedate:[NOW TO *]", "Current Courses")
		.addValueName("course_created:[NOW-14DAY TO NOW]", "New Courses");

	Manager.addFieldName("provider_title", "Department")
		.addFieldName("course_subject_rdf", "Skills Category")
		.addFieldName("course_subject_rm", "Research Method")
		.addFieldName("course_delivery", "Delivery Method")
		.addFieldName("course_created", "Timeframe")
		.addFieldName("course_basedate", "Timeframe");

    Manager.addWidget(new AjaxSolr.ResultWidget({
      id: 'result',
      target: '#docs'
    }));
    Manager.addWidget(new AjaxSolr.PagerWidget({
      id: 'pager',
      target: '#pager',
      prevLabel: '&lt;',
      nextLabel: '&gt;',
      innerWindow: 1,
      renderHeader: function (perPage, offset, total) {
        $('#pager-header').html($('<span></span>').text('displaying ' + Math.min(total, offset + 1) + ' to ' + Math.min(total, offset + perPage) + ' of ' + total));
      }
    }));
    
    var fields = [ 'provider_title', 'course_subject_rdf', 'course_subject_rm', 'course_delivery' ];
    for (var i = 0, l = fields.length; i < l; i++) {
      Manager.addWidget(new AjaxSolr.TagcloudWidget({
        id: fields[i],
        target: '#' + fields[i],
        field: fields[i]
      }));
    }
    
    Manager.addWidget(new AjaxSolr.TimeFrameWidget({
        id: 'course_timeframe',
        target: '#course_timeframe'
    }));
    
    Manager.addWidget(new AjaxSolr.CurrentSearchWidget({
        id: 'currentsearch',
        target: '#selection'
    }));
    
    Manager.addWidget(new AjaxSolr.TextWidget({
    	  id: 'text',
    	  target: '#search'
    }));
    
    Manager.addWidget(new AjaxSolr.ErrorWidget({
    	id: 'error',
    	target: '#error'
    }));
    
    Manager.addErrorWidget(new AjaxSolr.ErrorWidget({
    	id: 'error',
    	target: '#error'
    }));

    Manager.setStore(new AjaxSolr.ParameterExtraStore({
        extra: "fq=course_hidden:false" + "&" +
               "fq=course_basedate:[NOW/DAY-2YEAR TO *]" // Only courses within the last 2 years
    }));

    Manager.init();
    Manager.store.addByValue('q', '*:*');
   
    var params = {
      facet: true,
      'facet.field': [ 'provider_title', 'course_subject_rdf', 'course_subject_rm', 'course_delivery' ],
      'facet.limit': 20,
      'facet.mincount': 1,
      'f.topics.facet.limit': 50,
      'facet.range' : [ 'course_created', 'course_basedate' ],
      'f.course_created.facet.range.start' : 'NOW-14DAY',
      'f.course_created.facet.range.end' : 'NOW',
      'f.course_created.facet.range.gap' : '+14DAY',
      'f.course_basedate.facet.range.start' : 'NOW',
      'f.course_basedate.facet.range.end' : 'NOW',
      'f.course_basedate.facet.range.gap' : '+21DAY',
      'f.course_basedate.facet.range.other' : 'all',
    };
    for (var name in params) {
      Manager.store.addByValue(name, params[name]);
    }
    
    Manager.doRequest();
  });
})(jQuery);
