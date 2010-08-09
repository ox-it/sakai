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
            }
        },
		"signup": {
			"getActions": function(status, id) {
				switch (status) {
					case "PENDING": 
						return [
							{
								"name": "Accept",
								"url": "/course-signup/rest/signup/"+id+"/accept"
							},
							{
								"name": "Reject",
								"url": "/course-signup/rest/signup/"+id+"/reject"
							}
						];
					case "ACCEPTED":
						return [
							{
								"name": "Approve",
								"url": "/course-signup/rest/signup/"+id+"/approve"
							},
							{
								"name": "Reject",
								"url": "/course-signup/rest/signup/"+id+"/reject"
							}
						];
					case "APPROVED":
						return [];
					case "REJECTED":
						return [];
					case "WITHDRAWN":
						return [];
				}
				return [];
			},
			"formatActions": function(actions) {
				return $.map(actions, function(action) {
					return '<a class="action" href="'+ action.url+ '">'+ action.name+ '</a>';
				}).join(" / ");
			},
			/**
			 * Formats a notes string so we only display the first bit of it and then display a tooltip for the rest.
			 * @param {Object} notes
			 */
			"formatNotes": function(notes) {
				if (notes && notes.length> 50) {
					return '<span class="signup-notes">'+ Text.toHtml(notes.substr(0, 45))+'... <span class="more">[more]<span class="full">'+ Text.toHtml(notes)+ '</span></span></span>'
				} else {
					return Text.toHtml(notes);
				}
			}
		}
    };
}();


