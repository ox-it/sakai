/*
 * #%L
 * Course Signup Webapp
 * %%
 * Copyright (C) 2010 - 2013 University of Oxford
 * %%
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *             http://opensource.org/licenses/ecl2
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
var placesErrorLimit = 0;
var placesWarnPercent = 10;

var Signup = function(){
	return {
		/**
		 * For dealing with courses.
		 */
		"course": {
			/**
			 * This shows details of the course.
			 * @param {Object} dest A jQuery object of the element to put the content in.
			 * @param {Object} id The ID of the course to load.
			 * @param {Object} old If we should be showing upcoming or old data.
			 */
			show: function(dest, id, old, externalUser, prefix, success){
				var courseData;
				var waitingList;
				var signupData;
				var template;

				// Refactored out.
				var compareUser = Signup.util.compareUser;
				var inArray = Signup.util.inArray;

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

					$.ajax({
						url: prefix+"/course/" + id,
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
							url: prefix+"/signup/count/course/signups/" + id,
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
							url: prefix+"/signup/my/course/" + id,
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
					data.waiting = waitingList;
					data.url = window.location.protocol+"//"+window.location.host+"/course-signup/rest/course/"+id;
					data.returnurl = "/course-signup/rest/course/"+id;
					// Are there any components flagged as bookable?
					data.bookable = false;

					var parts = [];
					var applyTo;
					var memberApplyTo;

					for (var componentIdx in data.components) {
						var component = data.components[componentIdx];
						memberApplyTo = component.memberApplyTo;
						applyTo = component.applyTo;

						// Sort components into sets.
						if (component.presenter && !inArray(data.presenters, component.presenter, compareUser)) {
							data.presenters.push(component.presenter);
						}
						// Check it we're signed up to this one
						$.each(signupData, function(){
							// For all the components check...
							var signup = this; // So we can get at it.
							$.each(this.components, function(){
								if (component.id == this.presentationId) {
									component.signup = signup;
								}
							});
						});

						var sessionData = [];
						$.each(component.sessions, function() {
							var session = this;
							sessionData.push({
								"start": session.sessionStart,
								"end": session.sessionEnd,
								"singleDay": new Date(session.sessionStart).toDateString() === new Date(session.sessionEnd).toDateString(),
								"location": session.location
							});
						});

						component.sessionData = sessionData;

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

						if (!data.bookable && component.bookable) {
						    data.bookable = true; // We've found one bookable component.
						}
					}

					data.signup = Signup.signup.summary(data.components)["message"];
					data.parts = parts;
					data.applyTo = applyTo;

					data._MODIFIERS = Signup.util.trimpathModifiers();

					var output = template.process(data, {throwExceptions: true});
					dest.html(output);

					$("form", dest).submit(function(){
						try {
							var radioSelected = {};
							var errorFound = false;
							var anySelectable = false;
							var selectedParts = [];
							var selectedPartIds = [];

							jQuery("input:checkbox", dest).each(function(){
								var name = this.name;
								anySelectable = true;
								if (this.checked && this.value != "none") {
									selectedParts[selectedParts.length] = jQuery(this).parents("tr:first").find(".option-details").html();
									selectedPartIds.push(this.value);
								}
							});

							jQuery("input:radio", dest).each(function(){
								var name = this.name;
								anySelectable = true;
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
								if (anySelectable) {
									jQuery("#parts .error", dest).show().html("You need to select which components you wish to take.");
								} else {
									jQuery("#parts .error", dest).show().html("There are no components available for you to signup.");
								}
							}
							// TODO This needs processing.
							if (!errorFound) {
								jQuery(".error", dest).hide();
								var signup = Signup.course.signup({title: data.title, id: id, approval: data.supervisorApproval}, {titles: selectedParts, ids: selectedPartIds}, prefix);
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
			},
			/**
			 * Handle the displaying of a confirmation page for a signup.
			 */
			signup: function(course, components, prefix){
				// Return this and then trigger all events against it.
				var signupDialog = $("<div></div>");
								/**
				 * This handles the dialogue box for confirming a signup.
				 * @param {Object} dialog
				 */
				var confirmSetup = function(dialog){
					var signupConfirm = $("form", dialog);
					var noteOriginal = $("textarea[name=message]", dialog).first().val();
					var supervisor = $.cookie('coursesignup.supervisor');
					if (supervisor) {
						$("input[name=email]", dialog).val(supervisor);
					}

					signupConfirm.find(".cancel").click(function(event){
						dialog.dialog("close");
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

						if (course.approval) {
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
											url: prefix+"/user/find",
											data: {
												search: value
											},
											success: function(){
												$.cookie('coursesignup.supervisor', value);
											},
											error: function(){
												current.after('<span class="error">* no user exists in WebLearn with this email</span>');
											},
											complete: function(){
												delete current.data()["req"];
											}
										}));
									}
								}
							}
						}
					});

					$("textarea[name=message]", signupConfirm).bind("change", function(e){
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
							url: prefix+"/signup/my/new",
							data: form.serialize(),
							success: function(){
								signupDialog.trigger("ses.signup");
								dialog.dialog("close"); // Will remove it as well.
							},
							complete: function(){
								submit.removeAttr("disabled").prev("img").remove();
							}
						});
						return false;
					});
				};
				// Load the template and then display the dialog.
				$.ajax({
					url: "/course-signup/static/signup.tpl",
					dataType: "text",
					success: function(data){
						var position = Signup.util.dialogPosition();
						signupDialog.dialog({
							autoOpen: false,
							modal: true,
							stack: true,
							position: position,
							width: 600, // Would be nice to get inner content width.
							close: function(event, ui){
								signupDialog.remove(); // Tidy up the DOM.
							}
						});
						var templateData = {
							"components": components.titles,
							"componentIds": components.ids,
							"course": course.title,
							"courseId": course.id,
							"courseApproval": course.approval
						};
						var template = TrimPath.parseTemplate(data, {
							throwExceptions: true
						});
						var output = template.process(templateData);
						signupDialog.html(output);
						confirmSetup(signupDialog);
						signupDialog.dialog("open");

					}

				});


				return signupDialog;
			}
		},
		"util": {
			/**
			 * This attempts to parse a string of the form "2013-03-01T00:00:00Z".
			 * In some browser (Chrome, Firefox) new Date("2013-03-01T00:00:00Z") works, but in IE8
			 * it doesn't so we have to do this ourselves.
			 * @return the parsed date or null if the date couldn't be parsed.
			 */
			 parseDate: function(dateString) {
			  if(dateString) {
				var d = dateString.match(/(\d+)-(\d+)-(\d+)T(\d+):(\d+):(\d+)Z/);
				if (d) {
				  // It's a UTC time so create it as such.
				  // Month is zero based.
				  return new Date(Date.UTC(d[1], d[2]-1, d[3], d[4], d[5], d[6]));
				}
			  }
			  return new Date(NaN);
			},

			/**
			 * Gets the Trimpath Modifiers.
			 * @param {boolean} isAdmin Is the current user an administrator.
			 * @return an trimpath modifier object.
			 */
			"trimpathModifiers": function(isAdmin) {
				return {
					"dateFormat" : function(date) {
					return new Date(date).toDateString();
					},
					"timeFormat" : function(date) {
						return new Date(date).getHours()+":"+('0'+new Date(date).getMinutes()).slice(-2);
					},
					"placesFormat" : function(places) {
						return Signup.signup.formatPlaces(places, isAdmin);
					}
				};
			},

			/**
			 * Compare two users to see if they are equal.
			 * @param {Object} user1
			* @param {Object} user2
			 */
			compareUser: function(user1, user2) {
			  return (user1.id == user2.id && user1.name == user2.name && user1.email == user2.email);
			},

			/**
			 * Check is an object exists as a value in an array.
			 * @param {Object} array The array to look in.
			 * @param {Object} object To object to look for.
			 * @param {Object} compare A function to compare objects which returns true when they are the same.
			 */
			inArray: function(array, object, compare) {
			  for (var i in array) {
				if (compare(object, array[i])) {
				  return true;
				}
			  }
			  return false;
			},

			/**
			 * Formats a durations to display to the user.
			 * @param {Object} duration Duration in milliseconds.
			 */
			"formatDuration": function(remaining){
				if (remaining < 1000) {
					return "< 1 second";
				}
				else
					if (remaining < 60000) {
						return Math.floor(remaining / 1000) + " seconds";
					}
					else
						if (remaining < 3600000) {
							return Math.floor(remaining / 60000) + " minutes";
						}
						else
							if (remaining < 86400000) {
								return Math.floor(remaining / 3600000) + " hours";
							}
							else {
								return Math.floor(remaining / 86400000) + " days";
							}
			},
			"autoresize": function(name){
				var previousInnerHTML = document.body.innerHTML;
				return function(change){
					if (document.body.innerHTML !== previousInnerHTML) {
						previousInnerHTML = document.body.innerHTML;
						Signup.util.resize();
					}
					setTimeout(arguments.callee, 100);
				}();
			},

			/**
			 * This is normally in the Sakai script but we don't want to scroll the page when resizing
			 * so we have our own copy. We also assume the window name contains the ID of the parent frame.
			 */
			"resize": function(name, minHeight){
				if (minHeight) {
					$(document.body).data("min-height", minHeight);
				} else {
					if ( $(document.body).data("min-height") ) {
						minHeight = $(document.body).data("min-height");
					}
				}
				var id = window.name;
				if (!id) {
					return;
				}
				var frame = parent.document.getElementById(id);
				if (frame) {
					// reset the scroll
					//parent.window.scrollTo(0, 0);

					var objToResize = (frame.style) ? frame.style : frame;

					var height;
					var offsetH = document.body.offsetHeight;
					var innerDocScrollH = null;

					if (typeof(frame.contentDocument) != 'undefined' || typeof(frame.contentWindow) != 'undefined') {
						// very special way to get the height from IE on Windows!
						// note that the above special way of testing for undefined variables is necessary for older browsers
						// (IE 5.5 Mac) to not choke on the undefined variables.
						var innerDoc = (frame.contentDocument) ? frame.contentDocument : frame.contentWindow.document;
						innerDocScrollH = (innerDoc != null) ? innerDoc.body.scrollHeight : null;
					}

					if (document.all && innerDocScrollH != null) {
						// IE on Windows only
						height = innerDocScrollH;
					}
					else {
						// every other browser!
						height = offsetH;
					}
					// here we fudge to get a little bigger
					if (minHeight) {
						if (height < minHeight) {
							height = minHeight;
						}
					}

					var newHeight = height + 40;

					// but not too big!
					if (newHeight > 65520)
						newHeight = 65520;

					// capture my current scroll position


					// resize parent frame (this resets the scroll as well)
					objToResize.height = newHeight + "px";

					// optional hook triggered after the head script fires.

					if (parent.postIframeResize) {
						parent.postIframeResize(id);
					}
				}
			},
			/**
			 * Creates a position object for a dialog.
			 * The dialog is placed 5% down from the top of the page.
			 * It also works within an iFrame (for Sakai).
			 */
			dialogPosition: function(){
				var workingWindow = $(parent.window || window);
				var position = ["center", workingWindow.scrollTop() + Math.round(workingWindow.height() * 0.05)];
				if (window.name) { // If we're not running in a window created by Sakai.
					var iframeOffset = $("#" + window.name, workingWindow.get(0).document).offset();
					if (iframeOffset) { // If we are take account of the iframe location.
						position[1] -= iframeOffset.top;
					}
				}
				return position;	
			}
		},
		"signup": {
			/**
			 * The statuses that a signup can have.
			 */
			"statuses": ["WAITING", "PENDING", "ACCEPTED", "APPROVED", "CONFIRMED", "REJECTED", "WITHDRAWN"],

			"getActions": function(status, id, closes, admin){
				if (admin) {
					switch (status) {
						case "WAITING":
							return [{
								"name": "Accept",
								"url": "../rest/signup/" + id + "/accept"
							}, {
								"name": "Reject",
								"url": "../rest/signup/" + id + "/reject"
							}];
						case "PENDING":
							return [{
								"name": "Accept",
								"url": "../rest/signup/" + id + "/accept"
							}, {
								"name": "Reject",
								"url": "../rest/signup/" + id + "/reject"
							}, {
								"name": "Waiting",
								"url": "../rest/signup/" + id + "/waiting"
							}];
						case "ACCEPTED":
							return [{
								"name": "Approve",
								"url": "../rest/signup/" + id + "/approve"
							}, {
								"name": "Reject",
								"url": "../rest/signup/" + id + "/reject"
							}];
						case "APPROVED":
							return [{
								"name": "Confirm",
								"url": "../rest/signup/" + id + "/confirm"
							}, {
								"name": "Reject",
								"url": "../rest/signup/" + id + "/reject"
							}];
						case "CONFIRMED":
							return [];
						case "REJECTED":
							return [];
						case "WITHDRAWN":
							return [];
					}
				}
				else {
					switch (status) {
						case "WAITING":
							return [{
								"name": "Withdraw",
								"url": "../rest/signup/" + id + "/withdraw"
							}];
						case "PENDING":
							return [{
								"name": "Withdraw",
								"url": "../rest/signup/" + id + "/withdraw"
							}];
						case "ACCEPTED":
							var now = new Date().getTime();
							if (closes > now) {
							return [{
								"name": "Withdraw",
								"url": "../rest/signup/" + id + "/withdraw"
							}];
							} else {
								return [];
							}
						case "APPROVED":
							var now = new Date().getTime();
							if (closes > now) {
							return [{
								"name": "Withdraw",
								"url": "../rest/signup/" + id + "/withdraw"
							}];
							} else {
								return [];
							}
						case "CONFIRMED":
							var now = new Date().getTime();
							if (closes > now) {
							return [{
								"name": "Withdraw",
								"url": "../rest/signup/" + id + "/withdraw"
							}];
							} else {
								return [];
							}
					}
				}
				return [];
			},
			/**
			 * Displays an input selector listing the statuses with the current one selected.
			 * @param {Object} currentId
			 * @param {Object} currentStatus
			 */
			"selectStatus": function(currentId, currentStatus) {
				var output = '<select class="status-select" name="status-'+ currentId+ '">';
				$.each(Signup.signup.statuses, function() {
					output += (currentStatus == this?'<option selected="true">':'<option>')+ this+ '</option>';
				});
				output += '</select>';
				return output;
			},
			"formatActions": function(actions){
				return $.map(actions, function(action){
					return '<a class="action" href="' + action.url + '">' + action.name + '</a>';
				}).join(" / ");
			},
			/**
			 * Formats a notes string so we only display the first bit of it and then display a tooltip for the rest.
			 * @param {Object} notes
			 */
			"formatNotes": function(notes){
				if (notes && notes.length > 50) {
					return '<span class="signup-notes">' + Text.toHtml(notes.substr(0, 45)) + '... <span class="more">[more]<span class="full">' + Text.toHtml(notes) + '</span></span></span>'
				}
				else {
					return Text.toHtml(notes);
				}
			},
			/**
			 * This formats the available places.
			 * If the user isn't an admin and there aren't any place left it displays full
			 * rather than the number of places oversubscribed we are.
			 * @param {Object} places
			 * @param {Object} isAdmin
			 */
			formatPlaces: function(places, isAdmin) {
				return '(' + ((!isAdmin && places < 1)?"full":places+ '&nbsp;places')+')';
			},

			/**
			 * Produces a summary based on the components in the signup.
			 * @param {Object} components
			 */
			"summary": function(components, recentDays){
				var summary = {state: "unknown", previous: ["Current Courses"], message: null};
				if (components.length == 0) {
					summary.message = "No";
					summary.previous[0] = "Old Courses";
					return summary; 
				}
				var now = $.serverDate();
				var nextOpen = Number.MAX_VALUE;
				var willClose = 0;
				var baseDate = 0;
				var isOneOpen = false;
				var isOneBookable = false;
				var areSomePlaces = false;
				var newCourse = false;
				var opensText = "";
				var closesText = "";

				$.each(components, function() {
					var component = this;

					if (component.opensText) {
						opensText = component.opensText;
					}
					if (component.closesText) {
						closesText = component.closesText;
					}

					var isOpen = component.opens < now && component.closes > now;
					if (component.opens) {
						if (component.opens > now && component.opens < nextOpen) {
							nextOpen = component.opens;
						}
					}
					if (component.closes && component.closes > willClose) {
						willClose = component.closes;
					}
					if (component.baseDate > baseDate) {
						baseDate = component.baseDate;
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

					// If we are not recording signups, we must assume that there are places available.
					if ("Daisy" != component.source) {
						areSomePlaces = true;
						isOneBookable = true;
					}

					var newDate = now - (recentDays * 24 * 60 * 60 * 1000);
					if (component.created > newDate) {
						newCourse = true;
					}
				});

				if (opensText) {
					summary.message = "Opens "+opensText;
					if (closesText) {
						summary.message = summary.message+" and ";
					}
				}
				if (closesText) {
					summary.message = summary.message+"closes "+closesText;
				}

				if (willClose == 0) {
					if (baseDate > 0 && now > baseDate) {
						summary.previous[0] = "Old Courses";
					}
					return summary;
				}

				if (!isOneBookable) {
					summary.state = "No"; // (Not Bookable)";
					summary.message = "Not Bookable";
				}
				if (now > baseDate) {
					summary.previous[0] = "Old Courses";
				}

				if (now > willClose) {
					summary.previous[0] = "Old Courses";
					summary.message = "Booking Closed";
					summary.state = "No";
				}

				if (isOneOpen) {
					if (areSomePlaces) {
						var remaining = willClose - now;
						summary.message = "close in " + Signup.util.formatDuration(remaining);
						summary.state = "Yes";
					}
					else {
						summary.message = "full";
						summary.state = "No (Full)";
					}
				}
				else {
					if (nextOpen != Number.MAX_VALUE) {
						var until = nextOpen - now;
						summary.message = "open in " + Signup.util.formatDuration(until);
						summary.state = "Later";
					}
				}

				if (newCourse) {
					summary.previous[1] = "New Courses";
				}

				return summary;
			}
		},
		"user": {
			"render": function(user, group, components){
				var details = "";

				var previous = function(userid, groupid){
					var tip = "";
					$.ajax({
						url: "../rest/signup/previous/",
						type: "GET",
						data: {	userid: userid,
								componentid: "",
								groupid: groupid
								},
						success: function(result) {
							$.each(result, function(){
								var signupStatus = this.status;
								var signupGroup = this.group;
								$.each(this.components, function(){
									tip += signupGroup.title+" "+this.title+" ("+this.id+") "+this.when+" "+signupStatus+"<br />";
								});
							});
						}
					});
					return tip;
				};

				if (user) {
					if (user.email) {
						details += '<a href="mailto:' + user.email + '">' + user.name + '</a>';
					} else {
						details += user.name;
					}
					if (user.units && user.units.length > 0) {
						details += '<br />' + user.units.join(" / ");
					}
					if (user.degreeProgram && user.degreeProgram.length > 0) {
						details += '<br />' + user.degreeProgram;
					}
					if (user.yearOfStudy && user.yearOfStudy.length > 0) {
						details += '<br />Year of Study: ' + user.yearOfStudy;
					}
					if (user.type && user.type.length > 0) {
						var type = user.type.substr(0, 1).toUpperCase() + user.type.substr(1);
						details += '<br />Status: ' + type;
					}

					if (components) {
						details += '<br /><span class="previous-signup more" userid="'+user.id+'" groupid="'+group.id+'">[Previous SignUps]';
						details += '<span class="previous-signup-tooltip"></span>';
						$.each(components, function(){
							details += '<input class="componentid" type="hidden" name="componentid" value="'+this.id+'"/>';
						});
					}

					details += '</span>';
				} else {
					details += 'Leaver';
				}
				return details;
			},
			/**
			 * Standard function for sorting users. This basically just sorts based on the
			 * name of the user and if they are the same sort on the ID so it remains consistent.
			 * @param {Object} user1
			 * @param {Object} user2
			 */
			"sort": function (user1, user2) {
				if(user1.name < user2.name) {
					return -1;
				} else if ( user1.name > user2.name) {
					return 1;
				} else {
					return (user2.id < user1.id)-(user1.id<user2.id); // Fallback to ID http://www.merlyn.demon.co.uk/js-order.htm
				}
			}
		},
		"supervisor": {
			"render": function(supervisor, signup, admin){
				var details = "";
				if (supervisor) {
					if (supervisor.email) {
						details += '<a href="mailto:' + supervisor.email + '">' + supervisor.name + '</a>';
					} else {
						details += supervisor.name;
					}
				} else {
					if (admin) {
						if (signup.user) {
							details += '<a class="supervisor" user="'+signup.user.name+'" id="'+signup.id+'" href="#">Add Supervisor</a>';
						}
					}
				}
				return details;
			}
		},
		"term": {

			/**
			* Sort an Array of Terms into most recent first. The year is the calendar year of the term,
			* not the academic year.
			* @param {Array} termArray of strings in the format 'Michaelmas 2012', 'Hilary 2013' or 'Trinity 2013'.
			*/
			"sortArray": function(termsArray) {
				termsArray.sort(function(a,b){
					var awords=a.split(" ");
					var bwords=b.split(" ");
					if (awords[1] != bwords[1]) {
						return bwords[1] - awords[1]
					}
					if (awords[0] == bwords[0]) {
						return 0;
					}
					if (awords[0] == "Michaelmas") {
						return -1;
					}
					if (bwords[0] == "Michaelmas") {
						return 1;
					}
					if (awords[0] == "Trinity") {
						return -1;
					}
					if (bwords[0] == "Trinity") {
						return 1;
					}
				});
			}
		},
		"component": {
			/**
			 * This formats a component to display it to user.
			 */
			"format": function(component, isAdmin){
				var size = component.size;
				var limit = size * placesWarnPercent / 100;
				var componentPlacesClass;
				if (placesErrorLimit >= component.places) {
					componentPlacesClass = "course-component-error";
				} else if (limit >= component.places) {
					componentPlacesClass = "course-component-warn";
				} else {
					componentPlacesClass = "course-component";
				}
				return '<span class="course-component">' + component.title + " " +
					component.slot + " in " + component.when + ' <span class='+componentPlacesClass+'>'+
					Signup.signup.formatPlaces(component.places, isAdmin)+'</span></span>';
			}
		}
	};

}();

