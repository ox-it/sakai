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
<%
pageContext.setAttribute("jobTypes", ServerConfigurationService.getStrings("ses.import.jobs"));
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<title>Module Signup</title>
 
	<link href='<c:out value="${skinRepo}" />/tool_base.css' type="text/css" rel="stylesheet" media="all" />
	<link href="<c:out value="${skinRepo}" />/<c:out value="${skinPrefix}" /><c:out value="${skinDefault}" />/tool.css" type="text/css" rel="stylesheet" media="all" />
	
	<script type="text/javascript" language="JavaScript" src="/library/js/headscripts.js"></script>
	
	<script type="text/javascript">
		
	function jobToRun(radio) {
		
		var html = '<fieldset>';
		html += '<legend id="jobToRun">Run Job Now Confirmation: ';
		html += radio.value;
		html += '<table class="chefEditItem" cellspacing="0">';
		html += '<tbody>';
		html += '<tr>';
		html += '<td class="chefLabel">Are you sure you would like to run the job now?</td>';
		html += '</tr>';
		html += '</tbody>';
		html += '</table>';
		html += '</fieldset>';
		html += '<p>';
		html += '<input type="submit" value="Run Now" />';
		html += '</p>';
		
		document.getElementById("runJob").innerHTML= html;
		setMainFrameHeight(window.name);
	}

	</script>
    
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
        				<c:set var="flag" value="0" />
        				<c:forEach var="jobType" items="${jobTypes}">
        					<c:if test="${jobDetailWrapper.jobDetail.jobDataMap['org.sakaiproject.api.app.scheduler.JobBeanWrapper.jobType'] == jobType}"> 
        						<c:set var="flag" value="1" />
        					</c:if>
        				</c:forEach>
        				<c:choose>
        				<c:when test="${flag == 1}"> 
        					<input type="radio" name="jobName" 
        						value="<c:out value="${jobDetailWrapper.jobDetail.name}" />"
        						onclick="jobToRun(this)"/> 
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
        		
        		<div id="runJob">
        		</div>
			</form>
		</div>
		
		</div>
    </body>
</html>