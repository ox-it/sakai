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
  <title>Module Signup Search</title>
  
  <link href='<c:out value="${skinRepo}" />/tool_base.css' type="text/css" rel="stylesheet" media="all" />
  <link href="<c:out value="${skinRepo}" />/<c:out value="${skinPrefix}" /><c:out value="${skinDefault}" />/tool.css" type="text/css" rel="stylesheet" media="all" />
  
  <script type="text/javascript" src="lib/ajax-solr-master/jquery/jquery-1.9.1.min.js"></script>
  <script type="text/javascript" src="lib/ajax-solr-master/jquery/jquery-migrate-1.1.1.min.js"></script>
  <script type="text/javascript" src="lib/jquery-ui-1.8.4.custom/js/jquery-ui-1.8.4.custom.min.js"></script>
  <script type="text/javascript" src="lib/jstree-1.0rc2/_lib/jquery.cookie.js"></script>
  <script type="text/javascript" src="lib/jqmodal-r14/jqModal.js"></script>
  <script type="text/javascript" src="lib/signup.min.js"></script>
  <script type="text/javascript" src="lib/serverDate.js"></script>
  <script type="text/javascript" src="lib/trimpath-template-1.0.38/trimpath-template.js"></script>
  
  <link rel="stylesheet" type="text/css" href="lib/jqmodal-r14/jqModal.css" />
  <link rel="stylesheet" type="text/css" href="lib/jquery-ui-1.8.4.custom/css/smoothness/jquery-ui-1.8.4.custom.css" />
  <link rel="stylesheet" type="text/css" href="lib/tool.css" />
  
	<link rel="stylesheet" href="lib/ajax-solr-master/ajax-solr-bundle.min.css">

	<script src="lib/ajax-solr-master/search.js"></script>
	
	<script src="lib/ajax-solr-master/ajax-solr-bundle.min.js"></script>
	
	<!-- 
  <script src="lib/ajax-solr-master/core/Core.js"></script>
  <script src="lib/ajax-solr-master/core/AbstractManager.js"></script>
  <script src="lib/ajax-solr-master/managers/Manager.jquery.js"></script>
  <script src="lib/ajax-solr-master/core/Parameter.js"></script>
  <script src="lib/ajax-solr-master/core/ParameterStore.js"></script>
  <script src="lib/ajax-solr-master/core/ParameterExtraStore.js"></script>
  <script src="lib/ajax-solr-master/core/AbstractWidget.js"></script>
  <script src="lib/ajax-solr-master/search/widgets/ResultWidget.js"></script>
  <script src="lib/ajax-solr-master/widgets/jquery/PagerWidget.js"></script>
  <script src="lib/ajax-solr-master/core/AbstractFacetWidget.js"></script>
  <script src="lib/ajax-solr-master/search/widgets/TagcloudWidget.js"></script>
  <script src="lib/ajax-solr-master/search/widgets/CurrentSearchWidget.js"></script>
  <script src="lib/ajax-solr-master/core/AbstractTextWidget.js"></script>
  <script src="lib/ajax-solr-master/search/widgets/TextWidget.js"></script>
  <script src="lib/ajax-solr-master/search/widgets/ErrorWidget.js"></script>
  <script src="lib/ajax-solr-master/search/widgets/BooleanWidget.js"></script>
  <script src="lib/ajax-solr-master/search/widgets/BooleanFacetWidget.js"></script>
    -->
	<script type="text/javascript">
		var externalUser = <c:out value="${externalUser}" />;
		var recentDays = "<%= ServerConfigurationService.getString("recent.days", "14") %>";
		
		/* Adjust with the content. */
		$(function(){
			Signup.util.autoresize();
		});
	</script>
 
</head>
<body>

	<div id="toolbar">
		<ul class="navIntraTool actionToolBar">
			<li><span><a href="home.jsp">Home</a></span></li>
			<li><span>Search Modules</span></li>
			<li><span><a href="index.jsp">Browse by Department</a></span></li>
			<li><span><a href="calendar.jsp">Browse by Calendar</a></span></li>
			<li><span><a href="vitae.jsp">Researcher Development</a></span></li>
			<c:if test="${!externalUser}">
				<li><span><a href="my.jsp">My Modules</a></span></li>
				<c:if test="${isPending}">
					<li><span><a href="pending.jsp">Pending Acceptances</a></span></li>
				</c:if>
				<c:if test="${isApprover}">
					<li><span><a href="approve.jsp">Pending
								Confirmations</a></span></li>
				</c:if>
				<c:if test="${isAdministrator}">
					<li><span><a href="admin.jsp">Module Administration</a></span></li>
				</c:if>
				<c:if test="${isLecturer}">
				<li><span><a href="lecturer.jsp">Lecturer View</a></span></li>
			</c:if>
			</c:if>
		</ul>
	</div>
	
	<div id="search_wrapper" class="simple_search" >
	
		<div class="left">
	
			<div class="error" id="leftError">
			</div>
			
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
			<div id="search">
			
				<div class="error" id="rightError">
				</div>
			
				<form class="search">
					<input type="text" id="query" name="query" autocomplete="off">
					<br />
					<input type="submit" value="Search">
				</form>
				<!-- 
				<div class="advanced_link">
					<a class="advanced" href="#">Advanced Search</a>
				</div>	
				 -->
			</div>
				
			<div class="facets" id="facets">
				<h2>Department</h2>
				<div class="facet-body-frame">
					<div class="facet-body" id="provider_title"></div>
				</div>

				<h2>Skills Category</h2>
				<div class="facet-body-frame">
					<div class="facet-body" id="course_subject_rdf"></div>
				</div>

				<h2>Research Method</h2>
				<div class="facet-body-frame">
					<div class="facet-body" id="course_subject_rm"></div>
				</div>

				<h2>Delivery Method</h2>
				<div class="facet-body-frame">
					<div class="facet-body" id="course_delivery"></div>
				</div>

				<h2>RDF Domain</h2>
				<div class="facet-body-frame">
				    <div class="facet-body" id="course_subject_vitae_domain"></div>
				</div>

				<h2>RDF Sub-domain</h2>
				<div class="facet-body-frame">
				    <div class="facet-body" id="course_subject_vitae_subdomain"></div>
				</div>

			</div>

			<div>
			    <h2>Show Only</h2>
			    <div class="others">
			        <div class="other">
			            <input type="checkbox" id="show_old">
			            <label for="show_old">Old Courses</label><br/>
			        </div>
			        <div class="other" id="show_new">
			            <input type="checkbox" id="show_new_input">
			            <label for="show_new_input">Recently Added Courses</label><br/>
			        </div>
			    </div>
			</div>
		</div>
		<div class="clear"></div>
	</div> 
</body>
</html>
