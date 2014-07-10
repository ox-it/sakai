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
<%@ page import="org.sakaiproject.user.cover.UserDirectoryService"%>
<%@ page session="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%
	if (UserDirectoryService.getAnonymousUser().equals(
			UserDirectoryService.getCurrentUser())) {
		String redirectURL = "login.jsp";
		response.sendRedirect(redirectURL);
	}
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Module Signup</title>

<link href='<c:out value="${skinRepo}" />/tool_base.css' type="text/css" rel="stylesheet" media="all" />
<link href="<c:out value="${skinRepo}" />/<c:out value="${skinPrefix}" /><c:out value="${skinDefault}" />/tool.css" type="text/css" rel="stylesheet" media="all" />

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
	src="lib/trimpath-template-1.0.38/trimpath-template.js"></script>
<script type="text/javascript"
	src="lib/jquery-ui-1.8.4.custom/js/jquery-ui-1.8.4.custom.min.js"></script>
<script type="text/javascript"
	src="lib/dataTables-1.7/js/jquery.dataTables.js"></script>
<script type="text/javascript" src="lib/dataTables.reloadAjax.js"></script>
<script type="text/javascript" src="lib/signup.min.js"></script>
<script type="text/javascript" src="lib/Text.js"></script>
<script type="text/javascript" src="lib/serverDate.js"></script>

<script type="text/javascript">
	var courseData;

	$(function() {

		var processSignupRow = function(item) {
			var components = $.map(
					item.components,
					function(component) {
						return component.title + " " + component.slot + "<br>("
								+ component.places + " places)";
					}).join("<br>");
			return [
					item.id,
					item.created,
					Signup.user.render(item.user),
					Signup.user.render(item.supervisor),
					components,
					item.status,
					Signup.signup.formatActions(Signup.signup.getActions(
							item.status, item.id)) ];
		};

		/**
		 * Gets the status of a component. This is used on the admin page to show what state the component is in.
		 * @param {Object} component
		 * @return A string to represent the status of this component.
		 */
		var getComponentStatus = function(opens, closes) {
			var now = $.serverDate();
			if (now < opens) {
				return "Pending";
			} else if (now < closes) {
				return "Open";
			} else {
				return "Closed";
			}
		};

		var loadCourse = function(object) {

			var code = object.courseId;
			var name = object.title;

			// Table showing the components.
			var html = '<h3 style="display:inline">' + name + '</h3>';
			html += '<table border="0" class="display" id="course-details-table"></table>';
			$("#course-details").html(html);

			var summary = $("#course-details-table")
					.dataTable(
							{
								"bJQueryUI" : true,
								"bProcessing" : true,
								"bPaginate" : true,
								"bLengthChange" : false,
								"iDisplayLength" : 5,
								"bFilter" : false,
								"bInfo" : false,
								"bAutoWidth" : false,
								"sAjaxSource" : "../rest/course/",
								"aaSorting" : [ [ 8, 'asc' ] ], // Sort on the signup date.
								"aoColumns" : [
										{
											"sTitle" : "Component"
										},
										{
											"sTitle" : "Size"
										},
										{
											"sTitle" : "Places",
											"fnRender" : function(aObj) {
												var size = aObj.aData[1];
												var places = aObj.aData[2];
												var limit = size
														* placesWarnPercent
														/ 100
												var style;
												if (placesErrorLimit >= aObj.aData[2]) {
													style = "color: red";
												} else if (limit >= aObj.aData[2]) {
													style = "color: orange";
												} else {
													style = "color: black";
												}
												return '<span style="'+style+'">'
														+ aObj.aData[2]
														+ '</span>';
											}
										},
										{
											"sTitle" : "Signup Period",
											"fnRender" : function(aObj) {
												// Hack to force the data back to being a int.
												return new Date(+aObj.aData[3])
														.toDateString()
														+ " (for "
														+ Signup.util
																.formatDuration(aObj.aData[8]
																		- aObj.aData[3])
														+ ")";
											},
											"bUseRendered" : false
										},
										{
											"sTitle" : "Term"
										},
										{
											"sTitle" : "Status",
											"fnRender" : function(aObj) {
												return getComponentStatus(
														aObj.aData[3],
														aObj.aData[8]);
											}
										},
										{
											"bVisible" : false
										},
										{
											"bVisible" : false
										},
										{
											"bVisible" : false
										},
										{
											"sTitle" : "Export",
											"bSortable" : false,
											"fnRender" : function(aObj) {
												return '<a href="../rest/signup/component/'+ aObj.aData[9]+ '.csv">Export</a>';
											}
										},
										{
											"sTitle" : "Email",
											"bSortable" : false,
											"fnRender" : function(aObj) {
												return '<img class="mailto-all-course" id="'+aObj.aData[10]+'"src="images/email-send.png" title="send email to all CONFIRMED signups" />';
											}
										},
										{
											"sTitle" : "Attendance",
											"bSortable" : false,
											"fnRender" : function(aObj) {
												return '<a href="../rest/signup/component/'+ aObj.aData[11]+ '.pdf">Register</a>';
											}
										} ],

								"fnServerData" : function(sSource, aoData,
										fnCallback) {
									$.getJSON(sSource + code, {
										"range" : "ALL"
									}, function(data) {
										var tableData = {
											"aaData" : []
										};
										for ( var i in data.components) {
											var component = data.components[i];
											tableData.aaData.push([
													component.title, //0
													component.size,
													component.places,
													component.opens,
													component.when, //4
													component.sessions,
													component.presenter,
													component.administrator,
													component.closes, //8
													component.id, component.id,
													component.id ]);
										}
										fnCallback(tableData);
									});
								}
							});

			var html = '<h3 style="display:inline">Signups</h3>';
			html += '<span style="float:right; padding-right:20px;">Status Filter <select class="signups-table-status-filter">';
			html += '<option selected="true" value = "">All</option>';
			html += '<option value="WAITING">WAITING</option>';
			html += '<option value="PENDING">PENDING</option>';
			html += '<option value="ACCEPTED">ACCEPTED</option>';
			html += '<option value="APPROVED">APPROVED</option>';
			html += '<option value="CONFIRMED">CONFIRMED</option>';
			html += '<option value="REJECTED">REJECTED</option>';
			html += '<option value="WITHDRAWN">WITHDRAWN</option>';
			html += '</select></span>';
			html += '<table border="0" class="display" id="signups-table"></table>';
			$("#signups").html(html);
		
			// Load the signups.
			var signups = $("#signups-table").signupTable(
					"../rest/signup/course/" + code +"?status=CONFIRMED", false, false, false);
			signups.bind("reload", function() { // Reload the summary when this table changes.
				summary.fnReloadAjax(null, null, true);
			})
			return;
			// Handlers to decide what actions you can do when selecting multiple items.
			$("#signups-table input[type=checkbox]")
					.live(
							"change",
							function(e) {

								var matchingStatus;
								var allMatching = true;

								$("#signups-table input[type=checkbox]")
										.each(
												function() {
													if (this.checked) {
														var row = this.value;
														var status = $(
																"#signups-table")
																.dataTable()
																.fnGetData(row)[4];
														if (matchingStatus) {
															if (allMatching) {
																allMatching = matchingStatus == status;
															}
														} else {
															matchingStatus = status;
														}
													}
												});
							});
		};
		//
		// Switch to dropdown list.
		$.ajax({
			"url" : "../rest/course/lecture",
			"type" : "GET",
			"async" : true,
			"cache" : false,
			"success" :

			function(data) {
				var options = "";
				courseData = data;
				courseData.sort(function(x, y) {
					var a = x.title;
					var b = y.title;
					if (a === b) {
						return 0;
					} else if (a < b) {
						return -1;
					} else {
						return 1;
					}
				});
				$.each(courseData, function(index, value) {
					var title = value.title;
					if (value.isSuperuser && value.hideGroup) {
						title = "(hidden) " + title;
					}
					options += '<option value="'+ value.courseId + '">' + title
							+ '</option>';
				});

				if (data.length > 0) {
					$("#course-list").html(
							'<form>Select a module: <select id="admin-course" name="course">'
									+ options + '</select></form>');
					$("#admin-course").change(function(e) {
						var courseId = this.options[this.selectedIndex].value;
						for ( var i in courseData) {
							var dataId = courseData[i].courseId;
							if (dataId === courseId) {
								loadCourse(courseData[i]);
								return;
							}
						}
					});
					loadCourse(courseData[0]);
				} else {
					$("#course-list").html(
							'You are not an lecturer on any modules.');
				}
			}
		});

		Signup.util.autoresize();

		$("img.mailto-all-course", this).die().live(
				"click",
				function(e) {
					var courseId = $(this).attr("id");
					$.ajax({
						url : "../rest/signup/component/" + courseId,
						type : "GET",
						data : {
							status : "CONFIRMED"
						},
						success : function(result) {
							if (result.length > 0) {
								var users = [];
								$.each(result, function() {
									users.push([ this.user.email ]);
								});
								document.location.href = "mailto:?bcc="
										+ users.join(';') + "&subject=Re "
										+ result[0].group.department
										+ " department course title "
										+ result[0].group.title;
							}
						}
					});
				});
	});
