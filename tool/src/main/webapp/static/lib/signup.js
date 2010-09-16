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
			show: function(dest, id, old, success){
				var courseData;
				var signupData;
				var template;
				
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
					$.ajax({
						url: "../rest/course/" + id,
						data: {
							range: (old) ? "PREVIOUS" : "UPCOMING"
						},
						dataType: "json",
						cache: false,
						success: function(data){
							courseData = data;
							showCourse();
						}
					});
					$.ajax({
						url: "../rest/signup/my/course/" + id,
						dataType: "json",
						cache: false,
						success: function(data){
							signupData = data;
							showCourse();
						}
					});
					if (!template) { // When reloading we might already have the template loaded.
						$.ajax({
							url: "course.tpl",
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
					if (!courseData || !signupData || !template) {
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
					for (var componentIdx in data.components) {
						var component = data.components[componentIdx];
						
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
								if (parts[part].type.id == component.componentSet) {
									parts[part].signup = (component.signup) ? component.signup : null;
									parts[part].options.push(component);
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
						if (data.full && !component.full) {
							data.full = false; // At least one is open 
						}
					}
					
					data.signup = Signup.signup.summary(data.components)["message"];
					
					data.parts = parts;
					var output = template.process(data, {throwExceptions: true});
					dest.html(output);
					// If there is only one checkbox tick it.
					var radioButtons = $("input:radio:enabled", dest);
					if (radioButtons.length == 1) {
						radioButtons.first().attr("checked", true);
					}
					else 
						if (radioButtons.length == 0) {
							$(":submit", dest).attr("disabled", "true");
						}
					$("form", dest).submit(function(){
						try {
							var radioSelected = {};
							var errorFound = false;
							var selectedParts = [];
							var selectedPartIds = [];
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
							for (radio in radioSelected) {
								if (!radioSelected[radio]) {
									errorFound = true;
									jQuery("#parts .error", dest).show().html("You need to select which components you wish to take.");
								}
							}
							// TODO This needs processing.
							if (!errorFound) {
								jQuery(".error", dest).hide();
								var signup = Signup.course.signup({title: data.title, id: id}, {titles: selectedParts, ids: selectedPartIds});
								signup.bind("ses.signup", function(){
									loadCourse(); // Reload the course.
									// Display a nice message. Should we keep the exising success()?
									success = function(){
										$(".messages", dest).append('<div class="message"><span class="good">Signup Successful</span></div>');
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
					success && success();
				};
			
			loadCourse();	
			},
			/**
			 * Handle the displaying of a confirmation page for a signup.
			 */
			signup: function(course, components){
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
							url: "../rest/signup/my/new",
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
					url: "signup.tpl",
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
							"courseId": course.id
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
            "autoresize": function(){
                var previousInnerHTML = document.body.innerHTML;
                return function(change){
                    if (document.body.innerHTML !== previousInnerHTML) {
                        previousInnerHTML = document.body.innerHTML;
                        Signup.util.resize(window.name);
                    }
                    setTimeout(arguments.callee, 100);
                }();
            },
            
			/**
			 * This is normally in the Sakai script but we don't want to scroll the page when resizing
			 * so we have our own copy. We also assume the window name contains the ID of the parent frame.
			 */
            "resize": function(){
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
                    var newHeight = height + 40;
                    
                    // but not too big!
                    if (newHeight > 32760) 
                        newHeight = 32760;
                    
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
			"statuses": ["PENDING", "ACCEPTED", "APPROVED", "REJECTED", "WITHDRAWN"],
			
			
            "getActions": function(status, id, admin){
                if (admin) {
                    switch (status) {
                        case "PENDING":
                            return [{
                                "name": "Accept",
                                "url": "../rest/signup/" + id + "/accept"
                            }, {
                                "name": "Reject",
                                "url": "../rest/signup/" + id + "/reject"
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
                            return [];
                        case "REJECTED":
                            return [];
                        case "WITHDRAWN":
                            return [];
                    }
                }
                else {
                    switch (status) {
                        case "PENDING":
                            return [{
                                "name": "Withdraw",
                                "url": "../rest/signup/" + id + "/withdraw"
                            }];
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
			"summary": function(components){
				var summary = {state: "unknown", message: null};
				if (components.length == 0) {
					summary.message = "No";
					return summary; 
				}
				var now = $.serverDate();
				var nextOpen = Number.MAX_VALUE;
				var willClose = 0;
				var isOneOpen = false;
				var isOneBookable = false;
				var areSomePlaces = false;
				$.each(components, function() {
					var component = this;
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
				});
				var message = "";
				if (!isOneBookable) {
					summary.state = "No";
					return summary;
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
					if (nextOpen === Number.MAX_VALUE) {
						summary.state = "No";
					}
					else {
						var until = nextOpen - now;
						summary.message = "open in " + Signup.util.formatDuration(until);
						summary.state = "Later";
					}
				}
				return summary;
			}
        },
        "user": {
            "render": function(user){
                var details = "";
                if (user) {
					if (user.email) {
						details += '<a href="mailto:' + user.email + '">' + user.name + '</a>';
					} else {
						details += user.name;
					}
                    if (user.units && user.units.length > 0) {
                        details += '<br>' + user.units.join(" / ");
                    }
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
        }
    };
}();

/**
 * jQuery plugin to make a signup table.
 */
(function($){

    $.fn.signupTable = function(url, isAdmin, allowChangeStatus){
		allowChangeStatus = allowChangeStatus || false;
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
                "bSortable": false,
                "fnRender": function(aObj){
                    return '<input type="checkbox" value="' + aObj.aData[0] + '">';
                },
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
                "sTitle": "Student"
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
                            var course = ['<span class="course-group">' + this.group.title + "</span>"].concat($.map(this.components.concat(), function(component){
                                return '<span class="course-component">' + component.title + " " + component.slot + " in " + component.when + ' '+ Signup.signup.formatPlaces(component.places, isAdmin)+'</span>';
                            })).join("<br>");
                            var actions = Signup.signup.formatActions(Signup.signup.getActions(this.status, this.id, isAdmin));
                            data.push([this.id, (this.created) ? this.created : "", Signup.user.render(this.user), course, Signup.user.render(this.supervisor), Signup.signup.formatNotes(this.notes), this.status, actions]);
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
        $("a.action", this).die().live("click", function(e){
            var url = $(this).attr("href");
            $.ajax({
                "url": url,
                "type": "POST",
                "success": function(data){
                    element.dataTable().fnReloadAjax();
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
					element.dataTable().fnReloadAjax();
					$(table).trigger("reload");
				}
			});
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


