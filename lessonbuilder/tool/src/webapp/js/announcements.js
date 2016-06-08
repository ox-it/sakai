var number;
$(function(){
	String.prototype.escapeHTML = function(){
		return (this.replace(/&/g, '&amp;').replace(/>/g, '&gt;').replace(/</g, '&lt;').replace(/"/g, '&quot;'));
	};
	number = $('.announcements-div').find($('#numberOfAnnouncements')).val();
	//set the 'numberOfAnnouncements' in the hidden field for edit screen
	$('#numberOfAnnouncements').val(number);
	showAnnouncements();
});

function showAnnouncements(){
	var url = $('.announcements-site-url').text().replace(/'/g,"");
	var announcementsUrl = url + ".json?n=" + number;
	var tool_href = $('.announcements-view-url').text().replace(/'/g,'');
	//get the announcement tool url
	var link_to_tool = tool_href.split("?", 1);
	var text_for_announcements = '<div class="announcementsHeaderDiv"><h3 class="announcementSummaryHeader" style="border-bottom:1px solid #ccc;padding-bottom: 5px;"><a href="'+link_to_tool+'" target="_top" title ="Announcements">Announcements</a></h3></div>';
	//Get announcements
	$.ajaxSetup({ cache: false });	//Disable caching for ajax requests
	$.getJSON(announcementsUrl, function(data, status, xhr) {
		if($(data["announcement_collection"]).size() === 0) {
			//ie no announcements
			text_for_announcements += '<p>'+msg("simplepage.announcements-no-message")+'</p>';
		}
		else {
			$(data["announcement_collection"]).each(function(){
				//create a new javascript Date object based on the timestamp
				date = new Date(this["createdOn"]);
				var hour = date.getHours() < 10 ? '0' + date.getHours() : date.getHours();
				var min = date.getMinutes() < 10 ? '0' + date.getMinutes() : date.getMinutes();
				//using javascript's toLocaleDateString() to include user's locale and local time zone
				date_time = hour +":"+min+ " " + date.toLocaleDateString();
				text_for_announcements += '<div class="itemDiv">';
				var href = tool_href + this["announcementId"]+"&sakai_action=doShowmetadata";
				text_for_announcements += '<div class="itemTitle"><a href="'+href+'" target="_top">'+this["entityTitle"].escapeHTML()+'</a> by '+this["createdByDisplayName"].escapeHTML() +'</div>';
				text_for_announcements += '<div class="itemDate">'+date_time+'</div>';
				text_for_announcements += '</div>';
			});
		}
		$('.announcements-div').html(text_for_announcements);
	})
	.fail(function( jqxhr, textStatus, error ) {	//if ajax request fails display error message
		var err = textStatus + ", " + error;
		text_for_announcements += '<p>'+ msg("simplepage.announcements-error-message") + err +'</p>';
		$('.announcements-div').html(text_for_announcements);
	});
}