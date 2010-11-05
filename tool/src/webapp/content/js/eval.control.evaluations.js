

$(document).ready(function () {
	evalControlData.ajaxSetUp();
});

var evalControlData = (function() {
	
	//register click events for the mobile info section
	_initAjaxSetUp = function(){
		
		$('.mobile-info-link').bind('click', function() {
			id = $(this).attr('rel');
			_shortenUrl("/eval-evaluation/" + id);
			return false;
		});
		
	};

	
	_shortenUrl = function(fullUrl) {

		ebUrl = '/direct/oxford/shorten?path=' + fullUrl;
		
		$.ajax({
			url: ebUrl,
			type: "GET",
			cache: true,
			dataType: "text",
			timeout: 5000,
			success: function(data) {
	    		//set shortenedUrl into link
	    		$("#dialog-mox-url").html(data);
	    		
	    		//also set img tag for QR code
	    		//var imgUrl = "http://chart.apis.google.com/chart?chs=250x250&cht=qr&chl=" + data;
	    		imgUrl = "/direct/oxford/qr?height=547&width=547&s=" + data;
	    		$('#dialog-qr-code').attr("src", imgUrl);
	    		
	    		//and show the dialog
	    		_showDialog();
			},
			error: function(xhr, status) {
				alert("Failed to retrieve m.ox url for evaluation: " + id + ", error: " + xhr.status);
			}
		});
	};


	_showDialog = function() {
		$("#dialog").dialog({
			close: function(event, ui){
				_resizeFrame('shrink');
			},	
			height: 680,
			width: 580,
			resizable: false,
			draggable: true,
			closeOnEscape: true
		});
		//I haven't specified a position here so it is more flexible
		//i.e. click the first one, move the dialog, click the second, appears in same spot as the moved one.
		//If you specify a position it will reset to that position on subsequent clicks.
		
		//resize iframe
		_resizeFrame('grow');
	};



	_resizeFrame = function(updown){	 
	    if (top.location != self.location) {	 
	        frame = parent.document.getElementById(window.name);	 
	    }	 
	    if (frame) {	 
	        if (updown == 'shrink') {	 
	            clientH = document.body.clientHeight;	 
	        }	 
	        else {	 
	            clientH = document.body.clientHeight + 450;	 
	        }	 
	        $(frame).height(clientH);	 
	    }	 
	    else {	 
	        //throw( "resizeFrame did not get the frame (using name=" + window.name + ")" );	 
	    }	 
	}
	
	
	return {
		 ajaxSetUp: _initAjaxSetUp
	}
})($);



