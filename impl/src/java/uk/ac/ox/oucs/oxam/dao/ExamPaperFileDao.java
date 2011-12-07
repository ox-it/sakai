package uk.ac.ox.oucs.oxam.dao;

import uk.ac.ox.oucs.oxam.model.ExamPaperFile;

public interface ExamPaperFileDao {

	public ExamPaperFile get(long id);
	
	public void delete(long id);
	
	public void save(final ExamPaperFile examPaperFile);
	
}
