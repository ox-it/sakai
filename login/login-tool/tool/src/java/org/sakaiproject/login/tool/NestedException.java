package org.sakaiproject.login.tool;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Wrap up Exceptions in RuntimeException so it can be thrown, but most calls 
 * fall through to the original exception.
 * Mustn't implement fillInStackTrace, otherwise we NPE on creation.
 * @author buckett
 *
 */
public class NestedException extends RuntimeException {

	private static final long serialVersionUID = 4096548480606893544L;
	private Throwable throwable;

	/** Wraps another exeception in a RuntimeException. */
	public static RuntimeException wrap(Throwable t) {
		if (t instanceof RuntimeException)
			return (RuntimeException) t;
		return new NestedException(t);
	}

	protected NestedException(Throwable t) {
		this.throwable = t;
	}

	public boolean equals(Object obj) {
		return throwable.equals(obj);
	}

	public String getLocalizedMessage() {
		return throwable.getLocalizedMessage();
	}

	public String getMessage() {
		return throwable.getMessage();
	}

	public StackTraceElement[] getStackTrace() {
		return throwable.getStackTrace();
	}

	public int hashCode() {
		return throwable.hashCode();
	}

	public Throwable initCause(Throwable cause) {
		return throwable.initCause(cause);
	}

	public void printStackTrace(PrintStream s) {
		throwable.printStackTrace(s);
	}

	public void printStackTrace(PrintWriter s) {
		throwable.printStackTrace(s);
	}

	public void setStackTrace(StackTraceElement[] stackTrace) {
		throwable.setStackTrace(stackTrace);
	}

	public String toString() {
		return throwable.toString();
	}
	
	public Throwable getCause() {
		return throwable;
	}

}
