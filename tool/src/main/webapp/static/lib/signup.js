var Signup = function(){
    return {
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
                                return '<span class="course-component">' + component.title + " " + component.slot + " in " + component.when + ' (' + component.places + '&nbsp;places)</span>';
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


