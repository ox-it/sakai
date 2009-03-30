package uk.ac.ox.oucs.vle;

public class ExternalGroupException extends Exception {

	private static final long serialVersionUID = 1L;

	public enum Type {SIZE_LIMIT, UNKNOWN}
	
	private Type type;
	
	public ExternalGroupException(Type type) {
		this.type = type;
	}
	
	public Type getType() {
		return type;
	}
}
