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
package uk.ac.ox.oucs.vle;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class WebAppNotFoundException extends WebApplicationException {
	
	private static final Map<String, String> myMap = new HashMap<String, String>();
	static {
		myMap.put("status", "failed");
		myMap.put("message", "The requested resource was not found");
	}
	
	public WebAppNotFoundException() {
		super(Response
				.status(Response.Status.NOT_FOUND)
				// We set the type as when the request doesn't have an accept header
				// jersey will attempt to convert a Map to application/octet-stream
				// which will fail.
				.type(MediaType.APPLICATION_JSON)
				.entity(myMap)
				.build());
	}

}
