package uk.ac.ox.oucs.vle;

import java.util.HashMap;

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

	public Response toResponse(CourseSignupException exception) {
		if(exception instanceof NotFoundException) {
			return Response.status(Status.NOT_FOUND)
					.entity(new HashMap<String,String>() {{
						put("status", "failed");
						put("message", "The requested resource was not found");}})
						.build();
		} else if (exception instanceof PermissionDeniedException) {
			return Response.status(Status.FORBIDDEN)
					.entity(new HashMap<String,String>() {{
						put("status", "failed");
						put("message", "Not Authorized");}})
						.build();
		}
		
		return Response.status(Status.INTERNAL_SERVER_ERROR).build();
	}

}
