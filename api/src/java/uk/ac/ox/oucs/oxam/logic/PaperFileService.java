package uk.ac.ox.oucs.oxam.logic;

import java.io.InputStream;

public interface PaperFileService {

	public PaperFile get(String year, String term, String paperCode,
			String extension);

	public boolean exists(PaperFile paperFile);
	
	/**
	 * This is used so that calling code can calculate a checksum if it want.
	 * @param paperFiler
	 * @return
	 */
	public InputStream getInputStream(PaperFile paperFiler);

	public void deposit(PaperFile paperFile, InputStream source);

}