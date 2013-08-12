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
package uk.ac.ox.oucs.vle.resources;

import uk.ac.ox.oucs.vle.NotFoundException;
import uk.ac.ox.oucs.vle.PermissionDeniedException;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;


/**
 * This handles the exceptions from the course signup service and translates them
 * into nice HTTP error codes.
 *
 * @author buckett
 *
 */
@Provider
public class CustomExceptionMapper implements ExceptionMapper<Throwable> {

	private static final Map<String, String> forbiddenMap = new HashMap<String, String>();
	static {
		forbiddenMap.put("status", "failed");
		forbiddenMap.put("message", "Not Authorized");
	}
	
	private static final Map<String, String> notFoundMap = new HashMap<String, String>();
	static {
		notFoundMap.put("status", "failed");
		notFoundMap.put("message", "The requested resource was not found");
	}
	
	public Response toResponse(Throwable exception) {
		if(exception instanceof NotFoundException) {
			return Response.status(Status.NOT_FOUND)
					.entity(notFoundMap)
					.build();
		} else if (exception instanceof PermissionDeniedException) {
			return Response.status(Status.FORBIDDEN)
					.entity(forbiddenMap)
					.build();
		}
		
		return Response.status(Status.INTERNAL_SERVER_ERROR).build();
	}

}
