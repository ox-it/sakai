package uk.ac.ox.oucs.oxam.logic;

/**
 * This interface is to enable the code to run inside/outside of Sakai.
 * We are also careful that when in Sakai we don't needlessly have Sakai data
 * such as Site IDs end up in the database.
 * @author buckett
 *
 */
public interface Location {

	/**
	 * This retrieves the prefix of a file that has been uploaded so that we can generate a good
	 * URL to it.
	 * @return
	 */
	public abstract String getPrefix();

	/**
	 * This generates a path that the file should be saved to.
	 * As an example when running in a servlet it prefixes the full path to the container in the filesystem.
	 */
	public abstract String getPath(String path);

}