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
<%@ page session="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"  prefix="c" %>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	
	<title>Course Search</title>

	<link href='<c:out value="${skinRepo}" />/tool_base.css' type="text/css" rel="stylesheet" media="all" />
	<link href="<c:out value="${skinRepo}" />/<c:out value="${skinPrefix}" /><c:out value="${skinDefault}" />/tool.css" type="text/css" rel="stylesheet" media="all" />
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
	<div class="portletBody container-fluid">
		<div id="toolbar">
			<ul class="navIntraTool actionToolBar">
	
				<li><span class="current">Home</span></li>
				<li><span><a href="search.jsp">Search Courses</a></span></li>
				<li><span><a href="index.jsp">Browse by Department</a></span></li>  
				<li><span><a href="calendar.jsp">Browse by Calendar</a></span></li>
				<li><span><a href="vitae.jsp">Researcher Development</a></span></li>
				<c:if test="${!externalUser}" >
					<li><span><a href="my.jsp">My Courses</a></span></li>
					<c:if test="${isPending}" >
						<li><span><a href="pending.jsp">Pending Acceptances</a></span></li>	
					</c:if>
					<c:if test="${isApprover}" >
						<li><span><a href="approve.jsp">Pending Confirmations</a></span></li>
					</c:if>
					<c:if test="${isAdministrator}" >
						<li><span><a href="admin.jsp">Course Administration</a></span></li>
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
		
				<li class="search">
					<span class="searchImg"><a href="search.jsp">Search Courses</a></span>
					<p class="info">Search for courses. you can <strong>sort</strong> and <strong>filter</strong> your results by department, skill category etc.</p>
				</li>
				<li class="browse">
					<span class="browseImg"><a href="index.jsp">Browse by Department</a></span> 
					<p class="info">Browse for courses by division, department etc.</p>
				</li>
				<li class="calendar">
					<span class="calImg"><a href="calendar.jsp">Browse by Calendar</a></span> 
					<p class="info">Browse for courses by course start date.</p>
				</li>
				<li class="vitae">
					<span class="vitaeImg"><a href="vitae.jsp">Researcher Development</a></span>
					<p class="info">Search for courses by Vitae domains.</p>
				</li>
				<c:if test="${!externalUser}" >
					<li class="myModules" >
						<span class="myModImg"><a href="my.jsp">My Courses</a></span>
						<p class="info">View courses you are currently signed up for.</p>
					</li>
				</c:if>
			</ul>
		
			<ul class="options admin" >
		
				<c:if test="${!externalUser}" >
					<c:if test="${isPending}" >
						<li class="acceptances">
							<span class="adminImg"><a href="pending.jsp">Pending Acceptances</a></span> 
							<p class="info">View list of student sign-ups awaiting your approval.</p>
						</li>
					</c:if>
					<c:if test="${isApprover}">
						<li class="confirmations" >
							<span class="adminImg"><a href="approve.jsp">Pending Confirmations</a></span> 
							<p class="info">View courses which are waiting for your confirmation.</p>
						</li>
					</c:if>	
					<c:if test="${isAdministrator}">
						<li class="admin">
							<span class="adminImg"><a href="admin.jsp">Course Administration</a></span>
							<p class="info">Administer courses for which you are an administrator.</p>
						</li>
					</c:if>
					<c:if test="${isLecturer}">
						<li class="admin">
							<span class="adminImg"><a href="lecturer.jsp">Lecturers View</a></span>
							<p class="info">View courses which you are teaching.</p>
						</li>
					</c:if>
				</c:if>
			</ul>
		</div>
	</div>
 
<br clear="all" />
</body></html>
