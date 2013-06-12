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
			} 
	});

    Manager = new AjaxSolr.MyManager({
    	//solrUrl: 'http://localhost:8983/solr/ses/'
    	solrUrl: '../rest/course/solr/'
    });

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
    
    var fields = [ 'provider_title', 'course_subject_rdf', 'course_subject_rm', 'course_class', 'course_delivery', 'course_timeframe' ];
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

    Manager.init();
    Manager.store.addByValue('q', '*:*');
   
    var params = {
      facet: true,
      'facet.field': [ 'provider_title', 'course_subject_rdf', 'course_subject_rm', 'course_class', 'course_delivery', 'course_timeframe' ],
      'facet.limit': 20,
      'facet.mincount': 1,
      'f.topics.facet.limit': 50
    };
    for (var name in params) {
      Manager.store.addByValue(name, params[name]);
    }
    
    Manager.doRequest();
  });
})(jQuery);
