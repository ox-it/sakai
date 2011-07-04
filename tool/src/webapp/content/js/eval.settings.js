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
});