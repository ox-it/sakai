package uk.ac.ox.oucs.vle;

/**
 * Base exception class for course signup.
 * This class is abstract as you should always get a more specific instance but may
 * want to have generic handling of all of those errors.
 * @author buckett
 *
 */
public abstract class CourseSignupException extends RuntimeException {

	private static final long serialVersionUID = 3523664446891089880L;

}
