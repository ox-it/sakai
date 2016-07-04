package uk.ac.ox.oucs.oxam.readers;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 * Simple class for holding error messages when an import is done.
 * @author buckett
 *
 */
public class ErrorMessages<T> {

	private final int row;
	// This is a set so a message will only exist once.
	private final Set<String> messages = new HashSet<String>();
	
	private final T object;
	
	public ErrorMessages (int row, T object) {
		this.row = row;
		this.object = object;
	}

	@ColumnMapping("original_row")
	@Ordered(1)
	public int getRow() {
		return row;
	}

	public Set<String> getMessages() {
		return messages;
	}
	
	public void addMessage(String message) {
		this.messages.add(message);
	}

	@Include
	@Ordered(2)
	public T getObject() {
		return object;	
	}
	
	@ColumnMapping("errors")
	@Ordered(3)
	public String getErrors() {
		return StringUtils.join(getMessages(), ", ");
	}
	
	@Override
	public String toString() {
		return "ErrorMessages [row=" + row + ", messages=" + messages
				+ ", object=" + object + "]";
	}

}
