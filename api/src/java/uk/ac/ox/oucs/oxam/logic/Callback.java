package uk.ac.ox.oucs.oxam.logic;

/**
 * Generic callback interface.
 * This is used when we need to pass a function into a method.
 * @author buckett
 *
 * @param <T> The type of the value passed to the callback.
 */
public interface Callback<T> {

	void callback(T value);
	
}
