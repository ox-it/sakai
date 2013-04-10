package uk.ac.ox.oucs.vle;

/**
 * This error is thrown by the service when a request is made for
 * something that doesn't exsits. Having runtime errors 
 * means we can handle them well above the service layer but don't
 * need lots of declarations.
 * @author buckett
 *
 */
public class NotFoundException extends CourseSignupException {
	
	private static final long serialVersionUID = -8240382294176253690L;
	
	private String id;

	public NotFoundException(String id) {
		this.id = id;
	}

	public String getId() {
		return this.id;
	}
}
