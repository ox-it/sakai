// Javascript that's used by the News Tool to setup the inpage players. */

$(function() {
	var count = 0;
	$("a.enclosure").filter(".video,.audio").each(function() {
		var current = $(this);
		var id = "swf-embed-"+ count;
		var container = current.parent()
			.append("<br><div id='"+ id+ "' class='flash'></div>");
		var content = this.href;
		var flashvars = { file: content, autostart:'false' };
		var params = { allowfullscreen:'true', allowscriptaccess:'always' };
		var attributes = { id:'player'+count, name:'player'+count };
		var height = current.is('.video')?"320":"24";
		swfobject.embedSWF('/library/mediaplayer-5.4/player.swf',id,'480',height,'9','false',
				flashvars, params, attributes);
		
		count++;
	});
});