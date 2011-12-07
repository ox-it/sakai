package uk.ac.ox.oucs.oxam.logic;

import java.io.OutputStream;

import uk.ac.ox.oucs.oxam.model.Paper;

public interface PaperService {

	public Paper getPaper(long id);
		
	public void savePaper(Paper paper) throws RuntimeException;
		
	public Upload mapToUpload(int year, String term, String paper);
	
	public void depositUpload(Upload upload, Callback<OutputStream> callback);
	
	public boolean uploadExists(Upload upload);
	
}
