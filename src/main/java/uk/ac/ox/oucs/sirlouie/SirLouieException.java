package uk.ac.ox.oucs.sirlouie;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class SirLouieException extends WebApplicationException {
	
    /**
     * Create a HTTP 401 (Unauthorized) exception.
    */
    public SirLouieException() {
        super(Response.status(Status.INTERNAL_SERVER_ERROR).build());
    }

    /**
     * Create a HTTP 404 (Not Found) exception.
     * @param message the String that is the entity of the 404 response.
     */
    public SirLouieException(String message) {
        super(Response.status(Status.INTERNAL_SERVER_ERROR).entity(message).type("text/plain").build());
    }



}
