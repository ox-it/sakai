package uk.ac.ox.oucs.oxam.logic;

/**
 * Class to allow the progress of the Indexer to be displayed to the user.
 * @author buckett
 *
 */
public interface IndexerStatus {

	enum Status {
		/** The Indexer is running and progress should be increasing. */
		RUNNING,
		/** The Indexer isn't running. */
		STOPPED,
		/** The Indexer has almost completed and is just finishing up. */
		FINISHING}
	
	/**
	 * Get the progress of the Indexer.
	 * @return A number from 0 to 1, with 0 being no progress and 1 being complete.
	 */
	float getProgress();
	Status getStatus();
}
