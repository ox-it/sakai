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
pageContext.setAttribute("openCourse", (String)request.getAttribute("openCourse"));
%>
<c:set var="isExternalUser" value="${externalUser}" />
<c:set var="openAtCourse" value="${openCourse}" />
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<!-- Make the page render as IE8 for wrapping in jstree -->
	<meta http-equiv="X-UA-Compatible" content="IE=8" >
	
	<title>Browse by Department</title>

	<link href="<%= ServerConfigurationService.getString("skin.repo", "/library/skin") %>/tool_base.css" type="text/css" rel="stylesheet" media="all" />
	<link href="<%= ServerConfigurationService.getString("skin.repo", "/library/skin") %>/<%= ServerConfigurationService.getString("skin.default", "default") %>/tool.css" type="text/css" rel="stylesheet" media="all" />

	<link rel="stylesheet" type="text/css" href="/course-signup/static/lib/tool.css" />
	
	<script type="text/javascript" src="/course-signup/static/lib/jquery/jquery-1.4.2.min.js"></script>
	<script type="text/javascript" src="/course-signup/static/lib/jstree-1.0rc2/_lib/jquery.cookie.js"></script>

	<script type="text/javascript" src="/course-signup/static/lib/trimpath-template-1.0.38/trimpath-template.js"></script>
	<script type="text/javascript" src="/course-signup/static/lib/dataTables-1.7/js/jquery.dataTables.js"></script>
	<script type="text/javascript" src="/course-signup/static/lib/dataTables.reloadAjax.js"></script>

	<script type="text/javascript" src="/course-signup/static/lib/signup.js"></script>
	<script type="text/javascript" src="/course-signup/static/lib/Text.js"></script>
	<script type="text/javascript" src="/course-signup/static/lib/serverDate.js"></script>
	<script type="text/javascript" src="/course-signup/static/lib/QueryData.js"></script>
			
	<script type="text/javascript">
			jQuery(function() {

				var externalUser = <c:out value="${externalUser}" />;
				var openCourse = "<c:out value="${openCourse}" />";
	
				/**
				 * Returns a summary about signup for this group.
				 * @param The components to produce a summary for.
				 */
				//var signupSummary = Signup.signup.summary;		
					
				var myCourse = function(dest, id, old, externalUser){
					var courseData;
					var waitingList;
					var signupData;
					var template;
					var courseURL;
					
					/**
					 * Compare two users to see if they are equal.
					 * @param {Object} user1
					 * @param {Object} user2
					 */
					var compareUser = function(user1, user2) {
						return (user1.id == user2.id && user1.name == user2.name && user1.email == user2.email);
					};
					
					/**
					 * Check is an object exists as a value in an array.
					 * @param {Object} array The array to look in.
					 * @param {Object} object To object to look for.
					 * @param {Object} compare A function to compare objects which returns true when they are the same.
					 */
					var inArray = function(array, object, compare) {
						for (var i in array) {
							if (compare(object, array[i])) {
								return true;
							}
						}
						return false;
					};
					
					/**
					 * This is the entry point which gets called to fire off the AJAX requests.
					 * @param {Object} id
					 * @param {Object} old
					 */
					var loadCourse = function(){
						// Reset the data in-case someone clicked two items before we're loaded.
						courseData = undefined;
						signupData = undefined;
						waitingList = undefined;
						courseURL = undefined;
						
						$.ajax({
							url: "/course-signup/rest/course/" + id,
							data: {
								//range: (old) ? "PREVIOUS" : "UPCOMING"
								range: old
							},
							dataType: "json",
							cache: false,
							success: function(data){
								courseData = data;
								showCourse();
							}
						});
						
						if (!externalUser) {
							$.ajax({
								url: "/course-signup/rest/course/url/" + id,
								dataType: "json",
								cache: false,
								success: function(data){
									courseURL = data;
									showCourse();
								}
							});
							
							$.ajax({
								url: "/course-signup/rest/signup/count/course/signups/" + id,
								data: {
									status: "WAITING"
								},
								dataType: "json",
								cache: false,
								success: function(data){
									waitingList = data;
									showCourse();
								}
							});
						
							$.ajax({
								url: "/course-signup/rest/signup/my/course/" + id,
								dataType: "json",
								cache: false,
								success: function(data){
									signupData = data;
									showCourse();
								}
							});
						} else {
							signupData = [];
							waitingList = 0;
							courseURL = "";
							showCourse();
						}
						
						if (!template) { // When reloading we might already have the template loaded.
							$.ajax({
								url: "/course-signup/static/course.tpl",
								dataType: "text",
								cache: false,
								success: function(data){
									template = TrimPath.parseTemplate(data);
									showCourse();
								}
							});
						}
					};
					
					/**
					 * Shows the details of a course.
			 		 * It loads both the details of the course and the users signups.
			 		 */
					var showCourse = function(){
						// Check we have all our data.
						if (!courseData || !signupData || !template || (undefined == waitingList))	{
							return;
						}
						
						var data = courseData; // From refactoring...
						var now = $.serverDate();
						var id = data.id;
						data.full = false;
						data.open = false;
						data.hide = externalUser; // for externally visible courses
						data.presenters = [];
						data.description = Text.toHtml(data.description);
						data.waiting = waitingList;
						data.url = courseURL;
						
						var parts = [];
						for (var componentIdx in data.components) {
							var component = data.components[componentIdx];
							
							// Ignore components that are past
							if (component.closes < now) {
								continue;
							}
							// Sort components into sets.
							if (component.presenter && !inArray(data.presenters, component.presenter, compareUser)) {
								data.presenters.push(component.presenter);
							}
							// Check it we're signed up to this one
							$.each(signupData, function(){
								// For all the components check...
								var signup = this; // So we can get at it.
								$.each(this.components, function(){
									if (component.id == this.id) {
										component.signup = signup;
									}
								});
							});
							
							if (component.componentSet) {
								var found = false;
								$.each(parts, function() {
									var part = this;
									if (part.type.id == component.componentSet) {
										part.signup = (component.signup) ? component.signup : null;
										part.options.push(component);
										found = true;
									}
								});
								if (!found) {
									parts.push({
										"options": [component],
										"signup": (component.signup) ? component.signup : null,
										"subject": component.subject,
										"type": {
											"id": component.componentSet,
											"name": component.title
										}
									});
								}
							}
							
							// Work out if it's open.
							component.open = (now > component.opens && now < component.closes);
							if (!data.open && component.open) {
								data.open = true;
							}
							// Is there space.
							component.full = (component.places < 1);
							if (component.full) {
								data.full = true; // At least one component is full 
							}
						}
						
						data.signup = Signup.signup.summary(data.components)["message"];
						
						data.parts = parts;
						var output = template.process(data, {throwExceptions: true});
						dest.html(output);
						
						$("form", dest).submit(function(){
							try {
								var radioSelected = {};
								var errorFound = false;
								var selectedParts = [];
								var selectedPartIds = [];
								
								jQuery("input:checkbox", dest).each(function(){
									var name = this.name;
									if (this.checked && this.value != "none") {
										selectedParts[selectedParts.length] = jQuery(this).parents("tr:first").find(".option-details").html();
										selectedPartIds.push(this.value);
									}
								});
								
								jQuery("input:radio", dest).each(function(){
									var name = this.name;
									if (!radioSelected[name]) {
										radioSelected[name] = this.checked;
										// Save the selected parts so we can populate the popup.
										if (this.checked && this.value != "none") {
											selectedParts[selectedParts.length] = jQuery(this).parents("tr:first").find(".option-details").html();
											selectedPartIds.push(this.value);
										}
									}
								});
								if (selectedPartIds.length < 1) {
									errorFound = true;
									jQuery("#parts .error", dest).show().html("You need to select which components you wish to take.");
								}
								// TODO This needs processing.
								if (!errorFound) {
									jQuery(".error", dest).hide();
									var signup = Signup.course.signup({title: data.title, id: id, approval: data.supervisorApproval}, {titles: selectedParts, ids: selectedPartIds});
									signup.bind("ses.signup", function(){
										loadCourse(); // Reload the course.
										// Display a nice message. Should we keep the exising success()?
										success = function(){
											$(".messages", dest).append('<div class="message"><span class="good">Signup Submitted</span></div>');
											$(".messages .message:last", dest).slideDown(300).delay(2600).slideUp(300, function(){
												$(this).remove();
											});
										};
									});
								}
								return false;
							} 
							catch (e) {
								return false;
							}
						});
						success && success(courseData);
					};
				
					loadCourse();
				};	
				
                var openAtCourse = function(id){
					myCourse($("#coursedetails"), id, "ALL", externalUser);
				};	
				
				if (openCourse != null && openCourse != "") {
					openAtCourse(openCourse);
				}
			});
				
            jQuery(function(){
                
  				Signup.util.autoresize();
            });
            
		</script>
		
    </head>
    <body>
		<div id="messages">
        </div>
		
        <div id="coursedetails" style="padding:1em;">
            <!-- Show details of the course -->
		</div>
		<br clear="all">
    </body>
</html>
