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
			
			$(".adminSettings select").change(function(){
				var changeAnswersArray = $("input[id=showModifyResponsesAllowedToStu::modifyResponsesAllowed]");
				var evaluatorsTakePartArray = $("input[id=evaluatorsParticipate]");
				if (this.value == 'NONE') {
					for (var i=0; i<changeAnswersArray.length; i++) {
						changeAnswersArray[i].checked=false;
						changeAnswersArray[i].disabled=true;
					}
					for (var i=0; i<evaluatorsTakePartArray.length; i++) {
						evaluatorsTakePartArray[i].checked=false;
						evaluatorsTakePartArray[i].disabled=true;
					}
				}
				if (this.value == 'AUTH') {
					for (var i=0; i<changeAnswersArray.length; i++) {
						changeAnswersArray[i].checked=true;
						changeAnswersArray[i].disabled=false;
					}
					for (var i=0; i<evaluatorsTakePartArray.length; i++) {
						evaluatorsTakePartArray[i].checked=true;
						evaluatorsTakePartArray[i].disabled=false;
					}
				}
			});
			
});