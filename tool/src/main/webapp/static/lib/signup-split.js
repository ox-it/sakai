var Signup = Signup || {};

/**
 * This supports the splitting of a signup into multiple signups.
 * It needs trimpath and jqmodal.
 */
Signup.split = function(signupId, isAdmin, success) {
	// The trimpath template.
	var template = null;
	// The signup data.
	var signup = null;
	// The jQuery popup.
	var $popup = null;
	/**
	 * This loads the template and caches it in the object.
	 */
	var loadTemplate = function() {
		if (!template) { // When reloading we might already have the template loaded.
			$.ajax({
				url: "/course-signup/static/split-signup.tpl",
				dataType: "text",
				cache: true,
				success: function(data){
					template = TrimPath.parseTemplate(data);
					loaded();
				}
			});
		}
	};

	/**
	 * This loads the signups data.
	 */
	var loadSignup = function() {
		$.ajax({
			url: "/course-signup/rest/signup/"+signupId,
			success: function(data) {
				signup = data;
				signup._MODIFIERS = Signup.util.trimpathModifiers(isAdmin);
				loaded();
			}
		});
	};

	/**
	 * Do we have all the data loaded?
	 */
	var stillWaiting = function() {
		return !(template && signup);
	};

	var loaded = function() {
		if (stillWaiting()) {
			return;
		}
		$popup.html(template.process(signup, {throwExceptions: true}));
		var disabled = false;
		$("#signup-split-popup form").submit(function(event) {
			// We're going to submit with XHR.
			event.preventDefault();
			if (disabled) return;

			// Validate the form.
			var $errors = $("span.errors", this).html("");
			var $inputs = $("input[name=componentPresentationId]", this);
			if ($inputs.filter(":checked").length == 0) {
				$errors.append("You must select some components.");
			}
			if ($inputs.not(":checked").length == 0) {
				$errors.append("You must leave some components.");
			}
			// Submit the XHR request
			if ($errors.html() == "") {
				var $this = $(this);
				$this.children("input[type=submit]").attr("disabled", true);
				disabled = true;
				$.ajax({
					"type": this.method,
					"url": this.action,
					"data": $(this).serialize(),
					"success": function() {
						$popup.jqmHide();
						// Add Message
						// Reload the parent list.
						success();
					},
					"complete": function() {
						// Re-enable the submission.
						$this.children("input[type=submit]").removeAttr("disabled");
						disabled = false;
					},
					"error": function(e) {
						$errors.append("Error: "+ e.responseText);
					}
				});
			}
		});
		$("#signup-split-popup input[name=cancel]").click(function(){
			$popup.jqmHide();
		})
	};

	/**
	 * Displays the initial jqm window and kicks off the loading.
	 */
	var setup = function() {
		var html = '<div id="signup-split-popup" class="jqmWindow" style="display: none"></div>';
		$popup = $("#signup-split-popup");
		// Create the popup or reset the HTML.
		if($popup.length == 0) {
			$("body").append(html);
			$popup = $("#signup-split-popup");
		}
		$popup.html("Loading....");
		$popup.jqm().jqmShow();
		loadSignup();
		loadTemplate();
	}

	setup();
};