/**
 * jQuery plugin to make a signup table.
 */
(function($){

	$.fn.signupTable = function(url, isAdmin, allowChangeStatus, allowChangeAction){
		allowChangeStatus = allowChangeStatus || false;
		allowChangeAction = allowChangeAction || true;
		var element = this;
		var table = this.dataTable({
			"bJQueryUI": true,
			"sPaginationType": "full_numbers",
			"bProcessing": true,
			"sAjaxSource": url,
			"bAutoWidth": false,
			"aaSorting": [[1, "desc"]],
			"aoColumns": [{
				"sTitle": "",
				"bVisible": false,
			   "bUseRendered": false
			}, {
				"sTitle": "Created",
				"fnRender": function(aObj){
					if (aObj.aData[1]) {
						return Signup.util.formatDuration($.serverDate() - aObj.aData[1]) + " ago";
					}
					else {
						return "unknown";
					}
				},
				"bUseRendered": false
			}, {
				"sTitle": "Student",
				"sWidth": "20%"
			}, {
				"sTitle": "Module"
			}, {
				"sTitle": "Supervisor"
			}, {
				"sTitle": "Notes",
				"sWidth": "20%",
				"sClass": "signup-notes"
			}, {
				"sTitle": "Status",
				"fnRender": function(aObj) {
					return allowChangeStatus?Signup.signup.selectStatus(aObj.aData[0], aObj.aData[6]):aObj.aData[6];
				}
			}, {
				"sTitle": "Actions"
			}, {
				"sTitle": "Status",
				"bVisible": false
			}, {
				"sTitle": "Term",
				"bVisible": false
			}],
			"fnServerData": function(sSource, aoData, fnCallback){
				jQuery.ajax({
					dataType: "json",
					type: "GET",
					cache: false,
					url: sSource,
					success: function(result){
						var data = [];
						$.each(result, function(){
							var course = ['<span class="course-group">' + this.group.title + "</span>"].concat($.map(this.components,
									function(component){ return Signup.component.format(component, isAdmin); })).join("<br>");
							if (isAdmin && this.components.length > 1) {
								course += "<br><a href='' class='signup-split' data-signup-id='"+ this.id+ "'>Split Signup</a>"
							}

							var closes = 0;
							var slots = new Array();
							$.each(this.components,
									function(){
										slots.push(this.slot);
										if (closes != 0 && this.closes > closes) {
											return;
										}
										closes = this.closes;
							});

							var actions = "";
							if (allowChangeAction) {
								actions = Signup.signup.formatActions(Signup.signup.getActions(this.status, this.id, closes, isAdmin));
							}
							data.push([this.id, (this.created) ? this.created : "", Signup.user.render(this.user, this.group, this.components), course, Signup.supervisor.render(this.supervisor, this, isAdmin), Signup.signup.formatNotes(this.notes), this.status, actions, this.status, slots]);

						});
						fnCallback({
							"aaData": data
						});
					}
				});
			},
			// This is useful as when loading the data async we might want to handle it later.
			"fnInitComplete": function() {
				table.trigger("tableInit");
			}
		});

		// The die().live is a bad hack so that when someone switches to a different module and a
		// new table is displayed the old handlers don't get called for them.
		$("span.previous-signup").die().live("mouseover", function(e){
			var span = $(this);
			var userId = $(this).attr("userid");
			var groupId = $(this).attr("groupid");
			var componentId = $(this).children("input.componentid").map(function() {
				return this.value;
			}).get().join(',');

			$.ajax({
				url: "../rest/signup/previous/",
				type: "GET",
				data: {userid: userId,
					   componentid: componentId,
					   groupid: groupId
					  },
				success: function(result) {
						var tip = "";
						var lines = 0;
						$.each(result, function(){
							var signupStatus = this.status;
							var signupGroup = this.group;
							$.each(this.components, function(){
								tip += signupGroup.title+" "+this.title+" ("+this.id+") "+this.when+" "+signupStatus+"<br />";
								lines++;
							});
						});
						var tooltip = $("span.previous-signup-tooltip", span);
						if (tip.length == 0) {
							tooltip.html("None");
						} else {
							tooltip.html(tip); 
							tooltip.css("width", ((tip.length/lines)-6)*0.5+"em");
						}	
				}
			});

		});

		$("a.supervisor", this).die().live("click", function(e){
			signupAddSupervisor.attr("username", $(this).attr("user"));
			signupAddSupervisor.attr("signupid", $(this).attr("id"));
			signupAddSupervisor.jqmShow();
		});

		$("a.signup-split", this). die().live("click", function(e) {
			var signupId = $(this).attr("data-signup-id");
			Signup.split(signupId, isAdmin, function() {
				// This is the success after the split has worked.
				element.dataTable().fnReloadAjax(null, null, true);
				$(table).trigger("reload");
			});
			e.preventDefault();
		});


		$("a.action", this).die().live("click", function(e){
			var url = $(this).attr("href");
			$.ajax({
				"url": url,
				"type": "POST",
				"success": function(data){
					element.dataTable().fnReloadAjax(null, null, true);
					$(table).trigger("reload"); // Custom event type;
				}
			});
			return false;
		});

		$("select.status-select", this).die().live("change", function(e) {
			var select = $(this);
			var newStatus = select.val();
			var id = select.attr("name").substr(7); // Trim the leading "signup-"
			select.attr("disabled", true);
			$.ajax({
				url: "../rest/signup/"+id,
				type: "POST",
				data: {status: newStatus},
				success: function(data) {
					element.dataTable().fnReloadAjax(null, null, true);
					$(table).trigger("reload");
				}
			});
		});

		$("select.signups-table-status-filter").die().live("change", function(e) {
			var filterStatus = $(this).val();
			table.fnFilter(filterStatus, 8);
		});

		$("select.signups-table-term-filter").die().live("change", function(e) {
			var filterTerm = $(this).val();
			table.fnFilter(filterTerm, 9);
		});

		var html = '<div id="signup-add-supervisor-win" class="jqmWindow" style="display: none">'
		html += '<form id="signup-add-supervisor">';
		html += '<h2></h2>'
		html += '<p>Enter the Supervisor email.<br />';
		html += '<input type="text" name="supervisor" id="add-supervisor" size="28" />';
		html += '</p>';
		html += '<span class="errors"></span>';
		html += '<br>';
		html += '<input type="submit" value="Submit">';
		html += '<input type="button" class="cancel" value="Cancel"><br>';
		html += '</form>';
		html += '</div>';
		$("body").append(html);

		var signupAddSupervisor = $("#signup-add-supervisor-win");
		signupAddSupervisor.resize(function(e){
			// Calculate size.
		});
		signupAddSupervisor.jqm({
			onShow: function(objs) {
				$("body").css("overflow", "hidden"); // Doesn't seem to work on IE7
				objs.w.css("height", 150);
				objs.w.show();
				$("h2", signupAddSupervisor).html("Add Supervisor for " + signupAddSupervisor.attr("username"));
				$("input[name=supervisor]", signupAddSupervisor).val("");
				$(":submit", signupAddSupervisor).removeAttr("disabled");
				$(".errors", signupAddSupervisor).html("");
			},
			onHide: function(objs) {
				$("body").css("overflow", "auto");
				objs.w.fadeOut('250',function(){ objs.o.remove(); });
			}
		});
		signupAddSupervisor.jqmAddClose("input.cancel");

		$(window).resize(function(){
			var windowHeight = $(window).height();
			var positionTop = signupAddSupervisor[0].offsetTop;
			if (windowHeight < signupAddSupervisor.outerHeight() + positionTop) {
				// Too big.
				var newHeight = windowHeight - (signupAddSupervisor.outerHeight(false) - signupAddSupervisor.height()) -2;
				signupAddSupervisor.height(newHeight);
				signupAddSupervisor.css("top", "1px"); // Move almost to the top.
			};
		});

		signupAddSupervisor.unbind("submit").bind("submit", function(e) {
			var form = this;
			var supervisor = $("input[name=supervisor]", signupAddSupervisor).val();
			var user = signupAddSupervisor.attr("username");
			var signup = signupAddSupervisor.attr("signupid");
			var badSupervisor = true;
			var goodSupervisor;
			var continueSearch = true;

			var postSignup = function() {
				signupAddSupervisor.jqmHide(); // Hide the popup.
				element.dataTable().fnReloadAjax(null, null, true);
				$(table).trigger("reload"); // Custom event type;
			};

			var doSignup = function(){
				var supervisorId;
				if (goodSupervisor) {
					supervisorId = goodSupervisor.id;

					$.ajax({
						"url": "../rest/signup/supervisor",
						"type": "POST",
						"async": true,
						"traditional": true,
						"data": {
							"signupId": signup,
							"supervisorId": supervisorId
						},
						"complete": function() {
							postSignup();
						}
					});
				}

			};

			var findSupervisor = function() {
				$.ajax({
					"url": "../rest/user/find",
					"method": "GET",
					"async": true,
					"data": {"search": supervisor},
					"success": function(data) {
						goodSupervisor = data;
						badSupervisor = false;
					},
					"error": function() {
						badSupervisor = true;
					},
					"complete": function() {
						$(":submit", form).removeAttr("disabled");
						if (badSupervisor) {
							$(".errors", form).html("Couldn't find supervisor " + supervisor);
						} else {
							doSignup();
						}
					}
				});
			}; /* $.datepicker.formatDate('yy-mm-dd', new Date(2007, 1 - 1, 26)); */

			$(":submit", form).attr("disabled", "true");
			$("input.cancel", form).one("click", function(){ continueSearch = false;});
			findSupervisor();  
			return false;
		});

		return table;

	};

})(jQuery);

// Stop browsers without console.log from crashing.
var console = console ||
{
	"log": function(){
	}
};


