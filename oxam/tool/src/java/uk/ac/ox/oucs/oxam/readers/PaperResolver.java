package uk.ac.ox.oucs.oxam.readers;


/**
 * Simple interface which is passed to a Import when a paper is wanted.
 * This interface is so that we can resolve files out of the filesystem,
 * or directly out of a zipfile.
 * Resolvers probably want todo caching, as MD5 calculations are expensive.
 * 
 * @author buckett
 */
public interface PaperResolver {

	/**
	 * Find the data for the requested file.
	 * @param year The year we are wanting the file for.
	 * @param term The term this paper taken in, it's the shortcode for the term.
	 * @param paperCode The code of the paper.
	 * @return The InputStream for the file, or <code>null</code> if it doesn't exist.
	 */
	public PaperResolutionResult getPaper(int year, String term, String paperCode);
	
}
