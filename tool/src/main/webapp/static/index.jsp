<%@ page import="org.sakaiproject.tool.cover.ToolManager"%>
<%@ page import="org.sakaiproject.component.cover.ServerConfigurationService" %>
<%@ page import="org.sakaiproject.user.cover.UserDirectoryService" %>
<%@ page session="false" %> 
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"  prefix="c" %>
<%
if (UserDirectoryService.getAnonymousUser().equals(UserDirectoryService.getCurrentUser())) {
	pageContext.setAttribute("externalUser",true);
} else {
	pageContext.setAttribute("externalUser",false);
}
%>
<c:set var="isExternalUser" value="${externalUser}" />

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	
	<!--  IE8 runnung in IE7 compatability mode breaks this page 
	       work around is the line below --> 
	<meta http-equiv="X-UA-Compatible" content="IE=8">
	
	<title>Module Search</title>

	<link href="<%= ServerConfigurationService.getString("skin.repo", "/library/skin") %>/tool_base.css" type="text/css" rel="stylesheet" media="all" />
	<link href="<%= ServerConfigurationService.getString("skin.repo", "/library/skin") %>/<%= ServerConfigurationService.getString("skin.default", "default") %>/tool.css" type="text/css" rel="stylesheet" media="all" />
	<link rel="stylesheet" type="text/css" href="lib/tool.css">
    <!-- styles for mock-up -->
	<link rel="stylesheet" type="text/css" href="lib/ji-styles.css">
  	
  	<script type="text/javascript" src="lib/jquery/jquery-1.4.2.min.js"></script>
  	<script type="text/javascript" src="lib/jqmodal-r14/jqModal.js"></script>
	<script type="text/javascript" src="lib/jquery-ui-1.8.4.custom/js/jquery-ui-1.8.4.custom.min.js"></script>
	<script type="text/javascript" src="lib/trimpath-template-1.0.38/trimpath-template.js"></script>
	<script type="text/javascript" src="lib/signup.js"></script>
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
		<li><span><a href="browse.jsp">Browse by Department</a></span></li> 
		<!--  
		<li><span><a href="calendar.jsp">Browse by Calendar</a></span></li>
		-->
		<c:if test="${!isExternalUser}" >
			<li><span><a href="my.jsp">My Modules</a></span></li>
			<li><span><a href="approve.jsp">Pending Confirmations</a></span></li>
			<li><span><a href="pending.jsp">Pending Acceptances</a></span></li>	
			<li><span><a href="admin.jsp">Module Administration</a></span></li>
		</c:if>
	</ul>
</div>
    
    
<div class="wrapper" >   
	
	<p class="intro"><strong>Welcome to the Student Enrolment System.</strong> Here you can browse, search and sign up for  modules from across the University that will help you in your studies.</p>

	<ul class="options" >

		<li class="search" >
			<a href="search.jsp">Search Modules</a> 
			<span class="info">Search for modules. you can <strong>sort</strong> and <strong>filter</strong> your results by department, skill category, research methods, current/previous modules, etc.</span>
		</li>
	
		<li class="browse" >
			<a href="browse.jsp">Browse by Department</a> 
			<span class="info">Browse for modules by division, department etc.</span>
		</li>
		<!--  
		<li class="calendar" >
			<a href="calendar.jsp">Browse by Calendar</a> 
			<span class="info">Browse for modules by course start date.</span>
		</li>
		-->
		<c:if test="${!isExternalUser}" >
			<li class="myModules" >
				<a href="my.jsp">My Modules</a> 
				<span class="info">View modules you are currently signed up for.</span>
			</li>
		</c:if>
	</ul>

	<ul class="options admin" >

		<c:if test="${!isExternalUser}" >
			<li class="confirmations" >
				<a href="approve.jsp">Pending Confirmations</a> 
				<span class="info">View modules which are waiting for your confirmation.</span>
			</li>	
	
			<li class="acceptances" >
				<a href="pending.jsp">Pending Acceptances</a> 
				<span class="info">View list of student sign-ups awaiting your approval.</span>
			</li>
	
			<li class="admin">
				<a href="admin.jsp">Module Administration</a> 
				<span class="info">Administer modules for which you are an administrator.</span>
			</li>
		</c:if>
	</ul>

</div>
 
<br clear="all" />
</body></html>