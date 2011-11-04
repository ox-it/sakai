package uk.ac.ox.oucs.oxam.readers;

import java.util.HashSet;
import java.util.Set;

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

	public int getRow() {
		return row;
	}

	public Set<String> getMessages() {
		return messages;
	}
	
	public void addMessage(String message) {
		this.messages.add(message);
	}

	public T getObject() {
		return object;
	}
	
	@Override
	public String toString() {
		return "ErrorMessages [row=" + row + ", messages=" + messages
				+ ", object=" + object + "]";
	}

}
