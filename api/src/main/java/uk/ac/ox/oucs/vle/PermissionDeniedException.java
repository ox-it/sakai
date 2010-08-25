package uk.ac.ox.oucs.vle;

/**
 * This error is thrown by the service when the user attempts todo 
 * something which they aren't allowed todo. Having runtime errors 
 * means we can handle them well above the service layer but don't
 * need lots of declarations.
 * @author buckett
 *
 */
public class PermissionDeniedException extends CourseSignupException {

	private static final long serialVersionUID = 1908495537561080522L;
	
	private String userId;
	
	public PermissionDeniedException(String userId) {
		this.userId = userId;
	}

	public String getUserId() {
		return this.userId;
	}

}
