package uk.ac.ox.oucs.vle;

/**
 * This class exist as when using static types on the Jackson serializer you can't write
 * out Maps.
 * @author Matthew Buckett
 */
public class FailureMessage {

	// Constant effectively at the moment.
	private final String status = "failed";
	private final String message;

	public FailureMessage(String message) {
		this.message = message;
	}

	public String getStatus() {
		return this.status;
	}

	public String getMessage() {
		return this.message;
	}
}
