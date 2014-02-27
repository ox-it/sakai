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
<%@ page import="org.sakaiproject.tool.cover.ToolManager"%>
<%@ page import="org.sakaiproject.component.cover.ServerConfigurationService" %>
<%@ page import="org.sakaiproject.user.cover.UserDirectoryService" %>
<%@ page session="false" %> 
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"  prefix="c" %>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	
	<!--  IE8 runnung in IE7 compatability mode breaks this page 
	       work around is the line below --> 
	<meta http-equiv="X-UA-Compatible" content="IE=8">
	
	<title>Module Search</title>

	<link href='<c:out value="${skinRepo}" />/tool_base.css' type="text/css" rel="stylesheet" media="all" />
	<link href="<c:out value="${skinRepo}" />/<c:out value="${skinDefault}" />/tool.css" type="text/css" rel="stylesheet" media="all" />
	<link rel="stylesheet" type="text/css" href="lib/tool.css">
  	
  	<script type="text/javascript" src="lib/jquery/jquery-1.4.4.min.js"></script>
  	<script type="text/javascript" src="lib/jqmodal-r14/jqModal.js"></script>
	<script type="text/javascript" src="lib/jquery-ui-1.8.4.custom/js/jquery-ui-1.8.4.custom.min.js"></script>
	<script type="text/javascript" src="lib/trimpath-template-1.0.38/trimpath-template.js"></script>
	<script type="text/javascript" src="lib/signup.min.js"></script>
	<script type="text/javascript" src="lib/Text.js"></script>
	<script type="text/javascript" src="lib/serverDate.js"></script>
	<script type="text/javascript" src="lib/datejs/date-en-GB.js"></script>
	
	<script type="text/javascript">
	
		/* Adjust with the content. */
		$(function(){
  			Signup.util.resize(window.name);
        });
		
	</script>
</head>	
<body>
<div id="toolbar">
    <ul class="navIntraTool actionToolBar">

		<li><span>Home</span></li>
		<li><span><a href="search.jsp">Search Modules</a></span></li>
		<li><span><a href="index.jsp">Browse by Department</a></span></li>  
		<li><span><a href="calendar.jsp">Browse by Calendar</a></span></li>
		<li><span><a href="vitae.jsp">Researcher Development</a></span></li>
		<c:if test="${!externalUser}" >
			<li><span><a href="my.jsp">My Modules</a></span></li>
			<c:if test="${isPending}" >
				<li><span><a href="pending.jsp">Pending Acceptances</a></span></li>	
			</c:if>
			<c:if test="${isApprover}" >
				<li><span><a href="approve.jsp">Pending Confirmations</a></span></li>
			</c:if>
			<c:if test="${isAdministrator}" >
				<li><span><a href="admin.jsp">Module Administration</a></span></li>
			</c:if>
			<c:if test="${isLecturer}">
				<li><span><a href="lecturer.jsp">Lecturer View</a></span></li>
			</c:if>
		</c:if>
	</ul>
</div>
 
<div class="wrapper" >   
	
<h2>Welcome to the Researcher Training information site.</h2>

<p class="intro">There are a wealth of graduate and post-doc training opportunities across the University aimed at supporting you in your research and career development. You can browse, search and sign up for these opportunities using this site.</p>

	<c:if test="${externalUser}" >
	<p class="alert">If you are a member of the University of Oxford, then you should log in to make full use of this tool.</p>
	</c:if>

	<ul class="options" >

		<li class="search" >
			<a href="search.jsp">Search Modules</a> 
			<span class="info">Search for modules. you can <strong>sort</strong> and <strong>filter</strong> your results by department, skill category, research methods, current/previous modules, etc.</span>
		</li>
	
		<li class="browse" >
			<a href="index.jsp">Browse by Department</a> 
			<span class="info">Browse for modules by division, department etc.</span>
		</li>
		<li class="calendar" >
			<a href="calendar.jsp">Browse by Calendar</a> 
			<span class="info">Browse for modules by course start date.</span>
		</li>
		<li class="vitae" >
			<a href="vitae.jsp">Researcher Development</a>
			<span class="info">Search for modules by Vitae domains.</span>
		</li>
		<c:if test="${!externalUser}" >
			<li class="myModules" >
				<a href="my.jsp">My Modules</a> 
				<span class="info">View modules you are currently signed up for.</span>
			</li>
		</c:if>
	</ul>

	<ul class="options admin" >

		<c:if test="${!externalUser}" >
			<c:if test="${isPending}" >
				<li class="acceptances" >
					<a href="pending.jsp">Pending Acceptances</a> 
					<span class="info">View list of student sign-ups awaiting your approval.</span>
				</li>
			</c:if>
			<c:if test="${isApprover}" >
				<li class="confirmations" >
					<a href="approve.jsp">Pending Confirmations</a> 
					<span class="info">View modules which are waiting for your confirmation.</span>
				</li>
			</c:if>	
			<c:if test="${isAdministrator}" >
				<li class="admin">
					<a href="admin.jsp">Module Administration</a> 
					<span class="info">Administer modules for which you are an administrator.</span>
				</li>
			</c:if>
			<c:if test="${isLecturer}" >
				<li class="admin">
					<a href="lecturer.jsp">Lecturers View</a> 
					<span class="info">View modules which you are teaching.</span>
				</li>
			</c:if>
		</c:if>
	</ul>

</div>
 
<br clear="all" />
</body></html>
