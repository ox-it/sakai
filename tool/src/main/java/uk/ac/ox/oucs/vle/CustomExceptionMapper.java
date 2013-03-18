package uk.ac.ox.oucs.vle;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;


/**
 * This handles the exceptions from the course signup service and translates them
 * into nice HTTP error codes.
 * @author buckett
 *
 */
@Provider
public class CustomExceptionMapper implements ExceptionMapper<CourseSignupException> {

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
	
	public Response toResponse(CourseSignupException exception) {
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
