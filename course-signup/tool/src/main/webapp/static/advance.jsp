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
<%@ page session="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />

<title>Course Signup</title>

<!-- Jersey puts the model in the 'it' attribute -->
<link href='<c:out value="${it.skinRepo}" />/tool_base.css' type="text/css" rel="stylesheet" media="all" />
<link href="<c:out value="${it.skinRepo}" />/<c:out value="${it.skinPrefix}" /><c:out value="${it.skinDefault}" />/tool.css" type="text/css" rel="stylesheet" media="all" />
	
<link rel="stylesheet" type="text/css" href="lib/jqmodal-r14/jqModal.css" />
<link rel="stylesheet" type="text/css" href="lib/dataTables-1.7/css/demo_table_jui.css" />
<link rel="stylesheet" type="text/css" href="lib/jquery-ui-1.8.4.custom/css/smoothness/jquery-ui-1.8.4.custom.css" />
<link rel="stylesheet" type="text/css" href="lib/tool.css" />

</head>
<body>
	<div id="toolbar">
		<ul class="navIntraTool actionToolBar">
			<li><span>Home</span></li>
			<li><span>Search Courses</span></li>
			<li><span>Browse by Department</span></li>
			<li><span>Browse by Calendar</span></li>
		</ul>
	</div>
	<div id="messages"></div>

	<div id="browse">
		<c:forEach var="error" items="${it.errors}">
			<div class="alertMessage"><c:out value="${error}"/> </div>
		</c:forEach>
		<form method="POST">
			<input type="hidden" name="param" value="<c:out value='${it.encoded}' />" />
			<div id="tree">
				<p>
					<c:out value="${it.signup.user.name}" />
					has signed up to
					<c:out value="${it.signup.group.title}" />
					<c:choose>
						<c:when test="${it.status=='accept'}">
								.  As the course administrator your acceptance is needed.
							</c:when>
						<c:when test="${it.status=='approve'}">
								and entered you as their supervisor.  Your approval is needed for the following courses:
							</c:when>
						<c:when test="${it.status=='confirm'}">
								.  As the departmental approver your acceptance is needed.
							</c:when>
					</c:choose>

					<br />
					<br />
					<c:out value="${it.signup.group.title}" />
					(
					<c:out value="${it.signup.group.department}" />
					) <br />
					<c:forEach var="component" items="${it.signup.components}">
							&nbsp;- <c:out value="${component.title}" />
							: <c:out value="${component.teachingDetails}" />
							for <c:out value="${component.sessions}" /> 
							starts in <c:out value="${component.when}" />
						<c:if test="${not empty component.presenter.name}">
								with <c:out value="${component.presenter.name}" />
						</c:if>
						<br />
					</c:forEach>
					<c:if test="${not empty it.status}">
					<br /> Please either 
					<input type="submit" name="formStatus" value="<c:out value='${fn:toUpperCase(it.status)}' />" /> 
					or 
					<input type="submit" name="formStatus" value="REJECT" /> 
					this signup.
					</c:if>
				</p>
			</div>
		</form>
	</div>
	<br clear="all">
</body>
</html>
