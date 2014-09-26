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
			 * A collection of hidden fields that shouldn't be displayed to the end user.
			 */
			hiddenFields: [],

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
			},

            addHiddenField: function(field) {
                if (!this.isHiddenField(field)) {
                    this.hiddenFields.push(field);
                }
            },

			isHiddenField: function(field) {
				for (var i = 0; i < this.hiddenFields.length; i++) {
					if (this.hiddenFields[i] == field) {
						return true;
					}
				}
				return false;
			}
	});

	Manager = new AjaxSolr.MyManager({
		//solrUrl: 'http://localhost:8983/solr/ses/'
		solrUrl: '../rest/course/solr/'
	});
	
	Manager.addValueName("course_created:[NOW-14DAY TO NOW]", "New Courses");

	Manager.addFieldName("provider_title", "Department")
		.addFieldName("course_subject_rdf", "Skills Category")
		.addFieldName("course_subject_rm", "Research Method")
		.addFieldName("course_delivery", "Delivery Method")
		.addFieldName("course_subject_vitae_domain", "RDF Domain")
		.addFieldName("course_subject_vitae_subdomain", "RDF Sub-domain")
		.addFieldName("course_created", "Age")

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
    
    var fields = [ 'provider_title', 'course_subject_rdf', 'course_subject_rm', 'course_delivery',
        'course_subject_vitae_domain', 'course_subject_vitae_subdomain'];
    for (var i = 0, l = fields.length; i < l; i++) {
      Manager.addWidget(new AjaxSolr.TagcloudWidget({
        id: fields[i],
        target: '#' + fields[i],
        field: fields[i]
      }));
    }
    
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

    Manager.addWidget(new AjaxSolr.BooleanWidget({
        id: "show_old",
        target: "#show_old",
        field: "course_basedate",
        checked: "[NOW-2YEAR TO NOW]",
        unchecked: "[NOW TO *]"
    }));

    Manager.addWidget(new AjaxSolr.BooleanFacetWidget({
        id: "show_new",
        target: "#show_new",
        field: "course_created",
        label: "Recently Added Courses"
    }));
    // Toggles the search
    Manager.addWidget(new AjaxSolr.AbstractWidget({
        target: "#search_wrapper",
        afterRequest: function () {
            $(this.target).addClass('advanced_search').removeClass('simple_search');
        }
    }));

    Manager.setStore(new AjaxSolr.ParameterExtraStore({
         extra: "fq=course_hidden:false" // Hide hidden courses.
    }));

    Manager.init();
    Manager.store.addByValue('q', '*:*');
   
    var params = {
      facet: true,
      // We don't limit the size of the facets they are reasonably small and we want all the values.
      'facet.field': [ 'provider_title', 'course_subject_rdf', 'course_subject_rm', 'course_delivery',
            'course_subject_vitae_domain', 'course_subject_vitae_subdomain'],
      'facet.sort': 'index', // Sort alphabetically
      'facet.mincount': 1,
      'facet.range' : [ 'course_created' ],
      'f.course_created.facet.range.start' : 'NOW-14DAY',
      'f.course_created.facet.range.end' : 'NOW',
      'f.course_created.facet.range.gap' : '+14DAY'
    };
    for (var name in params) {
      Manager.store.addByValue(name, params[name]);
    }

    // Load any query parameter.
    if (window.location.search) {
        var search = window.location.search.substr(1); // trim leading '?'
        Manager.store.parseString(search);
        Manager.doRequest();
    }

  });
})(jQuery);
