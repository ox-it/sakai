/*
 * #%L
 * Course Signup Webapp
 * %%
 * Copyright (C) 2010 - 2013 University of Oxford
 * %%
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *             http://opensource.org/licenses/ecl2
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
// Simple file for handling text.
var Text = (function() {
	
	var emailRegex = /[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,4}/ig;
	var emailReplacement = '<a class="email" href="mailto:$&">$&</a>';
	var urlRegex = /(https?|ftps?):\/\/[a-z_0-9\\\-]+(\.([\w#!:?+=&%@!\-\/])+)+/ig;
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
			if (!source) {
				return "";
			}
			var dest = source.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/, "&quot;");
			dest = dest.replace(urlRegex, urlReplacement);
			dest = dest.replace(emailRegex, emailReplacement);
			dest = dest.replace(lineRegex, lineReplacement);
			return dest;
		},

		/**
		 * Check if the argument is an email address.
		 * @param email The email address to check.
		 * @return True if the arugment is an email address.
		 */
		"isEmail": function(email) {
		    return emailRegex.test(email);
		}
	};
})();
