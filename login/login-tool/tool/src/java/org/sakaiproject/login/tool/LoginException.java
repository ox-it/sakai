package org.sakaiproject.login.tool;

/**
 * Runtime exception we can handle in web.xml
 * @author buckett
 *
 */
public class LoginException extends NestedException {

	private static final long serialVersionUID = 3859441281275841698L;

	protected LoginException(Throwable t) {
		super(t);
	}
	
	public static RuntimeException wrap(Throwable t) {
		if (t instanceof RuntimeException)
			return (RuntimeException) t;
		return new LoginException(t);
	}
}
