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
	<script type="text/javascript" src="lib/jstree-1.0rc2/_lib/jquery.cookie.js"></script>
	<script type="text/javascript" src="lib/jstree-1.0rc2/jquery.jstree.js"></script>
	<script type="text/javascript" src="lib/jqmodal-r14/jqModal.js"></script>
	<script type="text/javascript" src="lib/trimpath-template-1.0.38/trimpath-template.js"></script>
	<script type="text/javascript" src="lib/dataTables-1.7/js/jquery.dataTables.js"></script>
	<script type="text/javascript" src="lib/dataTables-1.6/js/jquery.dataTables.reloadAjax.js"></script>
	<script type="text/javascript" src="lib/signup.js"></script>
	<script type="text/javascript" src="lib/Text.js"></script>
	<script type="text/javascript" src="lib/serverDate.js"></script>
			
	<script type="text/javascript">
			jQuery(function() {
				
				// The site to load the static files from.
				var signupSiteId = "/access/content/group/<%= ServerConfigurationService.getString("course-signup.site-id", "course-signup") %>";
				
				
				
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
				 * Used for sorting a jsTree. 
				 * @param {Object} x
				 * @param {Object} y
				 */
                var treeSort = function(x, y){
                    var a = x.data;
                    var b = y.data;
                    if (a === b) {
                        return 0;
                    }
                    else 
                        if (a < b) {
                            return -1;
                        }
                        else {
                            return 1;
                        }
                };

				/**
				 * Returns a summary about signup for this group.
				 * @param The components to produce a summary for.
				 */
				var signupSummary = function(components) {
					var now = $.serverDate();
					var nextOpen = Number.MAX_VALUE;
					var willClose = 0;
					var isOneOpen = false;
					var isOneBookable = false;
					var areSomePlaces = false;
					for(var i in components) {
						var component = components[i];
						var isOpen = component.opens < now && component.closes > now;
						if (component.opens > now && component.opens < nextOpen) {
							nextOpen = component.opens;
						}
						if (component.opens < now && component.closes > willClose) {
							willClose = component.closes;
						}
						if (isOpen) {
							isOneOpen = true;
							if (component.places > 0) {
								areSomePlaces = true;
							}
						}
						if (!isOneBookable) {
							isOneBookable = component.bookable;
						}
					}
					var message = "";
					if (!isOneBookable) {
						return null;
					}
					if (isOneOpen) {
						if (areSomePlaces) {
							var remaining = willClose - now;
							message = "close in "+ Signup.util.formatDuration(remaining);
						} else {
							message = "full";
						}
					} else {
						if (nextOpen === Number.MAX_VALUE) {
							return null;
						}
						var until = nextOpen - now;
						message = "open in "+ Signup.util.formatDuration(until);
					}
					return message;
				};
        				
				// Load the static data.
				jQuery.getJSON(signupSiteId+"/departments.json", function(treedata) {
					var courseData;
					var signupData;
					
					var loadCourse = function(id, old){
							// Reset the data in-case someone clicked two items before we're loaded.
							// TODO Need to find out if we're looking for an old one.
							courseData = undefined;
							signupData = undefined;
							jQuery.getJSON("../rest/course/" + id, {
								range: (old)?"PREVIOUS":"UPCOMING"
							}, function(data){
								courseData = data;
								showCourse();
							});
							jQuery.getJSON("../rest/signup/my/course/" + id, {}, function(data){
								signupData = data;
								showCourse();
							});
						};
					
					var showCourse = function(){
							// Check we have all our data.
							if (!courseData || !signupData) {
								return;
							}
							
							var data = courseData; // From refactoring...
							var now = $.serverDate();
							var id = data.id;
							data.full = true;
							data.open = false;
							data.presenters = [];
							data.administrators = [];
							data.description = Text.toHtml(data.description);
							var parts = [];
							for(var componentIdx in data.components) {
								var component = data.components[componentIdx];
								
								// Sort components into sets.
								if (component.presenter && !inArray(data.presenters, component.presenter, compareUser)) {
									data.presenters.push(component.presenter);
								}
								// Check it we're signed up to this one
								$.each(signupData, function() {
									// For all the components check...
									var signup = this; // So we can get at it.
									$.each(this.components, function() {
										if (component.id == this.id) {
											component.signup = signup;
										}
									});
								});
								
								if (component.componentSet) {
									var found = false;
									for (var part in parts) {
										if (parts[part].type.id == component.componentSet) {
											parts[part].signup = (component.signup)?component.signup:null;
											parts[part].options.push(component);
											found = true;
										}
									}
									if (!found) {
										parts.push({
											"options": [ component ],
											"signup": (component.signup)?component.signup:null,
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
								if (data.full && !component.full) {
									data.full = false; // At least one is open 
								}
							}
							
							data.signup = signupSummary(data.components);
							
							data.parts = parts;
							var output = TrimPath.processDOMTemplate("details-tpl", data, {throwExceptions: true});
                            jQuery("#details").html(output);
							// If there is only one checkbox tick it.
							var radioButtons = $("#signup input:radio:enabled");
							if (radioButtons.length == 1) {
								radioButtons.first().attr("checked", true);
							} else if (radioButtons.length == 0) {
								$("#signup :submit").attr("disabled", "true");
							}
                            jQuery("#signup").submit(function(){
								try {
									var radioSelected = {};
									var errorFound = false;
									var selectedParts = [];
									var selectedPartIds = [];
									jQuery("#signup input:radio").each(function(){
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
									for (radio in radioSelected) {
										if (!radioSelected[radio]) {
											errorFound = true;
											jQuery("#parts .error").show().html("You need to select which components you wish to take.");
										}
									}
									if (!errorFound) {
										jQuery("#parts .error").hide();
										var listNode = jQuery("#parts-confirm-details").empty().append(jQuery("#summary h3").clone()).append("<ul><li>" + selectedParts.join("</li><li>") + "</li></ul>");
										
										// Actually show the dialogue (resized first)
										var dialog = jQuery("#parts-confirm");
										dialog.height(dialog.innerHeight());
										dialog.data("components", selectedPartIds);
										var templateData = {
											"components": selectedParts,
											"componentIds": selectedPartIds,
											"course": data.title,
											"courseId": id
										};
										var confirmText = TrimPath.processDOMTemplate("parts-confirm-tpl", templateData, {throwExceptions: true});
										dialog.html(confirmText);
										confirmSetup(dialog);
										dialog.jqmShow();
									}
									return false;
								} catch (e) {
									return false;
								}
                            });
						};
				
				/**
				 * This handles the dialogue box for confirming a signup.
				 * @param {Object} dialog
				 */
				var confirmSetup = function(dialog){
					var signupConfirm = jQuery("#signup-confirm");
					var noteOriginal = $("textarea[name=message]").first().val();
					var supervisor = $.cookie('coursesignup.supervisor');
					if (supervisor) {
						$("input[name=email]").val(supervisor);
					}
					
					signupConfirm.find(".cancel").click(function(event){
						jQuery("#parts-confirm").jqmHide();
						event.stopPropagation();
						return false;
					});
					
					// Prevent double validation as we listen to two events on the same element.
					var isValidated = function(element){
						var value = element.val();
						if (value == element.data("validated")) {
							return true;
						}
						element.data("validated", value);
						return false;
					}
					
					// Change doesn't always fire, but now we have two event which both fire.
					$(".valid-email", signupConfirm).bind("change", function(e){
						var current = $(this);
						if (isValidated(current)) {
							return true;
						}
						var value = current.val();
						current.nextUntil(":not(.error)").remove(); // Remove any existing errors.
						if (value.length == 0) {
							current.after('<span class="error">* required</span>');
						}
						else {
							if (!/^([a-zA-Z0-9_.-])+@([a-zA-Z0-9_.-])+\.([a-zA-Z])+([a-zA-Z])+/.test(value)) {
								current.after('<span class="error">* not a valid email</span>');
							}
							else {
								// This has a potential problem in that it might not complete before user clicks submit.
								if (!current.data("req")) {
									current.data("req", $.ajax({ // Need to use error handler.
										url: "../rest/user/find",
										data: {
											search: value
										},
										success: function() {
											$.cookie('coursesignup.supervisor', value);
										},
										error: function(){
											current.after('<span class="error">* could not find a user with this email</span>');
										},
										complete: function(){
											delete current.data()["req"];
										}
									}));
								}
							}
						}
					});
					
					$("textarea[name=message]").bind("change", function(e){
						var current = $(this);
						if (isValidated(current)) {
							return true;
						}
						current.nextUntil(":not(.error)").remove(); // Remove any existing errors.
						if (noteOriginal == current.val()) {
							current.after('<span class="error">* please enter some reasons for your choice</span>');
						}
					});
					
					signupConfirm.submit(function(event){
						var form = jQuery(this);
						$(":text, textarea", form).trigger("change"); // Fire all the validation.
						// The AJAX validator probably won't have returned but it doesn't matter too much as the request will just fail.
						// TODO When we have better error handling we need to fix this.
						if ($(".error", form).length > 0) {
							return false;
						}
						
						var submit = form.find("input:submit:first").attr("disabled", "true").before('<img class="loader" src="images/loader.gif"/>');
						var courseId = jQuery("input[name=courseId]", this).first().val();
						$.ajax({
							type: "POST",
							url: "../rest/signup/my/new",
							data: form.serialize(),
							success: function(){
								dialog.jqmHide();
								loadCourse(courseId); // Not in scope.
							},
							complete: function(){
								submit.removeAttr("disabled").prev("img").remove();
							}
						});
						return false;
					});
				};
				
				var loadNodeDetails = function(id) {
					$.ajax( {
						"url": signupSiteId+"/html/"+id+".html",
						"cache": false,
						"success": function(data) {
							$("#details").html(data);
						}
					});
				};
					
					jQuery("#tree")
					.bind("select_node.jstree", function(event, data) {
						// Starts the loading of course details.
						var currentNode = data.rslt.obj[0];
						var id = currentNode.id;
						if ($(currentNode).is(".jstree-leaf")) {
							loadCourse(id, $(currentNode).hasClass("old"));
						} else {
							loadNodeDetails(id);
						}
					})
					.bind("open_node.jstree close_node.jstree", function(event, data) {

					})
					.delegate(".jstree-closed a", "click", function(e) {
						$("#tree").jstree("open_node", this);
					})
				 	.jstree({
						json_data: {
							data: treedata,
							ajax: {
								url: function (n) {
									var id = n.attr("id");
									id = id.replace(/-PREVIOUS$/, "");
									return "../rest/course/dept/"+ id;
								},
								data: function(n) {
									if (n.attr("id").match(/-PREVIOUS$/)) {
										return {components: "PREVIOUS"};
									}
									return {components: "UPCOMING"};
								},
								dataType: "json",
								success: function (data) {
                                    data.tree.sort(treeSort);
									if ("UPCOMING" == data.range) {
										data.tree.push({
											"attr":{"id": data.dept+"-PREVIOUS"},
											"data":"Old",
											"state": "closed"
										})
									} else {
										$.each(data.tree, function() {
											this.attr.class = "old";
										});
									}
                                    return data.tree;
								}
							}
							//correct_state: true
						},
						core: {
							initially_open: ["root", "4D"] // Open up MPLS for testing.
						},
						ui: {
							initially_select: ["4D"]
						},
						plugins: ["themes", "json_data", "ui"]
					});	
				});
			});
            jQuery(function(){
                jQuery("#parts-confirm").jqm({
                    modal: false
                });
                
  				Signup.util.autoresize();
            });
            
			
		</script>
		
    </head>
    <body>
    	<div id="toolbar" >
        	<ul class="navIntraTool actionToolBar">
            <li><span>Course Signup</span></li>
            <li><span><a href="my.jsp">My Courses</a></span></li>
            <li><span><a href="pending.jsp">Pending Acceptances</a></span></li>
            <li><span><a href="admin.jsp">Course Administration</a></span></li>
            <li><span><a href="debug.jsp">Debug</a></span></li>
			</ul>
        </div>
		
        <div id="browse">
            <!-- Browse the areas which there are courses -->
			<div id="tree"></div>
        </div>
        <div id="details">
            <!-- Show details of the course -->
		</div>
		<br clear="all"/>
				
		<!-- Hidden extra bits -->
		<div id="parts-confirm"  class="jqmWindow" style="display: none">
			
		</div>
		
		<textarea id="parts-confirm-tpl" style="display: none;" rows="0" cols="0">
			<h2>Signup to: \${course}</h2>
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
				<input type="hidden" name="components" value="\${componentId}"/>
				{/for}
				<input type="hidden" name="courseId" value="\${courseId}"/>
				<table>
					<tr>
						<th>
							<label for="supervisor-email">Supervisor Email</label>
						</th>
						<td><input type="text" class="valid-email" name="email" id="supervisor-email" size="40"/></td>
					</tr>
					<tr>
						<th>
							<label for="supervisor-note">Message to supervisor</label>
						</th>
						<td><\${textarea} name="message" id="supervisor-note" cols="40" rows="8">
Reason for requesting to attend this course: 

Other comments: </\${textarea}></td>
					</tr>
				</table>
				<input type="submit" value="Confirm Signup"/>
				<input type="submit" class="cancel" value="Cancel"/>
			</form>
			</div>
		</textarea>
		
		<!-- ** Template for displaying a course ** -->
		<textarea id="details-tpl" style="display:none" rows="0" cols="0">
            <!-- Show details of the course -->
            <div id="summary" class="">
                <h3>\${title}</h3>
                <table width="100%">
                	<tr>
                        <th>
                            Lecturers
                        </th>
                        <td>
                            {for presenter in presenters}
								<a href="mailto:\${presenter.email}">\${presenter.name}</a>
							{/for}
                        </td>
                    </tr>
                    <tr>
                        <th>
                            Course Administrator
                        </th>
                        <td>
							<a href="mailto:\${administrator.email}">\${administrator.name}</a>
                        </td>
                    </tr>
                    <tr>
                        <th>
                            Department
                        </th>
                        <td>
                        	{if defined('department')}
                            	\${department}
							{else}
								\${departmentCode}
							{/if}
                        </td>
                    </tr>
					{if signup}
                    <tr>
                        <th>
                            Signup Available
                        </th>
                        <td>
                            \${signup}
                        </td>
                    </tr>
					{/if}
                </table>
            </div>
            <div id="description">
            	<h4>Description</h4>
				\${description}
            </div>
			<div id="parts">
                <h4>Course Parts</h4>
				<span class="error" style="display:none"></span>
                <form id="signup" action="#">
                    <table width="100%">
                    	{for part in parts}
						<tr>
                            <th colspan="3">
                                \${part.type.name}
                            </th>
                        </tr>
						{var oneOpen = false}
						{for option in part.options}
                        <tr>
                            <td class="option-details">
                                <label for="option-\${option.id}">\${option.slot} for \${option.sessions} sessions starts in \${option.when},
								{if option.presenter}<a href="mailto:\${option.presenter.email}">\${option.presenter.name}</a>{/if}
								</label>
                                <br/>
                                <span class="location">\${option.location}</span>
                            </td>
                            <td>
                            	{if option.bookable}
                            		{if option.full}
										full
									{else}
										\${option.places} places
									{/if}
								{/if}
                            </td>
                            <td>
                            	
								{if option.signup && option.signup.status != "WITHDRAWN"}
									Signup: \${option.signup.status}
								{else}
								{if signup}
                                <input type="radio" name="\${part.type.id}" id="option-\${option.id}" value="\${option.id}"
								 {if option.full || !option.open }disabled="true"{else}{var oneOpen = true}{/if}/>
								 {/if}
								{/if}
                            </td>
                        </tr>
						{/for}
						{if parts.length > 1 && oneOpen}
						<tr>
							<td class="option-details">
								<label for="option-none-\${part.type.id}">Nothing for this option</label>
							</td>
							<td>N/A</td>
							<td><input type="radio" name="\${part.type.id}" id="option-none-\${part.type.id}" value="none"/></td>
						</tr>
						{/if}
						{/for}
                    </table>
					{if signup}
						<input type="submit" value="Signup" {if full || !open}disabled="true"{/if}/>
					{/if}
                </form>
            </div>
		</textarea>
		
    </body>
</html>