<%--
  #%L
  Course Signup Webapp
  %%
  Copyright (C) 2010 - 2013 University of Oxford
  %%
  Licensed under the Educational Community License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  
              http://opensource.org/licenses/ecl2
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  #L%
  --%>
<%@ page
	import="org.sakaiproject.component.cover.ServerConfigurationService"%>
<%@ page import="org.sakaiproject.user.cover.UserDirectoryService"%>
<%@ page session="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
  <meta charset="utf-8">
  <title>AJAX Solr</title>
  
  <link href='<c:out value="${skinRepo}" />/tool_base.css' type="text/css" rel="stylesheet" media="all" />
  <link href="<c:out value="${skinRepo}" />/<c:out value="${skinDefault}" />/tool.css" type="text/css" rel="stylesheet" media="all" />
  
  <script type="text/javascript" src="lib/ajax-solr-master/jquery/jquery-1.9.1.min.js"></script>
  <script type="text/javascript" src="lib/ajax-solr-master/jquery/jquery-migrate-1.1.1.min.js"></script>
  <script type="text/javascript" src="lib/jquery-ui-1.8.4.custom/js/jquery-ui-1.8.4.custom.min.js"></script>
  <script type="text/javascript" src="lib/jstree-1.0rc2/_lib/jquery.cookie.js"></script>
  <script type="text/javascript" src="lib/jqmodal-r14/jqModal.js"></script>
  <script type="text/javascript" src="lib/signup.js"></script>
  
  <link rel="stylesheet" type="text/css" href="lib/jqmodal-r14/jqModal.css" />
  <link rel="stylesheet" type="text/css" href="lib/jquery-ui-1.8.4.custom/css/smoothness/jquery-ui-1.8.4.custom.css" />
  <link rel="stylesheet" type="text/css" href="lib/tool.css" />
  
  <link rel="stylesheet" href="lib/ajax-solr-master/search/css/search.css">
  <script src="lib/ajax-solr-master/search.js"></script>
  <script src="lib/ajax-solr-master/core/Core.js"></script>
  <script src="lib/ajax-solr-master/core/AbstractManager.js"></script>
  <script src="lib/ajax-solr-master/managers/Manager.jquery.js"></script>
  <script src="lib/ajax-solr-master/core/Parameter.js"></script>
  <script src="lib/ajax-solr-master/core/ParameterStore.js"></script>
  <script src="lib/ajax-solr-master/core/AbstractWidget.js"></script>
  <script src="lib/ajax-solr-master/search/widgets/ResultWidget.js"></script>
  <script src="lib/ajax-solr-master/widgets/jquery/PagerWidget.js"></script>
  <script src="lib/ajax-solr-master/core/AbstractFacetWidget.js"></script>
  <script src="lib/ajax-solr-master/search/widgets/TagcloudWidget.js"></script>
  <script src="lib/ajax-solr-master/search/widgets/CurrentSearchWidget.js"></script>
  <script src="lib/ajax-solr-master/core/AbstractTextWidget.js"></script>
  <script src="lib/ajax-solr-master/search/widgets/TextWidget.js"></script>
<!-- 
  <script src="lib/ajax-solr-master/search/widgets/AutocompleteWidget.js"></script>
 -->
 <script type="text/javascript">

		var externalUser = <c:out value="${externalUser}" />;
		var recentDays = "<%= ServerConfigurationService.getString("recent.days", "14") %>";

		
		$(function() {
			/* Make the more details button work.
			 * IE has all sorts of problems with .live("submit".. ! 
			 * http://forum.jquery.com/topic/ie-specific-issues-with-live-submit
			 * $("form.details").live("submit", function() {
			 * The form class attributes don't work for these elements in IE, so we have to use
			 * a parent selector to make sure it doesn't fire for other forms in the page.
			 */
			$("body").delegate(".result form","submit", function(e) {
				e.preventDefault();
				try {
					var form = this;
					var id = $("input[name=id]", form).val();
					var previous = $("input[name=previous]", form).val();
					var workingWindow = parent.window || window;
					var position = Signup.util.dialogPosition();
					var height = Math.round($(workingWindow).height() * 0.9);
					var width = Math.round($(window).width() * 0.9);
										
					var courseDetails = $("<div></div>").dialog({
						autoOpen: false,
						stack: true,
						position: position,
						width: width,
						height: height,
						modal: true,
						close: function(event, ui){
							courseDetails.remove(); /* Tidy up the DOM. */
						}
					});
					var range = "UPCOMING";
					if (previous.indexOf("Old Courses") >= 0) {
						range = "PREVIOUS";
					}
					Signup.course.show(courseDetails, id, range, externalUser, function(){
						courseDetails.dialog("open");
					});
				} catch (e) {
					console.log(e);
				}
			});
		});
		
		/* Adjust with the content. */
		$(function(){
  			Signup.util.autoresize();
        });
	</script>
</head>
<body>
  <div id="wrap"> 
    <div id="header">
      <h1>AJAX Solr Demonstration</h1>
      <h2>Browse Reuters business news from 1987</h2>
    </div>

    <div class="left">
    
      <div id="current_selection">
        <h2>Current Selection</h2>
        <ul id="selection"></ul>
        
        <ul id="pager"></ul>
        <div id="pager-header"></div>
      </div>
      
      <div id="result">
        <div id="docs"></div>
      </div>
    </div>

    <div class="right">

      <h2>Search</h2>
      <span id="search_help">(press ESC to close suggestions)</span>
      <ul id="search">
        <input type="text" id="query" name="query" autocomplete="off">
      </ul>

      <h2>Departments</h2>
      <div class="facet-body-frame">
      	<div class="facet-body">
      		<!-- <div class="tagcloud" id="provider_title"></div>  -->
      	</div>
      </div>

      <h2>Skills Categories</h2>
      <div class="facet-body-frame">
      	<div class="facet-body">
      		<div class="tagcloud" id="course_subject_rdf"></div>
		</div>
      </div>
      
      <h2>Research Methods</h2>
      <div class="facet-body-frame">
      	<div class="facet-body">
      		<div class="tagcloud" id="course_subject_rm"></div>
		</div>
      </div>
    </div>
    <div class="clear"></div>
  </div>  
</body>
</html>
