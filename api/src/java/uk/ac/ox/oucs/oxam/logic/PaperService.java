package uk.ac.ox.oucs.oxam.logic;

import java.io.OutputStream;
import java.util.List;

import uk.ac.ox.oucs.oxam.model.Paper;

public interface PaperService {

	public Paper getPaper(long id);
	
	public List<Paper> getPapers(int start, int length);
	
	public void savePaper(Paper paper) throws RuntimeException;
	
	
	
	public String mapToFile(int year, String term, String paper);
	
	public void depositFile(String url, Callback<OutputStream> callback);
	
}
