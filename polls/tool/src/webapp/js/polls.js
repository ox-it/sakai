$(document).ready(function() {
    //A filter to extract the real date formatted as a simple number and pass that to the sorter
    var dateExtractor = function(_that) {
       var that = $(_that);
        if(that.attr('name') && (that.attr('name').search(/realDate:/) != -1)){
            return that.attr('name').replace(/realDate:/,'').replace(/-/g,' ').replace(':00.0','').replace(/:/g,'').replace(/ /g,'');
        }
       return that.text();
    }
    $("#sortableTable").tablesorter({
        sortList: [[3,1]], // Initial order by the latest (NOT earliest) closing date
        headers: { // disable it by setting the property sorter to false
            0: {sorter: 'digit'},
            1: {sorter: false},
            2: {sorter: 'digit'},
            3: {sorter: 'digit'},
            4: {sorter: false},
            5: {sorter: false}
        },
        textExtraction: dateExtractor
    });
    
    //register click events for the mobile info section
    $(".mobile-info-link").click(function(){ 
    	var id = $(this).attr('rel');
    	
    	shortenUrl("/direct/oxford/shorten?path=/poll/" + id + ".json");
    	
    	//get the shortened URL
    	function shortenUrl(url) {
    		
	    	$.ajax({
				url: url,
				type: "GET",
				cache: true,
				dataType: "text",
				timeout: 5000,
				success: function(data) {
		    		//set shortenedUrl into link
		    		$("#dialog-mox-url").html(data);
		    		//$("#dialog-mox-url").attr("href", data); //need it to be linked? if so, change to an <a> tag and uncomment this
		    		
		    		//also set img tag for QR code
		    		var imgUrl = "https://chart.googleapis.com/chart?chs=250x250&cht=qr&chl=" + data;
		    		$('#dialog-qr-code').attr("src", imgUrl);
		    		
		    		//and show the dialog
		    		showDialog();
				},
				error: function(xhr, status) {
					alert("Failed to retrieve m.ox url for poll: " + id + ", error: " + xhr.status);
				}
			});
    	}
    	
    	
    	function showDialog() {
    		$("#dialog").dialog({
    			resizable: false,
    			draggable: true
    		});
    		//I haven't specified a position here so it is more flexible
    		//i.e. click the first one, move the dialog, click the second, appears in same spot as the moved one.
    		//If you specify a position it will reset to that position on subsequent clicks.
    	}
    	
    	
    	return false;
    });

    
    
});