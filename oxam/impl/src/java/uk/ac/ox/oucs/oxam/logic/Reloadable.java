package uk.ac.ox.oucs.oxam.logic;

/**
 * A service implements this interface if it loads it's data on startup and then doesn't look again.
 * This interface allows external caller to ask it to reload it's data.
 * @author buckett
 *
 */
public interface Reloadable {

	public void reload();
	
}
