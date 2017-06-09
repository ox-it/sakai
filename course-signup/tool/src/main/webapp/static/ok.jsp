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

<title>Module Signup</title>

<!-- Jersey puts the model in the 'it' attribute -->
<link href='<c:out value="${it.skinRepo}" />/tool_base.css' type="text/css" rel="stylesheet" media="all" />
<link href="<c:out value="${it.skinRepo}" />/<c:out value="${it.skinPrefix}" /><c:out value="${it.skinDefault}" />/tool.css" type="text/css" rel="stylesheet" media="all" />

<link rel="stylesheet" type="text/css" href="lib/jqmodal-r14/jqModal.css" />
<link rel="stylesheet" type="text/css" href="lib/dataTables-1.7/css/demo_table_jui.css" />
<link rel="stylesheet" type="text/css" href="lib/jquery-ui-1.8.4.custom/css/smoothness/jquery-ui-1.8.4.custom.css" />
<link rel="stylesheet" type="text/css" href="lib/tool.css" />

<script type="text/javascript" src="lib/jquery/jquery-1.4.4.min.js"></script>
<script type="text/javascript"
	src="lib/jstree-1.0rc2/_lib/jquery.cookie.js"></script>
<script type="text/javascript" src="lib/jstree-1.0rc2/jquery.jstree.js"></script>
<script type="text/javascript" src="lib/jqmodal-r14/jqModal.js"></script>
<script type="text/javascript"
	src="lib/jquery-ui-1.8.4.custom/js/jquery-ui-1.8.4.custom.min.js"></script>
<script type="text/javascript"
	src="lib/trimpath-template-1.0.38/trimpath-template.js"></script>
<script type="text/javascript"
	src="lib/dataTables-1.7/js/jquery.dataTables.js"></script>
<script type="text/javascript" src="lib/dataTables.reloadAjax.js"></script>
<script type="text/javascript" src="lib/signup.min.js"></script>
<script type="text/javascript" src="lib/Text.js"></script>
<script type="text/javascript" src="lib/serverDate.js"></script>
<script type="text/javascript" src="lib/QueryData.js"></script>

</head>
<body>
	<div id="toolbar">
		<ul class="navIntraTool actionToolBar">
			<li><span>Home</span></li>
			<li><span>Search Modules</span></li>
			<li><span>Browse by Department</span></li>
			<li><span>Browse by Calendar</span></li>
		</ul>
	</div>
	<div id="messages"></div>

	<div id="browse">
		<c:forEach var="error" items="${it.errors}">
			<div class="alertMessage"><c:out value="${error}"/> </div>
		</c:forEach>
		<p>
		<c:if test="${not empty it.signup}">
			<c:out value="${it.signup.user.name}" />
			has signed up to
			<c:out value="${it.signup.group.title}" />
			<c:choose>
				<c:when test="${it.status=='accept'}">
						Your acceptance for the following modules was successful:
					</c:when>
				<c:when test="${it.status=='approve'}">
						Your approval for the following modules was successful:
					</c:when>
				<c:when test="${it.status=='confirm'}">
						Your acceptance for the following modules was successful:
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
			<br />
		</c:if>
		</p>
	</div>
	<br clear="all">
</body>
</html>
