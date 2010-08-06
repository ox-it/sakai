// Small jQuery plugin to get dates from server.
(function($){

    var serverDate;
    var init = function(){
        if (!serverDate) {
            $.ajax({
                "url": "/course-signup/rest/user/current",
                "type": "GET",
                "async": false,
                "dataType": "json",
                "success": function(data){
                    serverDate = data.date;
                }
            });
        }
    };
    
    $.serverDate = function(){
        init();
        return serverDate;
        
    };
})(jQuery);
