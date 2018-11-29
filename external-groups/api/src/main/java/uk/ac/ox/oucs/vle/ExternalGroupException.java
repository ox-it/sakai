package uk.ac.ox.oucs.vle;

/**
 * Exception to be thrown when something has gone wrong.
 *
 */
public class ExternalGroupException extends Exception {

	private static final long serialVersionUID = 1L;

	public enum Type {SIZE_LIMIT, UNKNOWN}
	
	private Type type = Type.UNKNOWN;
	
	public ExternalGroupException(Type type) {
		this.type = type;
	}

	public ExternalGroupException(Type type, Exception exception) {
		super(exception);
		this.type = type;
	}
	
	public ExternalGroupException(String message, Exception exception) {
		super(message, exception);
	}

	public Type getType() {
		return type;
	}
}