</script>

</head>
<body>
	<div id="toolbar">
		<ul class="navIntraTool actionToolBar">
			<li><span><a href="home.jsp">Home</a></span></li>
			<li><span><a href="search.jsp">Search Modules</a></span></li>
			<li><span><a href="index.jsp">Browse by Department</a></span></li>
			<li><span><a href="calendar.jsp">Browse by Calendar</a></span></li>
			<li><span><a href="vitae.jsp">Researcher Development</a></span></li>
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
			<li><span>Lecturers View</span></li>
		</ul>
	</div>

	<div id="course-list">
		<!-- Browse the areas which there are courses -->
	</div>
	<div id="course-details" style="margin-top: 14px;"></div>
	<!-- Show details of the course -->
	<div id="signups"></div>


	<!-- Hidden extra bits -->
	<!-- Popup window for selecting components. -->
	<div id="signup-add-components-win" class="jqmWindow"
		style="display: none"></div>

	<textarea id="signup-add-components-tpl" style="display: none" rows="0"
		cols="0">
	<h2>Users Found</h2>
	<ul>
	{for user in users}
		<li>\${user.name} (\${user.email})</li>
	{/for}
	</ul>
	<h2>Select Modules</h2>
	
	<form id="signup-add-components">
	<span class="errors"></span>
	<ul>
	{for component in components}
		<li>
			<input type="checkbox" name="\${component.id}"
					id="option-\${component.id}" value="true">
			<label for="component-\${component.id}">\${component.title} - \${component.slot} for \${component.sessions} sessions in \${component.when},
					{if component.presenter}<a
						href="mailto:\${component.presenter.email}">\${component.presenter.name}</a>{/if}
					</label>
                                <br />
                                <span class="location">\${component.location}</span>
		</li>
	{/for}
	</ul>
		<input type="submit" value="Add">
		<input type="button" class="cancel" value="Cancel">
		<div id="create-signups-progress"></div>

	</form>
</textarea>

</body>
</html>
