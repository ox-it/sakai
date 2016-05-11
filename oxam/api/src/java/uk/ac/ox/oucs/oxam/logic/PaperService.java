package uk.ac.ox.oucs.oxam.logic;

import uk.ac.ox.oucs.oxam.model.Paper;

public interface PaperService {

	public Paper getPaper(long id);
		
	public void savePaper(Paper paper) throws RuntimeException;

}
