<%@ page import="org.sakaiproject.component.cover.ServerConfigurationService" %>
<%@ page session="false" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<title>Course Signup</title>

	<link href="<%= ServerConfigurationService.getString("skin.repo", "/library/skin") %>/tool_base.css" type="text/css" rel="stylesheet" media="all" />
	<link href="<%= ServerConfigurationService.getString("skin.repo", "/library/skin") %>/<%= ServerConfigurationService.getString("skin.default", "default") %>/tool.css" type="text/css" rel="stylesheet" media="all" />

	<link rel="stylesheet" type="text/css" href="lib/jqmodal-r14/jqModal.css" />
	<link rel="stylesheet" type="text/css" href="lib/dataTables-1.7/css/demo_table_jui.css"/>
	<link rel="stylesheet" type="text/css" href="lib/jquery-ui-1.8.2.custom/css/smoothness/jquery-ui-1.8.2.custom.css"/>
	<link rel="stylesheet" type="text/css" href="lib/tool.css" />
	
	<script type="text/javascript" src="lib/jquery/jquery-1.4.2.min.js"></script>
	<script type="text/javascript" src="lib/jstree-1.0rc/_lib/jquery.cookie.js"></script>
	<script type="text/javascript" src="lib/jstree-1.0rc/jquery.jstree.js"></script>
	<script type="text/javascript" src="lib/jqmodal-r14/jqModal.js"></script>
	<script type="text/javascript" src="lib/trimpath-template-1.0.38/trimpath-template.js"></script>
	<script type="text/javascript" src="lib/dataTables-1.7/js/jquery.dataTables.js"></script>
	<script type="text/javascript" src="lib/dataTables-1.6/js/jquery.dataTables.reloadAjax.js"></script>
	<script type="text/javascript" src="lib/signup.js"></script>
	<script type="text/javascript" src="lib/Text.js"></script>
	<script type="text/javascript" src="lib/serverDate.js"></script>
		
