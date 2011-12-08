package uk.ac.ox.oucs.oxam.logic;

import java.util.Iterator;

import uk.ac.ox.oucs.oxam.model.ExamPaper;

public interface IndexingService {

	public void delete(String id);
	
	public void index(ExamPaper examPaper);
	
	public void reindex(Iterator<ExamPaper> examPapers);
	
}
