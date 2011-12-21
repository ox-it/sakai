package uk.ac.ox.oucs.oxam.logic;

import java.io.InputStream;

public interface PaperFileService {

	public PaperFile get(String year, String term, String paperCode,
			String extension);

	public boolean exists(PaperFile paperFile);

	public void deposit(PaperFile paperFile, InputStream source);

}