$(function() {
	// This is used on the editing of the exam papers to uppercase all the
	// input.
	// Don't use onchange as it moves the cursor if it changes the value.
	$("input.uppercase").css("text-transform", "uppercase").blur(function() {
		this.value = this.value.toUpperCase();
	});

	// Used to show the extra items on the factets
	$(".facet .control").click(function(e) {
		$(this).parent().children().toggle('fast');
		e.preventDefault();
	});

	var autoresize = function() {
		var previousInnerHTML = document.body.innerHTML;
		return function(change) {
			if (document.body.innerHTML !== previousInnerHTML) {
				previousInnerHTML = document.body.innerHTML;
				resize(window.name);
			}
			setTimeout(arguments.callee, 100);
		}();
	};

	/**
	 * This is normally in the Sakai script but we don't want to scroll the page
	 * when resizing so we have our own copy. We also assume the window name
	 * contains the ID of the parent frame.
	 */
	var resize = function() {
		var id = window.name;
		if (!id) {
			return;
		}
		var frame = parent.document.getElementById(id);
		if (frame) {
			// reset the scroll
			// parent.window.scrollTo(0, 0);

			var objToResize = (frame.style) ? frame.style : frame;

			var height;
			var offsetH = document.body.offsetHeight;
			var innerDocScrollH = null;

			if (typeof (frame.contentDocument) != 'undefined'
					|| typeof (frame.contentWindow) != 'undefined') {
				// very special way to get the height from IE on Windows!
				// note that the above special way of testing for undefined
				// variables is necessary for older browsers
				// (IE 5.5 Mac) to not choke on the undefined variables.
				var innerDoc = (frame.contentDocument) ? frame.contentDocument
						: frame.contentWindow.document;
				innerDocScrollH = (innerDoc != null) ? innerDoc.body.scrollHeight
						: null;
			}

			if (document.all && innerDocScrollH != null) {
				// IE on Windows only
				height = innerDocScrollH;
			} else {
				// every other browser!
				height = offsetH;
			}
			// here we fudge to get a little bigger
			var newHeight = height + 40;

			// but not too big!
			if (newHeight > 65520)
				newHeight = 65520;

			// capture my current scroll position

			// resize parent frame (this resets the scroll as well)
			objToResize.height = newHeight + "px";

			// optional hook triggered after the head script fires.

			if (parent.postIframeResize) {
				parent.postIframeResize(id);
			}
		}
	};
	
	autoresize();
});
