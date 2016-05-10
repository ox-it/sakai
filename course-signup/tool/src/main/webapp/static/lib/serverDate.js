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
// Small jQuery plugin to get dates from server.
(function($){

	// Work out the difference between client time and server time.
    var adjustment;
	
    var init = function(){
        if (!adjustment) {
            $.ajax({
                "url": "/course-signup/rest/user/current",
                "type": "GET",
                "async": false,
				"cache": false,
                "dataType": "json",
                "success": function(data){
                    var serverDate = data.date;
					var clientDate = new Date().getTime();
					adjustment = serverDate - clientDate;
                }
            });
        }
    };
    
    $.serverDate = function(){
        init();
        return (new Date().getTime() + adjustment);
        
    };
})(jQuery);
