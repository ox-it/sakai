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

<script type="text/javascript">
	$(function() {

		// The site to load the static files from.
		var signupSiteId = "/access/content/group/<%=ServerConfigurationService.getString(
					"course-signup.site-id", "course-signup")%>";

		/**
		 * This loads details about a node in the tree.
		 * This basically loads a HTML files and shows the user.
		 * @param {Object} id
		 */
		$.ajax({
			"url" : signupSiteId + "/my_modules.html",
			"cache" : false,
			"success" : function(data) {
				// This is because we now top and tail files in Sakai.
				data = data.replace(/^(.|\n)*<body[^>]*>/im, "");
				data = data.replace(/<\/body[^>]*>(.|\n)*$/im, "");
				$("#notes").html(data);
			}
		});

		/*
		$("#signups").html('<table border="0" class="display" id="signups-table"></table>');
		var signups = $("#signups-table").signupTable("../rest/signup/my", false);
		Signup.util.autoresize();
		 */

		$.fn.myTable = function(url) {
			var element = this;
			var table = this
					.dataTable({
						"bJQueryUI" : true,
						"sPaginationType" : "full_numbers",
						"bProcessing" : true,
						"sAjaxSource" : url,
						"bAutoWidth" : false,
						"aaSorting" : [ [ 1, "desc" ] ],
						"aoColumns" : [
								{
									"sTitle" : "",
									"bVisible" : false,
									"bUseRendered" : false
								},
								{
									"sTitle" : "Created",
									"fnRender" : function(aObj) {
										if (aObj.aData[1]) {
											return Signup.util.formatDuration($
													.serverDate()
													- aObj.aData[1])
													+ " ago";
										} else {
											return "unknown";
										}
									},
									"bUseRendered" : false
								}, {
									"sTitle" : "Student",
									"sWidth" : "20%"
								}, {
									"sTitle" : "Module"
								}, {
									"sTitle" : "Supervisor"
								}, {
									"sTitle" : "Notes",
									"sWidth" : "20%",
									"sClass" : "signup-notes"
								}, {
									"sTitle" : "Status"
								}, {
									"sTitle" : "Actions"
								}, {
									"sTitle" : "Status",
									"bVisible" : false
								} ],
						"fnServerData" : function(sSource, aoData, fnCallback) {
							jQuery
									.ajax({
										dataType : "json",
										type : "GET",
										cache : false,
										url : sSource,
										success : function(result) {
											var data = [];
											$
													.each(
															result,
															function() {
																var course = [ '<span class="course-group"><a class="more-details-link" href="'+this.group.courseId+'">'
																		+ this.group.title
																		+ "</a></span>" ]
																		.concat(
																				$
																						.map(
																								this.components
																										.concat(),
																								function(
																										component) {
																									var size = component.size;
																									var limit = size
																											* placesWarnPercent
																											/ 100;
																									var componentPlacesClass;
																									if (placesErrorLimit >= component.places) {
																										componentPlacesClass = "course-component-error";
																									} else if (limit >= component.places) {
																										componentPlacesClass = "course-component-warn";
																									} else {
																										componentPlacesClass = "course-component";
																									}

																									return '<span class="course-component">'
																											+ component.title
																											+ " "
																											+ component.attendanceModeText
																											+ "/"
																											+ component.attendancePatternText
																											+ " in "
																											+ component.when
																											+ ' <span class='+componentPlacesClass+'>'
																											+ Signup.signup
																													.formatPlaces(
																															component.places,
																															false)
																											+ '</span></span>';

																								}))
																		.join(
																				"<br>");

																var closes = 0;
																$
																		.each(
																				this.components,
																				function() {
																					if (closes != 0
																							&& this.closes > closes) {
																						return;
																					}
																					closes = this.closes;
																				});
																var actions = Signup.signup
																		.formatActions(Signup.signup
																				.getActions(
																						this.status,
																						this.id,
																						closes,
																						false));
																data
																		.push([
																				this.id,
																				(this.created) ? this.created
																						: "",
																				Signup.user
																						.render(
																								this.user,
																								this.group,
																								this.components),
																				course,
																				Signup.supervisor
																						.render(
																								this.supervisor,
																								this,
																								false),
																				Signup.signup
																						.formatNotes(this.notes),
																				this.status,
																				actions,
																				this.status ]);

															});
											fnCallback({
												"aaData" : data
											});
										}
									});
						},
						// This is useful as when loading the data async we might want to handle it later.
						"fnInitComplete" : function() {
							table.trigger("tableInit");
						}
					});

			$("span.previous-signup").die().live(
					"mouseover",
					function(e) {
						var span = $(this);
						var userId = $(this).attr("userid");
						var groupId = $(this).attr("groupid");
						var componentId = $(this).children("input.componentid")
								.map(function() {
									return this.value;
								}).get().join(',');

						$.ajax({
							url : "../rest/signup/previous/",
							type : "GET",
							data : {
								userid : userId,
								componentid : componentId,
								groupid : groupId
							},
							success : function(result) {
								var tip = "";
								var lines = 0;
								$.each(result, function() {
									var signupStatus = this.status;
									var signupGroup = this.group;
									$.each(this.components, function() {
										tip += signupGroup.title + " "
												+ this.title + " (" + this.id
												+ ") " + this.when + " "
												+ signupStatus + "<br />";
										lines++;
									});
								});
								var tooltip = $("span.previous-signup-tooltip",
										span);
								if (tip.length == 0) {
									tooltip.html("None");
								} else {
									tooltip.html(tip);
									tooltip.css("width",
											((tip.length / lines) - 6) * 0.5
													+ "em");
								}
							}
						});
					});

			$("a.supervisor", this).die().live("click", function(e) {
				signupAddSupervisor.attr("username", $(this).attr("user"));
				signupAddSupervisor.attr("signupid", $(this).attr("id"));
				signupAddSupervisor.jqmShow();
			});

			$("a.action", this).die().live("click", function(e) {
				var url = $(this).attr("href");
				$.ajax({
					"url" : url,
					"type" : "POST",
					"success" : function(data) {
						element.dataTable().fnReloadAjax(null, null, true);
						$(table).trigger("reload"); // Custom event type;
					}
				});
				return false;
			});

			$("a.more-details-link", this)
					.die()
					.live(
							"click",
							function(e) {
								e.preventDefault();
								try {
									var id = $(this).attr('href');
									var workingWindow = parent.window || window;
									var position = Signup.util.dialogPosition();
									var height = Math.round($(workingWindow)
											.height() * 0.9);
									var width = Math
											.round($(window).width() * 0.9);

									var courseDetails = $("<div></div>")
											.dialog(
													{
														autoOpen : false,
														stack : true,
														position : position,
														width : width,
														height : "auto",
														modal : true,
														open : function() {
															if ($(this)
																	.height() > $(
																	window)
																	.height()) {
																Signup.util
																		.resize(
																				window.name,
																				($(
																						this)
																						.height() + 40));
															}
														},
														close : function(event,
																ui) {
															courseDetails
																	.remove();
														}
													});

									Signup.course.show(courseDetails, id,
											"ALL", false, "../rest", function() {
												courseDetails.dialog("open");
											});

								} catch (e) {
									console.log(e);
								}
							});
		};

		var signups = $("#signups-table").myTable("../rest/signup/my", false);
		Signup.util.autoresize();

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
			<li><span>My Modules</span></li>
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
		</ul>
	</div>

	<div id="notes">
		<!-- Show the contents of my_modules.html -->
	</div>

	<div id="signups">
		<!-- Browse the areas which there are courses -->
		<table id="signups-table" class="display">
		</table>
	</div>

</body>
</html>
