package uk.ac.ox.oucs.vle;

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
			return Response.status(Status.NOT_FOUND).build();
		} else if (exception instanceof PermissionDeniedException) {
			return Response.status(Status.FORBIDDEN).build();
		}
		
		return Response.status(Status.INTERNAL_SERVER_ERROR).build();
	}

}
