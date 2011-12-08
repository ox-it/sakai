<%@ page import="org.sakaiproject.tool.cover.ToolManager"%>
<%@ page import="org.sakaiproject.component.cover.ServerConfigurationService" %>
<%@ page import="org.sakaiproject.user.cover.UserDirectoryService" %>
<%@ page session="false" %> 
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"  prefix="c" %>

<%
pageContext.setAttribute("jobName", ServerConfigurationService.getString("ses.import.job", "gobbledegoup"));
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<!-- Make the page render as IE8 for wrapping in jstree -->
	<meta http-equiv="X-UA-Compatible" content="IE=8" >
	
	<title>Module Signup</title>
 
	<link href="<%= ServerConfigurationService.getString("skin.repo", "/library/skin") %>/tool_base.css" type="text/css" rel="stylesheet" media="all" />
	<link href="<%= ServerConfigurationService.getString("skin.repo", "/library/skin") %>/<%= ServerConfigurationService.getString("skin.default", "default") %>/tool.css" type="text/css" rel="stylesheet" media="all" />
	
	<script type="text/javascript" language="JavaScript" src="/library/js/headscripts.js"></script>
    
    </head>
    <body onload="setMainFrameHeight(window.name)">
    	<div>
    	
		<div id="messages">
        </div>
		
        <div id="browse">
        	<form method="post">
        		<img alt="" src="/course-signup/static/images/quartz.jpg">
        		
        		<fieldset>
        			<c:forEach var="jobDetailWrapper" items="${jobDetailList}">
        				<c:choose>
        				<c:when test="${jobDetailWrapper.jobDetail.name == jobName}">
        					<input type="radio" name="jobName" 
        						value="<c:out value="${jobDetailWrapper.jobDetail.name}"/>" checked >
							<span style="font-weight:bolder">
								<c:out value="${jobDetailWrapper.jobDetail.name}" />
							</span>
						</c:when>
						<c:otherwise>
        					<input type="radio" name="jobName" 
        						value="<c:out value="${jobDetailWrapper.jobDetail.name}" />" disabled>
							<span style="font-weight:lighter">
								<c:out value="${jobDetailWrapper.jobDetail.name}" />
							</span>
						</c:otherwise>
						</c:choose>
						<br>
					</c:forEach>
        		</fieldset>
        		
        		<fieldset>
        			<legend>Run Job Now Confirmation:  <c:out value="${jobName}" /></legend>
        			<table class="chefEditItem" cellspacing="0">
						<tbody>
						<tr>
						<td class="chefLabel">Are you sure you would like to run the job now?</td>
						</tr>
						</tbody>
					</table>
        		</fieldset>
        		
        		<p>
					<input type="submit" value="Run Now" />
				</p>
			</form>
		</div>
		
		</div>
    </body>
</html>