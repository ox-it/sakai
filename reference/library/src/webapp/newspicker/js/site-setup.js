// This sets up the 

$(function() {
	var buttons = []; // Array of jQuery buttons we've added.
	var inputs = $("input[type=text][name^=channel-url]").each(function(){
		var urlInput = this;
		// Find the title input related to this one.
		// Need to escape the dot in the ID.
		var titleId = "#"+ urlInput.id.replace(/channel-url/, "title").replace(/\./, "\\.");
		var titleInput = $(titleId).get(0);
		var button = $("<input type='button' class='find-podcast' value='Browse Oxford Podcast Collection'>");
		button.data("titleElm", titleInput);
		button.data("urlElm", urlInput);
		$(this).after(button);
		buttons.push(button[0]);
	});
	
	// Our local version of resize, which allows for a minimum height.
	var resize = function(minHeight) {
		var frame = parent.document.getElementById(window.name);
		if (frame) {
			var height = contentSize(frame);
			if (height < minHeight) {
				height = minHeight;
			}
			// Fudge factor.
			height += 40;
			var objToResize = (frame.style) ? frame.style : frame;
			objToResize.height=height + "px";
		}
	};
	
	// Get the size of the content in the frame.
	var contentSize = function(frame) {
		var height; 		
		var offsetH = document.body.offsetHeight;
		var innerDocScrollH = null;

		if (typeof(frame.contentDocument) != 'undefined' || typeof(frame.contentWindow) != 'undefined')
		{
			// very special way to get the height from IE on Windows!
			// note that the above special way of testing for undefined variables is necessary for older browsers
			// (IE 5.5 Mac) to not choke on the undefined variables.
 			var innerDoc = (frame.contentDocument) ? frame.contentDocument : frame.contentWindow.document;
			innerDocScrollH = (innerDoc != null) ? innerDoc.body.scrollHeight : null;
		}
	
		if (document.all && innerDocScrollH != null)
		{
			// IE on Windows only
			height = innerDocScrollH;
		}
		else
		{
			// every other browser!
			height = offsetH;
		}
		return height;
	};

	PodcastPickerInit({
		rssFile: sourceUrl,
		triggerElements: $(buttons), // Array of elements that can be triggers.
		genericThumbnails: false, // use generic thumbnails instead of those specified in the file
		// selection callback, podcast contains the podcast data, $(this) refers to the input that originally opened the picker
		onSelect: function(podcast) {
			// this points to the element that originally triggered the podcast picker
			var button = $(this);
			button.data("titleElm").value = podcast.el.find('title').text();
			button.data("urlElm").value = podcast.el.find('link').text();
		},
		onShow: function(dialog) {
			// this is called when the window opens, dialog points to the HTML element that represents the picker GUI
			// useful for IFrame resizing
			var height = dialog.height();
			resize(height + 100); // This is because the modal dialog is offset.
		},

		onHide: function(dialog) {
			resize(100);
		},

		onReady: function(picker) {
			$(buttons).click(function() {
				picker.trigger(this);
			});
		}
	});
});