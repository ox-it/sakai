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
<title>Course Signup</title>

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
						return component.title + " " + component.teachingDetails + "<br>("
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
			var superuser = object.isSuperuser;

			var hideValue = "Hide Course";
			if (object.hideGroup) {
				hideValue = "Show Course";
			}
			// Table showing the components.
			var html = '<h3 style="display:inline">' + name + '</h3>';
			if (superuser) {
				html += '<span style="float:right; padding-right:20px;">';
				html += '<input class="show-hide-button" type="button" id="'+code+'" value="'+hideValue+'"/>';
				html += '</span>';
			}
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
								"aaSorting" : [ [ 5, 'desc' ] ], // Sort on the signup date.
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
											"sTitle" : "Teaching start date",
											"fnRender" : function(aObj) {
												if (aObj.aData[3] === 'undefined') {
													return '';
												} else {
													return new Date(+aObj.aData[3]).toDateString();
												}
											},
											"bUseRendered" : false
										},
										{
											"sTitle" : "Teaching Details"
										},
										{
											"sTitle" : "Status",
											"fnRender" : function(aObj) {
												return getComponentStatus(
														aObj.aData[9],
														aObj.aData[5]);
											}
										},
										{
											"sTitle" : "Export",
											"bSortable" : false,
											"fnRender" : function(aObj) {
												return '<a href="../rest/signup/component/'+ aObj.aData[6]+ '.csv">Export</a>';
											}
										},
										{
											"sTitle" : "Email",
											"bSortable" : false,
											"fnRender" : function(aObj) {
												return '<img class="mailto-all-course" id="'+aObj.aData[7]+'"src="images/email-send.png" title="send email to all CONFIRMED signups" />';
											}
										},
										{
											"sTitle" : "Attendance",
											"bSortable" : false,
											"fnRender" : function(aObj) {
												return '<a href="../rest/signup/component/'+ aObj.aData[8]+ '.pdf">Register</a>';
											}
										},
										{
											"bVisible" : false
										}],

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
													component.starts,
													component.teachingDetails, //4
													component.closes, 
													component.id, 
													component.id,
													component.id, //8
													component.opens]);
										}
										fnCallback(tableData);
									});
								}
							});
			
			var componentStarts = new Array();
			for ( var i in object.components) {
				var component = object.components[i];
				if (component.starts) {
					componentStarts.push(component.starts);
				}
			}

			// Sort the dates with the newest first as it is most usefull.
			componentStarts.sort().reverse();
			
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
			
			html += '<span style="float:right; padding-right:20px;">Teaching Start Filter <select class="signups-table-starts-filter" id="signups-table-starts-filter">';
			html += '<option value="">All</option>';
			html += '</select></span>';
			
			html += '<table border="0" class="display" id="signups-table"></table>';
			html += '<a href="#" id="signup-add">Add Oxford Signup</a>';
			html += '<span style="padding-right:20px;" />';
			html += '<a href="#" id="external-add">Add External Signup</a>';
			$("#signups").html(html);

			var selectElement = $('#signups-table-starts-filter').first();
			$.each(componentStarts, function(i, starts) {
				var date = new Date(starts).toDateString();
				selectElement.append($("<option/>", {
					value: starts,
					text: date
				}));
			});
			if (selectElement.options && selectElement.options.length > 0) {
				selectElement.options[0].setAttribute("selected", "selected");
			}
			
			// Load the signups.
			var signups = $("#signups-table").signupTable(
					"../rest/signup/course/" + code, true, true);
			signups.bind("reload", function() { // Reload the summary when this table changes.
				summary.fnReloadAjax(null, null, true);
			})

			var signupAddUser = $("#signup-add-user-win");
			signupAddUser.resize(function(e) {
				// Calculate size.
			});
			signupAddUser.jqm({
				onShow : function(objs) {
					$("body").css("overflow", "hidden"); // Doesn't seem to work on IE7
					objs.w.css("height", 330);
					objs.w.show();
					$("input[name=supervisor]", signupAddUser).val("");
					$("textarea", signupAddUser).val("");
					$(":submit", signupAddUser).removeAttr("disabled");
					$(".errors", signupAddUser).html("");
				},
				onHide : function(objs) {
					$("body").css("overflow", "auto");
					objs.w.fadeOut('250', function() {
						objs.o.remove();
					});
				}
			});
			signupAddUser.jqmAddClose("input.cancel");
			
			var signupAddExternal = $("#signup-add-external-win");
			signupAddExternal.resize(function(e) {
				// Calculate size.
			});
			signupAddExternal.jqm({
				onShow : function(objs) {
					$("body").css("overflow", "hidden"); // Doesn't seem to work on IE7
					objs.w.css("height", 220);
					objs.w.show();
					$("input[name=supervisor]", signupAddExternal).val("");
					$("textarea", signupAddExternal).val("");
					$(":submit", signupAddExternal).removeAttr("disabled");
					$(".errors", signupAddExternal).html("");
				},
				onHide : function(objs) {
					$("body").css("overflow", "auto");
					objs.w.fadeOut('250', function() {
						objs.o.remove();
					});
				}
			});
			signupAddExternal.jqmAddClose("input.cancel");
			
			$("#signup-add")
					.click(
							function() {
								// Reset the list of good users.
								goodUsers = [];
								signupAddUser.jqmShow();
								// Need to resize to content.
								var windowHeight = $(window).height();
								var positionTop = signupAddUser[0].offsetTop;
								if (windowHeight < signupAddUser.outerHeight()
										+ positionTop) {
									// Too big.
									var newHeight = windowHeight
											- (signupAddUser.outerHeight(false) - signupAddUser
													.height()) - 2;
									signupAddUser.height(newHeight);
									signupAddUser.css("top", "1px"); // Move almost to the top.
								}
								;
							});

			$(window)
					.resize(
							function() {
								var windowHeight = $(window).height();
								var positionTop = signupAddUser[0].offsetTop;
								if (windowHeight < signupAddUser.outerHeight()
										+ positionTop) {
									// Too big.
									var newHeight = windowHeight
											- (signupAddUser.outerHeight(false) - signupAddUser
													.height()) - 2;
									signupAddUser.height(newHeight);
									signupAddUser.css("top", "1px"); // Move almost to the top.
								}
								;
							});

			/**
			 * Callback once for once the users have been found and the components need to be selected.
			 * @param {Object} data Details of the course components form the REST call.
			 */
			var goodUsers = [];
			var goodSupervisor;
			
			var selectComponents = function(data) {
				var components = data.components;
				data.users = goodUsers;
				var output = TrimPath.processDOMTemplate(
						"signup-add-components-tpl", data);
				signupAddUser.jqmHide();
				signupAddExternal.jqmHide();
				var dialog = $("#signup-add-components-win");
				dialog.html(output);
				dialog.jqm();
				dialog.jqmAddClose("input.cancel");
				dialog.jqmShow();
				// Bind on the form submit.
				$("#signup-add-components")
						.bind(
								"submit",
								function(e) {
									var form = this;
									var progressElement = $("#create-signups-progress");
									var components = [];
									var originalGoodUserSize = goodUsers.length;

									progressElement
											.progressbar({
												value : 0
											});
									$("[type=checkbox]",
											this)
											.each(
													function() {
														if (this.checked) {
															components
																	.push(this.id
																			.substring(7)); // Remove 'option-' prefix
														}
													});
									if (components.length == 0) {
										$(".errors", form)
												.html(
														"You need to select some courses.");
										return false;
									}
									var postSignup = function() {
										progressElement
												.progressbar("destroy");
										dialog.jqmHide(); // Hide the popup.
										summary
												.fnReloadAjax(
														null,
														null,
														true);
										signups
												.fnReloadAjax(
														null,
														null,
														true);
									};

									var doSignup = function() {
										progressElement
												.progressbar(
														"value",
														(originalGoodUserSize - goodUsers.length)
																/ originalGoodUserSize
																* 100);
										var supervisorId;
										if (goodSupervisor) {
											supervisorId = goodSupervisor.id;
										}
										var user = goodUsers
												.pop();
										if (user === undefined) {
											postSignup();
										} else {
											$
													.ajax({
														"url" : "../rest/signup/new",
														"type" : "POST",
														"async" : true,
														"traditional" : true,
														"data" : {
															"userId" : user.id,
															"userName" : user.name,
															"userEmail" : user.email,
															"supervisorId" : supervisorId,
															"courseId" : code,
															"components" : components
														},
														"complete" : doSignup
													});
										}
									};
									// Disable the signup button.
									$(":submit", form)
											.attr(
													"disabled",
													"true");
									doSignup();
									return false;
								});
			};
			
			signupAddUser
					.unbind("submit")
					.bind(
							"submit",
							function(e) {
								var form = this;
								var progressbar = $("#find-users-progress");
								var supervisor = $("input[name=supervisor]",
										signupAddUser).val();
								
								var badSupervisor;
								var users = $("textarea[name=users]",
										signupAddUser).val().replace(/,/g, " "); // Incase people use commas to seperate users.
								// Need error handling when empty.
								
								var badUsers = [];
								var userIds = users.split(/\s+/);
								var originalUserIdSize = userIds.length; // Used in progressbar calc.
								var continueSearch = true;

								/**
								 * Function for once all the users have been found.
								 */
								var foundUsers = function() {
									$(":submit", form).removeAttr("disabled");
									progressbar.progressbar("destroy");
									var html = "";
									if (badSupervisor) {
										html += "Couldn't find supervisor "
												+ badSupervisor;
									}
									if (badUsers.length > 0) {
										// Display list of bad user.
										html += (html.length > 0 ? ", " : "")
												+ "Couldn't find user"
												+ (badUsers.length > 1 ? "s"
														: "") + ": "
												+ badUsers.join(", ");
									} else {
										if (goodUsers.length < 1) {
											html += (html.length > 0 ? ", "
													: "")
													+ "No users found.";
										}
									}
									if (html.length > 0) {
										$(".errors", form).html(html);
									} else {
										// Make sure they are in order.
										goodUsers = goodUsers
												.sort(Signup.user.sort);
										// Show next page...
										$.getJSON("../rest/course/" + code, {
											"range" : "ALL"
										}, selectComponents);
									}
								}

								/**
								 * Looks for a user. When all users have been processed calls foundUsers.
								 */
								var findUser = function() {
									if (!continueSearch) {
										progressbar.progressbar("destroy");
										return;
									}
									progressbar
											.progressbar(
													"value",
													(originalUserIdSize - userIds.length)
															/ originalUserIdSize
															* 100);
									var that = userIds.pop();
									if (that === undefined) {
										foundUsers();
										return;
									}
									if (!that || that.length < 1) {
										findUser();
										return;
									}
									$.ajax({
										"url" : "../rest/user/find",
										"method" : "GET",
										"async" : true,
										"data" : {
											"search" : that.toString()
										},
										"success" : function(data) {
											goodUsers.push(data);
										},
										"error" : function() {
											badUsers.push(that.toString());
										},
										"complete" : function() {
											findUser();
										}
									});
								};

								var findSupervisor = function() {
									$.ajax({
										"url" : "../rest/user/find",
										"method" : "GET",
										"async" : true,
										"data" : {
											"search" : supervisor
										},
										"success" : function(data) {
											goodSupervisor = data;
										},
										"error" : function() {
											badSupervisor = supervisor;
										},
										"complete" : function() {
											findUser(); // Startoff the searching of users.
										}
									});
								};

								$(":submit", form).attr("disabled", "true");
								$("input.cancel", form).one("click",
										function() {
											continueSearch = false;
										});
								progressbar.progressbar({
									value : 0
								});
								findSupervisor(); // Startoff validating the form
								//findUser(); // Startoff the searching of users.
								return false;
							});
			
			$("#external-add")
			.click(
					function() {
					    var studentName = $("input[name=studentName]",
							signupAddExternal).val("");
						var studentEmail = $("input[name=studentEmail]",
							signupAddExternal).val("");
						// Reset the list of good users.
						goodUsers = [];
						signupAddExternal.jqmShow();
						// Need to resize to content.
						var windowHeight = $(window).height();
						var positionTop = signupAddExternal[0].offsetTop;
						if (windowHeight < signupAddExternal.outerHeight()
								+ positionTop) {
							// Too big.
							var newHeight = windowHeight
									- (signupAddExternal.outerHeight(false) - signupAddExternal
											.height()) - 2;
							signupAddExternal.height(newHeight);
							signupAddExternal.css("top", "1px"); // Move almost to the top.
						}
						;
					});
			
			signupAddExternal
			.unbind("submit")
			.bind(
					"submit",
					function(e) {
						var form = this;
						var progressbar = $("#find-users-progress");
						var studentName = $("input[name=studentName]",
								signupAddExternal).val();
						var studentEmail = $("input[name=studentEmail]",
								signupAddExternal).val();
						if (!Text.isEmail(studentEmail)) {
						    $(".errors", form).html("Not a valid email address");
						    return false;
						}
						var user = new Object();
						user.id="newUser";
						user.name=studentName;
						user.email=studentEmail;
						goodUsers.push(user);
						
						$(":submit", form).attr("disabled", "true");
						$("input.cancel", form).one("click",
								function() {
									continueSearch = false;
								});
						progressbar.progressbar({
							value : 0
						});
						$.getJSON("../rest/course/" + code, {
							"range" : "ALL"
						}, selectComponents);
						return false;
					});

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
			"url" : "../rest/course/admin",
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
							'<form>Select a course: <select id="admin-course" name="course">'
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
							'You are not an administrator on any courses.');
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

		$("input.show-hide-button", this).die().live(
				"click",
				function(e) {
					var button = $(this);
					var code = button.attr("id");
					var hideValue = button.attr("value");
					var hide = false;
					if (hideValue === "Hide Course") {
						hide = true;
					}
					$.ajax({
						"url" : "../rest/course/hide",
						"type" : "POST",
						"async" : true,
						"traditional" : true,
						"data" : {
							"courseId" : code,
							"hideCourse" : hide
						},
						"success" : function(result) {
							if (hide) {
								button.attr("value", "Show Course");
							} else {
								button.attr("value", "Hide Course");
							}

							for ( var i in courseData) {
								var course = courseData[i];
								var courseId = course.id;
								if (courseId === code) {
									course.hideGroup = hide;
									var title = course.title;
									if (course.isSuperuser
											&& course.hideGroup) {
										title = "(hidden) " + title;
									}
									$("#admin-course option[value="
											+ code + "]").html(title);
									break;
								}
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
			<li><span><a href="search.jsp">Search Courses</a></span></li>
			<li><span><a href="index.jsp">Browse by Department</a></span></li>
			<li><span><a href="calendar.jsp">Browse by Calendar</a></span></li>
			<li><span><a href="vitae.jsp">Researcher Development</a></span></li>
			<li><span><a href="my.jsp">My Courses</a></span></li>
			<c:if test="${isPending}">
				<li><span><a href="pending.jsp">Pending Acceptances</a></span></li>
			</c:if>
			<c:if test="${isApprover}">
				<li><span><a href="approve.jsp">Pending
							Confirmations</a></span></li>
			</c:if>
			<li><span>Course Administration</span></li>
			<c:if test="${isLecturer}">
				<li><span><a href="lecturer.jsp">Lecturer View</a></span></li>
			</c:if>
		</ul>
	</div>

	<div id="course-list">
		<!-- Browse the areas which there are courses -->
	</div>
	<div id="course-details" style="margin-top: 14px;"></div>
	<!-- Show details of the course -->
	<div id="signups"></div>


	<!-- Hidden extra bits -->
	<!-- Popup window for finding users.-->
	<div id="signup-add-user-win" class="jqmWindow" style="display: none">
		<h2>Find users</h2>
		<form id="signup-add-user">
			<p>
				Enter the Supervisor email.<br /> <input type="text"
					name="supervisor" id="add-supervisor" size="28" />
			</p>
			<p>
				Enter one or more Oxford username or email addresses.<br />
				<textarea name="users" id="add-users" cols="30" rows="8"></textarea>
			</p>
			<span class="errors"></span> <br> <input type="submit"
				value="Find"> <input type="button" class="cancel"
				value="Cancel"><br>
			<div id="find-users-progress"></div>
		</form>
	</div>
	
	<!-- Popup window for adding external users.-->
	<div id="signup-add-external-win" class="jqmWindow" style="display: none">
		<h2>Add New External Student</h2>
		<form id="signup-add-external">
			<span class="errors"></span> <br>
			<p>
				Enter the student name.<br /> 
				<input type="text" name="studentName" id="student-name" size="60" />
			</p>
			<p>
				Enter the student email addresses.<br />
				<input type="text" name="studentEmail" id="student-email" size="60" />
			</p>
			<input type="submit" value="Select Components">
			<input type="button" class="cancel" value="Cancel"><br>
			<div id="find-users-progress"></div>
		</form>
	</div>

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
		<h2>Select Courses</h2>
	
		<form id="signup-add-components">
			<span class="errors"></span>
			<ul>
			{for component in components}
				<li>
					<input type="checkbox" name="\${component.id}"
						id="option-\${component.id}" value="true">
					<label for="component-\${component.id}">\${component.title} - \${component.teachingDetails} for \${component.sessions} sessions in \${component.when},
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