<script type="text/javascript">
			$(function(){

				var processSignupRow = function(item) {
					var components = $.map(item.components, function(component) {
						return component.title+ " "+ component.slot+ "<br>("+ component.places+ " places)";
					}).join("<br>");
					return [
							item.id,
							item.created,
							Signup.user.render(item.user),
							Signup.user.render(item.supervisor),
							components,
							item.status,
							Signup.signup.formatActions(Signup.signup.getActions(item.status, item.id))
					];
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
				
				var loadCourse = function(code, name) {

							// Table showing the components.
							$("#course-details").html('<h3>'+ name +'</h3><table border="0" class="display" id="course-details-table"></table>');
							var summary = $("#course-details-table").dataTable( {
								"bJQueryUI": true,
								"bProcessing": true,
								"bPaginate": true,
								"bLengthChange": false,
								"iDisplayLength": 5,
								"bFilter": false,
								"bInfo": false,
								"bAutoWidth": false,
								"sAjaxSource": "/course-signup/rest/course/",
								"aaSorting": [[3,'desc']], // Sort on the signup date.
								"aoColumns": [
								              {"sTitle": "Component"},
											  {"sTitle": "Size"},
								              {"sTitle": "Places"},
											  {
											  	"sTitle": "Signup Period",
												"fnRender": function(aObj) {
													// Hack to force the data back to being a int.
													return new Date(+aObj.aData[3]).toDateString() + " (for "+ Signup.util.formatDuration(aObj.aData[8] - aObj.aData[3])+ ")";
												},
												"bUseRendered": false
											  },
											  {"sTitle": "Term"},
								              {
											  	"sTitle": "Status",
												"fnRender": function(aObj) {
													return getComponentStatus(aObj.aData[3], aObj.aData[8]);
												}
											  },
											  {"bVisible": false},
											  {"bVisible": false},
											  {"bVisible": false}
								              ],
					
								"fnServerData": function(sSource, aoData, fnCallback) {
									$.getJSON(sSource+code, {"range": "ALL"}, function(data) {
										var tableData = {
												"aaData": []
										};
										for(var i in data.components) {
											var component = data.components[i];
											tableData.aaData.push([
											 component.title,
											 component.size,
											 component.places,
											 component.opens,
											 component.when, //4
											 component.sessions,
											 component.presenter,
											 component.administrator,
											 component.closes, //8
											]);
										}
										fnCallback(tableData);
									});
								}
							});
							
							$("#signups").html('<h3>Signups</h3><table border="0" class="display" id="signups-table"></table><a href="#" id="signup-add">Add Signup</a>');
							// Load the signups.
							var signups = $("#signups-table").signupTable("/course-signup/rest/signup/course/"+code, true);
							signups.bind("reload", function() {
								summary.fnReloadAjax();
							})
							
							var signupAddUser = $("#signup-add-user-win");
							signupAddUser.resize(function(e){
								// Calculate size.
							});
							signupAddUser.jqm({
								onShow: function(objs) {
									$("body").css("overflow", "hidden");
									objs.w.css('opacity',1).show();
								},
								onHide: function(objs) {
									$("body").css("overflow", "auto");
									objs.w.fadeOut('250',function(){ objs.o.remove(); });
								}
							});
							signupAddUser.jqmAddClose("input.cancel");
							$("#signup-add").click(function(){
								signupAddUser.jqmShow();
								// Need to resize to content.
								var windowHeight = $(window).height();
								var positionTop = signupAddUser[0].offsetTop;
								if (windowHeight < signupAddUser.outerHeight() + positionTop) {
									// Too big.
									var newHeight = windowHeight - (signupAddUser.outerHeight(false) - signupAddUser.height()) -2;
									signupAddUser.height(newHeight);
									signupAddUser.css("top", "1px"); // Move almost to the top.
								};
								
							});
							$(window).resize(function(){
								var windowHeight = $(window).height();
								var positionTop = signupAddUser[0].offsetTop;
								if (windowHeight < signupAddUser.outerHeight() + positionTop) {
									// Too big.
									var newHeight = windowHeight - (signupAddUser.outerHeight(false) - signupAddUser.height()) -2;
									signupAddUser.height(newHeight);
									signupAddUser.css("top", "1px"); // Move almost to the top.
								};
							});
							
							signupAddUser.bind("submit", function(e) {
								var users = e.target.users.value;
								// Need error handling when empty.
								var goodUsers = [];
								var badUsers = [];
								$.each(users.split(/\s+/), function() {
									var that = this;
									if (!this || this.length < 1) {
										return;
									}
									$.ajax({
										"url": "/course-signup/rest/user/find",
										"method": "GET",
										"async": false, // So we don't overload the server.
										"data": {"search": this.toString()},
										"success": function(data) {
											goodUsers.push(data);
										},
										"error": function() {
											badUsers.push(that.toString());
										}
									})
								});
								if (badUsers.length > 0) {
									// Display list of bad user.
									$(".errors",this).html("Couldn't find user"+ (badUsers.length > 1?"s":"")+ ": "+ badUsers.join(", "));
								} else {
									// Show next page...
									$.getJSON("/course-signup/rest/course/"+code, {"range": "ALL"}, function(data){
										var components = data.components;
										var output = TrimPath.processDOMTemplate("signup-add-components-tpl", data);
										signupAddUser.jqmHide();
										var dialog = $("#signup-add-components-win");
										dialog.html(output);
										dialog.jqm();
										dialog.jqmAddClose("input.cancel");
										dialog.jqmShow();
										// Bind on the form submit.
										$("#signup-add-components").bind("submit", function(e){
											//
											var components = [];
											$("[type=checkbox]", this).each(function() {
												if(this.checked) {
													components.push(this.id.substring(7)); // Remove 'option-' prefix
												}
											});
											if (components.length == 0) {
												//error.
											}
											// Go sign them up.
											$.each(goodUsers, function() {
												var user = this;
												$.ajax({
													"url": "/course-signup/rest/signup/new",
													"type": "POST",
													"async": false,
													"traditional": true,
													"data": {"userId": user.id, "courseId": code, "components": components},
													"success": function(data) {
														//console.log("Good for "+ user.id);
													}
												});
											});
											dialog.jqmHide(); // Hide the popup.
											summary.fnReloadAjax(); 
											signups.rnReloadAjax();
											return false;
											
										});
									});
								}
								
								return false;
							});
							
							
							 
							 return;
							// Handlers to decide what actions you can do when selecting multiple items.
							$("#signups-table input[type=checkbox]").live("change", function(e) {
								
								var matchingStatus;
								var allMatching = true;
								
								$("#signups-table input[type=checkbox]").each(function() {
									if (this.checked) {
										var row = this.value;
										var status = $("#signups-table").dataTable().fnGetData(row)[4];
										if(matchingStatus) {
											if(allMatching) {
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
				$.getJSON("/course-signup/rest/course/admin", function(data) {
					var options = "";
					data.sort(function(x,y){
						var a = x.title;
						var b = y.title;
						if (a === b) {
							return 0;
						} else if ( a < b) {
							return -1;
						} else {
							return 1;
						}
					});
					$.each(data, function(index, value) {
						options += '<option value="'+ value.id+ '">'+ value.title+ '</option>';
					});
									
					if (data.length > 0) {
						$("#course-list").html('<form>Select a course: <select id="admin-course" name="course">'+ options+ '</select></form>');
						loadCourse(data[0].id, data[0].title);
					} else {
						$("#course-list").html('You are not an administrator on any courses.');
					}
				});
				$("#admin-course").live("change", function(e) {
					var courseId = this.options[this.selectedIndex].value;
					var courseTitle = this.options[this.selectedIndex].text;
					loadCourse(courseId, courseTitle);
				});
				
				Signup.util.autoresize();

			});
			
		</script>

</head>
<body>
<div id="toolbar">
<ul class="navIntraTool actionToolBar">
	<li><span><a href="index.jsp">Course Signup</a></span></li>
	<li><span><a href="my.jsp">My Courses</a></span></li>
	<li><span><a href="pending.jsp">Pending Acceptances</a></span></li>
	<li><span>Course Administration</span></li>
	<li><span><a href="debug.jsp">Debug</a></span></li>
</ul>
</div>

<div id="course-list"><!-- Browse the areas which there are courses -->
</div>
<div id="course-details"></div><!-- Show details of the course -->
<div id="signups"></div>
</div>



<!-- Hidden extra bits -->
<!-- Popup window for finding users.-->
<div id="signup-add-user-win" class="jqmWindow" style="display: none">
	<h2>Find users</h2>
	<div>Enter one or more username or email addresses.</div>
	<form id="signup-add-user">
		<textarea name="users" id="add-users" cols="30" rows="8"></textarea>
		<span class="errors"></span>
		<br>
		<input type="submit" value="Find">
		<input type="button" class="cancel" value="Cancel">
	</form>
</div>

<!-- Popup window for selecting components. -->
<div id="signup-add-components-win" class="jqmWindow" style="display:none">

</div>

<textarea id="signup-add-components-tpl" style="display:none" rows="0" cols="0">
	<h2>Select components</h2>
	<form id="signup-add-components">
	<ul>
	{for component in components}
		<li>
			<input type="checkbox" name="\${component.id}" id="option-\${component.id}" value="true">
			<label for="component-\${component.id}">\${component.title} - \${component.slot} for \${component.sessions} sessions in \${component.when},
					{if component.presenter}<a href="mailto:\${component.presenter.email}">\${component.presenter.name}</a>{/if}
					</label>
                                <br />
                                <span class="location">\${component.location}</span>
		</li>
	{/for}
	</ul>
		<input type="submit" value="Add">
		<input type="button" class="cancel" value="Cancel">
	</form>
</textarea>

<textarea id="parts-confirm-tpl" style="display: none;" rows="0"
	cols="0">
			{var textarea = "textarea"}
			<div>
				<ul>
				{for component in components}
					<li>\${component}</li>
				{/for}
				</ul>
			</div>
			<div>
			<form id="signup-confirm" action="#">
				{for componentId in componentIds}
				<input type="hidden" name="components" value="\${componentId}" />
				{/for}
				<table>
					<tr>
						<th>
							<label for="supervisor-email">Supervisor Email</label>
						</th>
						<td><input type="text" class="valid-email" name="email"
			id="supervisor-email" size="40" /></td>
					</tr>
					<tr>
						<th>
							<label for="supervisor-note">Message to supervisor</label>
						</th>
						<td><\${textarea} name="message" id="supervisor-note" cols="40" rows="8"></\${textarea}></td>
					</tr>
				</table>
				<input type="submit" value="Confirm Signup" />
				<input type="submit" class="cancel" value="Cancel" />
			</form>
			</div>
		</textarea>


</body>
</html>