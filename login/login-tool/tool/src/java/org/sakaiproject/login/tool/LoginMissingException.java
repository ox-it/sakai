package org.sakaiproject.login.tool;

/**
 * Runtime exception we can handle in web.xml
 * @author buckett
 *
 */
public class LoginMissingException extends NestedException {

	public static RuntimeException wrap(Throwable t) {
		if (t instanceof RuntimeException)
			return (RuntimeException) t;
		return new LoginMissingException(t);
	}
	
	protected LoginMissingException(Throwable t) {
		super(t);
	}

}
