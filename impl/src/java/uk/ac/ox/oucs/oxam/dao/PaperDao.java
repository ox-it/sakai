package uk.ac.ox.oucs.oxam.dao;

import java.util.List;

import uk.ac.ox.oucs.oxam.model.Paper;

public interface PaperDao {
	
	public Paper getPaper(long id);

	public List<Paper> getPapers(int start, int length);

	public void savePaper(Paper paper) throws RuntimeException;	
}
