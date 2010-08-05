// Simple file for handling text.
var Text = (function() {
	
	var emailRegex = /[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,4}/ig;
	var emailReplacement = '<a class="email" href="mailto:$&">$&</a>';
	var urlRegex = /(ftp|http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/ig
	var urlReplacement = '<a class="url" href="$&" target="_blank">$&</a>';
	var lineRegex = /\n/g;
	var lineReplacement = '<br>';
	
	return {
		/**
		 * Escape text so we can output it in a HTML page.
		 * It attempts to escape HTML characters and marks up emails and links.
		 * It currently has a bug in that a &amp; in URL will get escaped twice.
		 * @param source The source text.
		 * @returns The escape/marked up version.
		 */
		"toHtml": function(source) {
			var dest = source.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/, "&quot;");
			dest = dest.replace(urlRegex, urlReplacement);
			dest = dest.replace(emailRegex, emailReplacement);
			dest = dest.replace(lineRegex, lineReplacement);
			return dest;
		}
	};
})();