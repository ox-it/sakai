$(function() {
			
			$(".resultSharing input[type=radio]").click(function(){
				var tickboxArray = $(".resultSharing input[type=checkbox]");
				if (this.value == 'private' || this.value == 'public') {
					for (var i=0; i<tickboxArray.length; i++) {
						tickboxArray[i].disabled=true;
					}
				}
				if (this.value == 'visible') {
					for (var i=0; i<tickboxArray.length; i++) {
						tickboxArray[i].disabled=false;
					}
				}
			});
			
			var updateOptions = function(value){
				// When the survey doesn't require users to login they can't come back to the survey so some
				// options in the interface don't makes sense, so we disable them.
				var nonLoggedIn = value ==='NONE';
				$('input[name="showModifyResponsesAllowedToStu::modifyResponsesAllowed"]')
					.prop({'disabled': nonLoggedIn, 'checked': !nonLoggedIn});
				$('input[name="showAllRolesCanParticipate::allRolesParticipate"]')
					.prop({'disabled': nonLoggedIn, 'checked': !nonLoggedIn});
			};

			// Fire when changing value
			$(".adminSettings select").change(function() {
				updateOptions(this.value);
			});
			// Fire with the current value on loading the page
			updateOptions($(".adminSettings select").val());
});