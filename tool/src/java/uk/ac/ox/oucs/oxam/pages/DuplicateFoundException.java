package uk.ac.ox.oucs.oxam.pages;

/**
 * Exception that is thrown when multiple copies of a file are found when we were only expecting one.
 * @author buckett
 */
public class DuplicateFoundException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private String original;
	private String duplicate;
	
	public DuplicateFoundException(String original, String duplicate) {
		super();
		this.original = original;
		this.duplicate = duplicate;
	}
	
	public String getOriginal() {
		return original;
	}
	
	public String getDuplicate() {
		return duplicate;
	}
}
