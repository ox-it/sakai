var itemsToShow;
$(function() {
	itemsToShow = $('.forum-summary-div').find($('#numberOfConversations')).val();
	//set the 'numberofconversations' in the hidden field for edit screen
	$('#numberOfMessages').val(itemsToShow);
	showForums();
});

function showForums(){
	var forumsUrl = $('.forum-summary-site-url').text().replace(/'/g,"");
	forumsUrl += '.json?n=' + itemsToShow; 
	var messagesArray = [];
	var errorText;
	//Get forums
	$.ajaxSetup({ cache: false });	//Disable caching for ajax requests
	$.getJSON(forumsUrl , function(data_forum_collection) {
		$(data_forum_collection["forums_collection"]).each(function(fc_index, fc_value){
			var author;
			if(fc_value['modifiedBy'].indexOf('(') > -1){
				author = fc_value['modifiedBy'].split('(')[0];
			}else{
				author = fc_value['modifiedBy'];
			}
			//escape markup
			var entityTitle = fc_value['entityTitle'].replace(/&/g, '&amp;').replace(/>/g, '&gt;').replace(/</g, '&lt;').replace(/"/g, '&quot;');
			//added forumId , topicId and messageId into the messageArray to link the message into the forum tool
			var messageObject = {forumId: fc_value["forumId"],
								entityTitle: entityTitle,
								entityUrl: fc_value['entityURL'],
								lastModified: fc_value['lastModified'], 
								author: author,
								topicId: fc_value['topicId'],
								messageId: fc_value['messageId']};
			messagesArray.push(messageObject);
		});
		outputForums(messagesArray);
		setTimeout('showForums()', 300000);
	});
}
function outputForums(messagesArray){
	var toolHref = $('.forum-summary-view-url').text().replace(/'/g,"");
	var title = msg("simplepage.forum-header-title");
	var text_for_forums = '<div class="forumSummaryHeaderDiv"><h3 class="forumSummaryHeader"><a href="'+toolHref+'" class="forumSummaryLink" target="_top" title ="'+title+'">'+title+'</a></h3></div>';
	if(messagesArray.length == 0){
		text_for_forums += '<p>'+msg("simplepage.forum-summary-no-message")+'</p>';
	}
	else{
		messagesArray.sort(function(a, b) {
			return b.lastModified - a.lastModified; //inverse sort by lastModified
		});
		text_for_forums+='<ul class="forumSummaryList">';
		for (i=0; i < messagesArray.length; i++){
			var date = new Date(messagesArray[i].lastModified * 1000);//get back date from unix timestamp;
			var hour = date.getHours() < 10 ? '0' + date.getHours() : date.getHours();
			var min = date.getMinutes() < 10 ? '0' + date.getMinutes() : date.getMinutes();
			//using javascript's toLocaleDateString() to include user's locale and local time zone
			var date_time = hour + ":" + min + " " + date.toLocaleDateString();
			var href = toolHref + "/discussionForum/message/dfViewThreadDirect.jsf?messageId=" + messagesArray[i].messageId + "&topicId=" +messagesArray[i].topicId + "&forumId=" + messagesArray[i].forumId;
			text_for_forums+='<li class="forumSummaryItem"><a href="'+href+'" target="_top">'+messagesArray[i].entityTitle+'</a> by '+messagesArray[i].author+'</br><span class="forumSummaryDate">'+date_time+'</span></li>';
		}
		text_for_forums+='</ul>';
	}
	$('.forum-summary-div').html(text_for_forums);
}