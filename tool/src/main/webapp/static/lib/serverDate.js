// Small jQuery plugin to get dates from server.
(function($){

	// Work out the difference between client time and server time.
    var adjustment;
	
    var init = function(){
        if (!adjustment) {
            $.ajax({
                "url": "/course-signup/rest/user/current",
                "type": "GET",
                "async": false,
                "dataType": "json",
                "success": function(data){
                    var serverDate = data.date;
					var clientDate = new Date().getTime();
					adjustment = serverDate - clientDate;
                }
            });
        }
    };
    
    $.serverDate = function(){
        init();
        return (new Date().getTime() + adjustment);
        
    };
})(jQuery);
